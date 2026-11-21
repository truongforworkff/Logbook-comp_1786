import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:eventa_user/features/agenda/data/catalog_repository.dart';
import 'package:eventa_user/features/agenda/logic/agenda_controller.dart';
import 'package:eventa_user/features/agenda/logic/event_search.dart';

import 'support/memory_favourites.dart';
import 'support/memory_schedule.dart';
import 'support/fixture_catalog.dart';

class LiveCatalog extends CatalogRepository {
  final changes = StreamController<CatalogData>.broadcast();
  @override
  Stream<CatalogData> watch() => changes.stream;
}

class RestartingCatalog extends CatalogRepository {
  final streams = <StreamController<CatalogData>>[];
  StreamController<CatalogData> get current => streams.last;
  @override
  Stream<CatalogData> watch() {
    final stream = StreamController<CatalogData>.broadcast();
    streams.add(stream);
    return stream.stream;
  }

  Future<void> close() async {
    for (final stream in streams) {
      if (!stream.isClosed) await stream.close();
    }
  }
}

Map<String, dynamic> document() => {
  'schemaVersion': 1,
  'updatedAt': Timestamp.fromDate(DateTime(2026, 11, 7)),
  'events': jsonDecode(File('assets/data/events.json').readAsStringSync()),
};

void main() {
  test(
    'valid cloud document preserves dates, IDs, sessions and optional photos',
    () {
      final catalog = CatalogData.fromDocument(document(), fromCache: true);
      expect(catalog.events.length, 5);
      expect(catalog.events.fold<int>(0, (n, e) => n + e.sessions.length), 9);
      expect(catalog.fromCache, isTrue);
    },
  );
  test(
    'creation time puts newly added events first, independent of event day',
    () {
      final data = document();
      final rows = data['events'] as List<dynamic>;
      final created = DateTime.utc(2026, 11, 20, 16);
      rows[0]['createdAt'] = created.millisecondsSinceEpoch;
      rows[2]['createdAt'] = created
          .subtract(const Duration(days: 1))
          .millisecondsSinceEpoch;
      final catalog = CatalogData.fromDocument(data, fromCache: false);
      final sorted = findEvents(catalog.events);
      expect(sorted.take(2).map((event) => event.id), [
        'gtc-2026',
        'gtc-dc-2026',
      ]);
      expect(sorted.first.createdAt, created);
      expect(sorted[2].id, 'ignite-2026');
      expect(sorted[2].createdAt, isNull);
    },
  );

  test(
    'missing end day lasts through the event day, and a range is inclusive',
    () {
      final data = document();
      final row = data['events'][1] as Map<String, dynamic>;
      final single = CatalogData.fromDocument(data, fromCache: false).events[1];
      expect(single.hasEnded(DateTime(2026, 11, 17, 23, 59)), isFalse);
      expect(single.hasEnded(DateTime(2026, 11, 18)), isTrue);

      row['endDate'] = '2026-11-19';
      final span = CatalogData.fromDocument(data, fromCache: false).events[1];
      expect(span.hasEnded(DateTime(2026, 11, 19, 23, 59)), isFalse);
      expect(span.hasEnded(DateTime(2026, 11, 20)), isTrue);
      row['status'] = 'Completed';
      expect(
        CatalogData.fromDocument(
          data,
          fromCache: false,
        ).events[1].hasEnded(DateTime(2026, 11, 18)),
        isTrue,
      );
    },
  );

  test('end time is optional for legacy catalogs and parsed when provided', () {
    final data = document();
    final row = data['events'][0]['sessions'][0] as Map<String, dynamic>;
    row['time'] = '09:00';
    row['endTime'] = '10:30';
    final session = CatalogData.fromDocument(
      data,
      fromCache: false,
    ).events.first.sessions.first;
    expect(session.endTime, '10:30');
    expect(session.timeRange, '09:00 – 10:30');
    row.remove('endTime');
    expect(
      CatalogData.fromDocument(
        data,
        fromCache: false,
      ).events.first.sessions.first.hasTimeRange,
      isFalse,
    );
    for (final invalid in ['08:30', '09:00', '24:00', 10]) {
      row['endTime'] = invalid;
      expect(
        () => CatalogData.fromDocument(data, fromCache: false),
        throwsFormatException,
      );
    }
  });

  test('malformed cloud payload is rejected at the boundary', () {
    for (final change in [
      (Map<String, dynamic> d) => d['schemaVersion'] = 2,
      (Map<String, dynamic> d) => d['updatedAt'] = 'fake',
      (Map<String, dynamic> d) => d['events'][0]['date'] = '2026-02-31',
      (Map<String, dynamic> d) => d['events'][0]['id'] = '',
      (Map<String, dynamic> d) => d['events'][0]['createdAt'] = '2026-11-20',
      (Map<String, dynamic> d) => d['events'][0]['createdAt'] = -1,
      (Map<String, dynamic> d) => d['events'][1]['endDate'] = '2026-11-16',
      (Map<String, dynamic> d) => d['events'][1]['endDate'] = '2026-11-31',
      (Map<String, dynamic> d) =>
          d['events'][0]['image'] = 'file:///private/image',
      (Map<String, dynamic> d) =>
          d['events'][0]['image'] = 'data:image/jpeg;base64,!!!!',
      (Map<String, dynamic> d) =>
          d['events'][0]['sessions'][0]['time'] = '25:60',
      (Map<String, dynamic> d) => d['events'][1]['id'] = d['events'][0]['id'],
    ]) {
      final data = document();
      change(data);
      expect(
        () => CatalogData.fromDocument(data, fromCache: false),
        throwsFormatException,
      );
    }
  });
  test('live updates, cache metadata and temporary deletion preserve stored favourite IDs', () async {
    final catalog = LiveCatalog();
    final fav = MemoryFavourites({'ignite-2026'});
    final controller = AgendaController(
      catalog: catalog,
      schedule: MemorySchedule(),
      favourites: fav,
    );
    final loading = controller.load();
    await Future<void>.delayed(Duration.zero);
    final events = readCatalog();
    catalog.changes.add(CatalogData(events));
    await loading;
    expect(controller.saved, {'ignite-2026'});
    catalog.changes.add(CatalogData(events, fromCache: true));
    await Future<void>.delayed(Duration.zero);
    expect(controller.fromCache, isTrue);
    catalog.changes.add(const CatalogData([]));
    await Future<void>.delayed(Duration.zero);
    expect(controller.events, isEmpty);
    expect(controller.saved, isEmpty);
    catalog.changes.add(CatalogData(events));
    await Future<void>.delayed(Duration.zero);
    expect(controller.saved, {'ignite-2026'});
    catalog.changes.addError(StateError('offline'));
    await Future<void>.delayed(Duration.zero);
    expect(controller.events.length, 5);
    expect(controller.error, isNotEmpty);
    controller.dispose();
    await catalog.changes.close();
  });
  testWidgets(
    'terminal listener errors reconnect automatically with bounded backoff',
    (tester) async {
      final catalog = RestartingCatalog();
      final controller = AgendaController(
        catalog: catalog,
        schedule: MemorySchedule(),
        favourites: MemoryFavourites({'ignite-2026'}),
      );
      final ready = controller.load();
      await tester.pump();
      catalog.current.add(CatalogData(readCatalog()));
      await tester.pump();
      await ready;
      catalog.current.addError(StateError('temporary transport error'));
      catalog.current.close();
      await tester.pump();
      expect(controller.events.length, 5);
      expect(controller.error, isNotEmpty);
      await tester.pump(const Duration(seconds: 5));
      expect(catalog.streams.length, 2);
      catalog.current.addError(StateError('still unavailable'));
      catalog.current.close();
      await tester.pump();
      await tester.pump(const Duration(seconds: 9));
      expect(catalog.streams.length, 2);
      await tester.pump(const Duration(seconds: 1));
      expect(catalog.streams.length, 3);
      catalog.current.add(CatalogData(readCatalog()));
      await tester.pump();
      expect(controller.error, isEmpty);
      expect(controller.saved, {'ignite-2026'});
      controller.dispose();
      await catalog.close();
    },
  );

  testWidgets(
    'resume retries immediately and disposing cancels pending reconnection',
    (tester) async {
      final catalog = RestartingCatalog();
      final controller = AgendaController(
        catalog: catalog,
        schedule: MemorySchedule(),
        favourites: MemoryFavourites({}),
      );
      final ready = controller.load();
      await tester.pump();
      catalog.current.addError(StateError('connection closed'));
      catalog.current.close();
      await tester.pump();
      await ready;
      controller.refreshAfterResume();
      await tester.pump();
      expect(catalog.streams.length, 2);
      controller.dispose();
      await tester.pump(const Duration(minutes: 2));
      expect(catalog.streams.length, 2);
      await catalog.close();
    },
  );
}
