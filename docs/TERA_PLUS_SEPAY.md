# Cấu hình Tera Plus với SePay

## 1. Biến môi trường backend

```env
SEPAY_BANK=MBBank
SEPAY_ACCOUNT_NUMBER=SO_TAI_KHOAN_NHAN_TIEN
SEPAY_ACCOUNT_HOLDER=TEN_CHU_TAI_KHOAN
SEPAY_WEBHOOK_SECRET=CHUOI_BI_MAT_DU_DAI_VA_NGAU_NHIEN
PAYMENT_ORDER_EXPIRY_MINUTES=30
```

`SEPAY_BANK` dùng mã ngân hàng mà dịch vụ QR của SePay hỗ trợ. Không đưa secret thật vào Git.

## 2. Webhook trên SePay

- Method: `POST`
- URL: `https://API_DOMAIN/api/payments/sepay/webhook`
- Chữ ký: HMAC SHA-256 của chuỗi `timestamp.raw_body` bằng `SEPAY_WEBHOOK_SECRET`.
- Header thời gian: `X-SePay-Timestamp`
- Header chữ ký: `X-SePay-Signature`, định dạng `sha256=<hex>`

Backend chỉ ghi nhận giao dịch tiền vào, đúng tài khoản, đúng số tiền và có nội dung chứa mã đơn dạng `TERA` + 10 ký tự. `transaction_id` của SePay được lưu duy nhất để webhook gửi lặp không cộng quyền lợi hai lần.

## 3. Dữ liệu và API

Migration `V19__create_tera_plus_payments.sql` tạo:

- cấu hình gói trong `plus_plan_configs`;
- đơn thanh toán trong `payment_orders`;
- nhật ký chống trùng webhook trong `payment_webhook_events`;
- thời hạn Plus của người học trong `users.plus_starts_at` và `users.plus_expires_at`.

Các API frontend sử dụng:

- `GET /api/payments/plus/plans`
- `POST /api/payments/plus/orders`
- `GET /api/payments/orders/{orderId}`
- `GET /api/payments/plus/status`
- `POST /api/payments/sepay/webhook`

Giá, thời hạn, mô tả và quyền lợi trên giao diện được đọc từ `plus_plan_configs`; có thể chỉnh dữ liệu bảng này khi CMS quản lý gói được bổ sung.

## 4. Kiểm thử nhanh

1. Khởi động backend để Flyway áp dụng migration V19.
2. Đăng nhập frontend, nhấn thẻ **Mở khóa Tera Plus** ở sidebar hoặc nút **Nâng cấp Plus** trên header.
3. Chọn gói, tạo đơn và kiểm tra QR có đúng số tiền cùng mã `TERA...`.
4. Gửi webhook có chữ ký hợp lệ với đúng tài khoản, số tiền và mã đơn.
5. Frontend polling mỗi 3 giây, tự chuyển sang trạng thái thành công và cập nhật nhãn Tera Plus.
