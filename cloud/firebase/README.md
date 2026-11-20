# Firebase cho bản CW-auth

Hai app dùng Firebase Authentication và Firestore trên project **eventa-coursework-fpt**. [Sơ đồ database](DATABASE_DIAGRAM.md) mô tả đường dẫn, quyền truy cập, cách cấp hoặc thu hồi Admin và cách nhập lịch từ bản cũ.

## Xem dữ liệu

Mở [Firestore Data](https://console.firebase.google.com/project/eventa-coursework-fpt/firestore/databases/-default-/data) và chọn collection.

| Đường dẫn | Nội dung |
| --- | --- |
| `role_catalogues/main` | Danh mục sự kiện/session công bố từ Admin |
| `roles/{uid}` | Quyền Admin do chủ project cấp |
| `users/{uid}` | Hồ sơ cá nhân |
| `users/{uid}/schedule` | Các session người tham dự đã lưu |
| `users/{uid}/favourites` | Các sự kiện yêu thích |

Tra UID theo email ở [Authentication → Users](https://console.firebase.google.com/project/eventa-coursework-fpt/authentication/users). Mỗi lựa chọn xuất hiện sau lần đầu người dùng lưu. Chủ project có quyền kiểm tra dữ liệu trong Console. Ứng dụng xác định người dùng bằng Firebase UID.

## Công bố

Admin quản lý event/session trong SQLite. WorkManager gửi thay đổi khi có mạng và khôi phục công việc định kỳ. Firestore kiểm tra role, UID người gửi, sourceId và revision. Danh mục có giới hạn 100 event, 500 session và 512 KiB JSON trong app. Ảnh tùy chọn được thu nhỏ trước khi gửi.

Bản CW cũ vẫn dùng `catalogues/main` và biên nhận riêng. Hai danh mục được cập nhật độc lập. Khi cài Admin vào thiết bị có bộ SQLite khác, cơ chế kiểm tra sourceId bảo vệ danh mục đã được công bố.

## Chạy kiểm thử

Từ `cloud/firebase` chạy `npm ci`. Khởi động emulator ở một terminal:

```sh
firebase emulators:start --only auth,firestore --project demo-eventa --config firebase.test.json
```

Ở terminal khác chạy `npm test` từ cùng thư mục. Firestore dùng cổng 8086, Auth dùng cổng 9098. Dùng Android emulator thử riêng khi chạy instrumentation và Flutter integration test. [Bộ kiểm thử Rules](test/firestore.test.mjs) bao phủ cả danh mục CW cũ và dữ liệu riêng của CW-auth.

Chủ project triển khai từ thư mục này bằng `firebase deploy --only firestore:rules --project eventa-coursework-fpt`. Chứng thư ký Android mới cần đăng ký SHA trong Firebase để dùng Google Sign-In.
