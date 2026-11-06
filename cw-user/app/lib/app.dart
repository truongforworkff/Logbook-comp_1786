import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

import 'features/agenda/logic/agenda_controller.dart';
import 'features/agenda/ui/explore_shell.dart';
import 'shared/ui/app_theme.dart';

class EventaApp extends StatelessWidget {
  const EventaApp({super.key, required this.controller});
  final AgendaController controller;
  @override
  Widget build(BuildContext context) => MaterialApp(
    title: 'Eventa Explore',
    debugShowCheckedModeBanner: false,
    theme: appTheme(),
    locale: const Locale('vi'),
    supportedLocales: const [Locale('vi'), Locale('en')],
    localizationsDelegates: GlobalMaterialLocalizations.delegates,
    home: ExploreShell(controller: controller),
  );
}
