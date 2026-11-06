# Thiết kế app người tham dự — G và H

Đây là bản HTML tương tác để duyệt giao diện trước khi triển khai app hybrid. Chưa phải app MAUI và chưa kết nối cloud.

## Phạm vi

- G: khám phá danh sách sự kiện, tìm theo tên/ngày, đọc chi tiết event và lịch session.
- H: lưu/bỏ yêu thích, trang yêu thích, trạng thái rỗng. Prototype lưu riêng trong localStorage của trình duyệt.
- Điều hướng chính chỉ có Khám phá và Yêu thích. Không có chức năng quản trị trong luồng người dùng.

## Phong cách theo ảnh tham chiếu

- Nền xám rất sáng #F4F5F9, thẻ trắng, đường viền mảnh #E8EBF2.
- Màu nhấn xanh #087DFF, tiêu đề đen, thông tin phụ xám.
- Ảnh bìa minh họa, nhãn loại nhỏ, nút trái tim ở góc phải, thông tin địa điểm gọn.
- Timeline trong chi tiết event; mỗi phiên mở màn chi tiết chỉ đọc.
- Vùng bấm và trạng thái focus, nhãn accessibility, thông báo aria-live.

## Các màn

1. Khám phá: tên/ngày, bộ lọc tháng, kết quả rỗng, thẻ sự kiện.
2. Event: ảnh bìa, ngày/địa điểm/đơn vị, giới thiệu, timeline, lưu yêu thích.
3. Session: tên, khung giờ, phòng, diễn giả, nội dung, quay lại lịch.
4. Yêu thích: danh sách đã lưu, tìm/lọc và trạng thái chưa có mục nào.

## Chia file

- design/index.html: khung prototype và hộp chọn ngày.
- design/styles.css: màu, bố cục và responsive.
- design/app.mjs: render màn, điều hướng, tương tác và lưu yêu thích.
- design/data/events.mjs: dữ liệu minh họa tách khỏi giao diện.
- design/logic/search.mjs: tìm tên/ngày, lọc yêu thích, kiểm tra dữ liệu lưu.
- design/logic/search.test.mjs: kiểm tra logic bằng Node assert.

Chạy local: py cw-user/preview.py, mở http://127.0.0.1:4180/. Preview server đặt MIME JavaScript module rõ ràng để chạy đúng trên Windows. Kiểm tra: node design/logic/search.test.mjs.

Ảnh bìa từ Unsplash chỉ là ảnh minh họa, không phải ảnh của sự kiện. Tên Microsoft Ignite và NVIDIA GTC dùng theo bộ dữ liệu của app quản trị; lịch phiên và các sự kiện cộng đồng còn lại là minh họa. Prototype có ghi nhãn Bản mẫu.

Khi triển khai G–H sau khi duyệt: dùng .NET MAUI/hybrid theo đề; thay dữ liệu mẫu bằng cloud service của E, có trạng thái tải/lỗi/offline rõ ràng. Giờ kết thúc trong prototype là dữ liệu thiết kế, cần thống nhất với hợp đồng dữ liệu thực tế trước khi triển khai.

## Chủ đề và ảnh tùy chọn

- Bộ lọc chủ đề có sheet riêng, kết hợp với tên/ngày/tháng và áp dụng cả danh sách Yêu thích.
- Chủ đề tách khỏi hình thức session/event. Mã dùng chung với admin: Other, AI, Technology, Design, Business, Education, Community.
- Thẻ sự kiện có ảnh dùng cover; không có ảnh dùng phần đầu gọn chứa chủ đề, nút yêu thích và thời gian, không để khoảng ảnh trống. Event Build with AI minh họa trường hợp này.
- Chi tiết không có ảnh bắt đầu từ chủ đề và tên. Ảnh lỗi được ẩn.
- Người tham dự chỉ xem ảnh do người tổ chức cung cấp. Chọn/thay/xóa ảnh thuộc form admin.
- Đây vẫn là bản thiết kế HTML, chưa triển khai app MAUI hay đồng bộ ảnh cloud.

## Triển khai theo thiết kế

Mã MAUI Blazor nằm trong app/, hướng dẫn và bản đồ file ở README.md. App dùng snapshot cục bộ của 5 event/9 session trong bộ dữ liệu quản trị, có ảnh offline và MAUI Preferences cho yêu thích. Prototype HTML vẫn được giữ để đối chiếu; cloud E chưa triển khai.
