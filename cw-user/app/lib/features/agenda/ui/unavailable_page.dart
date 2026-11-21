import 'package:flutter/material.dart';

import '../../../shared/ui/app_theme.dart';

class UnavailablePage extends StatelessWidget {
  const UnavailablePage({super.key});
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Agenda updated')),
    body: Center(
      child: Padding(
        padding: const EdgeInsets.all(28),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(
              Icons.event_busy_outlined,
              size: 44,
              color: AppColors.muted,
            ),
            const SizedBox(height: 16),
            const Text(
              'This event or agenda item has been removed.',
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 20),
            FilledButton(
              onPressed: () =>
                  Navigator.of(context).popUntil((route) => route.isFirst),
              child: const Text('Back to events'),
            ),
          ],
        ),
      ),
    ),
  );
}
