import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:eventa_user/app.dart';
import 'package:eventa_user/features/agenda/logic/agenda_controller.dart';
import 'package:eventa_user/features/schedule/data/schedule_store.dart';

import 'support/demo_catalog.dart';

void main() {
  final binding = IntegrationTestWidgetsFlutterBinding.ensureInitialized();
  testWidgets('attendee schedule persists on Android and can be removed', (tester) async {
    final store = ScheduleStore();
    final original = await store.load();
    addTearDown(() => store.save(original));
    await store.save({});
    final controller = AgendaController(catalog: DemoCatalog(), schedule: store);
    addTearDown(controller.dispose);
    await tester.pumpWidget(EventaApp(controller: controller));
    await tester.pumpAndSettle();
    await binding.convertFlutterSurfaceToImage();
    await tester.pumpAndSettle();
    await tester.enterText(find.byKey(const Key('event-search')), 'Microsoft Ignite');
    await tester.pumpAndSettle();
    await tester.ensureVisible(find.byKey(const Key('event-ignite-2026')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const Key('event-ignite-2026')));
    await tester.pumpAndSettle();
    await tester.scrollUntilVisible(
      find.byKey(const Key('session-ignite-2026-1')), 200,
      scrollable: find.byType(Scrollable).first,
    );
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const Key('session-ignite-2026-1')));
    await tester.pumpAndSettle();
    await tester.scrollUntilVisible(
      find.byKey(const Key('plan-ignite-2026-1')), 200,
      scrollable: find.byType(Scrollable).first,
    );
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const Key('plan-ignite-2026-1')));
    await tester.pumpAndSettle();
    expect((await store.load()).single,
      (eventId: 'ignite-2026', sessionId: 'ignite-2026-1'));
    await binding.takeScreenshot('06-session-planned');
    await tester.pumpWidget(const SizedBox.shrink());
    await tester.pumpAndSettle();

    final reopened = AgendaController(catalog: DemoCatalog(), schedule: ScheduleStore());
    addTearDown(reopened.dispose);
    reopened.selectTab(AgendaTab.schedule);
    await tester.pumpWidget(EventaApp(controller: reopened));
    await tester.pumpAndSettle();
    expect(find.text('1 session đã chọn'), findsOneWidget);
    expect(find.text('Demo: AI agents trong doanh nghiệp'), findsOneWidget);
    await binding.takeScreenshot('07-my-schedule');
    await tester.tap(find.byKey(const Key('plan-ignite-2026-1')));
    await tester.pumpAndSettle();
    expect(await store.load(), isEmpty);
    expect(find.text('Chưa có session trong lịch'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });
}
