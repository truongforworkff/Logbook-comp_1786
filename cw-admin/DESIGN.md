# Giao diện Eventa theo bản HTML

Bản Android dùng bố cục của khung điện thoại trong bản HTML đã thống nhất:
- Nền trắng ấm, chữ mận đậm, điểm nhấn hồng trầm; khoảng cách chính 24dp.
- Header logo Eventa và avatar vai trò quản trị.
- Tiêu đề và lời dẫn, ô tìm kiếm/bộ lọc trên cùng một hàng.
- Thẻ có ngày/tháng bên trái, tên và địa điểm bên phải, nhãn trạng thái và liên kết chi tiết ở chân thẻ.
- Nút thêm dạng + nổi, thanh chân trang nhẹ.
- Chi tiết toàn màn hình: hero hồng nhạt, ngày/địa điểm, phần mô tả và liên hệ.
- Form toàn màn hình: 4 trường cốt lõi trước; loại, trạng thái, mô tả và liên hệ trong phần mở rộng. Review trước lưu vẫn giữ nguyên.

Dữ liệu thật được lấy từ Room. Giao diện mục A không hiển thị số session hoặc nút đồng bộ chưa triển khai. Tìm tên và lọc ngày/organiser hoạt động trên danh sách local, không cần mạng.

Mở cw-admin bằng Android Studio, dùng nhánh CW. Workflow tạo APK, chạy validation/database tests và xuất screenshot giao diện từ Android emulator.
