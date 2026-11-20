import 'dart:async';
import 'dart:math' as math;

import 'package:flutter/foundation.dart';

import '../data/catalog_repository.dart';
import '../data/event_info.dart';
import '../../favourites/data/favourite_store.dart';
import '../../schedule/data/schedule_store.dart';
import '../../account/data/personal_data_repository.dart';
import '../../schedule/logic/personal_schedule.dart';

enum AgendaTab { explore, saved, schedule }

class AgendaController extends ChangeNotifier {
  AgendaController({
    CatalogRepository? catalog,
    FavouriteStore? favourites,
    ScheduleStore? schedule,
    this.personal,
  }) : _catalog = catalog ?? CatalogRepository(),
       _favourites = favourites ?? FavouriteStore(),
       _schedule = schedule ?? ScheduleStore();
  final CatalogRepository _catalog;
  final PersonalDataRepository? personal;
  bool _personalBound = false;
  final FavouriteStore _favourites;
  final ScheduleStore _schedule;
  Set<SessionSelection> _planned = {};
  bool _scheduleLoaded = false;
  bool savingSchedule = false;
  List<PlannedSession> get schedule => personalSchedule(events, _planned);

  bool hasPlannedSession(String eventId, String sessionId) =>
      _planned.contains((eventId: eventId, sessionId: sessionId));
  List<EventInfo> events = [];
  Set<String> _saved = {};
  Set<String> get saved =>
      _saved.intersection(events.map((event) => event.id).toSet());
  bool loading = true, saving = false, fromCache = false;
  String error = '';
  DateTime? updatedAt;
  AgendaTab tab = AgendaTab.explore;
  StreamSubscription<CatalogData>? _subscription;
  Timer? _initialTimer;
  Timer? _retryTimer;
  int _retrySeconds = 5;
  Completer<void>? _ready;
  int _generation = 0;
  bool _disposed = false;

  Future<void> load() async {
    final generation = ++_generation;
    _initialTimer?.cancel();
    _retryTimer?.cancel();
    if (_ready != null && !_ready!.isCompleted) _ready!.complete();
    await _subscription?.cancel();
    if (_disposed || generation != _generation) return;
    loading = events.isEmpty;
    error = '';
    notifyListeners();
    final ready = _ready = Completer<void>();
    void finish() {
      _initialTimer?.cancel();
      if (!ready.isCompleted) ready.complete();
    }

    void failed(Object problem, {bool retry = false}) {
      if (_disposed || generation != _generation) return;
      loading = false;
      error = problem is CatalogNotPublished
          ? 'Ban quản trị chưa công bố sự kiện. Danh sách sẽ tự cập nhật khi có dữ liệu.'
          : problem is FormatException
          ? 'Dữ liệu Cloud chưa hợp lệ. Hãy báo ban quản trị và thử lại.'
          : 'Chưa kết nối được Cloud. App sẽ tự cập nhật khi kết nối trở lại.';
      notifyListeners();
      finish();
      if (retry) _scheduleRetry(generation);
    }

    try {
      if (personal != null) {
        if (!_personalBound) {
          personal!.addListener(_receivePersonal);
          _personalBound = true;
        }
        await personal!.start();
        _receivePersonal();
      } else {
        _saved = await _favourites.load();
      }
      if (personal == null && !_scheduleLoaded) {
        _planned = await _schedule.load();
        _scheduleLoaded = true;
      }
      if (_disposed || generation != _generation) {
        finish();
        return;
      }
      _initialTimer = Timer(
        const Duration(seconds: 12),
        () => failed(StateError('Cloud timeout')),
      );
      _subscription = _catalog.watch().listen(
        (snapshot) {
          if (_disposed || generation != _generation) return;
          _retryTimer?.cancel();
          _retrySeconds = 5;
          events = snapshot.events;
          fromCache = snapshot.fromCache;
          updatedAt = snapshot.updatedAt;
          loading = false;
          error = '';
          notifyListeners();
          finish();
        },
        onError: (Object problem) => failed(
          problem,
          retry: problem is! CatalogNotPublished && problem is! FormatException,
        ),
        onDone: () {
          if (_disposed || generation != _generation) return;
          _subscription = null;
          if (!ready.isCompleted) failed(StateError('No catalog received'));
          if (error.isNotEmpty) _scheduleRetry(generation);
        },
      );
    } catch (problem) {
      failed(problem, retry: true);
    }
    await ready.future;
  }

  void _receivePersonal() {
    if (_disposed || personal == null) return;
    _saved = {...personal!.favourites};
    _planned = {...personal!.schedule};
    _scheduleLoaded = personal!.ready;
    notifyListeners();
  }

  void _scheduleRetry(int generation) {
    if (_disposed ||
        generation != _generation ||
        (_retryTimer?.isActive ?? false)) {
      return;
    }
    _retryTimer = Timer(Duration(seconds: _retrySeconds), () {
      if (!_disposed && generation == _generation) load();
    });
    _retrySeconds = math.min(_retrySeconds * 2, 60);
  }

  void refreshAfterResume() {
    if (!_disposed && (error.isNotEmpty || fromCache)) load();
  }

  void selectTab(AgendaTab value) {
    tab = value;
    notifyListeners();
  }

  Future<String?> toggleFavourite(String id) async {
    if (saving ||
        (personal != null && !personal!.ready) ||
        !events.any((event) => event.id == id)) {
      return null;
    }
    saving = true;
    notifyListeners();
    try {
      final next = {..._saved};
      final added = next.add(id);
      if (!added) next.remove(id);
      if (personal == null) {
        await _favourites.save(next);
      } else {
        await personal!.setFavourite(id, added);
      }
      _saved = next;
      return added ? 'Đã lưu vào Yêu thích' : 'Đã bỏ khỏi Yêu thích';
    } finally {
      saving = false;
      if (!_disposed) notifyListeners();
    }
  }

  Future<String?> toggleSession(
    String eventId,
    String sessionId, {
    bool allowConflicts = false,
  }) async {
    if (savingSchedule || !_scheduleLoaded) return null;
    final selection = (eventId: eventId, sessionId: sessionId);
    PlannedSession? candidate;
    for (final event in events) {
      if (event.id != eventId) continue;
      for (final session in event.sessions) {
        if (session.id == sessionId) candidate = PlannedSession(event, session);
      }
    }
    if (candidate == null) return null;
    if (!_planned.contains(selection) && !allowConflicts) {
      final conflicts = scheduleConflicts(candidate, schedule);
      if (conflicts.isNotEmpty) throw ScheduleConflict(conflicts);
    }
    savingSchedule = true;
    notifyListeners();
    try {
      final next = {..._planned};
      final added = next.add(selection);
      if (!added) next.remove(selection);
      if (personal == null) {
        await _schedule.save(next);
      } else {
        await personal!.setSession(selection, added);
      }
      _planned = next;
      return added ? 'Đã thêm vào Lịch của tôi' : 'Đã bỏ khỏi Lịch của tôi';
    } finally {
      savingSchedule = false;
      if (!_disposed) notifyListeners();
    }
  }

  @override
  void dispose() {
    _disposed = true;
    if (_personalBound) personal!.removeListener(_receivePersonal);
    _generation++;
    _initialTimer?.cancel();
    _retryTimer?.cancel();
    _subscription?.cancel();
    if (_ready != null && !_ready!.isCompleted) _ready!.complete();
    super.dispose();
  }
}
