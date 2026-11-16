# Quy định làm việc nhóm

## Nhận và thực hiện task

- Mỗi task có một Issue, một người chịu trách nhiệm chính, đầu ra, tiêu chí nghiệm thu, phụ thuộc và hạn dự kiến.
- Mỗi người ưu tiên một task đang làm. Báo sớm khi bị kẹt hoặc có nguy cơ trễ, kèm điều cần hỗ trợ.
- Lấy code mới nhất từ `main`, tạo nhánh theo task: `feature/12-them-mon`, `fix/28-tong-tien`, `docs/7-luong-ban-hang`.
- Không push trực tiếp vào `main` sau lần khởi tạo. Không force push hoặc xóa nhánh chung.
- Commit theo thay đổi có ý nghĩa, ví dụ `feat(menu): add product form`, `fix(order): validate quantity`, `docs: update setup`.

```powershell
git switch main
git pull --ff-only origin main
git switch -c feature/12-them-mon
# Làm việc, kiểm tra rồi chọn các file cần commit.
git add src/
git commit -m "feat(menu): add product form"
git push -u origin feature/12-them-mon
```

Sau đó mở PR trên GitHub, liên kết `Closes #12`, chọn người review. Nếu nhánh bị xung đột, cập nhật `main` vào nhánh của mình và phối hợp với người sửa phần liên quan; không chọn ghi đè toàn bộ cho nhanh.

## Review và hoàn thành

- PR tập trung vào một thay đổi liên quan; ghi cách kiểm tra và kèm ảnh/video khi đổi giao diện.
- Cần ít nhất một người khác review: kiểm tra yêu cầu, nghiệp vụ, lỗi có thể xảy ra và khả năng chạy.
- Chạy `mvnw.cmd clean verify` trên Windows hoặc `./mvnw clean verify` trên macOS/Linux. Kiểm tra thủ công luồng giao diện liên quan; thêm test cho nghiệp vụ đáng kiểm chứng.
- Chỉ merge khi đã xử lý góp ý bắt buộc và kiểm tra tự động thành công. Ưu tiên Squash and merge, xóa nhánh task sau khi ghép.
- Task Done khi đáp ứng nghiệm thu, đã merge và kiểm tra được trong ứng dụng chung. Có thể mở Draft PR sớm để xin góp ý.
- Review luân phiên; trưởng nhóm điều phối chứ không phải người duy nhất kiểm tra mọi phần.

## Quy ước code và dữ liệu

- Dùng JDK 17+, Maven và phiên bản thư viện trong `pom.xml`; thay đổi công nghệ chung phải thống nhất qua Issue/PR.
- Tên class dùng PascalCase; biến/phương thức dùng camelCase; package chữ thường; tên code tiếng Anh, giao diện tiếng Việt. File UTF-8, Java thụt 4 dấu cách.
- Tầng UI/Presentation xử lý tương tác và cập nhật giao diện. Tầng DAO truy cập cơ sở dữ liệu.
- Giá tiền dùng số thực hoặc số nguyên chuẩn. Quy tắc làm tròn, giảm giá, hủy đơn và thanh toán phải được tuân thủ chính xác.
- Tác vụ database, mạng, in/xuất file chạy ngoài Swing EDT; cập nhật thành phần Swing trên EDT. Không chặn EDT để chờ kết quả tác vụ dài.
- Thay đổi database phải có script có thứ tự và hướng dẫn cập nhật; không chỉ sửa database riêng trên máy.
- Khi sửa cấu hình chung, điều hướng hoặc file nhiều người cùng dùng, báo nhóm và ghi tác động trong PR.
- Không commit mật khẩu, khóa API, dữ liệu khách hàng thật, file build hoặc đường dẫn riêng của máy.

## Phối hợp

- Bảng task: Todo → Doing → Review → Done. Việc chưa đủ yêu cầu hoặc còn phụ thuộc giữ ở Todo và ghi rõ lý do.
- Cập nhật Issue khi đổi trạng thái, phát hiện trở ngại hoặc thay đổi yêu cầu.
- Mỗi tuần demo bản chung ít nhất một lần. Các quyết định quan trọng được ghi trong `docs/` và liên kết Issue liên quan.
- Đánh giá đóng góp qua đầu ra, chất lượng, review và phối hợp; không chỉ đếm số commit.
