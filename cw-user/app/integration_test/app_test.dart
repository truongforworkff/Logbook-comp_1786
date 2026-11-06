import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:eventa_user/app.dart';
import 'package:eventa_user/features/agenda/data/catalog_repository.dart';
import 'package:eventa_user/features/agenda/logic/agenda_controller.dart';
import 'package:eventa_user/features/favourites/data/favourite_store.dart';

void main() {
  final binding = IntegrationTestWidgetsFlutterBinding.ensureInitialized();
  testWidgets('G/H: filters, native navigation and persistent favourites', (
    tester,
  ) async {
    final store = FavouriteStore();
    final events = await CatalogRepository().load();
    final original = await store.load(events.map((event) => event.id));
    addTearDown(() async => store.save(original));
    final controller = AgendaController(favourites: store);
    await tester.pumpWidget(EventaApp(controller: controller));
    await tester.pumpAndSettle();
    await binding.convertFlutterSurfaceToImage();
    await tester.pumpAndSettle();
    expect(find.text('5 sự kiện'), findsOneWidget);
    await binding.takeScreenshot('01-explore');

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
    await binding.takeScreenshot('02-category-no-image');
    await tester.tap(find.byKey(const Key('category-filter')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const Key('category-')));
    await tester.pumpAndSettle();

    await tester.tap(find.byKey(const Key('date-filter')));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Xem sự kiện'));
    await tester.pumpAndSettle();
    expect(find.text('1 sự kiện'), findsOneWidget);
    await tester.scrollUntilVisible(
      find.byKey(const Key('event-ignite-2026')),
      220,
      scrollable: find.byType(Scrollable).first,
    );
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const Key('event-ignite-2026')));
    await tester.pumpAndSettle();
    final hadFavourite = controller.saved.contains('ignite-2026');
    await tester.tap(find.byKey(const Key('favourite-ignite-2026')));
    await tester.pumpAndSettle();
    await tester.pump(const Duration(seconds: 3));
    await tester.pumpAndSettle();
    expect(controller.saved.contains('ignite-2026'), !hadFavourite);
    final persisted = await store.load(events.map((event) => event.id));
    expect(persisted.contains('ignite-2026'), !hadFavourite);
    await tester.tap(find.byKey(const Key('favourite-ignite-2026')));
    await tester.pumpAndSettle();
    await tester.pump(const Duration(seconds: 3));
    await tester.pumpAndSettle();
    expect(controller.saved.contains('ignite-2026'), hadFavourite);
    await binding.takeScreenshot('03-event-inline-schedule');
    await tester.scrollUntilVisible(
      find.byKey(const Key('session-ignite-2026-1')),
      220,
      scrollable: find.byType(Scrollable).first,
    );
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const Key('session-ignite-2026-1')));
    await tester.pumpAndSettle();
    expect(find.byKey(const Key('session-title')), findsOneWidget);
    await binding.takeScreenshot('04-session');
    await tester.binding.handlePopRoute();
    await tester.pumpAndSettle();
    expect(find.byKey(const Key('event-details-ignite-2026')), findsOneWidget);
    await tester.tap(find.byKey(const Key('tab-saved')));
    await tester.pumpAndSettle();
    await binding.takeScreenshot('05-favourites');

    final reopened = AgendaController(favourites: store);
    await reopened.load();
    expect(reopened.saved, controller.saved);
    reopened.dispose();
  });
}
