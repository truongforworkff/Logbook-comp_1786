import 'package:flutter/material.dart';

import '../../../shared/ui/app_theme.dart';

class UnavailablePage extends StatelessWidget {
  const UnavailablePage({super.key});
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Thông tin đã cập nhật')),
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
              'Sự kiện hoặc phiên này đã được gỡ khỏi lịch trình.',
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 20),
            FilledButton(
              onPressed: () =>
                  Navigator.of(context).popUntil((route) => route.isFirst),
              child: const Text('Trở lại danh sách'),
            ),
          ],
        ),
      ),
    ),
  );
}
