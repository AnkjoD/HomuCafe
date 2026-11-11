# Swing và kiến trúc phân phần cho HomuCafe

Ngày 21/10/2026.

## 📋 Trạng thái

Đã chốt Java Swing theo lựa chọn chủ dự án. Bộ 53 use case 0.3 được chấp nhận làm đầu vào. Kiến trúc một ứng dụng chia phần nghiệp vụ là phương án triển khai đề xuất; database và một/nhiều máy chưa chốt. Không đưa Redis hoặc RabbitMQ thành phụ thuộc bắt buộc của bản đầu.

## 🧭 Bối cảnh và quyết định

Nhóm cần sản phẩm desktop có nghiệp vụ đúng và chạy được trên máy khác, đồng thời còn học cách code. Dựng UI và chọn công nghệ hạ tầng trước khi có hợp đồng dữ liệu dễ làm mỗi màn hình tự quyết định tiền, kho và quyền.

Dùng View–Controller–Service–Repository; package theo chức năng bên trong bốn nhóm module logic. Không tạo microservice hoặc nhiều Maven module chỉ vì có nhiều use case. Luồng tiền/kho giữ transaction rõ, kiểm soát requestId/version và lịch sử. Triển khai nhiều máy là một bước có API/xác thực/đồng bộ được thiết kế riêng.

## 📊 Điều kiện xem xét lại

Có nhu cầu nhiều máy đã được chốt; benchmark cho thấy index/phân trang/truy vấn chưa đủ; hoặc có tác vụ ngoài cần xử lý bất đồng bộ và retry độc lập. Đề xuất thay đổi phải có dữ liệu đo, chi phí vận hành, cách phục hồi và ranh giới nhất quán. Không bổ sung Redis/RabbitMQ để thay cho test thu lặp hoặc vì muốn danh sách công nghệ dài hơn.

Tham chiếu: [kiến trúc](../architecture.md), [use case](../requirements/use-cases.md), [kế hoạch](../implementation-plan.md).
