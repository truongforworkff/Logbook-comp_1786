# Quy tắc làm việc cho Eventa Coursework

Áp dụng cho mọi task trong repository.

- Luôn clean code: chia thư mục theo tính năng; trong mỗi tính năng tách ui, logic và data.
- XML đặt tên theo màn/chức năng; định dạng dễ đọc. Ghi rõ file UI đi cùng file xử lý trong cw-admin/CODE_MAP.md.
- Tách hàm theo một công việc cụ thể: khởi tạo, tải dữ liệu, đọc form, kiểm tra, lưu, xóa, hiển thị kết quả.
- Đặt tên rõ nghĩa; tránh nhồi logic vào onCreate hoặc viết nhiều thao tác trên một dòng.
- Tái sử dụng thành phần sẵn có, không thêm lớp/dependency hoặc tính năng chưa cần.
- Giữ validation và xử lý lỗi; không làm mất dữ liệu khi thay đổi schema. Migration phải có kiểm thử.
- Mỗi thay đổi phải cập nhật các nơi gọi, kiểm thử và tài liệu liên quan. Chạy build và kiểm thử phù hợp trước khi báo hoàn thành.
- Theo luồng nghiệp vụ: một event có nhiều session; session mới được tạo từ event và thuộc duy nhất event đó.
- Chỉ chạy bộ instrumentation có tạo/xóa dữ liệu mẫu trên emulator thử nghiệm riêng; không chạy toàn bộ trên máy chứa dữ liệu người dùng.
