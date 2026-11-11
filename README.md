<<<<<<< HEAD
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
=======
# HomuCafe

Ứng dụng desktop quản lý quán cà phê, được phát triển bằng Java Swing.

**Trạng thái:** giao diện Swing với dữ liệu mẫu. Có khung điều hướng theo vai trò xem thử và 12 màn hình chính, gồm voucher/mã giảm giá; chưa có đăng nhập thật, Controller/Service nghiệp vụ hoàn chỉnh, database hoặc AI. Menu theo vai trò trong bản mẫu chưa phải cơ chế bảo mật.

## Tài liệu và kế hoạch cho thành viên

Bắt đầu từ [trang tài liệu nhóm](docs/README.md): [53 use case](docs/requirements/use-cases.md), [luật nghiệp vụ](docs/business-rules.md), [quy trình làm việc](CONTRIBUTING.md), [kế hoạch triển khai](docs/implementation-plan.md) và [kiến trúc/mở rộng](docs/architecture.md). Có [bản Word](docs/requirements/HomuCafe-Use-case-chi-tiet.docx) để dùng cho báo cáo. Redis/RabbitMQ chưa phải phụ thuộc của bản đầu.

Bộ cập nhật được đưa qua PR #6; nếu PR chưa merge, thành viên đọc/chạy đúng nhánh PR. Repo vẫn giữ private cho nhóm.

## Chạy trên máy cá nhân

Yêu cầu JDK 21 và kết nối mạng trong lần tải công cụ build đầu tiên. Đặt `JAVA_HOME` trỏ tới thư mục JDK 21. Maven Wrapper đi kèm nên không cần cài Maven riêng. Swing có sẵn trong JDK.

```powershell
git clone https://github.com/AnkjoD/HomuCafe.git
cd HomuCafe
.\mvnw.cmd clean verify
.\mvnw.cmd exec:java
```

Trên macOS/Linux dùng `./mvnw` thay cho `.\mvnw.cmd`. Trong IDE, mở dự án Maven từ `pom.xml`, chọn JDK 21 và chạy `main()` trong `SwingLauncher`. Không dùng lệnh `javafx:run` nữa.

Giao diện dùng FlatLaf 3.7 để có nút, ô nhập và bảng hiện đại; Maven tải thư viện tự động. Sau khi sửa Java, chạy `mvnw.cmd compile exec:java` hoặc chạy lại main trong IDE. Chạy `java -cp target/classes ...` đơn lẻ sẽ thiếu thư viện FlatLaf.

## Cấu trúc giao diện

- `app/SwingLauncher.java`: khởi động cửa sổ trên Swing EDT.
- `app/WorkspaceView.java`: sidebar, vai trò mẫu và chuyển màn hình bằng CardLayout.
- `inventory/InventoryView.java`: màn hình kho, getter để nối Controller sau này.
- `voucher/VoucherView.java`: danh sách mã giảm giá và form tạo mã xem trước. Chủ quán/quản lý thấy mục quản lý; thu ngân có ô nhập mã trong màn hình bán hàng. Nút áp/lưu/tắt mã chưa nối xử lý.
- `report/ReportView.java`: 6 tab báo cáo mẫu (Tổng quan, Bán hàng, Món bán, Kho, Ca làm, Tài chính). `ReportChart` vẽ biểu đồ đường, cột, thanh, vòng và cột so sánh; `ReportSamples` chứa số liệu minh họa. Tài chính chỉ hiện khi chọn vai trò chủ quán. Khoảng ngày đang cố định; chưa truy vấn database hoặc tính lợi nhuận.
- `shared/ui/Theme.java`: màu, font, bảng, nút và bố cục dùng chung.
- `app/SampleScreens.java`: các màn hình mẫu khác; tách sang từng package khi viết chức năng tương ứng.

Các nút nhập kho, kiểm kê, thanh toán… mới được dựng giao diện; chúng chưa lưu dữ liệu. FXML/CSS cũ được giữ lại làm tham khảo và không được ứng dụng Swing nạp.

Voucher dự kiến hỗ trợ giảm phần trăm hoặc số tiền cố định, đơn tối thiểu, trần giảm với mã phần trăm, thời gian hiệu lực và tổng lượt sử dụng. Thiết kế ban đầu áp dụng một mã cho mỗi đơn và chưa cộng dồn giảm thủ công. Đây là phạm vi đề xuất; chưa triển khai kiểm tra điều kiện, giữ lượt dùng hoặc ghi nhận giảm tiền.

`verify` kiểm tra biên dịch/đóng gói và chạy test khi có; hiện chưa có test nghiệp vụ. File JAR tạo ra chưa phải bản cài đặt độc lập. Việc đóng gói cho người dùng sẽ làm ở giai đoạn phát hành.

## Trước khi nhận task

1. Đọc [trang tài liệu](docs/README.md), [phạm vi](docs/scope.md) và các quyết định còn mở.
2. Đọc [kiến trúc](docs/architecture.md) và [quy định đóng góp](CONTRIBUTING.md).
3. Xem [cách thiết lập GitHub](docs/github-setup.md) và [backlog khởi động](docs/backlog.md).
4. Chạy được app trên máy, rồi nhận một Issue có đầu ra rõ ràng.

## Công nghệ nền

- JDK 21; Swing; Maven Wrapper. Không cần FXML/CSS để chạy giao diện hiện tại.
- GitHub Issues quản lý task; Pull Requests dùng để review và ghép code.
- GitHub Actions kiểm tra build trên Windows và Linux.
- Database, mô hình triển khai và tính năng AI: chưa quyết định.

Quy định và tài liệu chung được lưu trong repo. Task cụ thể được theo dõi bằng Issues; trao đổi quan trọng trong chat cần ghi lại vào Issue hoặc tài liệu để nhóm tra cứu.

**Quy trình merge nhóm đã chọn:** giữ repo private, thành viên làm trên nhánh riêng và mở PR cho `AnkjoD` duyệt; chỉ trưởng nhóm hoặc người được chỉ định trong PR thực hiện merge. PR của trưởng nhóm cần một thành viên khác duyệt. Đây là quy ước thủ công vì repo hiện chưa có bảo vệ nhánh; xem chi tiết trong [CONTRIBUTING.md](CONTRIBUTING.md).

Repo chưa chọn giấy phép phân phối. Nhóm cần thống nhất quyền sử dụng mã nguồn trước khi công bố sản phẩm cho bên ngoài.
>>>>>>> origin/main
