# Verification and evidence checklist

Automated JVM tests cover BMI metric/imperial conversions, category boundaries, invalid numeric ranges, strict dates including leap years and text validation. Ex3's device test creates a file-backed Room database, inserts a row, closes/reopens the database, updates it, closes/reopens again, deletes it and verifies deletion after reopening. It uses a dedicated test database and does not erase the app's normal `events.db`.

## Exercise 1

1. Enter 70 kg and 170 cm. Expect BMI 24.22, Healthy weight and green background.
2. Enter equivalent lb/in values. Expect the same BMI within rounding.
3. With height 2 m, try weights 72, 80, 104 and 124 kg. Expect Underweight, Healthy weight, Overweight and Obese respectively, with distinct colours and labels.
4. Try empty fields, zero, very large numbers and pasted invalid text. Expect field errors and no stale result.
5. Rotate with a result visible; verify inputs, units and result. Check dark theme, large fonts and a small screen.

Capture the successful result, a second unit combination, different categories and validation feedback.

## Exercise 2

1. Launch with an empty list; add two different events. Check all five fields on each row.
2. Try blank/whitespace fields, `2025-02-29`, `2026-04-31` and `10/10/2026`. Expect errors without losing draft input.
3. Try `2024-02-29`; expect success. Use each type dropdown option and the date picker.
4. Rotate with events present and while entering a draft; verify both survive rotation.
5. Force-stop the app in Android Settings, then reopen. Expect an empty list (RAM-only design).

Capture the entry form, validation feedback and list showing every required field.

## Exercise 3

1. Add events with different types and statuses. Check the list includes status.
2. Force-stop then reopen the app. Verify all events remain.
3. Tap a row, edit its fields and change status to Completed. Save, force-stop and reopen; verify edits persist.
4. Choose Delete and cancel the confirmation; verify the event remains.
5. Delete and confirm. Force-stop/reopen; verify it remains deleted.
6. Run the same invalid date/text cases as Ex2. Rotate while editing and check the draft.
7. Verify an empty state after deleting all events and check scrolling with many entries.

Capture list/status, edit form, delete confirmation and saved data after reopening. Record actual observed outcomes; the checklist itself is not proof that a manual test passed.
