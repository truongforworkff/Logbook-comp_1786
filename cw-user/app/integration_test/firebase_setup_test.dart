import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:eventa_user/shared/data/firebase_options.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();
  testWidgets('Firebase connects and rejects unapproved access', (_) async {
    final app = await Firebase.initializeApp(
      options: DefaultFirebaseOptions.currentPlatform!,
    );
    expect(app.options.projectId, 'eventa-coursework-fpt');
    await expectLater(
      FirebaseFirestore.instance
          .collection('setup_checks')
          .doc('probe')
          .get(const GetOptions(source: Source.server))
          .timeout(const Duration(seconds: 25)),
      throwsA(
        isA<FirebaseException>().having(
          (error) => error.code,
          'server rejection',
          'permission-denied',
        ),
      ),
    );
  });
}
