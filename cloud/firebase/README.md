# Firebase — Eventa Admin → Explore

Project **eventa-coursework-fpt**, số **1011104656179**, thuộc tài khoản FPT. Gói **Spark**, Firestore Standard **(default)** tại **asia-southeast1**. Không bật thanh toán, Storage, Functions hoặc Analytics. Project cũ eventa-coursework được giữ nguyên.

## Luồng dữ liệu

Admin Java/XML lưu event/session trong Room/SQLite. Database v5 có publicId ổn định, nguồn dữ liệu riêng và số phiên bản. Trigger SQLite ghi nhận mọi thêm/sửa/xóa; WorkManager gửi lại khi có mạng, có lịch phục hồi 15 phút và nút Đồng bộ tất cả. Giao diện báo lỗi/quyền/trạng thái chờ; ghi cục bộ không phụ thuộc kết nối Cloud.

Một lần gửi ghi nguyên danh mục vào **catalogues/main**, gồm schemaVersion, sourceId, revision, publisherUid, events (session nằm trong event), updatedAt. Firestore transaction chặn bản cũ và nguồn khác ghi đè. Sửa/xóa event hoặc session được phản ánh nguyên tử; xóa event vẫn cascade trong SQLite. Không có luồng kéo Cloud về ghi đè Admin.

Explore Flutter lắng nghe Firestore trực tiếp, hiển thị cập nhật và cache offline. Không dùng JSON đóng gói làm dữ liệu production. Các ID yêu thích được giữ qua cập nhật, lỗi mạng và sự kiện tạm bị gỡ. Trang chi tiết đang mở cũng cập nhật; nội dung bị gỡ có thông báo và đường quay lại.

## Tài khoản và quyền

Email/Password đã bật. Trong Admin → Đồng bộ, người dùng tự tạo tài khoản hoặc đăng nhập; không gửi mật khẩu vào chat/Git. Tạo tài khoản không tự cấp quyền quản trị.

Chủ project xác nhận đúng UID của tài khoản được chỉ định rồi tạo document **admins/UID** với **enabled: true** bằng Firebase Console/owner API. Client không được tạo/sửa vai trò. Khi được cấp quyền, app tự thử đồng bộ lại. Tắt enabled thu hồi quyền ghi.

Người tham dự đọc được duy nhất catalogues/main; không liệt kê collection hoặc đọc vai trò người khác. Chỉ quản trị được duyệt mới ghi; không cho client xóa document danh mục. Danh mục rỗng biểu diễn xóa hết event. Mọi đường dẫn khác đều bị từ chối.

## Ảnh và giới hạn

Ảnh tùy chọn được nén thành JPEG data URL tối đa 32 KiB/ảnh, cạnh tối đa 640 px; ảnh gốc vẫn trên Admin. Không cần Firebase Storage hoặc gói trả phí. Không đọc được ảnh sẽ báo lỗi để chọn lại/bỏ ảnh, không âm thầm công bố thiếu ảnh.

shortcut: toàn bộ danh mục trong một document, tối đa 100 event, 500 session và JSON 512 KiB. Khi vượt giới hạn này, cần tách document và thiết kế lại việc lưu ảnh. App báo lỗi và giữ thay đổi trên thiết bị khi vượt giới hạn. Session cũ chưa gắn event phải được gắn trước khi công bố. Một bộ SQLite/một tài khoản xuất bản sở hữu danh mục; muốn đổi thiết bị cần phục hồi cùng bộ dữ liệu và tài khoản.

## Kiểm thử tách biệt

Dùng project giả **demo-eventa**, Firestore 8086/Auth 9098 và emulator Android thử nghiệm riêng. Không chạy test tạo/xóa dữ liệu trên máy chứa dữ liệu thật.

~~~text
cd cloud/firebase
npm ci
firebase emulators:start --only firestore,auth --project demo-eventa --config firebase.test.json
~~~

Ở cửa sổ khác: npm test kiểm tra rules. Native: build assembleDebug/assembleDebugAndroidTest rồi cài bằng adb -r và chạy CloudSyncTest, CloudCoverTest, CloudSyncWorkerTest, SessionDatabaseTest, EventDatabaseTest trên máy thử. Flutter:

~~~text
cd cw-user/app
flutter analyze
flutter test
flutter drive --driver=test_driver/integration_test.dart --target=integration_test/cloud_sync_test.dart -d emulator-5556 --keep-app-running
flutter drive --driver=test_driver/integration_test.dart --target=integration_test/app_test.dart -d emulator-5556 --keep-app-running
~~~

Không thêm --no-pub sau một build thông thường: integration_test cần plugin registrant riêng. Luôn cài lại entry lib/main.dart sau bài integration. Gradle connectedDebugAndroidTest và Flutter drive mặc định có thể gỡ app; chỉ dùng máy thử. Khi cập nhật app người dùng bằng adb -r, giữ chữ ký cũ và biến EVENTA_DEBUG_KEYSTORE nếu đã dùng bản MAUI.

## Triển khai project thật

~~~text
cd cloud/firebase
firebase deploy --only firestore:rules,firestore:indexes --project eventa-coursework-fpt --account YOUR_OWNER_EMAIL
~~~

Review rules trước khi deploy. Không dùng test mode hoặc allow read, write: if true. Các config client/project ID/API key Firebase là định danh công khai; không commit service account, khóa riêng, token CLI hoặc mật khẩu.

## Đã kiểm tra ngày 07/11/2026

- Rules: 5 test đạt, gồm người lạ, tự nâng quyền, thu hồi quyền, timestamp/schema, dữ liệu cũ/nguồn khác.
- Native: build, 14 unit test và lint đạt; 9 instrumentation test đạt trên máy thử Android 16, gồm migration v1–v4, CRUD/cascade, ID ổn định, revision chờ mạng, transaction và ảnh thu nhỏ.
- Flutter: analyze sạch, 11 logic/widget test đạt; integration Firestore SDK thật trên emulator đạt, gồm stream sửa/xóa, metadata cache, tắt/bật network và giữ yêu thích.
- Bản Admin trên máy người dùng đã nâng v4→v5: giữ nguyên 5 event, 9 session, nội dung các trường và 2 file ảnh gốc; ID demo cũ được giữ để tương thích yêu thích.
- Production project FPT: tài khoản do người dùng tự tạo đã được cấp quyền phía server. Admin báo Đã đồng bộ với Cloud; revision 1 có đủ 5 event, 9 session, 4 ảnh thu nhỏ. Đối chiếu từng trường công bố và quan hệ session với SQLite đều khớp; không còn lỗi chờ đồng bộ.
- Explore bản Android thông thường đã đọc danh mục production, hiển thị ảnh, lịch trình và chi tiết session. Cập nhật APK giữ đúng chữ ký và file yêu thích trước tác vụ.
- GitHub Actions CW Admin và CW User Flutter đều đạt trên mã đồng bộ (bao gồm emulator/rules, build Android/web và luồng G/H).
- Chưa cấu hình Firebase iOS/macOS/desktop hoặc kiểm tra tải lớn.

Nguồn: [Firebase Android](https://firebase.google.com/docs/android/setup), [Flutter](https://firebase.google.com/docs/flutter/setup), [Transactions](https://firebase.google.com/docs/firestore/manage-data/transactions), [Rules](https://firebase.google.com/docs/firestore/security/get-started), [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager).
