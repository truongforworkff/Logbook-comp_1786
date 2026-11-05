# Đọc code Eventa Admin

Trong Android Studio, mở project cw-admin và chọn **Project** ở bảng thư mục để thấy cấu trúc vật lý.

## Luồng giao diện hiện tại

- Tab **Sự kiện**: danh sách, tìm/lọc event, tạo event.
- Mở event: thông tin event và **Lịch trình** nằm ngay dưới phần mô tả. Timeline chỉ hiển thị session thuộc event đó.
- Chạm một phiên trong lịch: mở màn chi tiết session chỉ đọc. Nút Chỉnh sửa riêng mới mở form.
- **Thêm session trong event**: form tự nhận event đang mở, không phải chọn lại.
- Tab **Session**: chọn event bằng danh sách tên + ngày, xem các session của event, nhấn + để tạo session. Chưa chọn event thì nút + bị khóa.
- Hai cách thêm session dùng chung form, validation và lệnh ghi SQLite.
- Nếu có session từ bản B chưa gắn event: chọn event đích rồi Gắn session cũ → kiểm tra → Lưu.

## Tính năng event

Thư mục gốc: app/src/main/java/com/comp1786/room/features/events/

- ui/list/MainActivity.java: danh sách, tìm/lọc và chuyển tab.
- ui/list/EventAdapter.java: thẻ event.
- ui/list/EventFilterDialog.java: bộ lọc.
- ui/details/EventDetails.java: thông tin và liên hệ của event.
- ui/details/EventSchedule.java: quan sát session theo eventId, vẽ timeline, mở chi tiết và thêm nhanh session. Bỏ observer khi đóng dialog.
- ui/editor/EventEditor.java: thêm/sửa, chọn ngày, review và xác nhận xóa event.
- logic/EventViewModel.java: ghi dữ liệu ngoài UI thread, trả kết quả lưu/xóa/lỗi/trùng.
- logic/EventValidation.java: quy tắc trường bắt buộc, ngày, email, điện thoại.
- data/model/Event.java: model và ràng buộc tên chuẩn hóa + ngày không trùng.
- data/local/EventDao.java: SQL event.
- data/local/EventDatabase.java: Room dùng chung events.db, migration và các DAO.

XML đi cùng:
- event_list_screen.xml ↔ MainActivity.
- event_list_item.xml ↔ EventAdapter.
- event_details_screen.xml ↔ EventDetails, chứa Lịch trình.
- event_schedule_item.xml ↔ EventSchedule, dòng timeline.
- event_editor_screen.xml ↔ EventEditor.

## Tính năng session

Thư mục gốc: app/src/main/java/com/comp1786/room/features/sessions/

- ui/list/SessionActivity.java: tab Session, chọn event, danh sách và điều hướng; cũng dùng cho luồng gắn session cũ.
- ui/list/SessionEventPicker.java: danh sách chọn event theo tên + ngày. Không truy cập database trực tiếp.
- ui/list/SessionAdapter.java: thẻ session trong màn quản lý.
- ui/details/SessionDetailsActivity.java: màn đọc đầy đủ ngày, giờ, loại, diễn giả, phòng, mô tả; theo dõi dữ liệu thay đổi.
- ui/editor/SessionEditorActivity.java: form dùng chung cho mọi điểm thêm/sửa. Các hàm setupForm, observeParent, loadExisting, fillForm, readForm, save, confirmDelete, restoreDraft chia theo công việc.
- logic/SessionViewModel.java: đọc dữ liệu qua DAO, kiểm tra và ghi ngoài UI thread.
- logic/SessionValidation.java: tên/giờ bắt buộc, giờ HH:mm, sáu loại session và giới hạn trường.
- data/model/Session.java: ID tự sinh, eventId, tên, giờ, diễn giả, phòng, loại, mô tả.
- data/local/SessionDao.java: SQL session, danh sách theo event, session chưa gắn và số lượng.

XML đi cùng:
- session_list_screen.xml ↔ SessionActivity và SessionEventPicker.
- session_list_item.xml ↔ SessionAdapter.
- session_details_screen.xml ↔ SessionDetailsActivity.
- session_editor_screen.xml ↔ SessionEditorActivity.
- session_strings.xml: nội dung chữ của tính năng.

Khi đổi event trong tab Session, bỏ observer của danh sách cũ trước khi theo dõi event mới. Lựa chọn event được lưu khi xoay màn hình.

## Dùng chung và kiểu giao diện

- shared/ui/AppUi.java: khung popup, banner, lỗi inline và thanh hệ thống.
- shared_popup.xml, shared_notice.xml: UI popup và thông báo.
- res/values/colors.xml: bảng màu chung; themes.xml: style; strings.xml: chữ chung.
- res/values/timeline_colors.xml: màu chấm và đường nối riêng của lịch trong event.
- res/drawable/timeline_dot.xml: chấm timeline.
- res/drawable: nền bo góc và icon; res/anim: hiệu ứng popup.
- Android yêu cầu layout nằm trực tiếp trong res/layout, nên dùng tiền tố tên file thay cho thư mục con.

## Database và luồng ghi

App admin hiện lưu cục bộ bằng Room/SQLite, chưa có server/API.

Thêm session: chọn event → SessionEditorActivity.readForm → SessionValidation → SessionViewModel.save → SessionDao.insert → SQLite → LiveData cập nhật danh sách và lịch trong event.

- events.db hiện ở version 3.
- Migration 1 → 2 tạo bảng sessions; 2 → 3 bổ sung eventId và khóa ngoại, giữ ID và các trường cũ.
- Session mới phải có eventId hợp lệ. Giá trị null chỉ dành cho session cũ đang chờ người dùng gắn event.
- Một event có nhiều session. Khóa ngoại ON DELETE CASCADE tự xóa session của event bị xóa, không ảnh hưởng event khác.
- Không dùng fallbackToDestructiveMigration.

## Kiểm thử và dữ liệu mẫu

- EventValidationTest, SessionValidationTest: kiểm tra đầu vào.
- EventDatabaseTest: lưu thông tin event và chặn trùng.
- SessionDatabaseTest: nâng cấp v1/v2, bảo toàn dữ liệu, CRUD, khóa ngoại và cascade.
- DesignScreenshotTest: các màn event, timeline trực tiếp, mở chi tiết session và thêm nhanh.
- SessionScreenTest: tab Session, chọn event, form đúng event, danh sách không lẫn event khác, màn đọc và CRUD.
- Bộ instrumentation có tạo/xóa dữ liệu mẫu: chỉ chạy toàn bộ trên emulator thử nghiệm riêng, không chạy trên máy có dữ liệu người dùng.
- demo-data/technology-events.json: sự kiện công nghệ có nguồn; phiên mang nhãn Demo là lịch minh họa. File không tự nạp vào app khi clone Git.

Quy tắc clean code áp dụng cho mọi task nằm ở AGENTS.md tại gốc repo.

## Mục D — tìm kiếm (10%)

- Event: ô tìm theo tên hoặc mô tả, khớp một phần từ khóa, không phân biệt hoa/thường và dấu tiếng Việt.
- Lọc nâng cao: ngày chính xác và người tổ chức; kết hợp với từ khóa theo AND. Popup có xóa bộ lọc/từ khóa.
- Session: tìm theo tên/mô tả trong lịch của event và trong tab Session sau khi chọn event. Không tìm lẫn event khác.
- Hiển thị số kết quả, trạng thái không có kết quả và nút xóa từ khóa session. Chạm kết quả mở đầy đủ chi tiết.
- EventSearch.java, SessionSearch.java: các hàm lọc riêng trong logic của từng tính năng.
- shared/logic/SearchText.java: chuẩn hóa tiếng Việt, chữ hoa/thường và khoảng trắng đầu/cuối.
- shared/ui/SessionSearchField.java + shared_session_search.xml: ô tìm dùng chung cho lịch và màn quản lý session.
- Dữ liệu lấy từ Room/SQLite qua LiveData rồi lọc trong bộ nhớ; cập nhật kết quả khi dữ liệu thay đổi. Không có thay đổi schema.
- Unit test kiểm tra tên/mô tả, từ khóa một phần, dấu tiếng Việt, tổ hợp ngày/người tổ chức, rỗng và ký tự đặc biệt.
- Kiểm thử UI kiểm tra tìm theo mô tả, lọc ngày/người tổ chức, xóa lọc, mở kết quả; tìm session không lẫn event khác.

Chọn ngày: shared/ui/DateField.java dùng chung cho ô ngày form event và bộ lọc. Chạm ô nhập mở lịch bo góc, không tự bật bàn phím; form vẫn có nút Chọn ngày. Bộ lọc cho phép xóa ngày riêng. Giá trị hiển thị dd/MM/yyyy và ghi database ISO như cũ; kiểm thử UI mở lịch từ cả hai ô, xác nhận ngày và xóa ngày lọc.
