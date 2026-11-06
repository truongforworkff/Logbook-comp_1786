# Firebase — thiết lập Cloud cho Eventa

Project **eventa-coursework**, số **298519879781**. Gói **Spark**, Firestore Standard **(default)** tại **asia-southeast1 (Singapore)**. Không bật thanh toán, Storage, Functions hoặc Analytics.

## Đã thiết lập

- Android Admin com.comp1786.eventa.admin, Android Explore com.comp1786.eventa.user và Explore Web cùng project.
- Admin: Google Services plugin, Firestore/Auth SDK; FirebaseInitProvider tự khởi tạo từ app/google-services.json.
- Flutter: Firebase Core/Firestore, lib/shared/data/firebase_options.dart; main khởi tạo Android/web. Chưa cấu hình Firebase cho Apple/desktop; các nền tảng đó vẫn đọc snapshot như trước.
- Authentication Email/Password đã bật. Chưa tạo tài khoản hay cấp vai trò admin.
- Firestore từ chối toàn bộ client. Đây là bước thiết lập, chưa hoàn thành upload/đồng bộ E hoặc đọc Cloud G.

## Kiểm tra kết nối

Admin, chỉ đọc, không tạo/xóa dữ liệu hoặc sửa SQLite:

~~~text
cd cw-admin
gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.comp1786.room.features.cloud.data.FirebaseSetupTest
~~~

Flutter trên emulator:

~~~text
cd cw-user/app
flutter drive --driver=test_driver/integration_test.dart --target=integration_test/firebase_setup_test.dart -d emulator-5554 --keep-app-running
~~~

Các test xác nhận đúng project và server trả PERMISSION_DENIED cho setup_checks. Lỗi mạng/timeout phải làm test thất bại. Cài/chạy lại entry lib/main.dart sau integration test. Dùng đúng EVENTA_DEBUG_KEYSTORE khi cập nhật bản debug cũ; không gỡ app.

## Triển khai database

~~~text
cd cloud/firebase
firebase login
firebase deploy --only firestore:rules,firestore:indexes --project eventa-coursework
~~~

Review rules trước khi deploy: file hiện tại đóng toàn bộ client; deploy sau khi hoàn thành E sẽ chặn chức năng đó. Không dùng test mode hoặc allow read, write: if true.

## Tiếp theo

- Người dùng tự đặt mật khẩu tài khoản; cấp vai trò quản trị phía server. Đăng nhập không tự cấp quyền ghi.
- Admin: SQLite vẫn là nguồn dữ liệu, upload tất cả, ID ổn định, hàng đợi sửa/xóa và thử lại khi có mạng.
- Định nghĩa cấu trúc event/session, validation và quyền đọc dữ liệu công bố; kiểm thử rules trước khi mở quyền.
- Flutter: chuyển CatalogRepository sang Firestore, thêm tải/lỗi/cache. Hiện vẫn đọc snapshot.
- Ảnh vẫn cục bộ: Firebase Storage yêu cầu Blaze nên chưa đồng bộ ảnh ở Spark.

## Cấu hình công khai

Project/app ID và API key client là định danh công khai; Security Rules bảo vệ dữ liệu. Không commit service-account JSON, private key, token CLI hoặc mật khẩu.

Nguồn: [Android](https://firebase.google.com/docs/android/setup), [Flutter](https://firebase.google.com/docs/flutter/setup), [Rules](https://firebase.google.com/docs/firestore/security/get-started), [Pricing](https://firebase.google.com/pricing), [Storage](https://firebase.google.com/docs/storage/faqs-storage-changes-announced-sept-2024).
