import 'package:flutter/material.dart';

import '../../agenda/logic/agenda_controller.dart';
import '../logic/personal_schedule.dart';

class ScheduleButton extends StatefulWidget {
  const ScheduleButton({
    super.key,
    required this.controller,
    required this.eventId,
    required this.sessionId,
  });
  final AgendaController controller;
  final String eventId, sessionId;

  @override
  State<ScheduleButton> createState() => _ScheduleButtonState();
}

class _ScheduleButtonState extends State<ScheduleButton> {
  bool _working = false;

  Future<void> _toggle() async {
    setState(() => _working = true);
    try {
      String? message;
      try {
        message = await widget.controller.toggleSession(
          widget.eventId,
          widget.sessionId,
        );
      } on ScheduleConflict catch (conflict) {
        if (!mounted) return;
        final confirmed = await _confirm(conflict.entries);
        if (!mounted || confirmed != true) return;
        message = await widget.controller.toggleSession(
          widget.eventId,
          widget.sessionId,
          allowConflicts: true,
        );
      }
      if (mounted && message != null) {
        ScaffoldMessenger.of(context)
          ..hideCurrentSnackBar()
          ..showSnackBar(SnackBar(content: Text(message)));
      }
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Could not save your schedule. Try again.')),
        );
      }
    } finally {
      if (mounted) setState(() => _working = false);
    }
  }

  Future<bool?> _confirm(List<PlannedSession> entries) => showDialog<bool>(
    context: context,
    builder: (context) => AlertDialog(
      key: const Key('schedule-conflict-dialog'),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      title: const Text('Schedule conflict'),
      scrollable: true,
      content: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          const Text('These agenda items overlap with the one you want to add:'),
          for (final entry in entries) ...[
            const SizedBox(height: 16),
            Text(
              entry.session.name,
              style: const TextStyle(fontWeight: FontWeight.w700),
            ),
            Text('${entry.session.timeRange} · ${entry.event.name}'),
          ],
          const SizedBox(height: 16),
          const Text('You can add it anyway and decide which to attend later.'),
        ],
      ),
      actions: [
        TextButton(
          key: const Key('cancel-conflicting-session'),
          onPressed: () => Navigator.pop(context, false),
          child: const Text('Back'),
        ),
        FilledButton(
          key: const Key('confirm-conflicting-session'),
          onPressed: () => Navigator.pop(context, true),
          child: const Text('Add anyway'),
        ),
      ],
    ),
  );

  @override
  Widget build(BuildContext context) {
    final selected = widget.controller.hasPlannedSession(
      widget.eventId,
      widget.sessionId,
    );
    return Semantics(
      toggled: selected,
      child: OutlinedButton.icon(
        key: Key('plan-${widget.sessionId}'),
        onPressed: _working || widget.controller.savingSchedule
            ? null
            : _toggle,
        icon: Icon(
          selected ? Icons.event_available_outlined : Icons.event_note_outlined,
          size: 19,
        ),
        label: Text(
          selected ? 'Remove from my schedule' : 'Add to my schedule',
        ),
      ),
    );
  }
}
