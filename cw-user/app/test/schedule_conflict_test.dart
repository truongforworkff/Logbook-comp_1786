import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:eventa_user/features/schedule/ui/my_schedule_page.dart';
import 'package:eventa_user/features/agenda/data/catalog_repository.dart';
import 'package:eventa_user/features/agenda/logic/agenda_controller.dart';
import 'package:eventa_user/features/agenda/ui/session_details_page.dart';
import 'package:eventa_user/features/schedule/logic/personal_schedule.dart';
import 'package:eventa_user/features/schedule/ui/schedule_button.dart';

import 'support/conflict_catalog.dart';
import 'support/memory_favourites.dart';
import 'support/memory_schedule.dart';

class UpdatingCatalog extends CatalogRepository {
  final changes = StreamController<CatalogData>.broadcast();
  @override
  Stream<CatalogData> watch() => changes.stream;
}

void main() {
  test('overlap checks same-day intervals, containment, boundaries and composite IDs', () {
    PlannedSession entry(
      String event,
      String day,
      String id,
      String start,
      String end,
    ) {
      final session = timedSession(id, start, end);
      return PlannedSession(timedEvent(event, day, [session]), session);
    }

    final a = entry('one', '2026-12-01', 'a', '09:00', '10:00');
    final overlap = entry('two', '2026-12-01', 'a', '09:30', '10:30');
    final contained = entry('three', '2026-12-01', 'c', '09:15', '09:45');
    final identical = entry('four', '2026-12-01', 'd', '09:00', '10:00');
    final adjacent = entry('five', '2026-12-01', 'e', '10:00', '11:00');
    final previous = entry('six', '2026-12-01', 'f', '08:00', '09:00');
    final otherDay = entry('seven', '2026-12-02', 'g', '09:30', '10:30');
    final unknown = entry('eight', '2026-12-01', 'h', '09:30', '');
    final invalid = entry('nine', '2026-12-01', 'i', '11:00', '10:00');
    final planned = [
      a,
      overlap,
      contained,
      identical,
      adjacent,
      previous,
      otherDay,
      unknown,
      invalid,
    ];
    expect(scheduleConflicts(a, planned), [overlap, contained, identical]);
    expect(scheduleConflicts(contained, [a]), [a]);
    expect(scheduleConflicts(unknown, planned), isEmpty);
    expect(scheduleConflicts(invalid, planned), isEmpty);
  });

  test(
    'conflicting additions need explicit confirmation; removal never does',
    () async {
      final store = MemorySchedule();
      final controller = AgendaController(
        catalog: ConflictCatalog(),
        favourites: MemoryFavourites(),
        schedule: store,
      );
      addTearDown(controller.dispose);
      await controller.load();
      await controller.toggleSession('one', 'a');
      await expectLater(
        controller.toggleSession('two', 'b'),
        throwsA(isA<ScheduleConflict>()),
      );
      expect(store.writes, 1);
      expect(controller.schedule.length, 1);
      await controller.toggleSession('two', 'b', allowConflicts: true);
      expect(controller.schedule.length, 2);
      store.failWrites = true;
      await expectLater(controller.toggleSession('two', 'b'), throwsStateError);
      expect(controller.schedule.length, 2);
      store.failWrites = false;
      await controller.toggleSession('two', 'b');
      expect(controller.schedule.single.session.id, 'a');
    },
  );

  testWidgets(
    'cancel, confirm, cloud reschedule and removal update conflict indicators',
    (tester) async {
      tester.view.physicalSize = const Size(1080, 2400);
      tester.view.devicePixelRatio = 2.75;
      addTearDown(tester.view.resetPhysicalSize);
      addTearDown(tester.view.resetDevicePixelRatio);
      final catalog = UpdatingCatalog();
      final events = ConflictCatalog().entries;
      final store = MemorySchedule({(eventId: 'one', sessionId: 'a')});
      final controller = AgendaController(
        catalog: catalog,
        favourites: MemoryFavourites(),
        schedule: store,
      );
      addTearDown(controller.dispose);
      addTearDown(catalog.changes.close);
      final ready = controller.load();
      await tester.pump();
      catalog.changes.add(CatalogData(events));
      await tester.pump();
      await ready;
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ListenableBuilder(
              listenable: controller,
              builder: (_, _) => ScheduleButton(
                controller: controller,
                eventId: 'two',
                sessionId: 'b',
              ),
            ),
          ),
        ),
      );
      await tester.tap(find.byKey(const Key('plan-b')));
      await tester.pumpAndSettle();
      expect(find.byKey(const Key('schedule-conflict-dialog')), findsOneWidget);
      expect(find.text('09:00 – 10:00 · Sự kiện one'), findsOneWidget);
      await tester.tap(find.byKey(const Key('cancel-conflicting-session')));
      await tester.pumpAndSettle();
      expect(store.selected.length, 1);
      await tester.tap(find.byKey(const Key('plan-b')));
      await tester.pumpAndSettle();
      await tester.tap(find.byKey(const Key('confirm-conflicting-session')));
      await tester.pumpAndSettle();
      expect(store.selected.length, 2);
      controller.selectTab(AgendaTab.schedule);
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ListenableBuilder(
              listenable: controller,
              builder: (_, _) => MySchedulePage(controller: controller),
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();
      expect(find.byKey(const Key('conflict-one-a')), findsOneWidget);
      await tester.scrollUntilVisible(
        find.byKey(const Key('conflict-two-b')),
        180,
      );
      expect(find.byKey(const Key('conflict-two-b')), findsOneWidget);
      catalog.changes.add(
        CatalogData([
          events.first,
          timedEvent('two', '2026-12-01', [
            timedSession('b', '10:00', '11:00'),
          ]),
        ]),
      );
      await tester.pumpAndSettle();
      expect(find.byKey(const Key('conflict-one-a')), findsNothing);
      expect(find.byKey(const Key('conflict-two-b')), findsNothing);
      expect(store.selected.length, 2);
      await tester.scrollUntilVisible(find.byKey(const Key('plan-b')), 180);
      await tester.tap(find.byKey(const Key('plan-b')));
      await tester.pumpAndSettle();
      expect(store.selected.length, 1);
      expect(find.byKey(const Key('schedule-conflict-dialog')), findsNothing);
      expect(tester.takeException(), isNull);
    },
  );

  testWidgets(
    'session details show both times and legacy schedule explains unknown duration',
    (tester) async {
      final catalog = ConflictCatalog();
      final controller = AgendaController(
        catalog: catalog,
        favourites: MemoryFavourites(),
        schedule: MemorySchedule({(eventId: 'one', sessionId: 'a')}),
      );
      addTearDown(controller.dispose);
      await controller.load();
      final event = catalog.entries.first;
      await tester.pumpWidget(
        MaterialApp(
          home: SessionDetailsPage(
            event: event,
            session: event.sessions.first,
            controller: controller,
          ),
        ),
      );
      await tester.pumpAndSettle();
      expect(find.text('09:00 – 10:00'), findsOneWidget);
      controller.events = [
        timedEvent('one', '2026-12-01', [timedSession('a', '09:00', '')]),
      ];
      controller.selectTab(AgendaTab.schedule);
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ListenableBuilder(
              listenable: controller,
              builder: (_, _) => MySchedulePage(controller: controller),
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();
      expect(
        find.text('Chưa có giờ kết thúc; chưa kiểm tra được trùng lịch.'),
        findsOneWidget,
      );
      expect(tester.takeException(), isNull);
    },
  );
}
