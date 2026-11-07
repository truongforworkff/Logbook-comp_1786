# Eventa Admin — COMP1786, mục A–E

Ứng dụng Android native Java + XML Views, palette trắng ấm/hồng dịu theo bản thiết kế Eventa. Đây là project coursework riêng; các bài trong logbook vẫn được giữ.

## Đọc và sửa mã

Xem [bản đồ thư mục và luồng từng chức năng](CODE_MAP.md). UI nằm tại `features/events/ui`, xử lý tại `logic`, SQLite tại `data`. Các layout XML được đặt tên theo màn hình trong `res/layout`.

## Clone và chạy

```sh
git clone --branch CW --single-branch https://github.com/truongforworkff/Logbook-comp_1786.git
```

Mở thư mục **cw-admin** bằng Android Studio, không mở logbook. Dùng JDK 17, SDK platform 36 và emulator/điện thoại Android 7.0 (API 24) trở lên. Android Studio tự tạo local.properties theo SDK trên máy bạn. Chọn module app và Run.

Windows: `gradlew.bat assembleDebug testDebugUnitTest lintDebug`
macOS/Linux: `./gradlew assembleDebug testDebugUnitTest lintDebug`
Chỉ kiểm tra database trên emulator thử riêng (lệnh này có thể tự gỡ app): `./gradlew connectedDebugAndroidTest`.

APK debug ở app/build/outputs/apk/debug/app-debug.apk. Workflow GitHub CW Admin kiểm tra build/unit test/lint; kiểm thử database trên thiết bị là bài instrumentation riêng.

## Mục A đã viết trong mã

- Danh sách thẻ gọn: tên, ngày, địa điểm và trạng thái; bấm thẻ để xem đầy đủ.
- Form thêm/sửa: tên, ngày, địa điểm, người tổ chức, loại, trạng thái.
- Mô tả, email, điện thoại tùy chọn; để trống không báo lỗi.
- Ngày DD/MM/YYYY được kiểm tra nghiêm ngặt; lưu ISO để sắp xếp theo thời gian.
- Email có kiểm tra cấu trúc; điện thoại cho phép dấu +, khoảng trắng/dấu phân cách và 7–15 chữ số.
- Review đủ thông tin trước khi lưu; quay lại sửa giữ draft.
- Room/SQLite lưu cục bộ, thao tác ghi ngoài UI thread. Unique index tên chuẩn hóa/ngày chặn duplicate ngay tại database, kể cả bấm nhanh.
- Sửa giữ ID, xóa có xác nhận; lỗi lưu giữ form để thử lại.
- Draft phục hồi khi Activity được tái tạo.

Project dùng applicationId com.comp1786.eventa.admin và database riêng trong sandbox app. Xóa app/clear data sẽ xóa dữ liệu cục bộ.

## Kiểm tra thủ công trước demo

1. Tạo event với đủ trường, xem review, quay lại sửa rồi lưu.
2. Để trống lần lượt các trường bắt buộc; thử 31/04/2026, email sai và phone chỉ có dấu gạch.
3. Tạo tên trùng có khác chữ hoa/khoảng trắng cùng ngày: phải báo duplicate; khác ngày: được lưu.
4. Sửa chính event đó rồi lưu: không tự báo duplicate.
5. Đóng/mở app, xoay màn hình khi có draft và thử chữ lớn/màn hình nhỏ.
6. Xem chi tiết, hủy xóa rồi xác nhận xóa.

## Phạm vi

App quản trị hiện triển khai A–D: event, session, liên kết và tìm kiếm. Có bổ sung chủ đề/ảnh bìa tùy chọn. Cloud E đồng bộ toàn bộ/thay đổi và thử lại khi có mạng; không có tài khoản hoặc đăng nhập; app người tham dự G/H nằm riêng trong cw-user và đọc Cloud. Có mã test không đồng nghĩa mọi kiểm tra đã chạy: xem kết quả GitHub Actions và chạy instrumentation trên thiết bị trước khi khai hoàn thành coursework.

## Nguồn và cách dùng

Phát triển tiếp từ mã Java/Room trong logbook của repository này. Tham khảo: [Room](https://developer.android.com/training/data-storage/room), [Views](https://developer.android.com/develop/ui/views), [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel). Sinh viên cần hiểu mã, ghi nhận nguồn hỗ trợ và trình bày trung thực theo quy định coursework.

Mục B: Chi tiết event → Session hỗ trợ thêm/sửa/xóa nhiều session trong SQLite; ID tự sinh, giờ HH:mm, sáu loại và các trường tùy chọn. Database v1 → v2 có migration giữ event cũ. Chi tiết file/hàm xem CODE_MAP.md. Mục C đã liên kết session theo từng event, có số lượng và cascade delete.

Mục C: mở Chi tiết event → Session. Session mới tự thuộc event đang mở. Với session tạo ở bản B, mở event đích → Gắn session cũ → chọn session → Lưu. Migration v1/v2 lên v3 giữ dữ liệu. Quy tắc clean code áp dụng cho mọi task: AGENTS.md.

Mục D: tìm event theo tên/mô tả; lọc ngày/người tổ chức; tìm session theo tên/mô tả trong event và tab Session. Hỗ trợ hoa/thường, tiếng Việt có/không dấu, số kết quả, trạng thái rỗng và mở đầy đủ chi tiết. Dữ liệu đọc từ SQLite qua Room; không đổi schema.

### Chủ đề và ảnh sự kiện

Chọn chủ đề trong form tạo/sửa. Mở phần tùy chọn để thêm, thay hoặc bỏ ảnh bìa. Có thể tạo sự kiện không có ảnh. Danh sách/chi tiết hiển thị ảnh và chủ đề; bộ lọc bổ sung chủ đề.

Database v4 nâng cấp giữ nguyên event/session. Dữ liệu cũ mặc định chủ đề Khác, ảnh trống. Ảnh dùng URI cục bộ có quyền đọc lâu dài; ảnh thu nhỏ được gửi lên Cloud; URI gốc có thể mất truy cập nếu ảnh gốc bị xóa/di chuyển.

## Firebase Spark

Đã cấu hình project eventa-coursework-fpt, SDK Firestore và Google Services plugin. SQLite vẫn là nguồn dữ liệu. Tab Đồng bộ chỉ có trạng thái và gửi toàn bộ. Bản Admin dùng cấu hình kết nối riêng trong sync.properties hoặc EVENTA_SYNC_WRITE_KEY; cấu hình không đưa lên Git. Không dùng Firebase Authentication, đăng nhập ngầm hoặc vai trò tài khoản. WorkManager ghi nhận/sửa/xóa và gửi lại khi có mạng. Migration v5 giữ dữ liệu và ID yêu thích cũ. Xem [thiết lập và kiểm tra kết nối](../cloud/firebase/README.md).

### Đồng bộ tự động

Không cần bấm nút Đồng bộ tất cả cho mỗi lần sửa. Khi Room ghi thay đổi event/session, app tự xếp việc gửi có điều kiện mạng. Mất mạng vẫn lưu SQLite; có mạng WorkManager tiếp tục gửi. Nút Đồng bộ tất cả dùng khi muốn yêu cầu gửi lại. Android quản lý lịch chạy nền và có thể trì hoãn để tiết kiệm pin; Force stop cần mở lại app.
