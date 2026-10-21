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
