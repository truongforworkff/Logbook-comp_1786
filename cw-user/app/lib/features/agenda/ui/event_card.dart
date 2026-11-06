import 'package:flutter/material.dart';

import '../data/event_info.dart';
import '../logic/agenda_controller.dart';
import '../../favourites/ui/favourite_button.dart';
import '../../../shared/ui/app_theme.dart';
import '../../../shared/ui/app_widgets.dart';
import '../../../shared/ui/event_labels.dart';

class EventCard extends StatelessWidget {
  const EventCard({
    super.key,
    required this.event,
    required this.controller,
    required this.onOpen,
  });
  final EventInfo event;
  final AgendaController controller;
  final VoidCallback onOpen;

  @override
  Widget build(BuildContext context) => GestureDetector(
    onTap: onOpen,
    excludeFromSemantics: true,
    child: Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.line),
        boxShadow: const [
          BoxShadow(
            color: Color(0x080C2448),
            blurRadius: 12,
            offset: Offset(0, 4),
          ),
        ],
      ),
      clipBehavior: Clip.antiAlias,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _cover(),
          Padding(
            padding: const EdgeInsets.fromLTRB(15, 13, 15, 15),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Semantics(
                  label: 'Xem sự kiện: ${event.name}',
                  button: true,
                  onTap: onOpen,
                  child: ExcludeSemantics(
                    child: InkWell(
                      key: Key('event-${event.id}'),
                      onTap: onOpen,
                      child: Padding(
                        padding: const EdgeInsets.symmetric(vertical: 3),
                        child: Text(
                          event.name,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(
                            fontSize: 15,
                            height: 1.35,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  event.description,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    fontSize: 12,
                    height: 1.65,
                    color: AppColors.muted,
                  ),
                ),
                const SizedBox(height: 12),
                Container(
                  padding: const EdgeInsets.all(11),
                  decoration: BoxDecoration(
                    color: AppColors.canvas,
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Column(
                    children: [
                      _venue(
                        Icons.location_on_outlined,
                        event.location,
                        AppColors.blue,
                      ),
                      const SizedBox(height: 5),
                      _venue(
                        Icons.people_outline,
                        event.organiser,
                        AppColors.muted,
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    const Icon(Icons.circle, size: 6, color: Color(0xFF25B66B)),
                    const SizedBox(width: 5),
                    Expanded(
                      child: Text(
                        '${event.sessions.length} session',
                        style: const TextStyle(
                          fontSize: 11,
                          color: Color(0xFF25B66B),
                        ),
                      ),
                    ),
                    FilledButton(
                      onPressed: onOpen,
                      style: FilledButton.styleFrom(
                        minimumSize: const Size(0, 42),
                        padding: const EdgeInsets.symmetric(horizontal: 12),
                      ),
                      child: const Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(
                            'Xem lịch trình',
                            style: TextStyle(fontSize: 11),
                          ),
                          SizedBox(width: 5),
                          Icon(Icons.arrow_forward, size: 15),
                        ],
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    ),
  );

  Widget _cover() => Stack(
    children: [
      event.imageAsset.isEmpty
          ? _emptyCover()
          : Image.asset(
              event.imageAsset,
              width: double.infinity,
              height: 150,
              fit: BoxFit.cover,
              semanticLabel: 'Ảnh bìa minh họa',
              errorBuilder: (_, _, _) => _emptyCover(),
            ),
      if (event.imageAsset.isNotEmpty)
        const Positioned.fill(
          child: IgnorePointer(
            child: DecoratedBox(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.center,
                  end: Alignment.bottomCenter,
                  colors: [Colors.transparent, Color(0x990B152A)],
                ),
              ),
            ),
          ),
        ),
      Positioned(
        left: 12,
        top: 12,
        child: CategoryBadge(categoryLabel(event.categoryCode)),
      ),
      Positioned(
        right: 8,
        top: 5,
        child: FavouriteButton(
          controller: controller,
          eventId: event.id,
          eventName: event.name,
        ),
      ),
      Positioned(
        left: 14,
        bottom: 11,
        child: Row(
          children: [
            Icon(
              Icons.calendar_today_outlined,
              size: 13,
              color: event.image.isEmpty ? AppColors.muted : Colors.white,
            ),
            const SizedBox(width: 6),
            Text(
              '${shortDate(event.date)}${event.status == 'Completed' ? ' · Đã diễn ra' : ''}',
              style: TextStyle(
                fontSize: 11,
                color: event.image.isEmpty ? AppColors.muted : Colors.white,
              ),
            ),
          ],
        ),
      ),
    ],
  );

  Widget _emptyCover() => Container(height: 88, color: AppColors.blueSoft);

  Widget _venue(IconData icon, String text, Color color) => Row(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
      Icon(icon, size: 15, color: color),
      const SizedBox(width: 7),
      Expanded(
        child: Text(
          text,
          maxLines: 2,
          overflow: TextOverflow.ellipsis,
          style: const TextStyle(fontSize: 11, color: AppColors.muted),
        ),
      ),
    ],
  );
}
