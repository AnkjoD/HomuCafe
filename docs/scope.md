# Phạm vi Dự án HomuCafe (IUH 12 Thực thể)

## 1. Mục tiêu Dự án
HomuCafe là ứng dụng desktop quản lý bán hàng cho quán cà phê mô hình tự phục vụ kết hợp thẻ rung lấy nước (Guest Pager System), xây dựng trên nền tảng Java Swing và Microsoft SQL Server theo chuẩn kiến trúc hướng đối tượng và sự kiện tại ĐH Công nghiệp TP.HCM (IUH).

## 2. Phân hệ chức năng chính

### Phân hệ 1: Bán hàng (POS) & Giỏ hàng
- Chọn món nhanh theo nhóm món.
- Tùy biến món: Size (S, M, L), Đường (0-100%), Đá (Nóng, Không đá, Ít đá, Bình thường), Topping, Ghi chú.
- Quản lý giỏ hàng: Cộng dồn món cùng tùy chọn, sửa số lượng (+1/-1), sửa tùy chọn, xóa món.
- Áp dụng Voucher hợp lệ và đổi điểm tích lũy khách hàng (1 điểm = 1.000đ, tối đa 50%).
- Hỗ trợ thanh toán: Tiền mặt, VietQR (mã QR động có số tiền và nội dung `maHD`).
- Cấp số thẻ rung (Pager) từ 1-20.

### Phân hệ 2: Sơ đồ bàn & Đặt bàn trước
- 12 bàn ở 2 tầng với 3 trạng thái: `TRONG`, `DANG_PHUC_VU`, `DA_DAT`.
- Đặt bàn trước (`PHIEU_DAT_BAN`): Tên, SĐT, số người, giờ hẹn, món đặt trước.
- Nhận bàn (Check-in) và Dọn/Trả bàn.

### Phân hệ 3: Quầy pha chế (Bar Monitor)
- Hàng đợi FIFO: Chờ pha chế và Sẵn sàng giao (báo rung).
- Cảnh báo thời gian chờ > 10 phút.
- Báo hết món nhanh (86): Chuyển món sang `TAM_NGUNG`.
- Bàn giao nước và thu hồi thẻ rung.

### Phân hệ 4: Quản lý & Báo cáo
- Quản lý Hóa đơn, chi tiết hóa đơn, in lại hóa đơn (80mm) và in tem dán ly.
- Hủy hóa đơn (`DA_HUY`) có xác nhận mật khẩu Quản lý.
- Quản lý Thực đơn, Voucher, Nhân viên, Khách hàng.
- Báo cáo doanh thu thời gian thực, cơ cấu bán hàng, đối soát ca tiền mặt cuối ca.
