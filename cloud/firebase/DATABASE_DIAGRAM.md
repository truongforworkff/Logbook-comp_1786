# Database và phân quyền — nhánh CW-auth

Bản có tài khoản nằm ở nhánh CW-auth. Nhánh CW vẫn giữ bản coursework không có đăng nhập.

## Sơ đồ

```mermaid
erDiagram
    AUTH_USER ||--o| USER_PROFILE : "có hồ sơ"
    AUTH_USER ||--o| ROLE : "được chủ project cấp"
    USER_PROFILE ||--o{ FAVOURITE : "lưu event"
    USER_PROFILE ||--o{ SCHEDULE_ITEM : "chọn session"
    AUTH_USER ||--o{ CATALOG : "Admin công bố"
    CATALOG ||--o{ EVENT : "chứa mảng events"
    EVENT ||--o{ SESSION : "chứa mảng sessions"
    FAVOURITE }o--|| EVENT : "tham chiếu eventId"
    SCHEDULE_ITEM }o--|| SESSION : "tham chiếu eventId và sessionId"
    AUTH_USER {
        string uid PK
        string email
        string displayName
        string provider "password hoặc google.com"
    }
    USER_PROFILE {
        string uid PK
        string displayName
        string email
        timestamp updatedAt
    }
    ROLE {
        string uid PK
        string role "admin"
    }
    FAVOURITE {
        string selectionId PK
        string eventId FK
        timestamp updatedAt
    }
    SCHEDULE_ITEM {
        string selectionId PK
        string eventId FK
        string sessionId FK
        timestamp updatedAt
    }
    CATALOG {
        string path PK "role_catalogues/main"
        string sourceId
        int revision
        string updatedBy FK
        timestamp updatedAt
    }
    EVENT {
        string id PK
        string name
        string date
        string categoryCode
        string image
    }
    SESSION {
        string id PK
        string eventId FK "quan hệ chứa trong event"
        string name
        string time
        string endTime
    }
```

Đây là sơ đồ logic. Firestore là NoSQL: EVENT/SESSION là các phần tử trong mảng của document danh mục, không phải bảng SQL riêng. Firebase Authentication quản lý mật khẩu và token; không lưu mật khẩu trong Firestore.

## Đường dẫn dữ liệu

- Firebase Authentication: tài khoản có UID ổn định, email/password hoặc Google.
- roles/{uid}: document do chủ project quản lý; role: admin. Không có document này nghĩa là quyền User. Client không có quyền tự tạo/sửa role.
- users/{uid}: hồ sơ hiển thị email, displayName, updatedAt. Không chứa role.
- users/{uid}/favourites/{selectionId}: eventId, updatedAt.
- users/{uid}/schedule/{selectionId}: eventId, sessionId, updatedAt.
- role_catalogues/main: danh mục của CW-auth, cấu trúc event/session giữ nguyên, thêm updatedBy là UID Admin.
- selectionId là base64url của JSON [eventId] hoặc [eventId, sessionId]; cùng lựa chọn dùng lại cùng ID, tránh nhân bản.
- ID tham chiếu được giữ khi event/session tạm bị xóa; UI chỉ hiển thị bản ghi còn trong danh mục. Khi Admin cập nhật giờ, cảnh báo trùng lịch tính lại từ dữ liệu mới.

## Quyền

- Chưa đăng nhập: chỉ thấy màn đăng nhập của CW-auth.
- User: đăng ký Email/Password hoặc đăng nhập Google, xem danh mục; chỉ đọc/ghi lịch và yêu thích của chính UID.
- Admin: đăng nhập app quản trị bằng tài khoản được cấp quyền; CRUD SQLite, công bố và tự đồng bộ danh mục. Có thể dùng app User như người tham dự.
- Firestore Rules kiểm tra UID cho mọi truy cập dữ liệu riêng, kiểm tra roles/{uid} khi công bố. Thay role trong request/hồ sơ không nâng quyền.
- App Admin chỉ có đăng nhập, không có tự đăng ký Admin.
- Dữ liệu SQLite Admin vẫn là một bộ dữ liệu công bố chung, không tách database riêng theo từng Admin. Ràng buộc sourceId/revision chống đè nhầm bản Cloud vẫn giữ nguyên.

## Lưu và đồng bộ

Mỗi lựa chọn là một document riêng. Hai thiết bị thêm hai session khác nhau không ghi đè cả danh sách. Hai thiết bị sửa cùng lựa chọn: Firestore nhận thao tác sau cùng.

Firestore SDK lưu cache và hàng đợi khi offline. App hiển thị trạng thái đang chờ Cloud; không coi ghi cache là đã đồng bộ. Đăng xuất chờ các thao tác đang gửi được xác nhận, tránh mất thay đổi khi đổi tài khoản.

Tài khoản mới không tự nhận lịch chung cũ trên máy. Trong Tài khoản có nút Nhập lịch & yêu thích cũ từ máy, xác nhận email đích trước khi nhập. Dữ liệu cũ không bị xóa.

## Tách bản CW

catalogues/main và sync_writes/current thuộc bản CW cũ; quyền theo khóa công bố của bản đó giữ nguyên. CW-auth chỉ đọc/ghi role_catalogues/main và các collection tài khoản, không dùng khóa ghi nhúng APK. Hai danh mục độc lập để chạy/so sánh hai nhánh; chỉnh bản CW không tự cập nhật danh mục CW-auth.

## Cấp Admin

Email được người dùng chọn: truongtdgch210108@fpt.edu.vn. Chủ project tra UID trong Authentication và tạo roles/{uid} với role: admin bằng Firebase Console/Owner API. Không cấp quyền theo email được nhập vào UI, không cho client nâng quyền.

Để thu hồi: xóa document role hoặc đổi giá trị role khác admin. Rules chặn lần ghi tiếp theo; app Admin theo dõi thay đổi và đưa về đăng nhập.

## Cấu hình Google

Bật Google provider và Email/Password trong Authentication, đăng ký SHA-1/SHA-256 của chứng thư ký APK User trong Firebase. Web OAuth client ID là cấu hình công khai, không phải client secret. Máy clone ký bằng chứng thư khác phải đăng ký SHA của máy đó. Không đưa keystore, token CLI hoặc client secret vào Git.

## Trạng thái cấu hình và kiểm thử

Ngày 20/11/2026, project `eventa-coursework-fpt` đã bật Email/Password và Google. Tài khoản `truongtdgch210108@fpt.edu.vn` đã được cấp role Admin theo UID trong Firebase Authentication. Danh mục `role_catalogues/main` được khởi tạo với 5 sự kiện. Chứng thư debug trên máy phát triển đã được đăng ký SHA-1 và SHA-256.

| Kiểm tra | Kết quả |
| --- | --- |
| Firestore Rules | 8 bài đạt, gồm quyền riêng tư giữa hai UID, từ chối tự cấp Admin, quyền công bố và thu hồi quyền |
| App Admin | Build APK, lint và 15 unit test đạt. Bộ kiểm thử trên Android emulator đã đạt trên GitHub Actions |
| App User | Build APK Android, build web release, Flutter analyze và 24 logic/widget test đạt |
| Đồng bộ tài khoản trên Android | Hai Firebase client độc lập đăng nhập cùng tài khoản qua emulator, thêm yêu thích và session ở hai phía, xóa session offline rồi nối mạng. Đăng nhập tài khoản khác có lịch và yêu thích riêng |
| Đăng nhập Google | Đã đối chiếu cấu hình OAuth và chứng thư Android. Bước chọn và xác nhận một tài khoản Google thật cần kiểm tra tương tác |

Xem [test đồng bộ tài khoản Flutter](../../cw-user/app/integration_test/account_sync_test.dart), [test quyền Firestore](test/firestore.test.mjs) và [workflow Admin đã chạy](https://github.com/truongforworkff/Logbook-comp_1786/actions/runs/44759218803).
