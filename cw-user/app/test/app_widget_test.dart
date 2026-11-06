import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:eventa_user/app.dart';
import 'package:eventa_user/features/agenda/logic/agenda_controller.dart';

import 'support/memory_favourites.dart';
import 'support/fixture_catalog.dart';

Future<AgendaController> start(WidgetTester tester) async {
  tester.view.physicalSize = const Size(1080, 2400);
  tester.view.devicePixelRatio = 2.75;
  addTearDown(tester.view.resetPhysicalSize);
  addTearDown(tester.view.resetDevicePixelRatio);
  final controller = AgendaController(
    catalog: FixtureCatalog(),
    favourites: MemoryFavourites(),
  );
  await tester.pumpWidget(EventaApp(controller: controller));
  await tester.pumpAndSettle();
  return controller;
}

void main() {
  testWidgets('search and category controls show matching events', (
    tester,
  ) async {
    final controller = await start(tester);
    expect(find.text('5 sự kiện'), findsOneWidget);
    await tester.enterText(find.byKey(const Key('event-search')), 'NVIDIA');
    await tester.pumpAndSettle();
    expect(find.text('2 sự kiện'), findsOneWidget);
    await tester.tap(find.byKey(const Key('clear-search')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const Key('category-filter')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const Key('category-Technology')));
    await tester.pumpAndSettle();
    expect(find.text('2 sự kiện'), findsOneWidget);
    expect(tester.takeException(), isNull);
    controller.dispose();
  });

  testWidgets(
    'favourite, detail and inline session route retain the same event',
    (tester) async {
      final controller = await start(tester);
      await tester.scrollUntilVisible(
        find.byKey(const Key('event-ignite-2026')),
        200,
        scrollable: find.byType(Scrollable).first,
      );
      await tester.tap(find.byKey(const Key('event-ignite-2026')));
      await tester.pumpAndSettle();
      await tester.tap(find.byKey(const Key('favourite-ignite-2026')));
      await tester.pumpAndSettle();
      expect(controller.saved, {'ignite-2026'});
      await tester.pump(const Duration(seconds: 3));
      await tester.pumpAndSettle();
      await tester.scrollUntilVisible(
        find.byKey(const Key('session-ignite-2026-1')),
        200,
        scrollable: find.byType(Scrollable).first,
      );
      await tester.tap(find.byKey(const Key('session-ignite-2026-1')));
      await tester.pumpAndSettle();
      expect(find.byKey(const Key('session-title')), findsOneWidget);
      expect(find.text('Demo: AI agents trong doanh nghiệp'), findsOneWidget);
      await tester.binding.handlePopRoute();
      await tester.pumpAndSettle();
      expect(
        find.byKey(const Key('event-details-ignite-2026')),
        findsOneWidget,
      );
      await tester.tap(find.byKey(const Key('tab-saved')));
      await tester.pumpAndSettle();
      expect(find.text('1 sự kiện'), findsOneWidget);
      expect(tester.takeException(), isNull);
      controller.dispose();
    },
  );

  testWidgets('empty favourites are clear and have an exploration action', (
    tester,
  ) async {
    final controller = await start(tester);
    await tester.tap(find.byKey(const Key('tab-saved')));
    await tester.pumpAndSettle();
    expect(find.text('Dành một chỗ cho điều bạn thích'), findsOneWidget);
    expect(find.text('Khám phá sự kiện'), findsOneWidget);
    expect(tester.takeException(), isNull);
    controller.dispose();
  });
}
