import 'package:flutter/material.dart';

import '../data/event_info.dart';
import '../logic/agenda_controller.dart';
import '../../favourites/ui/favourite_button.dart';
import '../../../shared/ui/app_theme.dart';
import '../../../shared/ui/app_widgets.dart';
import '../../../shared/ui/event_labels.dart';
import 'session_details_page.dart';
import 'event_cover.dart';
import 'unavailable_page.dart';

class EventDetailsPage extends StatelessWidget {
  const EventDetailsPage({
    super.key,
    required EventInfo event,
    required this.controller,
  }) : initialEvent = event;
  final EventInfo initialEvent;
  EventInfo get event => controller.events.firstWhere(
    (value) => value.id == initialEvent.id,
    orElse: () => initialEvent,
  );
  final AgendaController controller;

  @override
  Widget build(BuildContext context) => ListenableBuilder(
    listenable: controller,
    builder: (context, _) {
      if (!controller.events.any((value) => value.id == initialEvent.id))
        return const UnavailablePage();
      final sessions = [...event.sessions]
        ..sort((a, b) => a.time.compareTo(b.time));
      final saved = controller.saved.contains(event.id);
      return Scaffold(
        appBar: AppBar(
          backgroundColor: AppColors.canvas,
          surfaceTintColor: Colors.transparent,
          leading: IconButton(
            tooltip: 'Quay lại',
            onPressed: () => Navigator.pop(context),
            icon: const Icon(Icons.arrow_back, color: AppColors.blue, size: 22),
          ),
          title: const Text(
            'Quay lại',
            style: TextStyle(fontSize: 13, color: AppColors.blue),
          ),
          titleSpacing: 0,
          actions: [
            FavouriteButton(
              controller: controller,
              eventId: event.id,
              eventName: event.name,
            ),
            const SizedBox(width: 12),
          ],
        ),
        bottomNavigationBar: BottomTabs(controller: controller),
        body: PageFrame(
          child: ListView(
            key: Key('event-details-${event.id}'),
            padding: const EdgeInsets.fromLTRB(18, 10, 18, 24),
            children: [
              if (event.image.isNotEmpty) ...[
                ClipRRect(
                  borderRadius: BorderRadius.circular(18),
                  child: EventCover(event: event, height: 190),
                ),
                const SizedBox(height: 18),
              ],
              Align(
                alignment: Alignment.centerLeft,
                child: CategoryBadge(categoryLabel(event.categoryCode)),
              ),
              const SizedBox(height: 10),
              Text(
                event.name,
                style: Theme.of(context).textTheme.headlineMedium
                    ?.copyWith(fontSize: 26, height: 1.2),
              ),
              const SizedBox(height: 19),
              SurfaceBox(
                child: Column(
                  children: [
                    MetaRow(
                      icon: Icons.calendar_today_outlined,
                      title: fullDate(event.date),
                      subtitle:
                          '${event.status == 'Completed' ? 'Đã diễn ra' : 'Sắp diễn ra'} · ${event.type}',
                    ),
                    const SizedBox(height: 16),
                    MetaRow(
                      icon: Icons.location_on_outlined,
                      title: event.location,
                      subtitle: event.organiser,
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              Text(
                'Về sự kiện',
                style: Theme.of(context).textTheme.titleMedium,
              ),
              const SizedBox(height: 9),
              Text(
                event.description.isEmpty
                    ? 'Thông tin đang được cập nhật.'
                    : event.description,
                style: const TextStyle(
                  color: AppColors.muted,
                  fontSize: 13,
                  height: 1.8,
                ),
              ),
              if (event.contactEmail.isNotEmpty ||
                  event.contactPhone.isNotEmpty) ...[
                const SizedBox(height: 16),
                SurfaceBox(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (event.contactEmail.isNotEmpty)
                        SelectableText(event.contactEmail),
                      if (event.contactPhone.isNotEmpty)
                        SelectableText(event.contactPhone),
                    ],
                  ),
                ),
              ],
              const SizedBox(height: 24),
              Row(
                children: [
                  Expanded(
                    child: Text(
                      'Lịch trình',
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                  ),
                  CategoryBadge('${sessions.length} session'),
                ],
              ),
              const SizedBox(height: 16),
              if (sessions.isEmpty)
                const Text(
                  'Lịch trình đang được cập nhật.',
                  style: TextStyle(color: AppColors.muted),
                ),
              for (final session in sessions) _session(context, session),
              const SizedBox(height: 8),
              OutlinedButton.icon(
                key: const Key('detail-favourite'),
                onPressed: controller.saving
                    ? null
                    : () => toggleFavourite(context, controller, event.id),
                icon: Icon(
                  saved ? Icons.favorite : Icons.favorite_border,
                  size: 19,
                ),
                label: Text(saved ? 'Đã lưu yêu thích' : 'Lưu sự kiện này'),
              ),
              const SizedBox(height: 17),
              const Text(
                'Các phiên có tiền tố Demo là lịch minh họa. Lịch trình có thể được ban tổ chức cập nhật.',
                style: TextStyle(
                  fontSize: 10,
                  color: AppColors.muted,
                  height: 1.7,
                ),
              ),
            ],
          ),
        ),
      );
    },
  );

  Widget _session(BuildContext context, SessionInfo session) => IntrinsicHeight(
    child: Row(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        SizedBox(
          width: 19,
          child: Stack(
            alignment: Alignment.topCenter,
            children: [
              Container(width: 1, color: const Color(0xFFDCE3EE)),
              Container(
                margin: const EdgeInsets.only(top: 10),
                width: 8,
                height: 8,
                decoration: const BoxDecoration(
                  color: Color(0xFF8DA6C7),
                  shape: BoxShape.circle,
                ),
              ),
            ],
          ),
        ),
        Expanded(
          child: Semantics(
            label: 'Xem phiên: ${session.name}',
            button: true,
            onTap: () => _openSession(context, session),
            child: ExcludeSemantics(
              child: InkWell(
                key: Key('session-${session.id}'),
                onTap: () => _openSession(context, session),
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(9, 4, 0, 24),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              session.time,
                              style: const TextStyle(
                                fontSize: 11,
                                color: AppColors.blue,
                              ),
                            ),
                            const SizedBox(height: 7),
                            Text(
                              session.name,
                              style: const TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                            const SizedBox(height: 6),
                            Text(
                              '${session.type} · ${session.room.isNotEmpty
                                  ? session.room
                                  : session.speaker.isNotEmpty
                                  ? session.speaker
                                  : 'Chưa công bố địa điểm'}',
                              style: const TextStyle(
                                fontSize: 11,
                                color: AppColors.muted,
                              ),
                            ),
                          ],
                        ),
                      ),
                      const Padding(
                        padding: EdgeInsets.only(top: 20, left: 6),
                        child: Icon(
                          Icons.chevron_right,
                          size: 20,
                          color: Color(0xFF9FB6D6),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ],
    ),
  );

  void _openSession(BuildContext context, SessionInfo session) =>
      Navigator.of(context).push(
        MaterialPageRoute<void>(
          builder: (_) => SessionDetailsPage(
            event: event,
            session: session,
            controller: controller,
          ),
        ),
      );
}
