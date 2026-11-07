import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:eventa_user/features/agenda/data/catalog_repository.dart';
import 'package:eventa_user/features/agenda/logic/agenda_controller.dart';

import 'support/memory_favourites.dart';
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
  test('malformed cloud payload is rejected at the boundary', () {
    for (final change in [
      (Map<String, dynamic> d) => d['schemaVersion'] = 2,
      (Map<String, dynamic> d) => d['updatedAt'] = 'fake',
      (Map<String, dynamic> d) => d['events'][0]['date'] = '2026-02-31',
      (Map<String, dynamic> d) => d['events'][0]['id'] = '',
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
    final controller = AgendaController(catalog: catalog, favourites: fav);
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
