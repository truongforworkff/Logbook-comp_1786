import 'package:flutter/material.dart';

import '../../features/agenda/logic/agenda_controller.dart';
import 'app_theme.dart';

class PageFrame extends StatelessWidget {
  const PageFrame({super.key, required this.child});
  final Widget child;
  @override
  Widget build(BuildContext context) => Center(
    heightFactor: 1,
    child: ConstrainedBox(
      constraints: const BoxConstraints(maxWidth: 620),
      child: child,
    ),
  );
}

class BrandHeader extends StatelessWidget {
  const BrandHeader({super.key});
  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.fromLTRB(18, 8, 18, 10),
    child: Row(
      children: [
        Container(
          width: 34,
          height: 34,
          decoration: BoxDecoration(
            color: AppColors.blueSoft,
            borderRadius: BorderRadius.circular(12),
          ),
          child: const Icon(
            Icons.auto_awesome_outlined,
            color: AppColors.blue,
            size: 22,
          ),
        ),
        const SizedBox(width: 9),
        const Expanded(
          child: Text(
            'Explore Agenda',
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.w800),
          ),
        ),
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 5),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(20),
            border: Border.all(color: AppColors.line),
          ),
          child: const Row(
            children: [
              Icon(Icons.circle, size: 6, color: Color(0xFF25B66B)),
              SizedBox(width: 4),
              Text(
                'Cục bộ',
                style: TextStyle(fontSize: 10, color: AppColors.muted),
              ),
            ],
          ),
        ),
        const SizedBox(width: 16),
        const CircleAvatar(
          radius: 16,
          backgroundColor: AppColors.blueSoft,
          child: Text(
            'EA',
            style: TextStyle(fontSize: 11, color: AppColors.blue),
          ),
        ),
      ],
    ),
  );
}

class BottomTabs extends StatelessWidget {
  const BottomTabs({super.key, required this.controller});
  final AgendaController controller;

  void _select(BuildContext context, AgendaTab tab) {
    FocusScope.of(context).unfocus();
    Navigator.of(context).popUntil((route) => route.isFirst);
    controller.selectTab(tab);
  }

  @override
  Widget build(BuildContext context) => Container(
    decoration: const BoxDecoration(
      color: Colors.white,
      border: Border(top: BorderSide(color: AppColors.line)),
    ),
    child: SafeArea(
      top: false,
      child: PageFrame(
        child: SizedBox(
          height: 72,
          child: Row(
            children: [
              _tab(
                context,
                AgendaTab.explore,
                Icons.event_note_outlined,
                'Khám phá',
              ),
              _tab(
                context,
                AgendaTab.saved,
                Icons.favorite_border,
                'Yêu thích',
              ),
            ],
          ),
        ),
      ),
    ),
  );

  Widget _tab(
    BuildContext context,
    AgendaTab tab,
    IconData icon,
    String label,
  ) {
    final active = controller.tab == tab;
    final color = active ? AppColors.blue : const Color(0xFFA0A8B5);
    return Expanded(
      child: Semantics(
        selected: active,
        child: TextButton(
          key: Key('tab-${tab.name}'),
          onPressed: () => _select(context, tab),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Badge(
                isLabelVisible:
                    tab == AgendaTab.saved && controller.saved.isNotEmpty,
                label: Text('${controller.saved.length}'),
                backgroundColor: AppColors.blue,
                child: Icon(icon, size: 24, color: color),
              ),
              const SizedBox(height: 5),
              Text(
                label,
                style: TextStyle(
                  fontSize: 11,
                  color: color,
                  fontWeight: active ? FontWeight.w700 : FontWeight.w500,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class CategoryBadge extends StatelessWidget {
  const CategoryBadge(this.label, {super.key});
  final String label;
  @override
  Widget build(BuildContext context) => DecoratedBox(
    decoration: BoxDecoration(
      color: AppColors.blueSoft,
      borderRadius: BorderRadius.circular(6),
    ),
    child: Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 5),
      child: Text(
        label,
        style: const TextStyle(
          color: AppColors.blue,
          fontSize: 10,
          fontWeight: FontWeight.w600,
        ),
      ),
    ),
  );
}

class MetaRow extends StatelessWidget {
  const MetaRow({
    super.key,
    required this.icon,
    required this.title,
    this.subtitle = '',
  });
  final IconData icon;
  final String title, subtitle;
  @override
  Widget build(BuildContext context) => Row(
    children: [
      Container(
        width: 36,
        height: 36,
        decoration: BoxDecoration(
          color: AppColors.blueSoft,
          borderRadius: BorderRadius.circular(11),
        ),
        child: Icon(icon, color: AppColors.blue, size: 19),
      ),
      const SizedBox(width: 11),
      Expanded(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600),
            ),
            if (subtitle.isNotEmpty)
              Text(subtitle, style: Theme.of(context).textTheme.bodySmall),
          ],
        ),
      ),
    ],
  );
}

class SurfaceBox extends StatelessWidget {
  const SurfaceBox({super.key, required this.child});
  final Widget child;
  @override
  Widget build(BuildContext context) => Container(
    width: double.infinity,
    padding: const EdgeInsets.all(15),
    decoration: BoxDecoration(
      color: Colors.white,
      borderRadius: BorderRadius.circular(16),
      border: Border.all(color: AppColors.line),
    ),
    child: child,
  );
}
