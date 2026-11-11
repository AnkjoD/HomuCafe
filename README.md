# ☕ HomuCafe · Coffee POS & Pager System
**Hệ thống Quản lý Bán hàng Quán Coffee Tự phục vụ**  
*Đồ án môn học: Lập trình Hướng sự kiện với Java (IUH)*

---

## 🌟 Giới thiệu tổng quan
**HomuCafe** là giải pháp phần mềm máy tính (Desktop App) chuyên biệt cho mô hình quán cà phê tự phục vụ kết hợp hệ thống thiết bị rung lấy nước (**Guest Pager System**). Ứng dụng được thiết kế theo phong cách tối giản hiện đại (Warm Minimalism), giao diện thân thiện, trực quan với độ phản hồi tức thì.

---

## 🏗️ Kiến trúc & Công nghệ nền
* **Ngôn ngữ**: Java 17+ (LTS).
* **Giao diện (GUI)**: Java Swing kết hợp FlatLaf Look & Feel hiện đại, bo góc mịn (Antialiased Java2D Rendering).
* **Kiến trúc phần mềm**: Kiến trúc 3 lớp (**3-Tier Architecture**: Presentation - Business/Util - Data Access Object - DAO) chuẩn hóa theo yêu cầu giảng dạy khoa CNTT - Đại học Công nghiệp TP.HCM (IUH).
* **Cơ sở dữ liệu**: Microsoft SQL Server (Chuẩn hóa bậc 3 - 3NF, 9 bảng quan hệ chặt chẽ).
* **Cơ chế Dual-Mode thông minh**:
  * Khi có CSDL SQL Server: Tự động kết nối, truy vấn và đồng bộ dữ liệu vào database `QuanLyCoffeePOS`.
  * Khi chưa bật SQL Server (hoặc chạy demo trên Mac/Linux): Tự động chuyển đổi sang In-Memory DataStore mượt mà, không gián đoạn thao tác người dùng.

---

## 📱 Các phân hệ chức năng chính

1. **Tổng quan (Dashboard)**: Thống kê nhanh doanh thu trong ngày, số bàn đang phục vụ, số thẻ rung đang phát cho khách, nhịp bán hàng theo tuần và top món bán chạy.
2. **Bán hàng (POS Counter)**:
   * Chọn món nhanh qua danh mục trực quan.
   * Modal tùy biến độ ngọt (% đường: 0%, 30%, 50%, 70%, 100%), lượng đá (Không đá, Ít đá, Bình thường, Nóng) và kích cỡ ly (Size S, M, L).
   * Gán thẻ rung tự phục vụ (Pager) tự động gợi ý thẻ trống kế tiếp.
   * Thanh toán linh hoạt: Tiền mặt hoặc Quét mã VietQR động.
3. **Sơ đồ bàn (Table Layout Map)**: Quản lý trực quan 12 bàn phục vụ (Tầng 1 & Tầng 2), đổi trạng thái bàn nhanh chỉ bằng 1 cú nhấp chuột.
4. **Quầy pha chế (Bar Monitor)**: 2 cột quy trình tinh gọn:
   * **CHỜ PHA CHẾ (FIFO)**: Danh sách order sắp xếp theo thứ tự thời gian, hiển thị rõ số thẻ rung và tùy biến món.
   * **SẴN SÀNG GIAO (ĐÃ BÁO RUNG)**: Kích hoạt báo rung thẻ để gọi khách nhận nước tại quầy.
5. **Quản lý hóa đơn**: Tra cứu lịch sử hóa đơn, lọc theo trạng thái, xem chi tiết và in lại hóa đơn.
6. **Thực đơn món**: Quản lý danh mục đồ uống, bảng giá từng size, cho phép **sửa trực tiếp trên bảng** và click bật/tắt trạng thái *Đang sử dụng / Không sử dụng*.
7. **Voucher & Ưu đãi**: Quản lý mã giảm giá (chiết khấu % hoặc số tiền cố định), điều kiện đơn tối thiểu và thời hạn áp dụng.
8. **Nhân viên & Tài khoản**: Quản lý hồ sơ nhân sự, phân quyền (Chủ quán, Quản lý, Thu ngân, Pha chế), kiểm tra định dạng SĐT (10 số) và CCCD (12 số).
9. **Khách hàng thân thiết**: Tích lũy điểm thưởng theo giá trị hóa đơn, quy đổi điểm sang tiền giảm giá.
10. **Báo cáo kinh doanh đa chiều**: Báo cáo doanh thu chi tiết theo ngày/tháng, cơ cấu nhóm món và hiệu suất bán hàng.

---

## 🚀 Hướng dẫn cài đặt & Khởi chạy

### 1. Yêu cầu hệ thống
* **Java Development Kit (JDK)**: JDK 17 hoặc mới hơn.
* **Hệ điều hành**: Windows 10/11, macOS hoặc Linux.
* **Cơ sở dữ liệu (tùy chọn khi nộp bài/chấm điểm)**: Microsoft SQL Server 2016 trở lên.

### 2. Thiết lập Cơ sở dữ liệu (Nếu dùng SQL Server)
1. Mở **SQL Server Management Studio (SSMS)**.
2. Mở file script [`sql/QuanLyCoffeePOS_HomuCafe.sql`](sql/QuanLyCoffeePOS_HomuCafe.sql).
3. Nhấn **Execute (F5)** để tự động tạo database `QuanLyCoffeePOS`, cấu trúc bảng và nạp dữ liệu mẫu ban đầu.
4. Kiểm tra tài khoản kết nối trong file [`src/com/homucafe/connectdb/ConnectDB.java`](src/com/homucafe/connectdb/ConnectDB.java) (mặc định: `user = sa`, `password = sapassword`, cổng `1433`).

### 3. Khởi chạy ứng dụng

#### Cách 1: Sử dụng Script tự động (Khuyên dùng trên macOS / Linux)
Mở Terminal trong thư mục dự án và chạy:
```bash
./run.sh
```

#### Cách 2: Khởi chạy trong IDE (VS Code, IntelliJ IDEA, Eclipse, NetBeans)
1. Mở thư mục `HomuCafe-main` trong IDE.
2. Tìm đến file [`src/App.java`](src/App.java).
3. Nhấp chuột phải chọn **Run 'App.main()'** hoặc bấm phím tắt **F5 / Run**.

#### Cách 3: Biên dịch và chạy bằng dòng lệnh thủ công
```bash
# Biên dịch mã nguồn
javac --release 17 -encoding UTF-8 -cp "lib/*" -d target/classes $(find src -name "*.java")

# Khởi chạy ứng dụng
java -cp "target/classes:lib/*" App
```

---
*HomuCafe · Coffee POS & Pager System · Bản quyền Đồ án môn học Lập trình Hướng sự kiện (IUH).*
