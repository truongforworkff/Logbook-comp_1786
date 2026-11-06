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
  Set<String> get saved => Set.unmodifiable(_saved);
  bool loading = true;
  bool saving = false;
  String error = '';
  AgendaTab tab = AgendaTab.explore;

  Future<void> load() async {
    loading = true;
    error = '';
    notifyListeners();
    try {
      events = await _catalog.load();
      _saved = await _favourites.load(events.map((event) => event.id));
    } catch (_) {
      error = 'Không đọc được dữ liệu trên thiết bị. Hãy thử lại.';
    } finally {
      loading = false;
      notifyListeners();
    }
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
      notifyListeners();
    }
  }
}
