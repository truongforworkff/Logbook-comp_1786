import 'dart:async';
import 'dart:convert';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/foundation.dart';

import '../../schedule/logic/personal_schedule.dart';
import '../logic/personal_selection.dart';

String preferenceId(String eventId, [String? sessionId]) =>
    base64UrlEncode(utf8.encode(jsonEncode([eventId, ?sessionId])))
        .replaceAll('=', '');

class PersonalDataRepository extends ChangeNotifier {
  PersonalDataRepository({required this.uid, FirebaseFirestore? firestore})
    : firestore = firestore ?? FirebaseFirestore.instance;
  final String uid;
  final FirebaseFirestore firestore;
  Set<String> favourites = {};
  Set<SessionSelection> schedule = {};
  bool ready = false, pending = false, fromCache = true;
  String error = '';
  bool _disposed = false;
  final List<StreamSubscription<QuerySnapshot<Map<String, dynamic>>>>
  _subscriptions = [];
  final _loaded = <String>{}, _pending = <String>{}, _cached = <String>{};

  CollectionReference<Map<String, dynamic>> _collection(String name) =>
      firestore.collection('users').doc(uid).collection(name);

  Future<void>? _starting;

  Future<void> start() => _starting ??= _listen();

  Future<void> _listen() async {
    for (final subscription in _subscriptions.toList()) {
      await subscription.cancel();
    }
    if (_disposed) return;
    _subscriptions.clear();
    _loaded.clear();
    _pending.clear();
    _cached.clear();
    ready = false;
    final first = <Future<void>>[];
    for (final kind in ['favourites', 'schedule']) {
      final ready = Completer<void>();
      first.add(ready.future);
      _subscriptions.add(
        _collection(kind)
            .snapshots(includeMetadataChanges: true)
            .listen(
              (snapshot) {
                if (_disposed) return;
                if (kind == 'favourites') {
                  favourites = cloudFavourites(
                    snapshot.docs.map((d) => d.data()),
                  );
                } else {
                  schedule = cloudSchedule(snapshot.docs.map((d) => d.data()));
                }
                _loaded.add(kind);
                snapshot.metadata.hasPendingWrites
                    ? _pending.add(kind)
                    : _pending.remove(kind);
                snapshot.metadata.isFromCache
                    ? _cached.add(kind)
                    : _cached.remove(kind);
                this.ready = _loaded.length == 2;
                pending = _pending.isNotEmpty;
                fromCache = _cached.isNotEmpty;
                error = '';
                notifyListeners();
                if (!ready.isCompleted) ready.complete();
              },
              onError: (Object problem) {
                _starting = null;
                if (!_disposed) {
                  this.ready = false;
                  error = 'Could not sync your personal data. Check your connection or sign in again.';
                  notifyListeners();
                }
                if (!ready.isCompleted) ready.complete();
              },
            ),
      );
    }
    try {
      await Future.wait(first).timeout(const Duration(seconds: 12));
    } on TimeoutException {
      _starting = null;
      rethrow;
    }
  }

  Future<void> setFavourite(String eventId, bool selected) async {
    _queue(
      _change(_collection('favourites').doc(preferenceId(eventId)), selected, {
        'eventId': eventId,
      }),
    );
  }

  Future<void> setSession(SessionSelection selection, bool selected) async {
    _queue(
      _change(
        _collection('schedule')
            .doc(preferenceId(selection.eventId, selection.sessionId)),
        selected,
        {'eventId': selection.eventId, 'sessionId': selection.sessionId},
      ),
    );
  }

  Future<void> importSelections(
    Set<String> events,
    Set<SessionSelection> sessions,
  ) async {
    final writes = [
      for (final id in events)
        _change(_collection('favourites').doc(preferenceId(id)), true, {
          'eventId': id,
        }),
      for (final session in sessions)
        _change(
          _collection('schedule')
              .doc(preferenceId(session.eventId, session.sessionId)),
          true,
          {'eventId': session.eventId, 'sessionId': session.sessionId},
        ),
    ];
    await Future.wait(writes).timeout(const Duration(seconds: 12));
  }

  Future<void> _change(
    DocumentReference<Map<String, dynamic>> ref,
    bool selected,
    Map<String, dynamic> fields,
  ) {
    if (!ready || _disposed) throw StateError('Personal data unavailable');
    if (!validPreferenceId(fields['eventId']) ||
        (fields.containsKey('sessionId') &&
            !validPreferenceId(fields['sessionId'], maxLength: 256))) {
      throw ArgumentError('Invalid selection');
    }
    error = '';
    // Firestore persists queued writes offline; each selection is one document to avoid replacing another device's list.
    return selected
        ? ref.set({...fields, 'updatedAt': FieldValue.serverTimestamp()})
        : ref.delete();
  }

  void _queue(Future<void> operation) {
    unawaited(
      operation.catchError((Object problem) {
        if (!_disposed) {
          error = 'A change could not be synced. Check your schedule and try again.';
          notifyListeners();
        }
      }),
    );
  }

  Future<void> waitForSync() =>
      firestore.waitForPendingWrites().timeout(const Duration(seconds: 12));

  @override
  void dispose() {
    _disposed = true;
    for (final subscription in _subscriptions) {
      unawaited(subscription.cancel());
    }
    super.dispose();
  }
}
