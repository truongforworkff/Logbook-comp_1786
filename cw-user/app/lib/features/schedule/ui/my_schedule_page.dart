import 'package:flutter/material.dart';

import '../../agenda/logic/agenda_controller.dart';
import '../../agenda/logic/event_search.dart';
import '../../agenda/ui/session_details_page.dart';
import '../../../shared/ui/app_theme.dart';
import '../../../shared/ui/app_widgets.dart';
import '../../../shared/ui/event_labels.dart';
import '../logic/personal_schedule.dart';
import 'schedule_button.dart';

class MySchedulePage extends StatelessWidget {
  const MySchedulePage({super.key, required this.controller});
  final AgendaController controller;

  @override
  Widget build(BuildContext context) {
    final entries = controller.schedule;
    return ListView(
      key: const PageStorageKey('my-schedule'),
      padding: const EdgeInsets.fromLTRB(18, 14, 18, 26),
      children: [
        Text('Lịch của tôi', style: Theme.of(context).textTheme.headlineMedium),
        const SizedBox(height: 7),
        Text(
          '${entries.length} session đã chọn',
          key: const Key('schedule-count'),
          style: const TextStyle(color: AppColors.muted),
        ),
        const SizedBox(height: 24),
        if (entries.isEmpty) _empty(context),
        for (var index = 0; index < entries.length; index++) ...[
          if (index == 0 ||
              dateKey(entries[index].event.date) !=
                  dateKey(entries[index - 1].event.date)) ...[
            Padding(
              padding: const EdgeInsets.only(bottom: 12, top: 8),
              child: Text(
                fullDate(entries[index].event.date),
                style: Theme.of(context).textTheme.titleMedium,
              ),
            ),
          ],
          _entry(context, entries[index]),
          const SizedBox(height: 12),
        ],
      ],
    );
  }

  Widget _entry(BuildContext context, PlannedSession entry) => SurfaceBox(
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        InkWell(
          key: Key('planned-${entry.session.id}'),
          borderRadius: BorderRadius.circular(12),
          onTap: () => Navigator.of(context).push(
            MaterialPageRoute<void>(
              builder: (_) => SessionDetailsPage(
                event: entry.event,
                session: entry.session,
                controller: controller,
              ),
            ),
          ),
          child: Padding(
            padding: const EdgeInsets.only(bottom: 14),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  entry.session.time,
                  style: const TextStyle(
                    color: AppColors.blue,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 7),
                Text(
                  entry.session.name,
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                const SizedBox(height: 5),
                Text(
                  entry.event.name,
                  style: const TextStyle(fontSize: 12, color: AppColors.muted),
                ),
                if (entry.session.room.isNotEmpty) ...[
                  const SizedBox(height: 8),
                  Text(
                    entry.session.room,
                    style: const TextStyle(fontSize: 12),
                  ),
                ],
              ],
            ),
          ),
        ),
        ScheduleButton(
          controller: controller,
          eventId: entry.event.id,
          sessionId: entry.session.id,
        ),
      ],
    ),
  );

  Widget _empty(BuildContext context) => Padding(
    padding: const EdgeInsets.symmetric(vertical: 36),
    child: Column(
      children: [
        const Icon(Icons.event_note_outlined, size: 44, color: AppColors.blue),
        const SizedBox(height: 16),
        Text(
          'Chưa có session trong lịch',
          style: Theme.of(context).textTheme.titleMedium,
        ),
        const SizedBox(height: 10),
        const Text(
          'Mở một sự kiện, chọn session bạn muốn tham dự rồi thêm vào lịch.',
          textAlign: TextAlign.center,
          style: TextStyle(color: AppColors.muted, height: 1.6),
        ),
        const SizedBox(height: 20),
        OutlinedButton(
          key: const Key('schedule-explore'),
          onPressed: () => controller.selectTab(AgendaTab.explore),
          child: const Text('Khám phá sự kiện'),
        ),
      ],
    ),
  );
}
