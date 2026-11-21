import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:eventa_user/app.dart';
import 'package:eventa_user/features/agenda/data/catalog_repository.dart';
import 'package:eventa_user/features/agenda/data/event_info.dart';
import 'package:eventa_user/features/agenda/logic/agenda_controller.dart';
import 'package:eventa_user/features/schedule/logic/personal_schedule.dart';

import 'support/fixture_catalog.dart';
import 'support/memory_favourites.dart';
import 'support/memory_schedule.dart';

class ScheduleCatalog extends CatalogRepository {
  final updates = StreamController<CatalogData>.broadcast();
  @override
  Stream<CatalogData> watch() => updates.stream;
}

EventInfo withSessions(EventInfo event, List<SessionInfo> sessions) =>
    EventInfo(
      id: event.id,
      name: event.name,
      date: event.date,
      location: event.location,
      organiser: event.organiser,
      type: event.type,
      status: event.status,
      categoryCode: event.categoryCode,
      image: event.image,
      description: event.description,
      source: event.source,
      sessions: sessions,
    );

void main() {
  final events = readCatalog();
  final event = events.firstWhere((e) => e.id == 'ignite-2026');
  final session = event.sessions.first;
  final selection = (eventId: event.id, sessionId: session.id);

  test('selection JSON validates IDs and distinguishes events with the same session ID', () {
    final selected = {selection, (eventId: 'other', sessionId: session.id)};
    expect(decodeSchedule(encodeSchedule(selected)), selected);
    expect(decodeSchedule('invalid'), isEmpty);
    expect(decodeSchedule('[{"eventId":"","sessionId":"s"},null,2]'), isEmpty);
    expect(personalSchedule(events, selected).length, 1);
    final all = {
      for (final event in events)
        for (final session in event.sessions)
          (eventId: event.id, sessionId: session.id),
    };
    final entries = personalSchedule(events.reversed.toList(), all);
    for (var i = 1; i < entries.length; i++) {
      final previous = entries[i - 1], current = entries[i];
      expect(
        previous.event.date.isBefore(current.event.date) ||
            (previous.event.date == current.event.date &&
                previous.session.time.compareTo(current.session.time) <= 0),
        isTrue,
      );
    }
  });

  test('plan survives reopening, stays separate from favourites and preserves state on write failure', () async {
    final store = MemorySchedule();
    final favourites = MemoryFavourites({event.id});
    AgendaController controller() => AgendaController(
      catalog: FixtureCatalog(),
      favourites: favourites,
      schedule: store,
    );
    final first = controller();
    await first.load();
    await first.toggleSession(event.id, session.id);
    expect(first.schedule.single.session.id, session.id);
    expect(favourites.ids, {event.id});
    final reopened = controller();
    await reopened.load();
    expect(reopened.hasPlannedSession(event.id, session.id), isTrue);
    store.failWrites = true;
    await expectLater(
      reopened.toggleSession(event.id, session.id),
      throwsStateError,
    );
    expect(reopened.hasPlannedSession(event.id, session.id), isTrue);
    expect(reopened.savingSchedule, isFalse);
    store.failWrites = false;
    await reopened.toggleSession(event.id, session.id);
    expect(reopened.schedule, isEmpty);
    await reopened.toggleSession('unknown', session.id);
    expect(store.writes, 2);
    first.dispose();
    reopened.dispose();
  });

  test('cloud changes update selected session and temporary removal keeps the stored selection', () async {
    final catalog = ScheduleCatalog();
    final store = MemorySchedule({selection});
    final controller = AgendaController(
      catalog: catalog,
      favourites: MemoryFavourites(),
      schedule: store,
    );
    final ready = controller.load();
    await Future<void>.delayed(Duration.zero);
    catalog.updates.add(CatalogData([event]));
    await ready;
    final updated = SessionInfo(
      id: session.id,
      name: 'Tên mới',
      time: '07:15',
      speaker: session.speaker,
      room: 'B201',
      type: session.type,
      description: session.description,
    );
    catalog.updates.add(
      CatalogData([
        withSessions(event, [updated]),
      ]),
    );
    await Future<void>.delayed(Duration.zero);
    expect(controller.schedule.single.session.name, 'Tên mới');
    expect(controller.schedule.single.session.time, '07:15');
    catalog.updates.add(CatalogData([withSessions(event, [])]));
    await Future<void>.delayed(Duration.zero);
    expect(controller.schedule, isEmpty);
    expect(store.selected, {selection});
    catalog.updates.add(CatalogData([event]));
    await Future<void>.delayed(Duration.zero);
    expect(controller.schedule.single.session.id, session.id);
    controller.dispose();
    await catalog.updates.close();
  });

  testWidgets(
    'attendee adds a session through its detail, opens My Schedule and removes it',
    (tester) async {
      tester.view.physicalSize = const Size(1080, 2400);
      tester.view.devicePixelRatio = 2.75;
      addTearDown(tester.view.resetPhysicalSize);
      addTearDown(tester.view.resetDevicePixelRatio);
      final controller = AgendaController(
        catalog: FixtureCatalog(),
        favourites: MemoryFavourites(),
        schedule: MemorySchedule(),
      );
      await tester.pumpWidget(EventaApp(controller: controller));
      await tester.pumpAndSettle();
      await tester.tap(find.byKey(const Key('tab-schedule')));
      await tester.pumpAndSettle();
      expect(find.text('Your schedule is empty'), findsOneWidget);
      await tester.tap(find.byKey(const Key('schedule-explore')));
      await tester.pumpAndSettle();
      await tester.scrollUntilVisible(
        find.byKey(Key('event-${event.id}')),
        200,
        scrollable: find.byType(Scrollable).first,
      );
      await tester.ensureVisible(find.byKey(Key('event-${event.id}')));
      await tester.pumpAndSettle();
      await tester.tap(find.byKey(Key('event-${event.id}')));
      await tester.pumpAndSettle();
      await tester.scrollUntilVisible(
        find.byKey(Key('session-${session.id}')),
        200,
        scrollable: find.byType(Scrollable).first,
      );
      await tester.tap(find.byKey(Key('session-${session.id}')));
      await tester.pumpAndSettle();
      await tester.scrollUntilVisible(
        find.byKey(Key('plan-${session.id}')),
        200,
        scrollable: find.byType(Scrollable).first,
      );
      await tester.tap(find.byKey(Key('plan-${session.id}')));
      await tester.pumpAndSettle();
      await tester.tap(find.byKey(const Key('tab-schedule')));
      await tester.pumpAndSettle();
      expect(find.text('1 agenda item selected'), findsOneWidget);
      expect(find.byKey(Key('planned-${session.id}')), findsOneWidget);
      await tester.tap(find.byKey(Key('planned-${session.id}')));
      await tester.pumpAndSettle();
      expect(find.byKey(const Key('session-title')), findsOneWidget);
      await tester.binding.handlePopRoute();
      await tester.pumpAndSettle();
      await tester.tap(find.byKey(Key('plan-${session.id}')));
      await tester.pumpAndSettle();
      expect(find.text('Your schedule is empty'), findsOneWidget);
      expect(tester.takeException(), isNull);
      controller.dispose();
    },
  );
}
