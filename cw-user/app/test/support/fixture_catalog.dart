import 'dart:convert';
import 'dart:io';

import 'package:eventa_user/features/agenda/data/event_info.dart';
import 'package:eventa_user/features/agenda/data/catalog_repository.dart';

List<EventInfo> readCatalog() =>
    (jsonDecode(File('assets/data/events.json').readAsStringSync())
            as List<dynamic>)
        .map((row) => EventInfo.fromJson(row as Map<String, dynamic>))
        .toList();

class FixtureCatalog extends CatalogRepository {
  @override
  Future<List<EventInfo>> load() async => readCatalog();
}
