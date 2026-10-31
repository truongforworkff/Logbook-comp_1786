# Eventa Admin — COMP1786, mục A

Ứng dụng Android native Java + XML Views, palette trắng ấm/hồng dịu theo bản thiết kế Eventa. Đây là project coursework riêng; các bài trong logbook vẫn được giữ.

## Clone và chạy

```sh
git clone --branch CW --single-branch https://github.com/truongforworkff/Logbook-comp_1786.git
```

Mở thư mục **cw-admin** bằng Android Studio, không mở logbook. Dùng JDK 17, SDK platform 36 và emulator/điện thoại Android 7.0 (API 24) trở lên. Android Studio tự tạo local.properties theo SDK trên máy bạn. Chọn module app và Run.

Windows: `gradlew.bat assembleDebug testDebugUnitTest lintDebug`
macOS/Linux: `./gradlew assembleDebug testDebugUnitTest lintDebug`
Kiểm tra database trên emulator đang chạy: `./gradlew connectedDebugAndroidTest`.

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

Lần bàn giao này tập trung mục A. Session, cloud, tìm kiếm nâng cao và app người tham dự chưa được đưa vào project này. Có mã test không đồng nghĩa mọi kiểm tra đã chạy: xem kết quả GitHub Actions và chạy instrumentation trên thiết bị trước khi khai hoàn thành coursework.

## Nguồn và cách dùng

Phát triển tiếp từ mã Java/Room trong logbook của repository này. Tham khảo: [Room](https://developer.android.com/training/data-storage/room), [Views](https://developer.android.com/develop/ui/views), [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel). Sinh viên cần hiểu mã, ghi nhận nguồn hỗ trợ và trình bày trung thực theo quy định coursework.
