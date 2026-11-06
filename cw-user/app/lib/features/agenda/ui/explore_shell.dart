import 'package:flutter/material.dart';

import '../logic/agenda_controller.dart';
import '../../../shared/ui/app_theme.dart';
import '../../../shared/ui/app_widgets.dart';
import 'browse_page.dart';

class ExploreShell extends StatefulWidget {
  const ExploreShell({super.key, required this.controller});
  final AgendaController controller;
  @override
  State<ExploreShell> createState() => _ExploreShellState();
}

class _ExploreShellState extends State<ExploreShell> {
  @override
  void initState() {
    super.initState();
    widget.controller.load();
  }

  @override
  Widget build(BuildContext context) => ListenableBuilder(
    listenable: widget.controller,
    builder: (context, _) {
      final controller = widget.controller;
      return Scaffold(
        bottomNavigationBar: BottomTabs(controller: controller),
        body: SafeArea(
          bottom: false,
          child: PageFrame(
            child: Column(
              children: [
                const BrandHeader(),
                Expanded(
                  child: controller.loading
                      ? const Center(
                          child: CircularProgressIndicator(
                            color: AppColors.blue,
                          ),
                        )
                      : controller.error.isNotEmpty
                      ? _error(controller)
                      : BrowsePage(
                          key: ValueKey(controller.tab),
                          controller: controller,
                          savedOnly: controller.tab == AgendaTab.saved,
                        ),
                ),
              ],
            ),
          ),
        ),
      );
    },
  );

  Widget _error(AgendaController controller) => Center(
    child: Padding(
      padding: const EdgeInsets.all(28),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(
            Icons.cloud_off_outlined,
            color: AppColors.muted,
            size: 42,
          ),
          const SizedBox(height: 15),
          const Text(
            'Chưa mở được danh sách',
            style: TextStyle(fontSize: 20, fontWeight: FontWeight.w700),
          ),
          const SizedBox(height: 10),
          Text(controller.error, textAlign: TextAlign.center),
          const SizedBox(height: 20),
          FilledButton(
            onPressed: controller.load,
            child: const Text('Thử lại'),
          ),
        ],
      ),
    ),
  );
}
