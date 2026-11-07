import 'dart:convert';
import 'dart:io';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:eventa_user/app.dart';
import 'package:eventa_user/features/agenda/data/catalog_repository.dart';
import 'package:eventa_user/features/agenda/logic/agenda_controller.dart';
import 'package:eventa_user/features/favourites/data/favourite_store.dart';

Map<String, dynamic> field(dynamic value) {
  if (value is String) return {'stringValue': value};
  if (value is int) return {'integerValue': value.toString()};
  if (value is List) {
    return {
      'arrayValue': {'values': value.map(field).toList()},
    };
  }
  if (value is Map) {
    return {
      'mapValue': {'fields': value.map((k, v) => MapEntry(k, field(v)))},
    };
  }
  throw StateError('Unsupported fixture value');
}

Future<void> publish(List<dynamic> events, {int schemaVersion = 1}) async {
  final client = HttpClient();
  try {
    final request = await client.patchUrl(
      Uri.parse(
        'http://10.0.2.2:8086/v1/projects/demo-eventa/databases/(default)/documents/catalogues/main',
      ),
    );
    request.headers.set('Authorization', 'Bearer owner');
    request.headers.contentType = ContentType.json;
    request.write(
      jsonEncode({
        'fields': {
          'schemaVersion': field(schemaVersion),
          'events': field(events),
          'updatedAt': {
            'timestampValue': DateTime.now().toUtc().toIso8601String(),
          },
        },
      }),
    );
    final response = await request.close();
    await response.drain<void>();
    expect(response.statusCode, 200);
  } finally {
    client.close(force: true);
  }
}

Future<void> removePublishedCatalog() async {
  final client = HttpClient();
  try {
    final request = await client.deleteUrl(
      Uri.parse(
        'http://10.0.2.2:8086/v1/projects/demo-eventa/databases/(default)/documents/catalogues/main',
      ),
    );
    request.headers.set('Authorization', 'Bearer owner');
    final response = await request.close();
    await response.drain<void>();
    expect([200, 404], contains(response.statusCode));
  } finally {
    client.close(force: true);
  }
}

Future<void> until(WidgetTester tester, bool Function() condition) async {
  for (var i = 0; i < 60 && !condition(); i++) {
    await tester.pump(const Duration(milliseconds: 500));
  }
  expect(condition(), isTrue);
  await tester.pumpAndSettle();
}

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();
  testWidgets(
    'real Firestore SDK streams edits, cache and removals without losing favourites',
    (tester) async {
      final app = await Firebase.initializeApp(
        name: 'cloud-test',
        options: const FirebaseOptions(
          apiKey: 'fake-emulator-api-key',
          appId: '1:123:android:test',
          messagingSenderId: '123',
          projectId: 'demo-eventa',
        ),
      );
      final cloud = FirebaseFirestore.instanceFor(app: app);
      cloud.settings = const Settings(persistenceEnabled: true);
      cloud.useFirestoreEmulator('10.0.2.2', 8086);
      await cloud.clearPersistence();
      final events = jsonDecode(
        await rootBundle.loadString('assets/data/events.json'),
      ) as List;
      final first = events.firstWhere((e) => e['id'] == 'ignite-2026') as Map;
      final id = first['id'] as String;
      final favourites = FavouriteStore();
      final original = await favourites.load();
      await favourites.save({id});
      final controller = AgendaController(
        catalog: CatalogRepository(firestore: cloud),
        favourites: favourites,
      );
      try {
        await removePublishedCatalog();
        await tester.pumpWidget(EventaApp(controller: controller));
        await until(tester, () => controller.error.contains('chưa công bố'));
        await publish(events);
        await until(
          tester,
          () => controller.events.length == 5 && !controller.fromCache,
        );
        expect(controller.saved, {id});
        await publish(events, schemaVersion: 99);
        await until(tester, () => controller.error.isNotEmpty);
        expect(controller.events.length, 5);
        await publish(events);
        await until(
          tester,
          () => controller.error.isEmpty && !controller.fromCache,
        );
        await tester.scrollUntilVisible(
          find.byKey(Key('event-$id')),
          200,
          scrollable: find.byType(Scrollable).first,
        );
        await tester.tap(find.byKey(Key('event-$id')));
        await tester.pumpAndSettle();
        first['name'] = 'Updated from live Firestore';
        await publish(events);
        await until(
          tester,
          () => find.text('Updated from live Firestore').evaluate().isNotEmpty,
        );
        await cloud.disableNetwork();
        await until(tester, () => controller.fromCache);
        first['name'] = 'Changed while offline';
        await publish(events);
        await tester.pump(const Duration(seconds: 1));
        expect(
          controller.events.firstWhere((e) => e.id == id).name,
          'Updated from live Firestore',
        );
        await cloud.enableNetwork();
        await until(
          tester,
          () =>
              controller.events.firstWhere((e) => e.id == id).name ==
              'Changed while offline',
        );
        final sessionId = (first['sessions'] as List).first['id'] as String;
        await tester.scrollUntilVisible(
          find.byKey(Key('session-$sessionId')),
          200,
          scrollable: find.byType(Scrollable).first,
        );
        await tester.tap(find.byKey(Key('session-$sessionId')));
        await tester.pumpAndSettle();
        expect(find.byKey(const Key('session-title')), findsOneWidget);
        first['sessions'] = [];
        await publish(events);
        await until(
          tester,
          () => find
              .text('Sự kiện hoặc phiên này đã được gỡ khỏi lịch trình.')
              .evaluate()
              .isNotEmpty,
        );
        await publish([]);
        await until(tester, () => controller.events.isEmpty);
        expect(await favourites.load(), {id});
      } finally {
        await tester.pumpWidget(const SizedBox.shrink());
        controller.dispose();
        await favourites.save(original);
        await cloud.terminate();
        await app.delete();
      }
    },
  );
}
