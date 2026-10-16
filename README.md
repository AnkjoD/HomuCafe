# HomuCafe

Ứng dụng desktop quản lý quán cà phê, được phát triển bằng Java Swing.

**Trạng thái:** giao diện Swing với dữ liệu mẫu. Có khung điều hướng theo vai trò xem thử và 11 màn hình chính; chưa có đăng nhập thật, Controller/Service nghiệp vụ, database hoặc AI. Menu theo vai trò trong bản mẫu chưa phải cơ chế bảo mật.

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
- `shared/ui/Theme.java`: màu, font, bảng, nút và bố cục dùng chung.
- `app/SampleScreens.java`: các màn hình mẫu khác; tách sang từng package khi viết chức năng tương ứng.

Các nút nhập kho, kiểm kê, thanh toán… mới được dựng giao diện; chúng chưa lưu dữ liệu. FXML/CSS cũ được giữ lại làm tham khảo và không được ứng dụng Swing nạp.

`verify` kiểm tra biên dịch/đóng gói và chạy test khi có; hiện chưa có test nghiệp vụ. File JAR tạo ra chưa phải bản cài đặt độc lập. Việc đóng gói cho người dùng sẽ làm ở giai đoạn phát hành.

## Trước khi nhận task

1. Đọc [phạm vi đề xuất](docs/scope.md) và cùng nhóm chốt các quyết định còn mở.
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
