# Eventa Explore — G/H bằng Dart và Flutter

Project đang dùng **Flutter 3.47.4 / Dart 3.13**, giao diện Flutter widgets theo bản HTML trong design/. Phần MAUI đã được thay; mã cũ còn trong lịch sử Git. App quản trị Java trong cw-admin giữ nguyên.

## Chức năng

- Danh sách 5 event/9 session, ảnh bìa tùy chọn và chủ đề.
- Tìm tên không phân biệt dấu/hoa thường; lọc ngày, tháng và chủ đề.
- Chi tiết event có timeline session, chi tiết session chỉ đọc.
- Thêm/bỏ yêu thích từ danh sách hoặc chi tiết; trang Yêu thích riêng, lưu trên thiết bị.
- Navigator xử lý Back, sheet chủ đề và lịch chọn ngày theo giao diện sáng/xanh đã chốt.

**G hiện dùng snapshot cục bộ theo phương án đề cho phép. Cloud E chưa triển khai; sửa dữ liệu admin chưa tự đồng bộ sang app này.**

## Chạy và kiểm tra

Mở thư mục app/ bằng Android Studio có Flutter plugin hoặc VS Code có Flutter extension.

~~~text
cd cw-user/app
flutter pub get
flutter analyze
flutter test
flutter run -d emulator-5554
flutter build apk --debug --target-platform android-arm64,android-x64
flutter build web
~~~

Kiểm tra luồng thực tế trên emulator demo:

~~~text
flutter drive --driver=test_driver/integration_test.dart --target=integration_test/app_test.dart -d emulator-5554
~~~

Bài kiểm tra giữ lại và khôi phục danh sách yêu thích ban đầu. Sau khi chạy integration test, dùng flutter run để cài lại entry point app thông thường.

## Nền tảng

Có cấu hình Android, iOS, web, Windows, macOS và Linux. Android/web được kiểm tra trong quy trình build; iOS/macOS cần Mac và Xcode, Windows cần Visual Studio với C++, Linux cần các thư viện desktop Flutter. Có cấu hình không đồng nghĩa đã chạy thử trên mọi nền tảng.

## Đọc code

- app/lib/main.dart, app.dart: khởi động và cấu hình ứng dụng.
- lib/features/agenda/ui: màn danh sách, thẻ event, sheet chủ đề, chi tiết event/session và khung chính.
- lib/features/agenda/logic: AgendaController đọc/lưu trạng thái; EventSearch tìm/lọc.
- lib/features/agenda/data: EventInfo/SessionInfo và CatalogRepository đọc asset.
- lib/features/favourites/ui: nút yêu thích và thông báo kết quả.
- lib/features/favourites/data: FavouriteStore đọc/ghi SharedPreferencesAsync.
- lib/features/favourites/logic: JSON yêu thích, loại ID không còn hợp lệ.
- lib/shared/ui: theme, nhãn và thành phần dùng chung.
- assets/data/events.json: snapshot; assets/images: ảnh đóng gói offline.
- test/: logic và widget tests; integration_test/: luồng thực tế; test_driver/: lưu ảnh kiểm thử.
- android/ios/web/windows/macos/linux: khung nền tảng do Flutter tạo.

## Giữ dữ liệu từ bản MAUI

Android giữ applicationId com.comp1786.eventa.user, dùng lại file preferences và key eventa.favourites.v1. Khi cập nhật bản debug đã cài, cần dùng cùng chữ ký. Gradle hỗ trợ biến EVENTA_DEBUG_KEYSTORE trỏ tới debug keystore cũ; không đưa keystore vào Git và không gỡ app để cập nhật. Máy mới chưa cài app dùng debug key Flutter mặc định.

Phiên Demo và ảnh bìa chỉ là minh họa; nguồn event nằm trong JSON. Giờ session lấy từ admin, không tự thêm giờ kết thúc.

Tham khảo [Flutter](https://docs.flutter.dev/) và [shared_preferences](https://pub.dev/packages/shared_preferences).
