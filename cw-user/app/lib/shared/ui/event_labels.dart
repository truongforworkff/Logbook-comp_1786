import '../../features/agenda/logic/event_search.dart';

const categories = <String, String>{
  '': 'All topics',
  'AI': 'AI & Data',
  'Technology': 'Technology',
  'Design': 'Design',
  'Business': 'Business',
  'Education': 'Education',
  'Community': 'Community',
  'Other': 'Other',
};

String categoryLabel(String code) => categories[code] ?? 'Other';

String shortDate(DateTime date) =>
    '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}';

String agendaItemCount(int count) =>
    '$count agenda item${count == 1 ? '' : 's'}';

String monthKey(DateTime date) => dateKey(date).substring(0, 7);
