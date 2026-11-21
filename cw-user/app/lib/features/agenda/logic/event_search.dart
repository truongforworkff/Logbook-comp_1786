import '../data/event_info.dart';

String dateKey(DateTime date) =>
    '${date.year.toString().padLeft(4, '0')}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';

String normalizeSearch(String value) {
  const letters = {
    'a': 'àáạảãâầấậẩẫăằắặẳẵ',
    'e': 'èéẹẻẽêềếệểễ',
    'i': 'ìíịỉĩ',
    'o': 'òóọỏõôồốộổỗơờớợởỡ',
    'u': 'ùúụủũưừứựửữ',
    'y': 'ỳýỵỷỹ',
    'd': 'đ',
  };
  var result = value.trim().toLowerCase().replaceAll(
    RegExp(r'[\u0300-\u036f]'),
    '',
  );
  for (final entry in letters.entries) {
    for (final letter in entry.value.split('')) {
      result = result.replaceAll(letter, entry.key);
    }
  }
  return result;
}

List<EventInfo> findEvents(
  Iterable<EventInfo> events, {
  String query = '',
  DateTime? date,
  String month = '',
  String category = '',
  Set<String>? saved,
}) {
  final keyword = normalizeSearch(query);
  final results = events
      .where(
        (event) =>
            normalizeSearch(event.name).contains(keyword) &&
            (date == null || dateKey(event.date) == dateKey(date)) &&
            (month.isEmpty || dateKey(event.date).startsWith(month)) &&
            (category.isEmpty || event.categoryCode == category) &&
            (saved == null || saved.contains(event.id)),
      )
      .toList();
  results.sort((a, b) {
    final created = (b.createdAt?.millisecondsSinceEpoch ?? 0).compareTo(
      a.createdAt?.millisecondsSinceEpoch ?? 0,
    );
    if (created != 0) return created;
    final byDate = b.date.compareTo(a.date);
    return byDate != 0 ? byDate : a.id.compareTo(b.id);
  });
  return results;
}
