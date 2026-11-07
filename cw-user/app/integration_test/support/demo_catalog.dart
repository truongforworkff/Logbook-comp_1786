import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:eventa_user/features/agenda/data/catalog_repository.dart';
import 'package:eventa_user/features/agenda/data/event_info.dart';

// Fixed data belongs only to UI tests; production reads Firestore.
class DemoCatalog extends CatalogRepository {
  @override
  Stream<CatalogData> watch() async* {
    final rows = jsonDecode(
      await rootBundle.loadString('assets/data/events.json'),
    ) as List;
    yield CatalogData(
      rows
          .map((row) => EventInfo.fromJson(row as Map<String, dynamic>))
          .toList(),
    );
  }
}
