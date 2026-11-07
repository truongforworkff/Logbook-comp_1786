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
    apiKey: "AIzaSyCRDAKvbGdrwsV1k4v6ify8afk-zxI3g48",
    appId: "1:1011104656179:android:be93151b56c252138fac46",
    messagingSenderId: "1011104656179",
    projectId: "eventa-coursework-fpt",
  );
  static const web = FirebaseOptions(
    apiKey: "AIzaSyAahQ1OJffTpAatVakIz0HRNyhhLV6sJ_k",
    appId: "1:1011104656179:web:28e6e979f4ef37f98fac46",
    messagingSenderId: "1011104656179",
    projectId: "eventa-coursework-fpt",
    authDomain: "eventa-coursework-fpt.firebaseapp.com",
  );
}
