# BE Tera Learn

Spring Boot API cho nền tảng học các môn Khoa học xã hội Tera, dùng JWT stateless, đăng nhập email và Google OAuth 2.0.

## Yêu cầu

- Java 21
- PostgreSQL 12+

## Chạy local

```bash
./mvnw spring-boot:run
```

Sao chép các biến trong `.env.example` vào môi trường chạy. Flyway tự cập nhật schema `tera` và tạo các bảng `users`, `refresh_tokens`, `email_verifications` khi ứng dụng khởi động.

## Xác thực

- `POST /api/auth/email/resolve`: kiểm tra email để chuyển sang đăng nhập hoặc đăng ký.
- `POST /api/auth/email/register`: tạo tài khoản chờ xác thực và gửi OTP 4 chữ số.
- `POST /api/auth/email/verification/confirm`: xác thực OTP và đăng nhập.
- `POST /api/auth/email/verification/resend`: gửi lại OTP, có thời gian chờ.
- `POST /api/auth/email/login`: đăng nhập bằng email và mật khẩu.
- `GET /api/auth/google`: chuyển hướng tới trang đăng nhập Google.
- `GET /api/auth/callback/google`: callback từ Google OAuth.
- `POST /api/auth/oauth/session`: đổi mã phiên OAuth một lần lấy cặp token.
- `POST /api/auth/refresh`: rotation refresh token từ cookie HttpOnly.
- `POST /api/auth/logout`: revoke refresh token và xóa cookie.

Access token có thời hạn 15 phút. Refresh token có thời hạn 7 ngày, được lưu trong database dưới dạng SHA-256 và giới hạn tối đa 5 phiên hoạt động cho mỗi tài khoản.

Mật khẩu được băm bằng BCrypt. OTP hết hạn sau 10 phút, chỉ lưu dưới dạng HMAC và bị giới hạn số lần nhập sai.

## Gửi email OTP

Khi phát triển local, để `MAIL_DELIVERY_ENABLED=false`; mã OTP sẽ được in trong console backend. Để gửi email thật qua Gmail, đặt `MAIL_DELIVERY_ENABLED=true`, điền `MAIL_USERNAME`, `MAIL_PASSWORD` bằng App Password, đặt `MAIL_FROM` thành địa chỉ gửi hợp lệ và có thể bật `MAIL_HEALTH_ENABLED=true`.

API dashboard `GET /api/v1/dashboard` yêu cầu header:

```text
Authorization: Bearer <access-token>
```

Health check công khai: `GET http://localhost:8080/actuator/health`

Biến môi trường:

- `SERVER_PORT`: cổng backend, mặc định `8080`.
- `DATABASE_*`: kết nối PostgreSQL.
- `JWT_SECRET`: secret ký JWT, tối thiểu 32 byte.
- `ALLOWED_ORIGINS`: danh sách origin frontend, phân tách bằng dấu phẩy.
- `GOOGLE_*`: thông tin OAuth client của Google.
- `MAIL_*`: thông tin SMTP và địa chỉ gửi OTP.
- `EMAIL_VERIFICATION_SECRET`: secret riêng dùng để HMAC mã OTP.
