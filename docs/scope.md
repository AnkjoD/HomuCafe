# Phạm vi sản phẩm HomuCafe

## 📋 Baseline yêu cầu ngày 21 tháng 10 năm 2026

Hệ thống desktop Java Swing dành cho một quán cà phê, có khả năng mở rộng cách triển khai sau này. Chủ dự án đã đọc bộ 53 use case và đồng ý dùng làm cơ sở thảo luận và chia việc; chưa chốt phải triển khai tất cả trong lần phát hành đầu.

Bốn actor trực tiếp là chủ quán, quản lý, thu ngân và pha chế. Khách mua hàng/nhà cung cấp chưa có tài khoản hoặc cổng riêng. Chỉ chủ quán xem hồ sơ khách hàng; quản lý không mặc định được xem tài chính hoặc cấp quyền tài khoản.

Nguồn chi tiết: [use case 0.3](requirements/use-cases.md) và [quy tắc nghiệp vụ](business-rules.md).

## 🖥️ Trạng thái mã nguồn

Nhánh giao diện Swing có điều hướng theo vai trò mẫu, màn hình kho, voucher và báo cáo dùng số liệu giả. Các nút nghiệp vụ chưa có xử lý hoàn chỉnh. Chưa có xác thực thật, Service/Repository nghiệp vụ, database, bản cài phát hành hoặc AI. FXML cũ không được ứng dụng Swing nạp.

PR giao diện phải được review/merge trước khi xem phần đó là đã có trên `main`. Menu vai trò mẫu không phải cơ chế bảo mật.

## 🎯 Các mốc chức năng

| Mốc | Phạm vi và kết quả |
| --- | --- |
| Chuẩn bị | Chốt quyết định còn mở, ERD, ràng buộc và hợp đồng giữa các phần |
| Lát cắt bán hàng | Đăng nhập, mở ca, menu tối thiểu, đơn có lưu/mở lại, gửi phiếu, cập nhật pha chế, thu tiền và xem lại phiếu |
| Hoàn thiện bán hàng | Hủy/hoàn, in lại, chốt/đối soát ca, báo cáo từ giao dịch thật, kiểm thử ghi lặp/xung đột |
| Mở rộng quản lý | Công thức và kho, nhập/kiểm kê/hao hụt, voucher, hồ sơ/lịch nhân viên, khách nội bộ và thu chi |
| Sẵn sàng phát hành | Sao lưu/khôi phục đã thử, cấu hình, bản cài và chạy trên máy khác, tài liệu người dùng và kiểm tra quyền |

Khi đưa kho vào vận hành, giữ kho lúc gửi và xuất dùng lúc bắt đầu phải hoàn chỉnh đồng thời; không bật nửa quy trình. Khi đưa voucher vào vận hành, kiểm tra và ghi lượt cùng thanh toán. Việc xuất hiện menu Kho/Voucher trong bản mẫu không có nghĩa các điều kiện này đã hoàn thành.

## 🧩 Các phần chưa nằm trong baseline triển khai

OCR/AI, nhiều chi nhánh, QR tự gọi món, tích điểm, giao hàng, hóa đơn điện tử, ngân hàng tự xác minh, công nợ/đổi trả nhà cung cấp, tính lương/chấm công, chia thanh toán và hoàn một phần, tách/gộp/chuyển bàn. Muốn thêm cần cập nhật use case, quy tắc, dữ liệu và kế hoạch qua Issue.

Chưa chọn database hoặc cách triển khai một/nhiều máy. Hướng kế hoạch là hoàn thành bản một máy có phân tầng, sau đó triển khai máy chủ dùng chung khi nhu cầu nhiều máy được chốt. Không tự xem các công nghệ trong hồ sơ JavaFX cũ là lựa chọn bắt buộc hiện tại.

## ✅ Tiêu chí sản phẩm có thể dùng bên ngoài

Người ngoài nhóm có thể cài và hoàn tất luồng bán hàng; dữ liệu lưu và mở lại được; quyền thực thi trong xử lý; không ghi trùng thu/hoàn hoặc xuất kho; lỗi có thể đối chiếu; backup khôi phục được; có hướng dẫn cập nhật. Chốt giấy phép và kênh phân phối trước phát hành. Thu tiền theo dòng tiền không được gắn nhãn lợi nhuận khi chưa có giá vốn và chi phí phù hợp.
