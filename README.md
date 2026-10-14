# HomuCafe

Ứng dụng desktop quản lý bán hàng quán cà phê, được phát triển bằng JavaFX.

**Trạng thái:** khởi tạo dự án nhóm. Hiện chỉ có màn hình khởi động và bộ quy ước; chưa có chức năng bán hàng, database hoặc AI.

## Chạy trên máy cá nhân

Yêu cầu JDK 21 và kết nối mạng trong lần tải thư viện đầu tiên. Đặt `JAVA_HOME` trỏ tới thư mục JDK 21. Maven Wrapper đi kèm nên không cần cài Maven hoặc JavaFX SDK riêng.

```powershell
git clone https://github.com/AnkjoD/HomuCafe.git
cd HomuCafe
.\mvnw.cmd clean verify
.\mvnw.cmd javafx:run
```

Trên macOS/Linux dùng `./mvnw` thay cho `.\mvnw.cmd`. Trong IDE, mở dự án Maven từ `pom.xml`, chọn JDK 21 và chạy mục Maven `javafx:run`.

`verify` kiểm tra biên dịch/đóng gói và chạy test khi có; hiện chưa có test nghiệp vụ. File JAR tạo ra chưa phải bản cài đặt độc lập. Việc đóng gói cho người dùng sẽ làm ở giai đoạn phát hành.

## Trước khi nhận task

1. Đọc [phạm vi đề xuất](docs/scope.md) và cùng nhóm chốt các quyết định còn mở.
2. Đọc [kiến trúc](docs/architecture.md) và [quy định đóng góp](CONTRIBUTING.md).
3. Xem [cách thiết lập GitHub](docs/github-setup.md) và [backlog khởi động](docs/backlog.md).
4. Chạy được app trên máy, rồi nhận một Issue có đầu ra rõ ràng.

## Công nghệ nền

- JDK 21; JavaFX Controls/FXML; Maven Wrapper.
- GitHub Issues quản lý task; Pull Requests dùng để review và ghép code.
- GitHub Actions kiểm tra build trên Windows và Linux.
- Database, mô hình triển khai và tính năng AI: chưa quyết định.

Quy định và tài liệu chung được lưu trong repo. Task cụ thể được theo dõi bằng Issues; trao đổi quan trọng trong chat cần ghi lại vào Issue hoặc tài liệu để nhóm tra cứu.

**Quy trình merge nhóm đã chọn:** giữ repo private, thành viên làm trên nhánh riêng và mở PR cho `AnkjoD` duyệt; chỉ trưởng nhóm hoặc người được chỉ định trong PR thực hiện merge. PR của trưởng nhóm cần một thành viên khác duyệt. Đây là quy ước thủ công vì repo hiện chưa có bảo vệ nhánh; xem chi tiết trong [CONTRIBUTING.md](CONTRIBUTING.md).

Repo chưa chọn giấy phép phân phối. Nhóm cần thống nhất quyền sử dụng mã nguồn trước khi công bố sản phẩm cho bên ngoài.
