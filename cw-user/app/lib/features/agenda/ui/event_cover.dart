import 'package:flutter/material.dart';

import '../data/event_info.dart';

class EventCover extends StatelessWidget {
  const EventCover({super.key, required this.event, required this.height});
  final EventInfo event;
  final double height;
  @override
  Widget build(BuildContext context) {
    Widget fallback(BuildContext context, Object error, StackTrace? stack) =>
        Container(
          height: height,
          color: const Color(0xFF73849A),
          alignment: Alignment.center,
          child: const Icon(Icons.image_outlined, color: Colors.white),
        );
    if (event.imageBytes != null) {
      return Image.memory(
        event.imageBytes!,
        height: height,
        width: double.infinity,
        fit: BoxFit.cover,
        semanticLabel: 'Ảnh bìa sự kiện',
        errorBuilder: fallback,
      );
    }
    return Image.asset(
      event.imageAsset,
      height: height,
      width: double.infinity,
      fit: BoxFit.cover,
      semanticLabel: 'Ảnh bìa sự kiện',
      errorBuilder: fallback,
    );
  }
}
