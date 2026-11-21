import 'package:firebase_auth/firebase_auth.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';

import '../../../app.dart';
import '../../agenda/logic/agenda_controller.dart';
import '../../agenda/ui/explore_shell.dart';
import '../../account/data/personal_data_repository.dart';
import '../../account/ui/account_sheet.dart';
import '../data/auth_repository.dart';
import 'auth_page.dart';

class AuthGate extends StatefulWidget {
  const AuthGate({super.key});
  @override
  State<AuthGate> createState() => _AuthGateState();
}

class _AuthGateState extends State<AuthGate> {
  final repository = AuthRepository();
  late final stream = repository.watch();
  @override
  Widget build(BuildContext context) => StreamBuilder<User?>(
    stream: stream,
    builder: (context, snapshot) {
      final user = snapshot.data;
      return EventaApp(
        key: ValueKey(user?.uid),
        home: snapshot.connectionState == ConnectionState.waiting
            ? const Scaffold(body: Center(child: CircularProgressIndicator()))
            : user == null
            ? AuthPage(repository: repository)
            : _SignedInUser(
                key: ValueKey(user.uid),
                user: user,
                auth: repository,
              ),
      );
    },
  );
}

class _SignedInUser extends StatefulWidget {
  const _SignedInUser({super.key, required this.user, required this.auth});
  final User user;
  final AuthRepository auth;
  @override
  State<_SignedInUser> createState() => _SignedInUserState();
}

class _SignedInUserState extends State<_SignedInUser> {
  late final personal = PersonalDataRepository(uid: widget.user.uid);
  late final controller = AgendaController(personal: personal);
  String? _profileName, _profileEmail;
  @override
  void initState() {
    super.initState();
    _saveProfile();
  }

  @override
  void didUpdateWidget(_SignedInUser oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (_profileName != widget.user.displayName ||
        _profileEmail != widget.user.email) {
      _saveProfile();
    }
  }

  void _saveProfile() {
    _profileName = widget.user.displayName;
    _profileEmail = widget.user.email;
    _profileError = '';
    personal.firestore
        .collection('users')
        .doc(widget.user.uid)
        .set({
          'displayName': widget.user.displayName ?? '',
          'email': widget.user.email ?? '',
          'updatedAt': FieldValue.serverTimestamp(),
        })
        .catchError((Object error) {
          if (mounted) {
            setState(() => _profileError = 'Could not save your profile.');
          }
        });
  }

  String _profileError = '';
  @override
  void dispose() {
    controller.dispose();
    personal.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Column(
    children: [
      if (_profileError.isNotEmpty)
        Material(child: SafeArea(bottom: false, child: Text(_profileError))),
      Expanded(
        child: ExploreShell(
          controller: controller,
          onAccount: () => showModalBottomSheet<void>(
            context: context,
            isScrollControlled: true,
            useSafeArea: true,
            builder: (_) => AccountSheet(
              user: widget.user,
              auth: widget.auth,
              personal: personal,
            ),
          ),
        ),
      ),
    ],
  );
}
