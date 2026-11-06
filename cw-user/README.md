# Eventa Explore — app người tham dự G/H

Ứng dụng **.NET MAUI Blazor Hybrid**, C# xử lý dữ liệu/tìm kiếm/yêu thích; Razor và CSS hiển thị giao diện đã chốt ở design/. App Android riêng: com.comp1786.eventa.user, không truy cập hoặc sửa database admin.

## Đã triển khai

- Danh sách, tìm tên có/không dấu, chọn ngày bằng lịch, lọc tháng/chủ đề, số kết quả và trạng thái rỗng.
- Chi tiết event, lịch session ngay trong event, chi tiết session chỉ đọc. Back phần cứng quay lại đúng luồng.
- Lưu/bỏ yêu thích từ thẻ hoặc chi tiết; tab Yêu thích có tìm/lọc. Lưu bằng MAUI Preferences, vẫn còn sau khi tắt/mở app.
- 5 event và 9 session theo snapshot của dữ liệu demo admin. Ảnh đóng gói trong APK nên xem được offline; event không ảnh không có khung trống.
- Giữ giao diện nền sáng, xanh nhấn, thẻ bo góc, sheet chọn chủ đề/ngày, thông báo trong app.

**Giới hạn G:** chưa có cloud E, nên dùng dữ liệu cục bộ theo phương án dự phòng đề bài cho phép. Thay đổi trong admin chưa tự xuất hiện ở app người tham dự. Không khai là đã đồng bộ cloud hoặc đạt đủ điểm G.

## Build Android

Cài .NET SDK theo global.json, workload maui-android, JDK và Android SDK 36.

~~~powershell
dotnet workload install maui-android --skip-manifest-update
dotnet run --project checks/Eventa.Checks.csproj
dotnet build app/Eventa.User.csproj -c Debug -p:AndroidSdkDirectory="C:\Users\YOUR_USER\AppData\Local\Android\Sdk" -p:JavaSdkDirectory="C:\Program Files\Android\Android Studio\jbr"
~~~

APK debug trong app/bin/Debug/net10.0-android/. Có thể mở project bằng Visual Studio hoặc VS Code với MAUI. Android Studio vẫn dùng để quản lý emulator và chạy app Java admin; không biên dịch MAUI bằng Gradle.

## Đọc code

- app/Features/Agenda/Ui: AgendaPage (khung/điều hướng), BrowsePanel, EventCard, EventPanel (lịch trong event), SessionPanel, FilterSheet.
- app/Features/Agenda/Logic: AgendaState quản lý luồng back và bộ lọc; EventSearch tìm/lọc thuần C#.
- app/Features/Agenda/Data: EventInfo/SessionInfo và CatalogRepository đọc asset. Resources/Raw/events.json là snapshot có ID ổn định, tách khỏi UI.
- app/Features/Favourites/Data/FavouritesStore.cs: đọc/ghi Preferences, chỉ thay trạng thái sau khi ghi thành công.
- app/Features/Favourites/Logic/FavouriteCodec.cs: kiểm tra JSON và loại ID không còn trong catalog.
- app/Shared/Ui: icon và nhãn dùng chung.
- app/wwwroot/app.css: kiểu giao diện; ui.js chỉ cuộn/focus, không chứa nghiệp vụ.
- checks: kiểm tra C# không cần emulator. design/ giữ nguyên bản HTML để đối chiếu.

Giờ session lấy nguyên từ admin; không tự thêm giờ kết thúc mà đề và database hiện chưa có. Các phiên Demo và ảnh Unsplash là minh họa. Nguồn sự kiện nằm trong events.json; không tự tạo email/điện thoại.

Tham khảo cấu trúc [MAUI Blazor template](https://github.com/dotnet/maui/tree/main/src/Templates/src/templates/maui-blazor) và [MAUI Preferences](https://learn.microsoft.com/dotnet/maui/platform-integration/storage/preferences).

## Kiểm tra trên emulator

Sau khi cài APK debug, chạy py checks/android_smoke.py --adb PATH_TO_ADB --output PATH_TO_SCREENSHOTS. Bài kiểm tra mở đúng app người tham dự, không xóa database của app nào; kiểm tra tìm/lọc, lịch chọn ngày, session và thêm/bỏ/giữ yêu thích sau khi khởi động lại. Để lại Microsoft Ignite trong Yêu thích cho demo. Chỉ chạy trên môi trường demo.
