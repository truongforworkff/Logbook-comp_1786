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
      if (mounted) {
        setState(
          () => _message = 'Sync is incomplete. Reconnect and try again.',
        );
      }
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _import() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Import saved items from this device?'),
        content: Text(
          'Your saved schedule and favourites will be added to ${widget.user.email}.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Back'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Import to account'),
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
      if (mounted) setState(() => _message = 'Your saved items have been imported and synced.');
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
          'My account',
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
          'Your schedule and favourites are saved to your account. Sign in on another device to access them.',
        ),
        if (_message.isNotEmpty)
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 12),
            child: Text(_message),
          ),
        const SizedBox(height: 20),
        OutlinedButton(
          onPressed: _busy ? null : _import,
          child: const Text('Import schedule and favourites from device'),
        ),
        const SizedBox(height: 12),
        FilledButton(
          key: const Key('auth-signout'),
          onPressed: _busy ? null : _logout,
          child: Text(_busy ? 'Waiting for sync…' : 'Sign out'),
        ),
      ],
    ),
  );
}
