# Thử thách tập trung

## Cấu hình cho CMS

- `lessons.focused_time_limit_minutes`: số phút của toàn bộ chủ đề, bắt buộc, 1–1440 phút.
- V12 seed giá trị tạm theo số câu hỏi (2 phút/câu, tối thiểu 5 phút). Người vận hành thay giá trị này khi nhập nội dung chính thức.
- `learning_sessions.focused_duration_seconds` giữ thời lượng ban đầu; `focused_remaining_seconds`, `focused_resumed_at` và `expires_at` giữ trạng thái chạy/tạm dừng của lần làm hiện tại. Sửa cấu hình chủ đề chỉ ảnh hưởng lần làm mới/làm lại.
- Câu hỏi, phương án, đáp án đúng, giải thích, YouTube dùng các bảng nội dung hiện có.
- `focused_session_choices` lưu lựa chọn trước khi nộp; `learning_session_answers` lưu kết quả sau khi chấm.

## Luồng

Chọn FOCUSED tại bất kỳ phần nào sẽ tạo hoặc mở lại phiên của toàn chủ đề và đúng người dùng. Phiên bao gồm các câu hỏi đang active, theo thứ tự phần và câu. Đồng hồ chạy khi màn học đang mở, tạm dừng khi rời trang/ẩn tab và tiếp tục từ số giây đã lưu khi quay lại.

API (yêu cầu đăng nhập):

- `GET /api/learning/sessions/{id}/focused`: mở/resume đồng hồ và đồng bộ lựa chọn. Nếu hết giờ, chấm các lựa chọn đã lưu trước khi trả kết quả.
- `POST /api/learning/sessions/{id}/focused/pause`: dừng đồng hồ và lưu số giây còn lại.
- `PUT /api/learning/sessions/{id}/focused/questions/{questionId}/choice`: body `{ "optionIds": [123] }`. Cho phép mảng rỗng để bỏ chọn. Không nhận lựa chọn khi đã hết hạn.
- `POST /api/learning/sessions/{id}/focused/finish`: nộp toàn bài; gọi lại trả kết quả cũ.
- `POST /api/learning/sessions/{id}/focused/restart`: chỉ sau khi kết thúc; reset dữ liệu của phiên này và bắt đầu đồng hồ mới.

Backend khóa phiên khi đọc/lưu/nộp, kiểm tra người sở hữu và phương án thuộc câu hỏi. Trước khi kết thúc, response không chứa đáp án đúng, giải thích hoặc link video. Không có giới hạn giờ cố định trong frontend.

Frontend gọi nộp khi đồng hồ về 0 và gọi pause khi rời màn học, ẩn tab hoặc đóng trang. Chỉ các lựa chọn đã lưu thành công được tính. Phiên không có bất kỳ lựa chọn nào không làm bài học chuyển sang đã học; nộp một phiên có lựa chọn đánh dấu chủ đề hoàn thành.

## Kiểm tra tích hợp khi chạy ứng dụng

1. Chỉnh hai chủ đề có số phút khác nhau; thẻ chế độ và đồng hồ phải hiển thị theo DB.
2. Chọn câu trả lời rồi rời màn học/đổi tab: số giây còn lại và lựa chọn giữ nguyên; khi quay lại đồng hồ mới chạy tiếp.
3. Trước khi nộp, kiểm tra payload không có correctOptionIds hoặc giải thích; thử gửi question/option của chủ đề khác phải bị từ chối.
4. Nộp khi còn câu trống: review phân biệt đúng/sai/bỏ trống. Gửi lại finish không tạo thêm kết quả.
5. Chờ hết giờ: tự hiện kết quả. Khi offline, kết nối lại để chấm đáp án đã lưu; đáp án gửi muộn không được nhận.
6. Tài khoản khác không được đọc/sửa phiên; làm lại chỉ reset phiên của đúng chủ đề/chế độ, đồng hồ dùng cấu hình mới.

Đã kiểm tra production build frontend. Backend và migration cần kiểm tra tích hợp trong môi trường chạy ứng dụng; chưa chạy Maven hoặc Spring Boot trong lần triển khai này.
