# Tài liệu làm việc HomuCafe

Bộ tham chiếu yêu cầu ngày 21/10/2026 gồm 53 use case cho bốn vai trò, đã được chủ dự án đọc và đồng ý dùng làm đầu vào phân tích. Các quyết định kỹ thuật và nghiệp vụ còn mở được đánh dấu riêng; không coi số lượng use case là số chức năng đã triển khai.

## 📖 Đọc trước khi nhận task

1. [Phạm vi và trạng thái thực tế](scope.md).
2. [Quy định đóng góp và merge](../CONTRIBUTING.md).
3. [Danh mục và đặc tả 53 use case](requirements/use-cases.md); [tải bản Word có bìa IUH](requirements/HomuCafe-Use-case-chi-tiet.docx).
4. [Quy tắc nghiệp vụ và câu hỏi còn mở](business-rules.md).
5. [Kiến trúc, hiệu năng và lộ trình mở rộng](architecture.md).
6. [Kế hoạch triển khai](implementation-plan.md) và [backlog liên kết Issues](backlog.md).

## 🧭 Tìm nội dung theo trách nhiệm

| Người đọc | Phần cần nắm |
| --- | --- |
| Tất cả thành viên | Phạm vi, quy trình PR, phần quy tắc chung của use case |
| Người làm giao diện | Actor, quyền, luồng chính/ngoại lệ, trạng thái tải/rỗng/lỗi và hợp đồng Service |
| Người làm nghiệp vụ | Tiền điều kiện, hậu điều kiện, transaction, chống ghi lặp và tiêu chí nghiệm thu |
| Người làm dữ liệu | Snapshot, khóa duy nhất, khóa ngoại, sổ biến động, migration và truy vấn báo cáo |
| Người review | Use case/task liên quan, kiểm chứng thực tế, tác động phần dùng chung và dữ liệu lịch sử |

## 📋 Nguồn thông tin thống nhất

- Use case mô tả mục tiêu và hành vi. Feature/module là cách gom để tổ chức công việc và code, chưa phải danh sách microservices.
- Issues giữ trạng thái, Assignee và hạn được thống nhất. Tài liệu kế hoạch giữ thứ tự và đầu ra; không ghi một trạng thái khác với Issue.
- `docs/decisions/` ghi lựa chọn ảnh hưởng nhiều người và lý do thay đổi.
- README mô tả cách chạy. Không lấy ảnh minh họa hoặc các mảng mẫu làm chứng cứ nghiệm thu nghiệp vụ.
- Bản Word là ảnh chụp hồ sơ ở phiên bản 0.3; Markdown dùng review trên GitHub. Nếu sửa yêu cầu, phải cập nhật cả bản phát hành Word tương ứng trước khi nộp báo cáo.

Nhánh PR có thể chứa thay đổi chưa có trên `main`. Thành viên đọc hoặc chạy theo đúng nhánh được chỉ định trong PR cho tới khi ghép code.
