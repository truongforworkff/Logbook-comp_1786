import 'package:flutter/foundation.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:google_sign_in/google_sign_in.dart';

class AuthRepository {
  AuthRepository({FirebaseAuth? auth}) : auth = auth ?? FirebaseAuth.instance;
  final FirebaseAuth auth;
  Future<void>? _googleReady;
  Stream<User?> watch() => auth.userChanges();

  Future<void> signIn(String email, String password) async {
    await auth.signInWithEmailAndPassword(
      email: email.trim(),
      password: password,
    );
  }

  Future<void> register(String name, String email, String password) async {
    final result = await auth.createUserWithEmailAndPassword(
      email: email.trim(),
      password: password,
    );
    await result.user!.updateDisplayName(name.trim());
  }

  Future<void> google() async {
    if (kIsWeb) {
      await auth.signInWithPopup(GoogleAuthProvider());
      return;
    }
    final google = GoogleSignIn.instance;
    await (_googleReady ??= google.initialize());
    final account = await google.authenticate();
    final token = account.authentication.idToken;
    if (token == null) {
      throw StateError('Google did not return sign-in details.');
    }
    await auth.signInWithCredential(
      GoogleAuthProvider.credential(idToken: token),
    );
  }

  Future<void> signOut() async {
    await auth.signOut();
    if (!kIsWeb && _googleReady != null) await GoogleSignIn.instance.signOut();
  }
}

String authMessage(Object error) {
  if (error is GoogleSignInException) {
    return error.code == GoogleSignInExceptionCode.canceled
        ? 'Google sign-in cancelled.'
        : 'Google sign-in failed. Check your connection and the Google sign-in configuration.';
  }
  if (error is FirebaseAuthException) {
    return switch (error.code) {
      'email-already-in-use' =>
        'This email is already registered. Sign in to continue.',
      'weak-password' => 'Choose a stronger password.',
      'invalid-email' => 'Enter a valid email address.',
      'invalid-credential' ||
      'wrong-password' ||
      'user-not-found' => 'Incorrect email or password.',
      'user-disabled' => 'This account has been disabled.',
      'network-request-failed' => 'You are offline. Reconnect and try again.',
      'too-many-requests' => 'Too many attempts. Wait a moment and try again.',
      'account-exists-with-different-credential' => 'This email uses another sign-in method. Sign in with the password you created.',
      _ => 'Could not sign in. Try again.',
    };
  }
  return 'Could not complete the request. Try again.';
}
