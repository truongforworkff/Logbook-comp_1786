import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/foundation.dart';

// Public identifiers select the project; Firestore rules control access.
abstract final class DefaultFirebaseOptions {
  static FirebaseOptions? get currentPlatform {
    if (kIsWeb) return web;
    // shortcut: register Apple/desktop apps before enabling Firebase on those targets.
    return defaultTargetPlatform == TargetPlatform.android ? android : null;
  }

  static const android = FirebaseOptions(
    apiKey: "AIzaSyAVeRAhTGLE2cNiFU_iM2wtoUfF4dzWvzA",
    appId: "1:298519879781:android:0cde9e115aec7b89f1e68c",
    messagingSenderId: "298519879781",
    projectId: "eventa-coursework",
  );

  static const web = FirebaseOptions(
    apiKey: "AIzaSyB3vKvWNcbSXM3HSiInc2yBqpvXVJZruLU",
    appId: "1:298519879781:web:2df9c9ee040eb526f1e68c",
    messagingSenderId: "298519879781",
    projectId: "eventa-coursework",
    authDomain: "eventa-coursework.firebaseapp.com",
  );
}
