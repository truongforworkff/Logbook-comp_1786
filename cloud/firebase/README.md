# Firebase — đồng bộ Eventa

Project **eventa-coursework-fpt**, số **1011104656179**, gói Spark. Firestore Standard tại asia-southeast1. Không bật thanh toán, Storage hoặc Functions.

## Sử dụng

Admin không có tài khoản, đăng ký, đăng nhập hoặc phân quyền theo người dùng; không dùng Firebase Authentication, kể cả đăng nhập ẩn danh/ngầm. Tab Đồng bộ chỉ hiển thị trạng thái, lần thành công gần nhất và nút Đồng bộ tất cả. Thêm/sửa/xóa được ghi nhận và gửi tự động khi có mạng.

SQLite vẫn là nguồn dữ liệu của Admin. WorkManager giữ hàng đợi/thử lại và lịch phục hồi 15 phút. Event/session được công bố nguyên tử trong catalogues/main; sourceId và revision chặn dữ liệu cũ hoặc bộ SQLite khác ghi đè. Cloud không kéo về ghi đè dữ liệu Admin.

Explore Flutter tiếp tục đọc Firestore trực tiếp, có cache và cập nhật trang chi tiết. Gỡ session/event được phản ánh trên app người tham dự. ID yêu thích giữ nguyên. Cấu trúc danh mục gồm schemaVersion, sourceId, revision, events (chứa session) và updatedAt; không còn publisherUid.

## Cấu hình dành cho bản Admin

Quyền ghi dùng khóa kết nối riêng của bản Admin, không dùng người dùng/mật khẩu. Khóa này chỉ cho phép công bố danh mục và biên nhận đồng bộ, không cấp quyền quản lý project.

File **cw-admin/sync.properties** chứa thuộc tính **writeKey** là 64 ký tự hex ngẫu nhiên, được Git bỏ qua. Khi build cũng có thể đặt biến môi trường **EVENTA_SYNC_WRITE_KEY**. Không commit khóa vào Git, không đưa khóa hoặc service account vào google-services.json. Bản build không có cấu hình vẫn quản lý cục bộ và báo chưa cấu hình khi đồng bộ; không gửi dữ liệu công khai.

Chủ project cấu hình cùng khóa trong document private **sync_config/publisher**, trường **writeKey**, bằng công cụ owner. Không cần tạo Firebase Auth user hay document admins/UID. Máy clone mới cần sao chép file cấu hình riêng qua kênh riêng trước khi build để kết nối project này; APK CI không chứa khóa production.

CloudPublisher gửi danh mục cùng **sync_writes/current** trong một transaction. Biên nhận có khóa, sourceId, revision và server timestamp. Rules kiểm tra getAfter(), cùng revision/source và thời điểm để buộc hai document được ghi nguyên tử. Danh mục public không chứa khóa; client không đọc được cấu hình hoặc biên nhận, không liệt kê/xóa danh mục và không ghi các đường dẫn khác. Các vai trò tài khoản cũ không còn có quyền ghi.

shortcut: khóa nằm trong bản cài Admin và có thể bị trích xuất từ APK. Chỉ phân phối APK đã cấu hình cho người được phép đồng bộ. Khi cần phân phối rộng/thu hồi theo từng thiết bị, chuyển quyền ghi sang backend hoặc cơ chế chứng thực thiết bị. Đây là giới hạn của bản coursework không có hệ thống tài khoản.

## Giới hạn dữ liệu

Một document chứa toàn bộ danh mục: tối đa 100 event, 500 session và JSON 512 KiB. Ảnh tùy chọn được nén JPEG <=32 KiB/ảnh, cạnh tối đa 640 px; ảnh gốc giữ trên Admin. Khi vượt giới hạn cần tách document và cơ chế ảnh. Ảnh lỗi hoặc session cũ chưa gắn event sẽ được báo để sửa, không bị âm thầm bỏ khỏi dữ liệu gửi đi.

## Kiểm thử

Chỉ dùng project demo-eventa và máy ảo thử riêng cho test tạo/xóa dữ liệu.

~~~text
cd cloud/firebase
npm ci
firebase emulators:start --only firestore --project demo-eventa --config firebase.test.json
~~~

Ở cửa sổ khác chạy npm test. Sáu bài kiểm tra bao gồm đồng bộ không tài khoản, sai/thiếu khóa, giữ khóa private, tính nguyên tử, chống dữ liệu cũ, giới hạn schema và thay cấu hình kết nối.

Admin: assembleDebug, assembleDebugAndroidTest, testDebugUnitTest và lintDebug. CloudSyncTest sử dụng Firestore emulator, không tạo tài khoản; kiểm tra CRUD/cascade, revision, mất mạng và nguồn dữ liệu khác. CloudSyncWorkerTest kiểm tra phân loại lỗi.

Flutter giữ các kiểm tra analyze, test, integration_test/app_test.dart và integration_test/cloud_sync_test.dart. Khi drive cần --keep-app-running; cài lại entry lib/main.dart sau integration test. Không chạy Gradle connectedDebugAndroidTest trên máy có dữ liệu vì có thể tự gỡ app.

## Triển khai

~~~text
cd cloud/firebase
firebase deploy --only firestore:rules,firestore:indexes --project eventa-coursework-fpt --account YOUR_OWNER_EMAIL
~~~

Cấu hình private trước khi cài bản Admin mới. Không mở allow write: if true. Khi đổi khóa, cập nhật cấu hình owner và build lại Admin. Không cần sửa app Explore.

## Xác minh

- Bản bỏ Authentication: build, lint và 14 unit test đạt; 6 rules test và 3 instrumentation test đồng bộ đạt trên máy ảo thử riêng.
- Đã kiểm tra APK không chứa Firebase Authentication SDK và mã production không còn đăng nhập/đăng ký/vai trò tài khoản. Khóa build không xuất hiện trong file được Git theo dõi.
- Trên máy ảo người dùng: phiên đăng nhập cũ đã được gỡ, màn Đồng bộ chỉ còn trạng thái và một nút gửi tất cả. Đồng bộ production không cần tài khoản đã đạt; dữ liệu vẫn đủ 5 event, 9 session, 4 ảnh bìa, giữ SQLite/yêu thích và không công bố khóa kết nối.
- Firebase hiện cấu hình Android/web; chưa kiểm thử iOS/desktop.

Nguồn: [Firestore transaction](https://firebase.google.com/docs/firestore/manage-data/transactions), [Security Rules getAfter](https://firebase.google.com/docs/reference/rules/rules.firestore).
