import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

import 'features/agenda/logic/agenda_controller.dart';
import 'features/agenda/ui/explore_shell.dart';
import 'shared/ui/app_theme.dart';

class EventaApp extends StatelessWidget {
  const EventaApp({super.key, this.controller, this.home});
  final AgendaController? controller;
  final Widget? home;
  @override
  Widget build(BuildContext context) => MaterialApp(
    title: 'Eventa Explore',
    debugShowCheckedModeBanner: false,
    theme: appTheme(),
    locale: const Locale('en', 'GB'),
    supportedLocales: const [Locale('en', 'GB')],
    localizationsDelegates: GlobalMaterialLocalizations.delegates,
    home: home ?? ExploreShell(controller: controller!),
  );
}
