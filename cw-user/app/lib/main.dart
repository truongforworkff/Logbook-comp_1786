import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/services.dart';

import 'app.dart';
import 'shared/data/firebase_options.dart';
import 'features/agenda/logic/agenda_controller.dart';

Future<void> main() async {
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
  final options = DefaultFirebaseOptions.currentPlatform;
  if (options != null) {
    await Firebase.initializeApp(options: options);
    FirebaseFirestore.instance.settings = const Settings(
      persistenceEnabled: true,
    );
  }
  runApp(EventaApp(controller: AgendaController()));
}
