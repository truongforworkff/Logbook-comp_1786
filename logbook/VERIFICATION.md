# Verification — 10 October 2026

Verified locally with JDK 17 and Android SDK 36.

- `assembleDebug`: all three APKs built successfully.
- `testDebugUnitTest`: 14 tests passed (BMI: 6; Ex2 validation/state: 5; Ex3 validation: 3).
- `lintDebug`: all three modules passed with zero errors. Non-blocking warnings remain, including pinned dependency versions, autofill/accessibility compatibility attributes and launcher/backup metadata.
- `:exercise3-room:connectedDebugAndroidTest`: one device test passed on the API 36 Android 16 emulator. It verifies insert, update and delete across close/reopen cycles of a file-backed Room database.
- All three apps were installed and launched successfully on that emulator.
- `git diff --cached --check`: no whitespace errors.

Windows locked an intermediate JAR during an earlier rebuild. The final successful verification used a fresh Gradle process and in-process Kotlin compilation:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug :exercise3-room:connectedDebugAndroidTest --no-daemon --max-workers=2 '-Pkotlin.compiler.execution.strategy=in-process'
```

The database test verifies the storage layer. Full manual UI scenarios, rotation checks, force-stop checks and screenshot evidence remain listed in `TESTING.md`; they are not all claimed as completed here.
