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
