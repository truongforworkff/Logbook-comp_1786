import 'package:flutter/material.dart';

import '../../../shared/ui/app_theme.dart';
import '../../../shared/ui/event_labels.dart';

Future<String?> showCategorySheet(
  BuildContext context,
  String selected,
) => showModalBottomSheet<String>(
  context: context,
  isScrollControlled: true,
  builder: (context) => SafeArea(
    child: SingleChildScrollView(
      padding: const EdgeInsets.fromLTRB(22, 0, 22, 20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            children: [
              const Expanded(
                child: Text(
                  'Khám phá theo chủ đề',
                  style: TextStyle(fontSize: 23, fontWeight: FontWeight.w700),
                ),
              ),
              IconButton(
                tooltip: 'Đóng bộ lọc',
                onPressed: () => Navigator.pop(context),
                icon: const Icon(Icons.close),
              ),
            ],
          ),
          const SizedBox(height: 14),
          for (final category in categories.entries)
            Padding(
              padding: const EdgeInsets.only(bottom: 7),
              child: ListTile(
                key: Key('category-${category.key}'),
                dense: true,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
                tileColor: selected == category.key
                    ? AppColors.blueSoft
                    : AppColors.canvas,
                title: Text(
                  category.value,
                  style: TextStyle(
                    fontSize: 14,
                    color: selected == category.key
                        ? AppColors.blue
                        : AppColors.ink,
                  ),
                ),
                trailing: selected == category.key
                    ? const Icon(Icons.check, color: AppColors.blue, size: 19)
                    : null,
                onTap: () => Navigator.pop(context, category.key),
              ),
            ),
        ],
      ),
    ),
  ),
);
