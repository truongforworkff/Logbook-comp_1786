import 'dart:convert';

Set<String> readFavourites(String? raw, Iterable<String> validIds) {
  try {
    final decoded = jsonDecode(raw ?? '[]');
    if (decoded is! List) return {};
    final valid = validIds.toSet();
    return decoded.whereType<String>().where(valid.contains).toSet();
  } on FormatException {
    return {};
  }
}

String writeFavourites(Set<String> ids) => jsonEncode(ids.toList()..sort());
