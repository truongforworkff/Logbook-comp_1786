String? emailError(String? value) {
  final email = value?.trim() ?? '';
  return RegExp(r'^[^\s@]+@[^\s@]+\.[^\s@]+$').hasMatch(email) && email.length <= 254
      ? null : 'Nhập email hợp lệ.';
}

String? passwordError(String? value, {bool registering = false}) {
  if (value == null || value.isEmpty) return 'Nhập mật khẩu.';
  if (registering && value.length < 8) return 'Mật khẩu cần ít nhất 8 ký tự.';
  return null;
}
