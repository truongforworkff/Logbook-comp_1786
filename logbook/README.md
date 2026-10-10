# COMP1786 CW2 — three exercises

Open **this directory** in Android Studio as a single Gradle project. Each module is a separate installable app with its own package, so all three can coexist on one device.

| Module | Package | Behaviour |
| --- | --- | --- |
| `exercise1-bmi` | `com.comp1786.bmi` | Weight/height, units, validation, BMI, category and colour |
| `exercise2-events` | `com.comp1786.events` | List/add events in memory; no database or preferences |
| `exercise3-room` | `com.comp1786.room` | List/add/edit/delete, status, Room database and restart persistence |

## Build and run

Use JDK 17, Android SDK platform 36 and a device/emulator on Android 7.0 (API 24) or newer. Android Studio can install SDK components and configure `local.properties`; this machine-specific file is ignored by Git. Gradle downloads dependencies on first build.

Windows:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
.\gradlew.bat :exercise3-room:connectedDebugAndroidTest
```

macOS/Linux (make the launcher executable first if necessary):

```sh
chmod +x gradlew
./gradlew assembleDebug testDebugUnitTest lintDebug
./gradlew :exercise3-room:connectedDebugAndroidTest
```

The connected test requires a running emulator or connected device. Select an exercise run configuration in Android Studio and press Run. APKs appear in each module's `build/outputs/apk/debug/` directory. Build artifacts are not committed.

Pinned toolchain: AGP 8.13.2, Gradle 8.13, Kotlin 2.2.21, KSP 2.2.21-2.0.4, Room 2.8.5, RecyclerView 1.4.0. `java.time` is desugared to support strict date validation on API 24–25.

## Implementation notes for the logbook

**Ex1:** `BmiCalculator` converts kg/lb/stone and cm/m/inches/decimal feet to kg and metres before calculating `kg / metres²`. Reasonable input bounds are explicitly defined as 10–500 kg and 0.5–2.5 m after conversion. The form reports empty, non-numeric, non-finite and out-of-range values. Category cut-offs are 18.5, 25 and 30, using the unrounded BMI. Text labels accompany the colours, with readable foreground/background colours in light and dark mode. Unit values use decimals: 5.5 ft means 5 ft 6 in, not 5 ft 5 in.

**Ex2:** `EventViewModel` stores a `StateFlow<List<Event>>` in RAM. Rotation retains the list through the ViewModel. Process termination clears it intentionally; ordinary backgrounding does not guarantee the process ends. `EventAdapter` displays every required field. Adding an event uses an XML form, predefined types and a date picker. Required text is trimmed and limited to 120 characters. Dates must be real ISO dates `YYYY-MM-DD`, year 1900–2100; past dates are allowed. No persistence API is used.

**Ex3:** Same event model and form structure, extended with predefined status values Upcoming/Completed/Cancelled. `Event` is a Room entity, `EventDao` has insert/update/delete and observable queries, and `EventDatabase` is a singleton using application context. Database operations are suspend functions called in `viewModelScope`; no main-thread database access is enabled. Lifecycle-aware Flow collection updates the list automatically. Tap a row to edit, or choose Delete and confirm. Database errors leave the editor available for retry. Room schema version 1 is exported under `schemas/`; future schema changes require an explicit migration. No destructive fallback is configured.

All apps use XML Views, View Binding, resource strings, a DayNight theme and system-bar insets. Event drafts are restored after rotation. The apps need no internet or storage permission. Ex3 persists through app restart, but uninstalling/clearing app data removes its database.

## Evidence and submission

See [TESTING.md](TESTING.md) for demo cases and screenshot suggestions. The coursework still needs the student's completed logbook PDF with personal details, actual screenshots and explanations, plus the requested ZIP/app evidence. This repository supplies code and verification guidance; it does not fabricate screenshots or personal completion claims.

The Ex3 specification mentions validating an “amount”, but defines no amount field. This implementation follows the named event fields and validates their text, date, type and status using constrained controls.

References: [Room documentation](https://developer.android.com/training/data-storage/room), [Room releases](https://developer.android.com/jetpack/androidx/releases/room), [RecyclerView](https://developer.android.com/develop/ui/views/layout/recyclerview), [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel), [AGP 8.13 compatibility](https://developer.android.com/build/releases/agp-8-13-0-release-notes).
