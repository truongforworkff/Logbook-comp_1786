import 'dart:convert';

import '../../agenda/data/event_info.dart';

typedef SessionSelection = ({String eventId, String sessionId});

class PlannedSession {
  const PlannedSession(this.event, this.session);
  final EventInfo event;
  final SessionInfo session;
}

Set<SessionSelection> decodeSchedule(String? raw) {
  try {
    final rows = jsonDecode(raw ?? '[]');
    if (rows is! List) return {};
    return {
      for (final row in rows)
        if (row is Map &&
            row['eventId'] is String &&
            (row['eventId'] as String).isNotEmpty &&
            (row['eventId'] as String).length <= 160 &&
            row['sessionId'] is String &&
            (row['sessionId'] as String).isNotEmpty &&
            (row['sessionId'] as String).length <= 16384)
          (
            eventId: row['eventId'] as String,
            sessionId: row['sessionId'] as String,
          ),
    };
  } on FormatException {
    return {};
  }
}

String encodeSchedule(Set<SessionSelection> selected) => jsonEncode([
  for (final item in selected)
    {'eventId': item.eventId, 'sessionId': item.sessionId},
]);

List<PlannedSession> personalSchedule(
  List<EventInfo> events,
  Set<SessionSelection> selected,
) {
  final entries = [
    for (final event in events)
      for (final session in event.sessions)
        if (selected.contains((eventId: event.id, sessionId: session.id)))
          PlannedSession(event, session),
  ];
  entries.sort((a, b) {
    final date = a.event.date.compareTo(b.event.date);
    if (date != 0) return date;
    final time = a.session.time.compareTo(b.session.time);
    if (time != 0) return time;
    final event = a.event.id.compareTo(b.event.id);
    return event != 0 ? event : a.session.id.compareTo(b.session.id);
  });
  return entries;
}
