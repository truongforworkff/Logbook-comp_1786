import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:eventa_user/features/agenda/logic/agenda_controller.dart';
import 'package:eventa_user/features/agenda/logic/event_search.dart';
import 'package:eventa_user/features/favourites/logic/favourite_codec.dart';

import 'support/memory_favourites.dart';
import 'support/fixture_catalog.dart';

void main() {
  final events = readCatalog();
  test('snapshot preserves 5 events, 9 sessions and optional images', () {
    expect(events.length, 5);
    expect(
      events.fold<int>(0, (count, event) => count + event.sessions.length),
      9,
    );
    expect(events.map((event) => event.id).toSet().length, 5);
    expect(events.any((event) => event.image.isEmpty), isTrue);
    for (final event in events) {
      if (event.imageAsset.isNotEmpty) {
        expect(File(event.imageAsset).existsSync(), isTrue);
      }
      expect(
        event.sessions.map((session) => session.id).toSet().length,
        event.sessions.length,
      );
    }
  });
  test('Vietnamese accents, combining marks and case fold correctly', () {
    expect(normalizeSearch(' ĐIỆN TOÁN '), 'dien toan');
    expect(normalizeSearch('Đie\u0323\u0302n toa\u0301n'), 'dien toan');
    expect(findEvents(events, query: 'AI VA DIEN').single.id, 'gtc-2026');
    expect(findEvents(events, query: 'NVIDIA').length, 2);
  });
  test('name, day, month and category combine with AND', () {
    expect(
      findEvents(events, date: DateTime(2026, 11, 17)).single.id,
      'ignite-2026',
    );
    expect(
      findEvents(events, query: 'NVIDIA', date: DateTime(2026, 11, 17)),
      isEmpty,
    );
    expect(findEvents(events, category: 'AI').length, 3);
    expect(
      findEvents(events, category: 'Technology', month: '2026-11'),
      isEmpty,
    );
    expect(findEvents(events, query: "%_'"), isEmpty);
    expect(findEvents(events, query: '  ').length, 5);
    expect(findEvents(events).first.id, 'ignite-2026');
  });
  test(
    'legacy native favourite JSON is retained and invalid IDs are ignored',
    () {
      final valid = events.map((event) => event.id);
      expect(readFavourites('["ignite-2026","unknown","ignite-2026"]', valid), {
        'ignite-2026',
      });
      expect(readFavourites('invalid', valid), isEmpty);
      expect(readFavourites('{"ignite-2026":true}', valid), isEmpty);
      expect(readFavourites('null', valid), isEmpty);
      expect(readFavourites(writeFavourites({'ignite-2026'}), valid), {
        'ignite-2026',
      });
      expect(
        findEvents(events, saved: {'ignite-2026'}).single.id,
        'ignite-2026',
      );
      expect(findEvents(events, saved: {}), isEmpty);
    },
  );
  test(
    'save survives controller recreation; failed writes keep previous state',
    () async {
      final store = MemoryFavourites({'ignite-2026'});
      final first = AgendaController(
        catalog: FixtureCatalog(),
        favourites: store,
      );
      await first.load();
      expect(first.saved, {'ignite-2026'});
      await first.toggleFavourite('gtc-2026');
      final reopened = AgendaController(
        catalog: FixtureCatalog(),
        favourites: store,
      );
      await reopened.load();
      expect(reopened.saved, {'ignite-2026', 'gtc-2026'});
      store.failWrites = true;
      await expectLater(
        reopened.toggleFavourite('ignite-2026'),
        throwsStateError,
      );
      expect(reopened.saved, {'ignite-2026', 'gtc-2026'});
      expect(reopened.saving, isFalse);
      first.dispose();
      reopened.dispose();
    },
  );
}
