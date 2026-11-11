# Quy tắc nghiệp vụ HomuCafe

Bản này dùng cùng bộ mã BR01–BR15 của [đặc tả use case 0.3](requirements/use-cases.md). Đây là baseline thiết kế để triển khai theo mốc, không phải mô tả tất cả quy tắc đã được code. Các câu hỏi còn mở phía cuối phải được xử lý trước task phụ thuộc.

## 📋 Quy tắc dùng chung

| Mã | Quy tắc |
| --- | --- |
| BR01 | Một bàn có tối đa một đơn mở. Tại bàn có thể pha trước thu. Mang đi tạo payment và đợt pha chế đầu trong cùng transaction. Chưa tách/gộp/chuyển bàn. |
| BR02 | Trạng thái đơn, thanh toán và món độc lập. Đóng đơn khi đã thu đủ và mọi món đã giao/hủy hợp lệ. Sau thu không sửa giá, lượng hoặc giảm của phiếu. |
| BR03 | Chụp tên/giá/nhóm/tùy chọn lên dòng đơn; khi có kho chụp công thức/định lượng cho đợt. Sửa danh mục không đổi lịch sử. |
| BR04 | Có kho: gửi giữ nguyên liệu, bắt đầu pha xuất dùng một lần. Hủy trước pha giải phóng giữ; đã pha thì hoàn tiền không tự nhập lại kho hoặc trừ hao hụt lần hai. |
| BR05 | Một voucher/đơn, chưa cộng giảm thủ công. Kiểm tra lại khi thu và ghi usage cùng payment; giới hạn lượt phải được bảo vệ khi ghi đồng thời. Hoàn đơn không tự hoàn lượt theo baseline. |
| BR06 | VND nguyên, BigDecimal hoặc số nguyên đồng. Thu đủ một lần bằng một phương thức; hoàn toàn đơn tối đa một lần. Chuyển khoản cần người có quyền xác nhận thực nhận, không tự tin ảnh khách đưa. |
| BR07 | Tiền mặt dự kiến = đầu ca + thu mặt − hoàn mặt + nạp − rút/nộp quỹ. Tiền chuyển khoản không nằm trong thực đếm ngăn kéo. |
| BR08 | Ghi quan trọng có requestId và phiên bản. Cùng mã/nội dung trả kết quả cũ; khác nội dung bị từ chối. Khóa/ràng buộc còn ngăn hai requestId khác nhau cùng thu một đơn. |
| BR09 | Các dữ liệu liên quan commit/rollback cùng transaction. Timeout phải tra kết quả, không tự tạo lần thu/hoàn mới. |
| BR10 | Database không tự đảo tiền thực đã giao bên ngoài. Đã giao tiền mà chưa rõ bản ghi phải đối chiếu; máy in lỗi sau commit không đảo payment. |
| BR11 | Người và thời điểm ghi lấy từ tầng xử lý tin cậy. Báo cáo tiền thu theo thời điểm thu, hoàn theo thời điểm hoàn; kỳ đầu bao gồm/cuối loại trừ theo múi giờ quán. |
| BR12 | Danh mục có lịch sử thì ngừng dùng thay vì xóa. Chứng từ, sổ kho và ca đã xác nhận không sửa đè; audit không chứa mật khẩu/token. |
| BR13 | Thu sau hoàn chưa phải lợi nhuận. Nhập hàng chưa chắc đã chi tiền; không tự tạo chi từ nháp/giá trị nhập hoặc cộng trùng payment vào sổ ngoài bán hàng. |
| BR14 | Kiểm tra quyền khi đọc, ghi và lấy lại kết quả cũ. Ẩn menu không thay cho kiểm tra quyền. Nhiều máy cần xác thực và kiểm soát đồng thời phía máy chủ. |
| BR15 | Hiển thị rõ rỗng, đang tải, lỗi và dữ liệu mẫu. Không trình bày số minh họa như dữ liệu vận hành. |

## 🔗 Ranh giới giao dịch cần giữ

| Thao tác | Các phần cùng ghi thành công |
| --- | --- |
| Gửi tại bàn | Đợt pha chế + trạng thái dòng + snapshot và giữ kho nếu đã bật kho + audit |
| Bắt đầu pha | PREPARING + đổi giữ thành xuất dùng + người/thời điểm + audit |
| Thu tại bàn | Payment + receipt + voucher usage nếu có + trạng thái tiền + audit/kết quả requestId |
| Thu mang đi | Các phần thu ở trên + đợt đầu + giữ kho khi bật kho |
| Hủy trước pha | Trạng thái hủy + giải phóng giữ + quyết định/lý do |
| Hoàn toàn đơn | Refund + hủy dòng chưa bắt đầu + giải phóng giữ tương ứng + audit |
| Ghi nhập kho | POSTED + các biến động nhập gắn dòng phiếu, chống ghi lại |
| Chốt ca | Số thực đếm + chênh lệch + CLOSED; tuần tự hóa với thu/hoàn |

In phiếu là việc sau commit. Báo cáo là luồng đọc từ chứng từ/sổ đã lưu, không tạo một bộ số dư riêng trong View. Các nhóm chức năng giao tiếp qua Service; không để mỗi nhóm tự cập nhật bảng tiền hoặc tồn của nhóm khác.

## ❓ Quyết định phải chốt trước khi triển khai phần liên quan

| Mã | Cần quyết định | Chặn task |
| --- | --- | --- |
| OPEN01 | Database, cách kết nối và một/nhiều máy cho bản đầu; kiểu thời gian và migration | Thiết kế DDL và Repository |
| OPEN02 | Làm tròn giảm giá VND, phân bổ giảm theo dòng, xử lý phần dư; đơn phải thu 0 đồng xử lý thế nào | Voucher, thanh toán và báo cáo món |
| OPEN03 | Ai được hủy trước pha, tự duyệt ngoại lệ ở mức nào, bằng chứng khi hoàn | Hủy/hoàn và quyền |
| OPEN04 | Quầy, phân công, chuyển quỹ, ca qua nửa đêm và xử lý ca chưa chốt | Ca và báo cáo ca |
| OPEN05 | Mốc kiểm kê, lượng đang giữ và xử lý biến động khi đang đếm | Kho/kiểm kê |
| OPEN06 | Danh sách chỉ tiêu báo cáo và cách tính lượng bán/hoàn theo kỳ | ReportService và nghiệm thu tổng |
| OPEN07 | Mục tiêu tải, máy tham chiếu, thời gian sao lưu/khôi phục và kênh phân phối | Kiểm chứng hiệu năng và phát hành |

Ghi quyết định vào `docs/decisions/` và liên kết Issue; cập nhật use case nếu hành vi đổi. Không tự chọn khác nhau ở hai module hoặc xem giả định trong giao diện là quyết định chung.
