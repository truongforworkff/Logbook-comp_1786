import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'app.dart';
import 'features/agenda/logic/agenda_controller.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  SystemChrome.setSystemUIOverlayStyle(
    const SystemUiOverlayStyle(
      statusBarColor: Colors.transparent,
      statusBarIconBrightness: Brightness.dark,
      statusBarBrightness: Brightness.light,
      systemNavigationBarColor: Color(0xFFF4F5F9),
      systemNavigationBarIconBrightness: Brightness.dark,
    ),
  );
  runApp(EventaApp(controller: AgendaController()));
}
