import 'package:flutter/foundation.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:google_sign_in/google_sign_in.dart';

class AuthRepository {
  AuthRepository({FirebaseAuth? auth}) : auth = auth ?? FirebaseAuth.instance;
  final FirebaseAuth auth;
  Future<void>? _googleReady;
  Stream<User?> watch() => auth.userChanges();

  Future<void> signIn(String email, String password) async {
    await auth.signInWithEmailAndPassword(email: email.trim(), password: password);
  }

  Future<void> register(String name, String email, String password) async {
    final result = await auth.createUserWithEmailAndPassword(email: email.trim(), password: password);
    await result.user!.updateDisplayName(name.trim());
  }

  Future<void> google() async {
    if (kIsWeb) {
      await auth.signInWithPopup(GoogleAuthProvider());
      return;
    }
    final google = GoogleSignIn.instance;
    await (_googleReady ??= google.initialize(
      serverClientId: '1011104656179-b4kiil45g0sc6h7ooi9hp8nfudvgc9ct.apps.googleusercontent.com',
    ));
    final account = await google.authenticate();
    final token = account.authentication.idToken;
    if (token == null) throw StateError('Google chưa trả về thông tin đăng nhập.');
    await auth.signInWithCredential(GoogleAuthProvider.credential(idToken: token));
  }

  Future<void> signOut() async {
    await auth.signOut();
    if (!kIsWeb && _googleReady != null) await GoogleSignIn.instance.signOut();
  }
}

String authMessage(Object error) {
  if (error is GoogleSignInException) {
    return error.code == GoogleSignInExceptionCode.canceled
        ? 'Bạn đã hủy đăng nhập Google.'
        : 'Chưa đăng nhập Google được. Kiểm tra mạng và cấu hình Google của app.';
  }
  if (error is FirebaseAuthException) {
    return switch (error.code) {
      'email-already-in-use' => 'Email đã có tài khoản. Hãy chuyển sang đăng nhập.',
      'weak-password' => 'Mật khẩu chưa đủ mạnh.',
      'invalid-email' => 'Email chưa hợp lệ.',
      'invalid-credential' || 'wrong-password' || 'user-not-found' => 'Email hoặc mật khẩu chưa đúng.',
      'user-disabled' => 'Tài khoản đã bị vô hiệu hóa.',
      'network-request-failed' => 'Chưa có kết nối mạng. Hãy thử lại.',
      'too-many-requests' => 'Có quá nhiều lần thử. Hãy chờ rồi thử lại.',
      'account-exists-with-different-credential' => 'Email này đã dùng cách đăng nhập khác. Hãy đăng nhập bằng mật khẩu đã tạo.',
      _ => 'Chưa đăng nhập được. Hãy thử lại.',
    };
  }
  return 'Chưa hoàn tất thao tác. Hãy thử lại.';
}
