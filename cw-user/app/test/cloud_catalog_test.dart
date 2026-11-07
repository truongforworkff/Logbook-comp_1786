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
}
