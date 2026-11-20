import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:shared_preferences_android/shared_preferences_android.dart';

// Keep the existing MAUI preference file when updating an installed Android app.
SharedPreferencesAsync createLocalPreferences() => SharedPreferencesAsync(
  options: !kIsWeb && defaultTargetPlatform == TargetPlatform.android
      ? const SharedPreferencesAsyncAndroidOptions(
          backend: SharedPreferencesAndroidBackendLibrary.SharedPreferences,
          originalSharedPreferencesOptions: AndroidSharedPreferencesStoreOptions(
            fileName: 'com.comp1786.eventa.user_preferences',
          ),
        )
      : const SharedPreferencesOptions(),
);
