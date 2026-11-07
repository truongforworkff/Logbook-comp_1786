import 'dart:async';

import 'package:flutter/foundation.dart';

import '../data/catalog_repository.dart';
import '../data/event_info.dart';
import '../../favourites/data/favourite_store.dart';

enum AgendaTab { explore, saved }

class AgendaController extends ChangeNotifier {
  AgendaController({CatalogRepository? catalog, FavouriteStore? favourites})
    : _catalog = catalog ?? CatalogRepository(),
      _favourites = favourites ?? FavouriteStore();
  final CatalogRepository _catalog;
  final FavouriteStore _favourites;
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
  Completer<void>? _ready;
  int _generation = 0;
  bool _disposed = false;

  Future<void> load() async {
    final generation = ++_generation;
    _initialTimer?.cancel();
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

    void failed(Object problem) {
      if (_disposed || generation != _generation) return;
      loading = false;
      error = problem is CatalogNotPublished
          ? 'Ban quản trị chưa công bố sự kiện. Hãy thử lại sau khi đồng bộ Admin.'
          : problem is FormatException
          ? 'Dữ liệu Cloud chưa hợp lệ. Hãy báo ban quản trị và thử lại.'
          : 'Chưa kết nối được Cloud. Kiểm tra Internet rồi tải lại.';
      notifyListeners();
      finish();
    }

    try {
      _saved = await _favourites.load();
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
          events = snapshot.events;
          fromCache = snapshot.fromCache;
          updatedAt = snapshot.updatedAt;
          loading = false;
          error = '';
          notifyListeners();
          finish();
        },
        onError: failed,
        onDone: () {
          if (!ready.isCompleted) failed(StateError('No catalog received'));
        },
      );
    } catch (problem) {
      failed(problem);
    }
    await ready.future;
  }

  void selectTab(AgendaTab value) {
    tab = value;
    notifyListeners();
  }

  Future<String?> toggleFavourite(String id) async {
    if (saving || !events.any((event) => event.id == id)) return null;
    saving = true;
    notifyListeners();
    try {
      final next = {..._saved};
      final added = next.add(id);
      if (!added) next.remove(id);
      await _favourites.save(next);
      _saved = next;
      return added ? 'Đã lưu vào Yêu thích' : 'Đã bỏ khỏi Yêu thích';
    } finally {
      saving = false;
      if (!_disposed) notifyListeners();
    }
  }

  @override
  void dispose() {
    _disposed = true;
    _generation++;
    _initialTimer?.cancel();
    _subscription?.cancel();
    if (_ready != null && !_ready!.isCompleted) _ready!.complete();
    super.dispose();
  }
}
