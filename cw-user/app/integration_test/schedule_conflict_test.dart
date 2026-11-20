import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:eventa_user/app.dart';
import 'package:eventa_user/shared/ui/app_theme.dart';
import 'package:eventa_user/features/agenda/logic/agenda_controller.dart';
import 'package:eventa_user/features/agenda/ui/session_details_page.dart';
import 'package:eventa_user/features/schedule/data/schedule_store.dart';

import '../test/support/conflict_catalog.dart';

void main() {
  final binding = IntegrationTestWidgetsFlutterBinding.ensureInitialized();
  testWidgets('Android confirms conflicts and retains them after reopening', (
    tester,
  ) async {
    final store = ScheduleStore();
    final original = await store.load();
    addTearDown(() => store.save(original));
    await store.save({(eventId: 'one', sessionId: 'a')});
    final catalog = ConflictCatalog();
    final controller = AgendaController(catalog: catalog, schedule: store);
    addTearDown(controller.dispose);
    await controller.load();
    final event = catalog.entries.last;
    await tester.pumpWidget(
      MaterialApp(
        theme: appTheme(),
        home: SessionDetailsPage(
          event: event,
          session: event.sessions.first,
          controller: controller,
        ),
      ),
    );
    await tester.pumpAndSettle();
    await binding.convertFlutterSurfaceToImage();
    await tester.pumpAndSettle();
    await tester.scrollUntilVisible(
      find.byKey(const Key('plan-b')),
      180,
      scrollable: find.byType(Scrollable).first,
    );
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const Key('plan-b')));
    await tester.pumpAndSettle();
    expect(find.byKey(const Key('schedule-conflict-dialog')), findsOneWidget);
    await binding.takeScreenshot('08-schedule-conflict-dialog');
    await tester.tap(find.byKey(const Key('cancel-conflicting-session')));
    await tester.pumpAndSettle();
    expect((await store.load()).length, 1);
    await tester.tap(find.byKey(const Key('plan-b')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const Key('confirm-conflicting-session')));
    await tester.pumpAndSettle();
    expect((await store.load()).length, 2);
    await tester.pumpWidget(const SizedBox.shrink());
    await tester.pumpAndSettle();

    final reopened = AgendaController(
      catalog: ConflictCatalog(),
      schedule: ScheduleStore(),
    );
    addTearDown(reopened.dispose);
    reopened.selectTab(AgendaTab.schedule);
    await tester.pumpWidget(EventaApp(controller: reopened));
    await tester.pumpAndSettle();
    expect(find.text('2 session đã chọn'), findsOneWidget);
    expect(find.byKey(const Key('conflict-one-a')), findsOneWidget);
    await binding.takeScreenshot('09-my-schedule-conflicts');
    await tester.scrollUntilVisible(
      find.byKey(const Key('plan-b')),
      180,
      scrollable: find.byType(Scrollable).first,
    );
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const Key('plan-b')));
    await tester.pumpAndSettle();
    expect((await store.load()).length, 1);
    expect(find.byKey(const Key('conflict-one-a')), findsNothing);
    expect(tester.takeException(), isNull);
  });
}
