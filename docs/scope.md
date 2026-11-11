<<<<<<< HEAD
# Phạm vi sản phẩm — đề xuất để nhóm chốt

## Mục tiêu

Hỗ trợ nhân viên và quản lý vận hành bán hàng tại một quán cà phê. Đây là phạm vi đề xuất, chưa phải danh sách tính năng đã được nhóm duyệt.

## Bản đầu tiên đề xuất

| Vai trò | Khả năng |
| --- | --- |
| Nhân viên | Đăng nhập, xem menu, tạo đơn, sửa số lượng, thanh toán, xem hóa đơn theo quyền |
| Quản lý | Các chức năng bán hàng, quản lý menu/danh mục, tài khoản nhân viên, xem báo cáo doanh thu |

Luồng demo mục tiêu: đăng nhập → chọn món → kiểm tra đơn → thanh toán → xem lại hóa đơn → xem báo cáo.

## Quyết định cần chốt trước khi thiết kế database

- Nhóm bao nhiêu người, deadline, thời gian có thể dành mỗi tuần?
- Bán mang đi hay cả phục vụ tại bàn? Có gộp/tách/chuyển bàn không?
- Một máy hay nhiều máy dùng dữ liệu chung? Mất mạng cần hoạt động thế nào?
- Database nào, lưu ở đâu, cách cài đặt và sao lưu?
- Trạng thái đơn, điều kiện sửa/hủy, ai được làm và có cần lý do?
- Thanh toán tiền mặt/chuyển khoản ở mức ghi nhận thủ công hay tích hợp thật? Không coi ghi nhận phương thức thanh toán là đã xác minh giao dịch ngân hàng.
- Size, topping, đường/đá, giảm giá, thuế/phí và quy tắc làm tròn có trong bản đầu không?
- Giá món thay đổi phải giữ giá lịch sử trên hóa đơn thế nào?
- Phân quyền xem lịch sử/báo cáo; dữ liệu có được xóa hay chuyển sang ngừng sử dụng?

## Để sau, trừ khi nhóm đổi phạm vi

Quản lý kho và nhà cung cấp, khách hàng/tích điểm, nhiều chi nhánh, tích hợp thanh toán thật, AI/OCR, bản cài đặt cho nhiều hệ điều hành.

Nếu chọn OCR hóa đơn nhập hàng làm điểm nhấn: bổ sung nghiệp vụ nhập kho và đối chiếu dữ liệu OCR có xác nhận; đồng thời cắt bớt chức năng khác. AI chưa phải phụ thuộc của khung hiện tại.

## Mục tiêu trước khi phát hành

Luồng nghiệp vụ chính được kiểm tra; phân quyền thực thi ở tầng xử lý; có xử lý lỗi, hướng dẫn cài đặt, sao lưu/phục hồi dữ liệu phù hợp và dữ liệu demo. Chốt giấy phép, hệ điều hành hỗ trợ và cách xử lý thông tin nhạy cảm trước khi phân phối.
=======
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
>>>>>>> origin/main
