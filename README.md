# COMP1786 — Eventa có tài khoản

Nhánh `CW-auth` gồm hai ứng dụng dùng chung Firebase project `eventa-coursework-fpt`.

| Ứng dụng | Mở trong Android Studio | Tài khoản |
| --- | --- | --- |
| [Eventa Admin](cw-admin/README.md) | `cw-admin` | Email và mật khẩu của tài khoản được cấp quyền Admin |
| [Eventa Explore](cw-user/README.md) | `cw-user/app` | Đăng ký bằng email hoặc đăng nhập bằng Google |

Người tham dự đăng nhập cùng tài khoản trên thiết bị khác sẽ thấy lại sự kiện yêu thích và các session trong **Lịch của tôi**. Xem [sơ đồ database và bảng quyền](cloud/firebase/DATABASE_DIAGRAM.md) để tìm từng loại dữ liệu trong Firestore.

```sh
git clone --branch CW-auth --single-branch https://github.com/truongforworkff/Logbook-comp_1786.git
```

Bản coursework ban đầu ở nhánh [CW](https://github.com/truongforworkff/Logbook-comp_1786/tree/CW). Các bài thực hành ở [logbook](logbook/README.md).
