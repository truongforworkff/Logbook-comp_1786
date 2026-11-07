import 'package:cloud_firestore/cloud_firestore.dart';

import 'event_info.dart';

class CatalogData {
  const CatalogData(this.events, {this.fromCache = false, this.updatedAt});
  final List<EventInfo> events;
  final bool fromCache;
  final DateTime? updatedAt;

  factory CatalogData.fromDocument(
    Map<String, dynamic> data, {
    required bool fromCache,
  }) {
    final rows = data['events'];
    if (data['schemaVersion'] != 1 ||
        rows is! List ||
        rows.length > 100 ||
        data['updatedAt'] is! Timestamp) {
      throw const FormatException('Invalid catalog');
    }
    final events = <EventInfo>[];
    for (final row in rows) {
      if (row is! Map) throw const FormatException('Invalid event');
      final event = Map<String, dynamic>.from(row);
      validateText(event, [
        'id',
        'name',
        'date',
        'location',
        'organiser',
        'type',
        'status',
      ]);
      validateText(event, [
        'categoryCode',
        'description',
        'source',
        'contactEmail',
        'contactPhone',
      ], optional: true);
      if ((event['id'] as String).length > 160) {
        throw const FormatException('Invalid event ID');
      }
      final rawDate = event['date'] as String;
      if (!RegExp(r'^\d{4}-\d{2}-\d{2}$').hasMatch(rawDate) ||
          DateTime.parse(rawDate).toIso8601String().substring(0, 10) !=
              rawDate) {
        throw const FormatException('Invalid date');
      }
      final image = event['image'] ?? '';
      if (image is! String ||
          image.length > 45000 ||
          (image.isNotEmpty &&
              !image.startsWith('data:image/jpeg;base64,') &&
              !{
                'images/conference.jpg',
                'images/computer.jpg',
              }.contains(image))) {
        throw const FormatException('Invalid cover');
      }
      final sessions = event['sessions'];
      if (sessions is! List || sessions.length > 500) {
        throw const FormatException('Invalid sessions');
      }
      final ids = <String>{};
      for (final session in sessions) {
        if (session is! Map) throw const FormatException('Invalid session');
        final value = Map<String, dynamic>.from(session);
        validateText(value, ['id', 'name', 'time']);
        validateText(value, [
          'speaker',
          'room',
          'type',
          'description',
        ], optional: true);
        if (!RegExp(r'^(?:[01]\d|2[0-3]):[0-5]\d$')
                .hasMatch(value['time'] as String) ||
            !ids.add(value['id'] as String)) {
          throw const FormatException('Invalid time or duplicate session');
        }
      }
      events.add(EventInfo.fromJson(event));
    }
    if (events.map((e) => e.id).toSet().length != events.length ||
        events.fold<int>(0, (n, e) => n + e.sessions.length) > 500) {
      throw const FormatException('Invalid catalog IDs or size');
    }
    return CatalogData(
      List.unmodifiable(events),
      fromCache: fromCache,
      updatedAt: (data['updatedAt'] as Timestamp).toDate(),
    );
  }
}

void validateText(
  Map<String, dynamic> value,
  List<String> keys, {
  bool optional = false,
}) {
  for (final key in keys) {
    final text = value[key];
    if ((text == null && optional)) continue;
    if (text is! String ||
        text.length > 16384 ||
        (!optional && text.trim().isEmpty)) {
      throw FormatException('Invalid $key');
    }
  }
}

class CatalogRepository {
  CatalogRepository({this.firestore});
  final FirebaseFirestore? firestore;
  Stream<CatalogData> watch() async* {
    await for (final snapshot
        in (firestore ?? FirebaseFirestore.instance)
            .doc('catalogues/main')
            .snapshots(includeMetadataChanges: true)) {
      if (!snapshot.exists && snapshot.metadata.isFromCache) continue;
      if (!snapshot.exists) throw const CatalogNotPublished();
      yield CatalogData.fromDocument(
        snapshot.data()!,
        fromCache: snapshot.metadata.isFromCache,
      );
    }
  }

  Future<List<EventInfo>> load() async => (await watch().first).events;
}

class CatalogNotPublished implements Exception {
  const CatalogNotPublished();
}
