import 'package:flutter/material.dart';
import '../../../shared/ui/app_widgets.dart';
import '../data/auth_repository.dart';
import '../logic/auth_validation.dart';

class AuthPage extends StatefulWidget {
  const AuthPage({super.key, required this.repository});
  final AuthRepository repository;
  @override
  State<AuthPage> createState() => _AuthPageState();
}

class _AuthPageState extends State<AuthPage> {
  final _form = GlobalKey<FormState>();
  final _name = TextEditingController(), _email = TextEditingController(), _password = TextEditingController(), _confirm = TextEditingController();
  bool _register = false, _busy = false, _obscure = true;
  String _error = '';

  Future<void> _submit({bool google = false}) async {
    if (!google && !_form.currentState!.validate()) return;
    setState(() { _busy = true; _error = ''; });
    try {
      if (google) {
        await widget.repository.google();
      } else if (_register) {
        await widget.repository.register(_name.text, _email.text, _password.text);
      } else {
        await widget.repository.signIn(_email.text, _password.text);
      }
    } catch (error) {
      if (mounted) setState(() => _error = authMessage(error));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  void dispose() {
    _name.dispose(); _email.dispose(); _password.dispose(); _confirm.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    body: SafeArea(child: PageFrame(child: Center(child: SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: AutofillGroup(child: Form(key: _form, child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Icon(Icons.auto_awesome_outlined, color: Color(0xFF087DFF), size: 40),
          const SizedBox(height: 20),
          Text(_register ? 'Tạo tài khoản Eventa' : 'Chào mừng trở lại', style: Theme.of(context).textTheme.headlineMedium),
          const SizedBox(height: 10),
          const Text('Giữ lịch cá nhân và sự kiện yêu thích khi đổi thiết bị.'),
          const SizedBox(height: 28),
          if (_register) ...[
            TextFormField(key: const Key('auth-name'), controller: _name, enabled: !_busy,
              decoration: const InputDecoration(labelText: 'Tên hiển thị', border: OutlineInputBorder(borderRadius: BorderRadius.all(Radius.circular(16)))),
              maxLength: 80, validator: (v) => (v?.trim().isEmpty ?? true) ? 'Nhập tên của bạn.' : null),
            const SizedBox(height: 16),
          ],
          TextFormField(key: const Key('auth-email'), controller: _email, enabled: !_busy,
            keyboardType: TextInputType.emailAddress, autofillHints: const [AutofillHints.email],
            decoration: const InputDecoration(labelText: 'Email', border: OutlineInputBorder(borderRadius: BorderRadius.all(Radius.circular(16)))), validator: emailError),
          const SizedBox(height: 16),
          TextFormField(key: const Key('auth-password'), controller: _password, enabled: !_busy,
            obscureText: _obscure, autofillHints: [_register ? AutofillHints.newPassword : AutofillHints.password],
            decoration: InputDecoration(labelText: 'Mật khẩu', border: const OutlineInputBorder(borderRadius: BorderRadius.all(Radius.circular(16))),
              suffixIcon: IconButton(tooltip: _obscure ? 'Hiện mật khẩu' : 'Ẩn mật khẩu',
                onPressed: () => setState(() => _obscure = !_obscure), icon: Icon(_obscure ? Icons.visibility_outlined : Icons.visibility_off_outlined))),
            validator: (v) => passwordError(v, registering: _register)),
          if (_register) ...[
            const SizedBox(height: 16),
            TextFormField(key: const Key('auth-confirm'), controller: _confirm, enabled: !_busy, obscureText: _obscure,
              decoration: const InputDecoration(labelText: 'Nhập lại mật khẩu', border: OutlineInputBorder(borderRadius: BorderRadius.all(Radius.circular(16)))),
              validator: (v) => v == _password.text ? null : 'Hai mật khẩu chưa khớp.'),
          ],
          if (_error.isNotEmpty) Padding(padding: const EdgeInsets.symmetric(vertical: 16),
            child: Text(_error, semanticsLabel: _error, style: TextStyle(color: Theme.of(context).colorScheme.error))),
          const SizedBox(height: 24),
          FilledButton(key: const Key('auth-submit'), onPressed: _busy ? null : () => _submit(),
            child: Text(_busy ? 'Đang xử lý…' : _register ? 'Tạo tài khoản' : 'Đăng nhập')),
          const SizedBox(height: 12),
          OutlinedButton.icon(key: const Key('auth-google'), onPressed: _busy ? null : () => _submit(google: true),
            icon: const Icon(Icons.account_circle_outlined), label: const Text('Tiếp tục với Google')),
          TextButton(key: const Key('auth-switch'), onPressed: _busy ? null : () => setState(() {
            _register = !_register; _error = ''; _form.currentState?.reset(); _password.clear(); _confirm.clear();
          }), child: Text(_register ? 'Đã có tài khoản? Đăng nhập' : 'Chưa có tài khoản? Đăng ký')),
        ],
      ))),
    )))),
  );
}
