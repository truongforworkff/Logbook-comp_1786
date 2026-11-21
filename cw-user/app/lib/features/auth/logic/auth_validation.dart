String? emailError(String? value) {
  final email = value?.trim() ?? '';
  return RegExp(r'^[^\s@]+@[^\s@]+\.[^\s@]+$').hasMatch(email) &&
          email.length <= 254
      ? null
      : 'Enter a valid email address.';
}

String? passwordError(String? value, {bool registering = false}) {
  if (value == null || value.isEmpty) return 'Enter your password.';
  if (registering && value.length < 8) return 'Use at least 8 characters for your password.';
  return null;
}
