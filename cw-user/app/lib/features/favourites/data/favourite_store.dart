import 'package:shared_preferences/shared_preferences.dart';

import '../../../shared/data/local_preferences.dart';
import '../logic/favourite_codec.dart';

class FavouriteStore {
  static const key = 'eventa.favourites.v1';
  SharedPreferencesAsync? _preferences;
  SharedPreferencesAsync get _store =>
      _preferences ??= createLocalPreferences();

  Future<Set<String>> load([Iterable<String>? validIds]) async =>
      readFavourites(await _store.getString(key), validIds);

  Future<void> save(Set<String> ids) =>
      _store.setString(key, writeFavourites(ids));
}
