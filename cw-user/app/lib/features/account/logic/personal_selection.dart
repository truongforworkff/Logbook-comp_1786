import '../../schedule/logic/personal_schedule.dart';

bool validPreferenceId(Object? value, {int maxLength = 160}) =>
    value is String && value.isNotEmpty && value.length <= maxLength;

Set<String> cloudFavourites(Iterable<Map<String, dynamic>> rows) => {
  for (final row in rows)
    if (validPreferenceId(row['eventId'])) row['eventId'] as String,
};

Set<SessionSelection> cloudSchedule(Iterable<Map<String, dynamic>> rows) => {
  for (final row in rows)
    if (validPreferenceId(row['eventId']) &&
        validPreferenceId(row['sessionId'], maxLength: 256))
      (
        eventId: row['eventId'] as String,
        sessionId: row['sessionId'] as String,
      ),
};
