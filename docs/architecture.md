# Kiến trúc Hệ thống HomuCafe (IUH 12 Thực thể)

## 1. Giới thiệu Kiến trúc 3 Lớp (3-Tier Architecture)

Hệ thống HomuCafe tuân thủ mô hình 3 lớp chuẩn hóa theo đề cương môn học Lập trình Hướng sự kiện với Java (IUH):

```
+-----------------------------------------------------------+
|               1. TẦNG GIAO DIỆN (UI / VIEW)               |
|  - com.homucafe.app (SwingLauncher, WorkspaceView,        |
|    SampleScreens, DialogTuyBienMon)                       |
|  - com.homucafe.shared.ui (Theme, ComponentFactory)       |
|  - com.homucafe.report (ReportView, ReportChart)          |
|  - com.homucafe.voucher (VoucherView)                     |
+-----------------------------+-----------------------------+
                              | (Event / Data Binding)
+-----------------------------v-----------------------------+
|             2. TẦNG DỮ LIỆU & BỘ NHỚ (DATASTORE)          |
|  - com.homucafe.dao.DataStore (In-Memory Dual-Mode)       |
|  - com.homucafe.util (DateUtils, FormatUtils)             |
+-----------------------------+-----------------------------+
                              | (JDBC Calls / SQL Queries)
+-----------------------------v-----------------------------+
|               3. TẦNG TRUY XUẤT CSDL (DAO)                |
|  - com.homucafe.dao.*_DAO (12 bảng nghiệp vụ)             |
|  - com.homucafe.connectdb.ConnectDB (SQL Server Pool)     |
+-----------------------------------------------------------+
```

## 2. Mô hình 12 Thực thể CSDL (QuanLyCoffeePOS)

CSDL SQL Server bao gồm 12 bảng quan hệ chặt chẽ:
1. `NHAN_VIEN`: Quản lý thông tin nhân sự.
2. `TAI_KHOAN`: Tài khoản đăng nhập hệ thống, liên kết 1-1 với nhân viên.
3. `KHACH_HANG`: Khách hàng thân thiết, tích lũy điểm thưởng.
4. `BAN`: 12 bàn phục vụ (Tầng 1 & Tầng 2), theo dõi trạng thái `TRONG`, `DANG_PHUC_VU`, `DA_DAT`.
5. `LOAI_MON`: Danh mục thức uống (Cà phê, Trà hoa quả, Đá xay, Bánh...).
6. `MON`: Danh sách món uống/đồ ăn.
7. `KICH_CO_MON`: Kích cỡ và giá tiền tương ứng (Size S, M, L).
8. `VOUCHER`: Mã giảm giá, chiết khấu theo %, số tiền, điều kiện đơn tối thiểu.
9. `PHUONG_THUC_THANH_TOAN`: Tiền mặt, Chuyển khoản VietQR, Ví điện tử.
10. `HOA_DON`: Hóa đơn bán hàng, số thẻ rung, trạng thái thanh toán.
11. `CHI_TIET_HOA_DON`: Chi tiết từng món trong hóa đơn (size, đường, đá, topping).
12. `PHIEU_DAT_BAN`: Phiếu đặt bàn trước của khách hàng.

## 3. Cơ chế Dual-Mode Thông minh

- **Online Mode (SQL Server)**: Kết nối tới Microsoft SQL Server qua JDBC Driver, đồng bộ dữ liệu chuẩn ACID.
- **Offline Mode (In-Memory Fallback)**: Khi không phát hiện SQL Server (hoặc chạy demo nhanh), `DataStore` tự động nạp bộ dữ liệu mẫu ban đầu để ứng dụng chạy mượt mà không văng lỗi.
