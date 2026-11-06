# Thiết kế app người tham dự — G và H

Bản HTML trong design/ là giao diện tham chiếu đã chốt. Bản ứng dụng hiện tại được viết bằng Dart/Flutter trong app/, giữ cùng phong cách và luồng màn hình.

## Phong cách

- Nền xám rất sáng #F4F5F9, thẻ trắng, viền mảnh #E8EBF2.
- Nhấn xanh #087DFF, tiêu đề đậm, thông tin phụ xám.
- Thẻ event có ảnh bìa tùy chọn, nhãn chủ đề, trái tim và thông tin địa điểm gọn.
- Không có ảnh thì dùng phần đầu thẻ thấp, không để khung ảnh trống.
- Timeline trong chi tiết event: đường mảnh/chấm xanh xám, giờ và tên phiên rõ; chạm mở chi tiết chỉ đọc.
- Hai tab chính: Khám phá và Yêu thích. Điều khiển ngày/chủ đề dùng lịch và sheet bo góc.

## Các màn

1. Khám phá: tìm tên, lọc ngày/tháng/chủ đề, số kết quả, trạng thái rỗng.
2. Event: ảnh, ngày/địa điểm/đơn vị, giới thiệu, timeline session và lưu yêu thích.
3. Session: tên, giờ bắt đầu, ngày, phòng, diễn giả, mô tả và quay lại lịch.
4. Yêu thích: danh sách đã lưu, tìm/lọc và trạng thái chưa lưu gì.

## File và bản chạy

- design/index.html, styles.css, app.mjs: prototype HTML.
- design/data và design/logic: dữ liệu/logic riêng của prototype.
- app/lib/features: UI, logic và data của bản Flutter. Xem README.md để đọc code.
- Chạy HTML: py cw-user/preview.py → http://127.0.0.1:4180/.
- Chạy Flutter: cd cw-user/app rồi flutter run.

Nguồn dữ liệu, lịch Demo và ảnh minh họa được ghi rõ trong JSON/tài liệu. App dùng snapshot 5 event/9 session của admin; chưa đồng bộ cloud E. Chủ đề khác với hình thức tổ chức; giờ kết thúc chưa có trong backend nên không tự suy diễn từ lịch thiết kế.
