# Kiến trúc nền

## Trạng thái

Khung hiện tại là ứng dụng JavaFX Maven với một màn hình chào bằng FXML/CSS. Chưa có database, xác thực hoặc nghiệp vụ. Cấu trúc phía dưới là quy ước mở rộng, không phải các chức năng đã hoàn thành.

## Luồng phụ thuộc

FXML/View → Controller → Service → Repository → nguồn dữ liệu.

- View: bố trí giao diện.
- Controller: nhận tương tác, gọi service và hiển thị kết quả/lỗi.
- Service: kiểm tra quyền, quy tắc nghiệp vụ và điều phối giao dịch khi có database.
- Repository: đọc/ghi dữ liệu, không quyết định quy tắc giao diện.
- Model/DTO: dữ liệu miền nghiệp vụ và dữ liệu trao đổi khi cần.

Tổ chức Java theo chức năng dưới `com.homucafe`: `auth`, `menu`, `order`, `payment`, `report`. Mỗi chức năng thêm các lớp controller/service/repository/model khi thực sự cần. Tên phân biệt rõ, ví dụ `MenuController`, `MenuService`, `MenuRepository`.

`app` giữ khởi động/điều hướng/cấu hình. Chỉ tạo `shared` cho thành phần thật sự dùng chung; không biến thành nơi chứa mọi thứ.

```text
src/main/java/com/homucafe/
  app/                      # Đã có: Launcher, HomuCafeApplication, WelcomeController
  menu/                     # Ví dụ mở rộng sau: MenuController, MenuService, ...
src/main/resources/com/homucafe/
  views/                    # FXML theo chức năng
  styles/                   # CSS
  images/                   # Thêm khi có tài nguyên
src/test/java/com/homucafe/  # Thêm test nghiệp vụ khi triển khai
db/migrations/              # Thêm script sau khi chốt database
db/seed/                    # Dữ liệu mẫu không chứa thông tin thật
```

## Các giới hạn cần giữ

- Không kết nối database từ Controller; không chặn luồng JavaFX bằng truy vấn/mạng.
- Không tạo singleton toàn cục để lưu mọi dữ liệu. Truyền dependency rõ ràng khi thêm service/repository.
- Nếu cần nhiều máy, quyết định kiến trúc kết nối và ranh giới tin cậy trước khi triển khai; khung desktop này chưa giải quyết triển khai nhiều máy.
- Nếu gọi AI bằng khóa của hệ thống, giữ khóa trên backend do nhóm kiểm soát, không nhúng vào bản cài JavaFX.
- Bản khung chưa có `module-info.java` để giảm cấu hình ban đầu. Đóng gói cài đặt và chiến lược module sẽ quyết định ở giai đoạn phát hành.

## Tham khảo chính thức

- [OpenJFX với Maven](https://openjfx.io/openjfx-docs/maven)
- [Apache Maven Wrapper](https://maven.apache.org/tools/wrapper/)
