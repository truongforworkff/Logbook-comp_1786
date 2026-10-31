# Đọc mã Eventa Admin theo chức năng

Trong Android Studio, chọn chế độ **Project** thay cho **Android** ở đầu bảng thư mục nếu muốn thấy cấu trúc vật lý. Mở project **cw-admin**.

## Thư mục Java

```text
app/src/main/java/com/comp1786/room/
├── features/events/
│   ├── ui/
│   │   ├── list/
│   │   │   ├── MainActivity.java        Màn danh sách, nhận thao tác và cập nhật màn hình
│   │   │   ├── EventAdapter.java        Hiển thị từng thẻ event
│   │   │   └── EventFilterDialog.java   Popup bộ lọc
│   │   ├── details/EventDetails.java    Màn chi tiết
│   │   └── editor/EventEditor.java      Form thêm/sửa, chọn ngày, review và xác nhận xóa
│   ├── logic/
│   │   ├── EventViewModel.java          Nhận lệnh lưu/xóa, chạy ghi ngoài UI thread, trả kết quả
│   │   └── EventValidation.java         Quy tắc dữ liệu và chuyển định dạng ngày
│   └── data/
│       ├── model/Event.java            Các trường event và unique index tên/ngày
│       └── local/
│           ├── EventDao.java           Câu SQL đọc/thêm/sửa/xóa
│           └── EventDatabase.java      Khởi tạo Room/SQLite, tên database
└── shared/ui/AppUi.java                 Khung popup, banner, lỗi inline và thanh hệ thống
```

## File giao diện XML

Android yêu cầu các file layout nằm trực tiếp trong `res/layout`; vì vậy các file được đặt tên theo chức năng thay vì tạo thư mục con.

| File trong app/src/main/res/layout | Mã Java điều khiển |
|---|---|
| event_list_screen.xml | ui/list/MainActivity.java |
| event_list_item.xml | ui/list/EventAdapter.java |
| event_details_screen.xml | ui/details/EventDetails.java |
| event_editor_screen.xml | ui/editor/EventEditor.java |
| shared_popup.xml | shared/ui/AppUi.java; được editor và bộ lọc sử dụng |
| shared_notice.xml | shared/ui/AppUi.java |

Màu: `res/values/colors.xml`. Font/style: `res/values/themes.xml`. Nội dung chữ: `res/values/strings.xml`. Nền bo góc/icon: `res/drawable`. Hiệu ứng popup: `res/anim`.

## File frontend và phần xử lý dữ liệu cho từng việc

| Việc cần đọc/sửa | Frontend Java + XML | Logic và dữ liệu liên quan |
|---|---|---|
| Danh sách event | MainActivity + EventAdapter; event_list_screen + event_list_item | EventViewModel.getEvents → EventDao.observeAll |
| Xem chi tiết | EventDetails; event_details_screen | Nhận model Event đã đọc từ Room |
| Thêm event | EventEditor.show; event_editor_screen; shared_popup để review | EventValidation → EventViewModel.save → EventDao.insert |
| Sửa event | EventEditor.show với event có ID; cùng form editor | EventValidation → EventViewModel.save → EventDao.update |
| Xóa event | EventEditor, popup xác nhận dùng shared_popup | EventViewModel.delete → EventDao.delete |
| Chọn ngày | EventEditor mở lịch trong shared_popup | EventValidation.displayDate/storageDate; giữ ngày trong draft trước khi lưu |
| Lỗi trường nhập | EventEditor gọi AppUi.inlineValidation | EventValidation cho biết giá trị hợp lệ hay không |
| Chặn trùng | EventEditor và banner AppUi khi nhận DUPLICATE | Unique index trong Event; EventViewModel bắt lỗi constraint |
| Tìm/lọc danh sách hiện tại | MainActivity.renderEvents + EventFilterDialog | Lọc danh sách Event đã được Room cung cấp; không gọi server |
| Thông báo lưu/xóa/lỗi | MainActivity nhận kết quả, AppUi hiển thị shared_notice | EventViewModel.getOperation |

## Một luồng để đọc từ đầu tới cuối

**Thêm event:** nhấn + trong MainActivity → EventEditor.show → nhập form event_editor_screen → kiểm tra EventValidation → review bằng shared_popup → EventViewModel.save → EventDao.insert → SQLite → LiveData cập nhật MainActivity → EventAdapter vẽ lại thẻ → shared_notice báo thành công.

**Sửa/xóa:** bắt đầu từ thẻ trong MainActivity → EventDetails → EventEditor. Các lệnh ghi đều đi qua EventViewModel và EventDao.

## Backend ở đâu?

Bản mục A hoạt động offline. Phần tương đương xử lý backend cục bộ nằm ở `features/events/logic` và `features/events/data`. Hiện project này **chưa có server/API**. Không cần tạo một server chỉ để lưu event local.

Khi làm session (B–C), đặt tính năng tại `features/sessions` với cùng ba nhóm ui/logic/data. Khi làm cloud (E), bổ sung service/API và phần gọi mạng theo yêu cầu thực tế; chưa tạo các thư mục rỗng cho tính năng chưa làm.

## Kiểm thử và dữ liệu

- `app/src/test/.../features/events/logic/EventValidationTest.java`: validation ngày/contact và chuẩn hóa tên.
- `app/src/androidTest/.../features/events/data/local/EventDatabaseTest.java`: lưu dữ liệu và unique constraint.
- `app/src/androidTest/.../features/events/ui/DesignScreenshotTest.java`: chạy giao diện trên emulator, chụp ảnh các màn/popup.
- Test screenshot tạo/xóa dữ liệu mẫu trong app test; chỉ chạy trên emulator thử nghiệm riêng, không chạy trên thiết bị có dữ liệu bạn muốn giữ.
- Giữ nguyên applicationId, tên file `events.db`, schema version 1 và cấu trúc bảng trong lần tổ chức mã này. Việc đổi package Java không phải yêu cầu xóa dữ liệu ứng dụng.
