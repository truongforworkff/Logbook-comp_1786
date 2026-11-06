import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:shared_preferences_android/shared_preferences_android.dart';

import '../logic/favourite_codec.dart';

class FavouriteStore {
  static const key = 'eventa.favourites.v1';
  SharedPreferencesAsync? _preferences;

  // Reuse the native MAUI preference file so installed favourites survive the rewrite.
  SharedPreferencesAsync get _store => _preferences ??= SharedPreferencesAsync(
    options: !kIsWeb && defaultTargetPlatform == TargetPlatform.android
        ? const SharedPreferencesAsyncAndroidOptions(
            backend: SharedPreferencesAndroidBackendLibrary.SharedPreferences,
            originalSharedPreferencesOptions:
                AndroidSharedPreferencesStoreOptions(
                  fileName: 'com.comp1786.eventa.user_preferences',
                ),
          )
        : const SharedPreferencesOptions(),
  );

  Future<Set<String>> load(Iterable<String> validIds) async =>
      readFavourites(await _store.getString(key), validIds);

  Future<void> save(Set<String> ids) =>
      _store.setString(key, writeFavourites(ids));
}
