import 'package:eventa_user/features/schedule/data/schedule_store.dart';
import 'package:eventa_user/features/schedule/logic/personal_schedule.dart';

class MemorySchedule extends ScheduleStore {
  MemorySchedule([Set<SessionSelection>? initial]) : selected = {...?initial};
  Set<SessionSelection> selected;
  bool failWrites = false;
  int writes = 0;

  @override
  Future<Set<SessionSelection>> load() async => {...selected};

  @override
  Future<void> save(Set<SessionSelection> value) async {
    if (failWrites) throw StateError('Storage unavailable');
    selected = {...value};
    writes++;
  }
}
