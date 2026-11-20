import 'package:flutter/material.dart';

import '../../agenda/logic/agenda_controller.dart';

class ScheduleButton extends StatelessWidget {
  const ScheduleButton({
    super.key,
    required this.controller,
    required this.eventId,
    required this.sessionId,
  });
  final AgendaController controller;
  final String eventId, sessionId;

  Future<void> _toggle(BuildContext context) async {
    try {
      final message = await controller.toggleSession(eventId, sessionId);
      if (context.mounted && message != null) {
        ScaffoldMessenger.of(context)
          ..hideCurrentSnackBar()
          ..showSnackBar(SnackBar(content: Text(message)));
      }
    } catch (_) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Chưa lưu được lịch. Hãy thử lại.')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final selected = controller.hasPlannedSession(eventId, sessionId);
    return Semantics(
      toggled: selected,
      child: OutlinedButton.icon(
        key: Key('plan-$sessionId'),
        onPressed: controller.savingSchedule ? null : () => _toggle(context),
        icon: Icon(
          selected ? Icons.event_available_outlined : Icons.event_note_outlined,
          size: 19,
        ),
        label: Text(selected ? 'Bỏ khỏi lịch của tôi' : 'Thêm vào lịch của tôi'),
      ),
    );
  }
}
