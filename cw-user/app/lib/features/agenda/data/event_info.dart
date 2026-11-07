import 'dart:convert';
import 'dart:typed_data';

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
    this.imageBytes,
    this.contactEmail = '',
    this.contactPhone = '',
  });

  final String id, name, location, organiser, type, status;
  final String categoryCode, image, description, source;
  final DateTime date;
  final Uint8List? imageBytes;
  final String contactEmail, contactPhone;
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
    imageBytes: decodeCover(json['image'] as String? ?? ''),
    contactEmail: json['contactEmail'] as String? ?? '',
    contactPhone: json['contactPhone'] as String? ?? '',
    description: json['description'] as String? ?? '',
    source: json['source'] as String? ?? '',
    sessions: List.unmodifiable(
      (json['sessions'] as List<dynamic>? ?? []).map(
        (item) => SessionInfo.fromJson(item as Map<String, dynamic>),
      ),
    ),
  );

  String get imageAsset => image.startsWith('images/') ? 'assets/$image' : '';
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

Uint8List? decodeCover(String image) {
  if (!image.startsWith('data:image/jpeg;base64,')) return null;
  if (image.length > 45000) throw const FormatException('Thumbnail too large');
  final bytes = base64Decode(image.substring(23));
  if (bytes.length < 3 ||
      bytes.length > 32768 ||
      bytes[0] != 255 ||
      bytes[1] != 216) {
    throw const FormatException('Invalid thumbnail');
  }
  return bytes;
}
