import 'package:flutter/material.dart';

abstract final class AppColors {
  static const canvas = Color(0xFFF4F5F9);
  static const ink = Color(0xFF171B26);
  static const muted = Color(0xFF7C8494);
  static const blue = Color(0xFF087DFF);
  static const blueSoft = Color(0xFFEAF3FF);
  static const line = Color(0xFFE8EBF2);
}

ThemeData appTheme() => ThemeData(
  useMaterial3: true,
  brightness: Brightness.light,
  colorScheme: ColorScheme.fromSeed(
    seedColor: AppColors.blue,
    primary: AppColors.blue,
    surface: Colors.white,
  ),
  scaffoldBackgroundColor: AppColors.canvas,
  dividerColor: AppColors.line,
  textTheme: const TextTheme(
    headlineMedium: TextStyle(
      fontSize: 28,
      fontWeight: FontWeight.w800,
      color: AppColors.ink,
      letterSpacing: -0.8,
    ),
    titleLarge: TextStyle(
      fontSize: 24,
      fontWeight: FontWeight.w700,
      color: AppColors.ink,
    ),
    titleMedium: TextStyle(
      fontSize: 16,
      fontWeight: FontWeight.w700,
      color: AppColors.ink,
    ),
    bodyMedium: TextStyle(fontSize: 13, color: AppColors.ink, height: 1.5),
    bodySmall: TextStyle(fontSize: 11, color: AppColors.muted, height: 1.5),
  ),
  snackBarTheme: SnackBarThemeData(
    behavior: SnackBarBehavior.floating,
    backgroundColor: const Color(0xFF182A46),
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
    showCloseIcon: true,
  ),
  filledButtonTheme: FilledButtonThemeData(
    style: FilledButton.styleFrom(
      backgroundColor: AppColors.blue,
      foregroundColor: Colors.white,
      minimumSize: const Size(0, 48),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(13)),
    ),
  ),
  outlinedButtonTheme: OutlinedButtonThemeData(
    style: OutlinedButton.styleFrom(
      foregroundColor: AppColors.blue,
      minimumSize: const Size(0, 48),
      side: const BorderSide(color: AppColors.line),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(13)),
    ),
  ),
  bottomSheetTheme: const BottomSheetThemeData(
    backgroundColor: Colors.white,
    showDragHandle: true,
    shape: RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(26)),
    ),
  ),
);
