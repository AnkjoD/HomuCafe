# Backlog Triển khai HomuCafe (IUH 12 Thực thể)

## Phase 1: Dọn dẹp Merge Conflict, Chuẩn hóa Cấu trúc Thư mục & Fix Build
- Giải quyết Git Merge Conflict trong 8 file.
- Xóa các cây thư mục trùng lặp (`src/main/java/com/homucafe/`).
- Đảm bảo biên dịch sạch 100%.

## Phase 2: Đồng bộ CSDL 12 Thực thể & Sửa lỗi Runtime trong DAO
- Cập nhật Schema SQL (`soTheRung`, `maCTHD` / tùy chọn trong ChiTietHoaDon).
- Sửa lỗi `HoaDon_DAO`: Tính động `tongTien` và `soThuTuNgay`, xử lý `soTheRung`, `maVoucher`, `huyHoaDon`.
- Bổ sung Entity và DAO cho `Voucher`, `Ban`, `PhuongThucThanhToan`, `PhieuDatBan`.

## Phase 3: Nâng cấp Nghiệp vụ Bán hàng (POS), Giỏ hàng, Voucher & Thanh toán
- Tùy biến món có số lượng (1-50), cộng dồn giỏ hàng, thanh công cụ (+1 SL, -1 SL, sửa tùy chọn, xóa).
- Áp Voucher động từ DataStore/DAO, đổi điểm tích lũy khách hàng.
- Thanh toán VietQR và Dialog In Hóa đơn (80mm) / In Tem dán ly.
- Hủy hóa đơn có xác nhận mật khẩu Quản lý.

## Phase 4: Khép kín luồng Sơ đồ Bàn, Đặt bàn trước & Quầy Bar
- 3 trạng thái màu bàn (`TRONG`, `DANG_PHUC_VU`, `DA_DAT`).
- Đặt bàn trước (`PHIEU_DAT_BAN`), Check-in, Dọn bàn.
- Quầy Bar: Bộ đếm thời gian chờ (>10p cảnh báo đỏ), Báo hết món nhanh (86), Giao khách & thu hồi thẻ.

## Phase 5: Đăng nhập thật, Quản lý Ca làm việc & Quản lý Voucher/Thực đơn
- Đăng nhập xác thực tài khoản thật, phân quyền menu, đổi mật khẩu.
- Kết nối `VoucherView` trực tiếp vào DataStore/DAO.
- Đối soát tiền mặt cuối ca (Đầu ca + Thu tiền mặt - Chi = Lý thuyết vs Thực tế).

## Phase 6: Báo cáo Động & Xuất CSV/In Báo cáo
- Báo cáo và Dashboard tính toán động từ hóa đơn thật (loại trừ `DA_HUY`).
- Biểu đồ doanh thu theo khung giờ, Top món bán chạy nhất.
- Bộ lọc thời gian (Hôm nay, 7 ngày, Tháng này, Tất cả), xuất CSV (UTF-8 BOM).
