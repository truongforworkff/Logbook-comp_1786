import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:eventa_user/features/account/data/personal_data_repository.dart';
import 'package:eventa_user/features/auth/data/auth_repository.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';

Future<({AuthRepository auth, FirebaseFirestore cloud})> client(
  String name,
) async {
  final app = await Firebase.initializeApp(
    name: name,
    options: const FirebaseOptions(
      projectId: 'demo-eventa',
      apiKey: 'fake-emulator-api-key',
      appId: '1:123:android:test',
      messagingSenderId: '123',
    ),
  );
  final auth = FirebaseAuth.instanceFor(app: app);
  await auth.useAuthEmulator('10.0.2.2', 9098);
  final cloud = FirebaseFirestore.instanceFor(app: app);
  cloud.useFirestoreEmulator('10.0.2.2', 8086);
  return (auth: AuthRepository(auth: auth), cloud: cloud);
}

Future<void> until(bool Function() matches) async {
  for (var i = 0; i < 150 && !matches(); i++) {
    await Future<void>.delayed(const Duration(milliseconds: 100));
  }
  expect(matches(), isTrue);
}

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();
  testWidgets(
    'Account keeps selections on another client and queues offline edits',
    (_) async {
      final first = await client('account-first');
      final second = await client('account-second');
      final label = DateTime.now().microsecondsSinceEpoch;
      final email = 'attendee-$label@test.eventa';
      const password = 'EmulatorOnly123!';
      await first.auth.register('Người tham dự', email, password);
      await second.auth.signIn(email, password);
      final uid = first.auth.auth.currentUser!.uid;
      expect(second.auth.auth.currentUser!.uid, uid);
      expect(second.auth.auth.currentUser!.displayName, 'Người tham dự');
      final a = PersonalDataRepository(uid: uid, firestore: first.cloud);
      final b = PersonalDataRepository(uid: uid, firestore: second.cloud);
      const session = (eventId: 'event', sessionId: 's1');
      try {
        await Future.wait([a.start(), b.start()]);
        await a.setFavourite('event', true);
        await b.setSession(session, true);
        await until(
          () => a.schedule.contains(session) && b.favourites.contains('event'),
        );
        await first.cloud.disableNetwork();
        await a.setSession(session, false);
        await until(() => !a.schedule.contains(session));
        expect(b.schedule, contains(session));
        await first.cloud.enableNetwork();
        await a.waitForSync();
        await until(() => !b.schedule.contains(session));
        b.dispose();
        await second.auth.signOut();
        await second.auth.register(
          'Tài khoản khác',
          'other-$label@test.eventa',
          password,
        );
        final other = PersonalDataRepository(
          uid: second.auth.auth.currentUser!.uid,
          firestore: second.cloud,
        );
        try {
          await other.start();
          await until(() => other.ready && !other.fromCache);
          expect(other.favourites, isEmpty);
          expect(other.schedule, isEmpty);
        } finally {
          other.dispose();
        }
      } finally {
        await first.cloud.enableNetwork();
        a.dispose();
        await first.auth.signOut();
        await second.auth.signOut();
      }
    },
  );
}
