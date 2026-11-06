import 'package:flutter/material.dart';

import '../../agenda/logic/agenda_controller.dart';
import '../../../shared/ui/app_theme.dart';

Future<void> toggleFavourite(
  BuildContext context,
  AgendaController controller,
  String id,
) async {
  try {
    final message = await controller.toggleFavourite(id);
    if (context.mounted && message != null) {
      ScaffoldMessenger.of(context)
        ..hideCurrentSnackBar()
        ..showSnackBar(
          SnackBar(
            content: Text(message),
            duration: const Duration(milliseconds: 2600),
          ),
        );
    }
  } catch (_) {
    if (context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Chưa lưu được thay đổi. Hãy thử lại.')),
      );
    }
  }
}

class FavouriteButton extends StatelessWidget {
  const FavouriteButton({
    super.key,
    required this.controller,
    required this.eventId,
    required this.eventName,
  });
  final AgendaController controller;
  final String eventId, eventName;

  @override
  Widget build(BuildContext context) {
    final saved = controller.saved.contains(eventId);
    return Semantics(
      toggled: saved,
      child: IconButton.filledTonal(
        key: Key('favourite-$eventId'),
        tooltip: '${saved ? 'Bỏ yêu thích' : 'Lưu yêu thích'}: $eventName',
        style: IconButton.styleFrom(
          backgroundColor: Colors.white,
          foregroundColor: saved ? AppColors.blue : const Color(0xFFA0A8B5),
          minimumSize: const Size(44, 44),
        ),
        onPressed: controller.saving
            ? null
            : () => toggleFavourite(context, controller, eventId),
        icon: Icon(saved ? Icons.favorite : Icons.favorite_border, size: 23),
      ),
    );
  }
}
