import '../../features/agenda/logic/event_search.dart';

const categories = <String, String>{
  '': 'Tất cả chủ đề',
  'AI': 'AI & Dữ liệu',
  'Technology': 'Công nghệ',
  'Design': 'Thiết kế',
  'Business': 'Kinh doanh',
  'Education': 'Giáo dục',
  'Community': 'Cộng đồng',
  'Other': 'Khác',
};

String categoryLabel(String code) => categories[code] ?? 'Khác';

String shortDate(DateTime date) =>
    '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}';

String fullDate(DateTime date) {
  const days = [
    'Thứ Hai',
    'Thứ Ba',
    'Thứ Tư',
    'Thứ Năm',
    'Thứ Sáu',
    'Thứ Bảy',
    'Chủ nhật',
  ];
  return '${days[date.weekday - 1]}, ${date.day} tháng ${date.month} năm ${date.year}';
}

String monthKey(DateTime date) => dateKey(date).substring(0, 7);
