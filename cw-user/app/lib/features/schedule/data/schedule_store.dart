import 'package:shared_preferences/shared_preferences.dart';

import '../../../shared/data/local_preferences.dart';
import '../logic/personal_schedule.dart';

class ScheduleStore {
  static const key = 'eventa.schedule.v1';
  SharedPreferencesAsync? _preferences;
  SharedPreferencesAsync get _store =>
      _preferences ??= createLocalPreferences();

  Future<Set<SessionSelection>> load() async =>
      decodeSchedule(await _store.getString(key));

  Future<void> save(Set<SessionSelection> selected) =>
      _store.setString(key, encodeSchedule(selected));
}
