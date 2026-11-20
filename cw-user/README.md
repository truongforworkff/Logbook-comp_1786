# Eventa Explore — G/H và tài khoản người tham dự

Mở `cw-user/app` trong Android Studio. Chọn một thiết bị Android hoặc Chrome rồi chạy `lib/main.dart`. Project dùng Flutter 3.47.4 và Dart 3.13. Android và web đã có cấu hình Firebase.

## Người tham dự

1. Tạo tài khoản bằng tên, email và mật khẩu hoặc chọn **Tiếp tục với Google**.
2. Mở một event để xem timeline. Chạm trái tim để lưu sự kiện yêu thích hoặc mở một session và chọn thêm vào **Lịch của tôi**.
3. Khi session trùng thời gian, app hiển thị những session bị trùng và cho phép quay lại hoặc vẫn thêm.
4. Đăng nhập bằng cùng tài khoản trên thiết bị khác để xem lại hai danh sách. Ở màn tài khoản có thể nhập các lựa chọn cũ còn trên thiết bị sau khi xác nhận email đích.

Danh mục và lựa chọn đã tải được lưu trong cache. Khi mất mạng, thay đổi được xếp hàng và gửi lại lúc có kết nối. Màn tài khoản chờ các thay đổi đang ghi trước khi đăng xuất. Session bị gỡ khỏi danh mục sẽ biến mất khỏi lịch hiển thị.

## Đọc code

| Phần | Giao diện | Xử lý và dữ liệu |
| --- | --- | --- |
| Đăng nhập và đăng ký | `lib/features/auth/ui` | `auth/data/auth_repository.dart`, `auth/logic/auth_validation.dart` |
| Tài khoản và nhập lịch đã lưu | `lib/features/account/ui` | `account/data/personal_data_repository.dart`, `account/logic/personal_selection.dart` |
| Danh sách và chi tiết | `lib/features/agenda/ui` | `agenda/logic/agenda_controller.dart`, `agenda/data/catalog_repository.dart` |
| Lịch và yêu thích | `lib/features/schedule/ui`, `lib/features/favourites/ui` | `schedule/logic/personal_schedule.dart`, repository tài khoản |

Cấu hình project ở `lib/shared/data/firebase_options.dart`. Khi ký bằng keystore khác, đăng ký SHA-1/SHA-256 Android với Firebase cho đăng nhập Google. [Sơ đồ database và quyền truy cập](../cloud/firebase/DATABASE_DIAGRAM.md)

## Build và kiểm tra

```sh
flutter pub get
flutter analyze
flutter test
flutter build apk --debug
flutter build web --release
```

Test `integration_test/account_sync_test.dart` kiểm tra hai client cùng tài khoản, thay đổi offline và chuyển người dùng. Khởi động [Firebase emulators](../cloud/firebase/README.md), rồi chạy `flutter test integration_test/account_sync_test.dart -d <id-may-ao-thu>`.
