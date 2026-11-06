import 'dart:convert';

import 'package:flutter/services.dart';

import 'event_info.dart';

class CatalogRepository {
  Future<List<EventInfo>> load() async {
    // shortcut: local admin snapshot; replace this source when cloud E is ready.
    final raw = await rootBundle.loadString('assets/data/events.json');
    final rows = jsonDecode(raw) as List<dynamic>;
    final events = rows
        .map((row) => EventInfo.fromJson(row as Map<String, dynamic>))
        .toList();
    if (events.any((event) => event.id.isEmpty || event.name.trim().isEmpty) ||
        events.map((event) => event.id).toSet().length != events.length) {
      throw const FormatException('Invalid event catalog.');
    }
    return List.unmodifiable(events);
  }
}
