import 'package:flutter/material.dart';
import 'package:firebase_auth/firebase_auth.dart';

import '../../auth/data/auth_repository.dart';
import '../../favourites/data/favourite_store.dart';
import '../../schedule/data/schedule_store.dart';
import '../data/personal_data_repository.dart';

class AccountSheet extends StatefulWidget {
  const AccountSheet({
    super.key,
    required this.user,
    required this.auth,
    required this.personal,
  });
  final User user;
  final AuthRepository auth;
  final PersonalDataRepository personal;
  @override
  State<AccountSheet> createState() => _AccountSheetState();
}

class _AccountSheetState extends State<AccountSheet> {
  bool _busy = false;
  String _message = '';
  Future<void> _run(Future<void> Function() action) async {
    setState(() {
      _busy = true;
      _message = '';
    });
    try {
      await action();
    } catch (_) {
      if (mounted)
        setState(
          () => _message = 'Chưa hoàn tất đồng bộ. Kết nối mạng rồi thử lại.',
        );
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _import() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Nhập lựa chọn cũ từ máy?'),
        content: Text(
          'Lịch và yêu thích lưu trên máy trước đây sẽ được thêm vào tài khoản ${widget.user.email}.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Quay lại'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Nhập vào tài khoản'),
          ),
        ],
      ),
    );
    if (confirmed != true || !mounted) return;
    await _run(() async {
      await widget.personal.importSelections(
        await FavouriteStore().load(),
        await ScheduleStore().load(),
      );
      if (mounted) setState(() => _message = 'Đã nhập và đồng bộ lựa chọn cũ.');
    });
  }

  Future<void> _logout() => _run(() async {
    await widget.personal.waitForSync();
    if (!mounted) return;
    await widget.auth.signOut();
  });

  @override
  Widget build(BuildContext context) => SingleChildScrollView(
    padding: const EdgeInsets.fromLTRB(24, 8, 24, 32),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(
          'Tài khoản của tôi',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 16),
        Text(
          widget.user.displayName ?? '',
          style: Theme.of(context).textTheme.titleMedium,
        ),
        Text(widget.user.email ?? ''),
        const SizedBox(height: 10),
        const Text(
          'Lịch cá nhân và yêu thích được lưu theo tài khoản. Đăng nhập cùng tài khoản trên thiết bị khác để tiếp tục.',
        ),
        if (_message.isNotEmpty)
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 12),
            child: Text(_message),
          ),
        const SizedBox(height: 20),
        OutlinedButton(
          onPressed: _busy ? null : _import,
          child: const Text('Nhập lịch & yêu thích cũ từ máy'),
        ),
        const SizedBox(height: 12),
        FilledButton(
          key: const Key('auth-signout'),
          onPressed: _busy ? null : _logout,
          child: Text(_busy ? 'Đang chờ đồng bộ…' : 'Đăng xuất'),
        ),
      ],
    ),
  );
}
