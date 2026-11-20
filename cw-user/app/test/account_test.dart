import 'package:eventa_user/features/account/data/personal_data_repository.dart';
import 'package:eventa_user/features/account/logic/personal_selection.dart';
import 'package:eventa_user/features/auth/logic/auth_validation.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('Registration validates email and minimum password length', () {
    expect(emailError(' attendee@example.test '), isNull);
    expect(emailError('attendee@'), isNotNull);
    expect(passwordError('1234567', registering: true), isNotNull);
    expect(passwordError('12345678', registering: true), isNull);
  });

  test(
    'Selections have distinct stable keys and malformed rows are ignored',
    () {
      expect(preferenceId('e/s', '1'), isNot(preferenceId('e', 's/1')));
      expect(preferenceId('e', 's'), isNot(contains('/')));
      expect(
        cloudFavourites([
          {'eventId': 'e'},
          {'eventId': 'e'},
          {},
        ]),
        {'e'},
      );
      expect(
        cloudSchedule([
          {'eventId': 'e', 'sessionId': 's'},
          {'eventId': 'e'},
        ]),
        {(eventId: 'e', sessionId: 's')},
      );
    },
  );
}
