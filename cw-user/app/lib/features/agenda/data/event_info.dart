class EventInfo {
  const EventInfo({
    required this.id,
    required this.name,
    required this.date,
    required this.location,
    required this.organiser,
    required this.type,
    required this.status,
    required this.categoryCode,
    required this.image,
    required this.description,
    required this.source,
    required this.sessions,
  });

  final String id, name, location, organiser, type, status;
  final String categoryCode, image, description, source;
  final DateTime date;
  final List<SessionInfo> sessions;

  factory EventInfo.fromJson(Map<String, dynamic> json) => EventInfo(
    id: json['id'] as String,
    name: json['name'] as String,
    date: DateTime.parse(json['date'] as String),
    location: json['location'] as String,
    organiser: json['organiser'] as String,
    type: json['type'] as String,
    status: json['status'] as String,
    categoryCode: json['categoryCode'] as String? ?? 'Other',
    image: json['image'] as String? ?? '',
    description: json['description'] as String? ?? '',
    source: json['source'] as String? ?? '',
    sessions: List.unmodifiable(
      (json['sessions'] as List<dynamic>? ?? []).map(
        (item) => SessionInfo.fromJson(item as Map<String, dynamic>),
      ),
    ),
  );

  String get imageAsset => image.isEmpty ? '' : 'assets/$image';
}

class SessionInfo {
  const SessionInfo({
    required this.id,
    required this.name,
    required this.time,
    required this.speaker,
    required this.room,
    required this.type,
    required this.description,
  });
  final String id, name, time, speaker, room, type, description;

  factory SessionInfo.fromJson(Map<String, dynamic> json) => SessionInfo(
    id: json['id'] as String,
    name: json['name'] as String,
    time: json['time'] as String,
    speaker: json['speaker'] as String? ?? '',
    room: json['room'] as String? ?? '',
    type: json['type'] as String? ?? '',
    description: json['description'] as String? ?? '',
  );
}
