import 'package:flutter/material.dart';

import '../data/event_info.dart';
import '../logic/agenda_controller.dart';
import '../logic/event_search.dart';
import '../../../shared/ui/app_theme.dart';
import '../../../shared/ui/event_labels.dart';
import 'category_sheet.dart';
import 'event_card.dart';
import 'event_details_page.dart';

class BrowsePage extends StatefulWidget {
  const BrowsePage({
    super.key,
    required this.controller,
    required this.savedOnly,
  });
  final AgendaController controller;
  final bool savedOnly;

  @override
  State<BrowsePage> createState() => _BrowsePageState();
}

class _BrowsePageState extends State<BrowsePage> {
  final query = TextEditingController();
  DateTime? date;
  String month = '', category = '';

  @override
  void dispose() {
    query.dispose();
    super.dispose();
  }

  bool get filtered =>
      query.text.trim().isNotEmpty ||
      date != null ||
      month.isNotEmpty ||
      category.isNotEmpty;

  void _clearFilters() => setState(() {
    query.clear();
    date = null;
    month = category = '';
  });

  Future<void> _pickDate() async {
    FocusScope.of(context).unfocus();
    final available = findEvents(widget.controller.events);
    final initial =
        date ?? (available.isEmpty ? DateTime.now() : available.first.date);
    final result = await showDatePicker(
      context: context,
      initialDate: initial,
      firstDate: DateTime(1900),
      lastDate: DateTime(2100, 12, 31),
      helpText: 'Bạn muốn đi ngày nào?',
      confirmText: 'Xem sự kiện',
      cancelText: 'Hủy',
      initialEntryMode: DatePickerEntryMode.calendarOnly,
      builder: (context, child) => Theme(
        data: Theme.of(context).copyWith(
          datePickerTheme: DatePickerThemeData(
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(26),
            ),
          ),
        ),
        child: child!,
      ),
    );
    if (mounted && result != null)
      setState(() {
        date = result;
        month = '';
      });
  }

  Future<void> _pickCategory() async {
    FocusScope.of(context).unfocus();
    final selected = await showCategorySheet(context, category);
    if (mounted && selected != null) setState(() => category = selected);
  }

  void _open(EventInfo event) {
    FocusScope.of(context).unfocus();
    Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (_) =>
            EventDetailsPage(event: event, controller: widget.controller),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final controller = widget.controller;
    final noSaved = widget.savedOnly && controller.saved.isEmpty;
    final results = findEvents(
      controller.events,
      query: query.text,
      date: date,
      month: month,
      category: category,
      saved: widget.savedOnly ? controller.saved : null,
    );
    return ListView(
      key: PageStorageKey(widget.savedOnly ? 'saved-browse' : 'explore-browse'),
      keyboardDismissBehavior: ScrollViewKeyboardDismissBehavior.onDrag,
      padding: const EdgeInsets.fromLTRB(18, 14, 18, 26),
      children: [
        _intro(context),
        if (!noSaved) ...[
          const SizedBox(height: 22),
          TextField(
            key: const Key('event-search'),
            controller: query,
            textInputAction: TextInputAction.search,
            onChanged: (_) => setState(() {}),
            onSubmitted: (_) => FocusScope.of(context).unfocus(),
            decoration: InputDecoration(
              hintText: widget.savedOnly
                  ? 'Tìm trong sự kiện đã lưu…'
                  : 'Tìm sự kiện bạn quan tâm…',
              hintStyle: const TextStyle(fontSize: 13, color: AppColors.muted),
              filled: true,
              fillColor: const Color(0xFFE9ECF3),
              prefixIcon: const Icon(
                Icons.search,
                size: 21,
                color: AppColors.muted,
              ),
              suffixIcon: query.text.isEmpty
                  ? null
                  : IconButton(
                      key: const Key('clear-search'),
                      tooltip: 'Xóa từ khóa',
                      onPressed: () => setState(query.clear),
                      icon: const Icon(Icons.close, size: 19),
                    ),
              contentPadding: const EdgeInsets.symmetric(
                horizontal: 14,
                vertical: 14,
              ),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide.none,
              ),
            ),
          ),
          const SizedBox(height: 11),
          _filters(controller.events),
          const SizedBox(height: 10),
          Align(
            alignment: Alignment.centerLeft,
            child: OutlinedButton(
              key: const Key('category-filter'),
              onPressed: _pickCategory,
              style: OutlinedButton.styleFrom(
                backgroundColor: category.isEmpty
                    ? Colors.white
                    : AppColors.blue,
                foregroundColor: category.isEmpty
                    ? AppColors.muted
                    : Colors.white,
                minimumSize: const Size(0, 42),
                padding: const EdgeInsets.symmetric(horizontal: 13),
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    categoryLabel(category),
                    style: const TextStyle(fontSize: 12),
                  ),
                  const SizedBox(width: 18),
                  const Icon(Icons.keyboard_arrow_down, size: 16),
                ],
              ),
            ),
          ),
        ],
        const SizedBox(height: 14),
        Row(
          children: [
            Expanded(
              child: Text(
                filtered
                    ? 'Kết quả tìm kiếm'
                    : widget.savedOnly
                    ? 'Đã lưu yêu thích'
                    : 'Sự kiện nổi bật',
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
            Semantics(
              liveRegion: true,
              child: Text(
                '${results.length} sự kiện',
                key: const Key('results-count'),
                style: const TextStyle(fontSize: 11, color: AppColors.muted),
              ),
            ),
          ],
        ),
        const SizedBox(height: 15),
        for (final event in results)
          Padding(
            padding: const EdgeInsets.only(bottom: 15),
            child: EventCard(
              event: event,
              controller: controller,
              onOpen: () => _open(event),
            ),
          ),
        if (results.isEmpty) _empty(noSaved),
      ],
    );
  }

  Widget _intro(BuildContext context) => widget.savedOnly
      ? Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: AppColors.blueSoft,
                borderRadius: BorderRadius.circular(16),
              ),
              child: const Icon(Icons.favorite_border, color: AppColors.blue),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Điều bạn quan tâm',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const Text(
                    'Những sự kiện bạn muốn dành thời gian.',
                    style: TextStyle(fontSize: 11, color: AppColors.muted),
                  ),
                ],
              ),
            ),
          ],
        )
      : Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Khám phá sự kiện',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            const SizedBox(height: 7),
            const Text(
              'Những cuộc gặp mở ra ý tưởng mới.',
              style: TextStyle(color: AppColors.muted),
            ),
            const SizedBox(height: 20),
            Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: AppColors.line),
              ),
              child: const Row(
                children: [
                  Icon(
                    Icons.auto_awesome_outlined,
                    color: AppColors.blue,
                    size: 22,
                  ),
                  SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Một điều mới cho ngày sắp tới',
                          style: TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        SizedBox(height: 3),
                        Text(
                          'AI, công nghệ và những trải nghiệm sáng tạo.',
                          style: TextStyle(
                            fontSize: 10,
                            color: AppColors.muted,
                          ),
                        ),
                      ],
                    ),
                  ),
                  Icon(Icons.north_east, color: AppColors.muted, size: 15),
                ],
              ),
            ),
          ],
        );

  Widget _filters(List<EventInfo> events) {
    final months =
        events
            .where((event) => event.status != 'Completed')
            .map((event) => monthKey(event.date))
            .toSet()
            .toList()
          ..sort();
    return Wrap(
      spacing: 7,
      runSpacing: 7,
      children: [
        _chip(
          'Tất cả ngày',
          date == null && month.isEmpty,
          () => setState(() {
            date = null;
            month = '';
          }),
        ),
        for (final value in months.take(2))
          _chip(
            'Tháng ${int.parse(value.substring(5))}',
            month == value && date == null,
            () => setState(() {
              month = value;
              date = null;
            }),
          ),
        _chip(
          date == null ? 'Chọn ngày' : shortDate(date!),
          date != null,
          _pickDate,
          icon: Icons.calendar_today_outlined,
          key: const Key('date-filter'),
        ),
      ],
    );
  }

  Widget _chip(
    String label,
    bool selected,
    VoidCallback onTap, {
    IconData? icon,
    Key? key,
  }) => OutlinedButton(
    key: key,
    onPressed: onTap,
    style: OutlinedButton.styleFrom(
      minimumSize: const Size(0, 42),
      padding: const EdgeInsets.symmetric(horizontal: 12),
      backgroundColor: selected ? AppColors.blue : Colors.white,
      foregroundColor: selected ? Colors.white : AppColors.muted,
      side: BorderSide(color: selected ? AppColors.blue : AppColors.line),
      shape: const StadiumBorder(),
    ),
    child: Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        if (icon != null) ...[Icon(icon, size: 15), const SizedBox(width: 6)],
        Text(label, style: const TextStyle(fontSize: 11)),
      ],
    ),
  );

  Widget _empty(bool noSaved) => Padding(
    padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 38),
    child: Column(
      children: [
        CircleAvatar(
          radius: 33,
          backgroundColor: AppColors.blueSoft,
          child: Icon(
            noSaved ? Icons.favorite_border : Icons.search,
            color: AppColors.blue,
            size: 28,
          ),
        ),
        const SizedBox(height: 18),
        Text(
          noSaved
              ? 'Dành một chỗ cho điều bạn thích'
              : 'Chưa tìm thấy sự kiện phù hợp',
          textAlign: TextAlign.center,
          style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w700),
        ),
        const SizedBox(height: 10),
        Text(
          noSaved
              ? 'Chạm vào trái tim ở sự kiện bạn quan tâm. Bạn sẽ tìm lại chúng thật nhanh ở đây.'
              : 'Thử tên khác hoặc xóa bộ lọc để tiếp tục khám phá.',
          textAlign: TextAlign.center,
          style: const TextStyle(
            fontSize: 12,
            color: AppColors.muted,
            height: 1.7,
          ),
        ),
        const SizedBox(height: 22),
        OutlinedButton(
          onPressed: noSaved
              ? () => widget.controller.selectTab(AgendaTab.explore)
              : _clearFilters,
          child: Text(noSaved ? 'Khám phá sự kiện' : 'Xóa bộ lọc'),
        ),
      ],
    ),
  );
}
