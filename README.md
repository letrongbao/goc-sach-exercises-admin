# Bài tập Lập trình Web

Sinh viên: Lê Trọng Bảo

MSSV: 22110106

Đề tài: Website bán sách Góc Sách.

## Nội dung

- Bài 01: đăng nhập Session/Cookie và quản lý danh mục bằng JDBC.
- Bài 02: chuyển phần lưu dữ liệu sang JPA.
- Bài 03: OTP qua email, quản lý sách, trang chủ và phân trang.
- Bài 04: SiteMesh 3 với Bootstrap, validation các form và cập nhật hồ sơ có tải ảnh.
- Bài 05: hoàn thiện CRUD Category/User trong khu vực admin, tìm kiếm và phân trang.

## Route và luồng xử lý

### Route công khai

| Method | Route | Chức năng |
| --- | --- | --- |
| GET | `/` | Trang chủ và 10 sách mới nhất |
| GET | `/product` | Danh sách sách, phân trang 6 sản phẩm/trang |
| GET | `/product/detail?id={id}` | Chi tiết một sách |
| GET | `/media/{file}` | Đọc ảnh đã tải lên |
| GET/POST | `/auth/login` | Hiển thị và xử lý đăng nhập Session/Cookie |
| POST | `/auth/logout` | Đăng xuất và xóa remember token |
| GET/POST | `/auth/register` | Đăng ký tài khoản và gửi OTP |
| GET/POST | `/auth/activate` | Kích hoạt tài khoản bằng OTP |
| POST | `/auth/resend` | Gửi lại OTP |
| GET/POST | `/auth/forgot` | Yêu cầu OTP đặt lại mật khẩu |
| GET/POST | `/auth/reset` | Đặt lại mật khẩu bằng OTP |
| GET/POST | `/auth/profile` | Xem và cập nhật hồ sơ, số điện thoại, ảnh đại diện |

### Route quản trị

Các route `/admin/*` chỉ cho tài khoản có role `ADMIN` truy cập.

| Method | Route | Chức năng |
| --- | --- | --- |
| GET | `/admin/categories?q={q}&page={n}` | Danh sách, tìm kiếm và phân trang danh mục |
| GET | `/admin/category/add` | Form thêm danh mục |
| GET | `/admin/category/edit?id={id}` | Form sửa danh mục |
| POST | `/admin/category/save` | Thêm hoặc cập nhật danh mục |
| POST | `/admin/category/delete` | Xóa danh mục nếu chưa có sách |
| GET | `/admin/products?page={n}` | Danh sách sách trong khu vực quản trị |
| GET | `/admin/product/add` | Form thêm sách |
| GET | `/admin/product/edit?id={id}` | Form sửa sách |
| POST | `/admin/product/save` | Thêm hoặc cập nhật sách, hỗ trợ upload ảnh |
| POST | `/admin/product/delete` | Xóa sách |
| GET | `/admin/users?q={q}&page={n}` | Danh sách, tìm kiếm và phân trang người dùng |
| GET | `/admin/user/add` | Form thêm người dùng |
| GET | `/admin/user/edit?id={id}` | Form sửa người dùng |
| POST | `/admin/user/save` | Thêm hoặc cập nhật người dùng |
| POST | `/admin/user/delete` | Xóa người dùng, không cho tự xóa tài khoản đang đăng nhập |

### Luồng xử lý chung

1. Request đi qua `SecurityFilter`. Filter thiết lập encoding, session, CSRF token và khôi phục identity từ session hoặc remember cookie.
2. Route `/admin/*` yêu cầu đăng nhập và role `ADMIN`; route `/auth/profile` yêu cầu đăng nhập. Request POST phải gửi CSRF token.
3. Servlet nhận request và chuyển dữ liệu cho service tương ứng: `AuthService`, `OtpService`, `ProfileService`, `CategoryService`, `ProductService` hoặc `UserService`.
4. Service kiểm tra dữ liệu, thực hiện transaction qua `Store`. Khi chạy thật, `JpaStore` sử dụng PostgreSQL; test web dùng `MemoryStore` và fixture trong bộ nhớ.
5. GET render JSP view. SiteMesh bọc view trong Bootstrap decorator tại `WEB-INF/decorators/bootstrap.jsp`.
6. Lỗi nghiệp vụ được chuyển thành trang lỗi an toàn với mã HTTP tương ứng; upload ảnh được lưu qua `LocalImageStorage` và truy cập bằng route `/media/*`.

### Luồng chính

- **Đăng nhập:** mở `/auth/login` → gửi CSRF và thông tin đăng nhập → kiểm tra mật khẩu → tạo Session identity hoặc remember cookie → chuyển admin đến `/admin/categories`, người dùng thường về `/`.
- **CRUD danh mục/sách/người dùng:** mở danh sách → tìm kiếm hoặc chọn trang → mở form thêm/sửa → POST kèm CSRF → service validate và lưu transaction → redirect về danh sách.
- **Cập nhật hồ sơ:** mở `/auth/profile` → gửi thông tin và multipart image → validate → lưu User và ảnh → redirect về hồ sơ.
- **OTP:** đăng ký hoặc yêu cầu quên mật khẩu → tạo challenge và gửi email → nhập OTP → kích hoạt tài khoản hoặc đổi mật khẩu.

### Kiểm thử

- `WebTest` khởi động Tomcat embedded và kiểm tra route, JSP, CSRF, phân quyền, CRUD, upload ảnh và OTP.
- `CoreTest`, `ProductTest`, `OtpTest`, `ImageTest` kiểm tra service và nghiệp vụ độc lập.
- `JpaTransactionTest` kiểm tra transaction JPA.
- `PostgresReadinessTest`, `PostgresWebTest` và `LiveAcceptanceTest` chỉ chạy khi có PostgreSQL hoặc môi trường live tương ứng.

Không đưa mật khẩu hoặc file `config/local.properties` lên GitHub. Các bản nộp được đánh dấu bằng tag `submission-01` đến `submission-05`.
