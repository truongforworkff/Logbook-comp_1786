import 'package:eventa_user/features/favourites/data/favourite_store.dart';

class MemoryFavourites extends FavouriteStore {
  MemoryFavourites([Set<String>? initial]) : ids = {...?initial};
  Set<String> ids;
  bool failWrites = false;
  @override
  Future<Set<String>> load([Iterable<String>? validIds]) async =>
      validIds == null ? {...ids} : ids.intersection(validIds.toSet());
  @override
  Future<void> save(Set<String> value) async {
    if (failWrites) throw StateError('Storage unavailable');
    ids = {...value};
  }
}
