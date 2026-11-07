import 'package:flutter/material.dart';

import '../data/event_info.dart';
import 'unavailable_page.dart';
import '../logic/agenda_controller.dart';
import '../../favourites/ui/favourite_button.dart';
import '../../../shared/ui/app_theme.dart';
import '../../../shared/ui/app_widgets.dart';
import '../../../shared/ui/event_labels.dart';

class SessionDetailsPage extends StatelessWidget {
  const SessionDetailsPage({
    super.key,
    required EventInfo event,
    required SessionInfo session,
    required this.controller,
  }) : initialEvent = event,
       initialSession = session;
  final EventInfo initialEvent;
  final SessionInfo initialSession;
  EventInfo get event => controller.events.firstWhere(
    (value) => value.id == initialEvent.id,
    orElse: () => initialEvent,
  );
  SessionInfo get session => event.sessions.firstWhere(
    (value) => value.id == initialSession.id,
    orElse: () => initialSession,
  );
  final AgendaController controller;

  @override
  Widget build(BuildContext context) => ListenableBuilder(
    listenable: controller,
    builder: (context, _) =>
        !controller.events.any((value) => value.id == initialEvent.id) ||
            !event.sessions.any((value) => value.id == initialSession.id)
        ? const UnavailablePage()
        : Scaffold(
            appBar: AppBar(
              backgroundColor: AppColors.canvas,
              surfaceTintColor: Colors.transparent,
              leading: IconButton(
                tooltip: 'Chi tiết sự kiện',
                onPressed: () => Navigator.pop(context),
                icon: const Icon(Icons.arrow_back, color: AppColors.blue),
              ),
              titleSpacing: 0,
              title: const Text(
                'Chi tiết sự kiện',
                style: TextStyle(color: AppColors.blue, fontSize: 13),
              ),
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
                padding: const EdgeInsets.fromLTRB(18, 10, 18, 24),
                children: [
                  Container(
                    padding: const EdgeInsets.all(21),
                    decoration: BoxDecoration(
                      gradient: const LinearGradient(
                        colors: [Color(0xFFE5F0FF), Color(0xFFF4F7FD)],
                      ),
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(color: AppColors.line),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        CategoryBadge(session.type),
                        const SizedBox(height: 14),
                        Text(
                          session.name,
                          key: const Key('session-title'),
                          style: Theme.of(context).textTheme.headlineMedium
                              ?.copyWith(fontSize: 25),
                        ),
                        const SizedBox(height: 11),
                        Text(
                          event.name,
                          style: const TextStyle(
                            fontSize: 12,
                            color: AppColors.muted,
                          ),
                        ),
                        const SizedBox(height: 20),
                        Row(
                          children: [
                            const Icon(
                              Icons.schedule,
                              size: 19,
                              color: AppColors.blue,
                            ),
                            const SizedBox(width: 8),
                            Text(
                              session.time,
                              style: const TextStyle(
                                color: AppColors.blue,
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 19),
                  SurfaceBox(
                    child: Column(
                      children: [
                        MetaRow(
                          icon: Icons.calendar_today_outlined,
                          title: fullDate(event.date),
                        ),
                        const SizedBox(height: 16),
                        MetaRow(
                          icon: Icons.location_on_outlined,
                          title: session.room.isEmpty
                              ? 'Chưa công bố phòng'
                              : session.room,
                          subtitle: event.location,
                        ),
                        const SizedBox(height: 16),
                        MetaRow(
                          icon: Icons.people_outline,
                          title: session.speaker.isEmpty
                              ? 'Chưa công bố diễn giả'
                              : session.speaker,
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 25),
                  Text(
                    'Bạn sẽ khám phá điều gì?',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  const SizedBox(height: 10),
                  Text(
                    session.description.isEmpty
                        ? 'Nội dung đang được cập nhật.'
                        : session.description,
                    style: const TextStyle(
                      color: AppColors.muted,
                      fontSize: 13,
                      height: 1.8,
                    ),
                  ),
                  const SizedBox(height: 26),
                  OutlinedButton.icon(
                    onPressed: () => Navigator.pop(context),
                    icon: const Icon(Icons.arrow_back, size: 18),
                    label: const Text('Trở lại lịch trình'),
                  ),
                ],
              ),
            ),
          ),
  );
}
