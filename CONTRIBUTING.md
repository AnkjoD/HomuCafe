# Quy định làm việc nhóm

<<<<<<< HEAD
## Nhận và thực hiện task

- Mỗi task có một Issue, một người chịu trách nhiệm chính, đầu ra, tiêu chí nghiệm thu, phụ thuộc và hạn dự kiến.
=======
## Quyền duyệt và merge — quy trình nhóm đã chọn

Nhóm giữ repo private và dùng gói hiện tại, không yêu cầu nâng cấp trả phí. Các quy định dưới đây là cam kết làm việc; GitHub hiện chưa tự chặn người có quyền write bỏ qua chúng.

1. Thành viên chỉ push lên nhánh task của mình, không push trực tiếp, force push hoặc xóa nhánh `main`.
2. Khi sẵn sàng, mở Pull Request (PR) từ nhánh task vào `main`, liên kết Issue và yêu cầu `AnkjoD` review.
3. PR của thành viên phải được trưởng nhóm `AnkjoD` duyệt bằng **Approve** trên GitHub. Thành viên khác có thể góp ý/review, nhưng việc đó không thay thế phê duyệt của trưởng nhóm.
4. Chỉ `AnkjoD` hoặc người được trưởng nhóm chỉ định rõ trong chính PR mới được bấm **Merge**. Người được ủy quyền chỉ merge sau khi PR đáp ứng điều kiện review và kiểm tra.
5. Chỉ merge khi đã giải quyết góp ý bắt buộc, kiểm tra liên quan thành công và hai build Windows/Linux đều đạt. Có commit mới sau khi duyệt thì phải yêu cầu người review kiểm tra và duyệt lại trước khi merge.
6. Với PR do `AnkjoD` tạo, cần một thành viên khác review và Approve; `AnkjoD` thực hiện merge sau đó. Không tự duyệt PR của mình.
7. Nếu lỡ đẩy code trực tiếp vào `main`, báo trưởng nhóm ngay cùng commit liên quan. Thống nhất cách khôi phục qua PR/revert khi cần; không tự force push để che hoặc sửa lịch sử chung.

Luồng thống nhất: **nhánh task → PR → review/Approve → build đạt → trưởng nhóm merge → cập nhật task**.

Collaborator có quyền write vẫn có khả năng kỹ thuật push/merge khi nhánh chưa được bảo vệ. Quy định này không phải cơ chế khóa quyền; không được coi file nội quy hoặc mẫu PR là đã bật bảo vệ nhánh.

## Nhận và thực hiện task

- Mỗi task có một Issue, mã HC-UC/BR liên quan, một người chịu trách nhiệm chính, đầu ra, nghiệm thu và phụ thuộc. Hạn chỉ gán sau khi người làm xác nhận; không bịa deadline hoặc coi ước lượng là cam kết.
>>>>>>> origin/main
- Mỗi người ưu tiên một task đang làm. Báo sớm khi bị kẹt hoặc có nguy cơ trễ, kèm điều cần hỗ trợ.
- Lấy code mới nhất từ `main`, tạo nhánh theo task: `feature/12-them-mon`, `fix/28-tong-tien`, `docs/7-luong-ban-hang`.
- Không push trực tiếp vào `main` sau lần khởi tạo. Không force push hoặc xóa nhánh chung.
- Commit theo thay đổi có ý nghĩa, ví dụ `feat(menu): add product form`, `fix(order): validate quantity`, `docs: update setup`.

```powershell
git switch main
git pull --ff-only origin main
git switch -c feature/12-them-mon
# Làm việc, kiểm tra rồi chọn các file cần commit.
<<<<<<< HEAD
git add src/
=======
git add <duong-dan-file-thuoc-task>
>>>>>>> origin/main
git commit -m "feat(menu): add product form"
git push -u origin feature/12-them-mon
```

<<<<<<< HEAD
Sau đó mở PR trên GitHub, liên kết `Closes #12`, chọn người review. Nếu nhánh bị xung đột, cập nhật `main` vào nhánh của mình và phối hợp với người sửa phần liên quan; không chọn ghi đè toàn bộ cho nhanh.
=======
Sau đó mở PR trên GitHub, liên kết `Closes #12`, yêu cầu `AnkjoD` review. Nếu nhánh bị xung đột, cập nhật `main` vào nhánh của mình và phối hợp với người sửa phần liên quan; không chọn ghi đè toàn bộ cho nhanh.
>>>>>>> origin/main

## Review và hoàn thành

- PR tập trung vào một thay đổi liên quan; ghi cách kiểm tra và kèm ảnh/video khi đổi giao diện.
<<<<<<< HEAD
- Cần ít nhất một người khác review: kiểm tra yêu cầu, nghiệp vụ, lỗi có thể xảy ra và khả năng chạy.
- Chạy `mvnw.cmd clean verify` trên Windows hoặc `./mvnw clean verify` trên macOS/Linux. Kiểm tra thủ công luồng giao diện liên quan; thêm test cho nghiệp vụ đáng kiểm chứng.
- Chỉ merge khi đã xử lý góp ý bắt buộc và kiểm tra tự động thành công. Ưu tiên Squash and merge, xóa nhánh task sau khi ghép.
- Task Done khi đáp ứng nghiệm thu, đã merge và kiểm tra được trong ứng dụng chung. Có thể mở Draft PR sớm để xin góp ý.
- Review luân phiên; trưởng nhóm điều phối chứ không phải người duy nhất kiểm tra mọi phần.
=======
- Reviewer kiểm tra yêu cầu, nghiệp vụ, lỗi có thể xảy ra và khả năng chạy; người phê duyệt thực hiện theo mục Quyền duyệt và merge bên trên.
- Chạy `mvnw.cmd clean verify` trên Windows hoặc `./mvnw clean verify` trên macOS/Linux. Kiểm tra thủ công luồng giao diện liên quan; thêm test cho nghiệp vụ đáng kiểm chứng.
- Chỉ merge khi đã xử lý góp ý bắt buộc và kiểm tra tự động thành công. Ưu tiên Squash and merge, xóa nhánh task sau khi ghép.
- Task Done khi đáp ứng nghiệm thu, đã merge và kiểm tra được trong ứng dụng chung. Có thể mở Draft PR sớm để xin góp ý.
- Các thành viên luân phiên hỗ trợ review; trưởng nhóm giữ trách nhiệm phê duyệt PR của thành viên và điều phối merge.
>>>>>>> origin/main

## Quy ước code và dữ liệu

- Dùng JDK 21, Maven và phiên bản thư viện trong `pom.xml`; thay đổi công nghệ chung phải thống nhất qua Issue/PR.
- Tên class dùng PascalCase; biến/phương thức dùng camelCase; package chữ thường; tên code tiếng Anh, giao diện tiếng Việt. File UTF-8, Java thụt 4 dấu cách.
- Controller xử lý tương tác và cập nhật giao diện. Service xử lý nghiệp vụ. Repository truy cập dữ liệu. Không đặt SQL trong Controller.
- Giá tiền dùng `BigDecimal`. Quy tắc làm tròn, giảm giá, hủy đơn và thanh toán phải được ghi rõ trước khi triển khai.
<<<<<<< HEAD
- Tác vụ database, mạng và AI chạy ngoài luồng giao diện JavaFX; cập nhật UI trên luồng JavaFX.
=======
- Tác vụ database, mạng, in/xuất file lâu và AI chạy ngoài Swing EDT; cập nhật thành phần Swing trên EDT. Không chặn EDT để chờ kết quả tác vụ dài.
>>>>>>> origin/main
- Thay đổi database phải có script có thứ tự và hướng dẫn cập nhật; không chỉ sửa database riêng trên máy.
- Khi sửa cấu hình chung, điều hướng hoặc file nhiều người cùng dùng, báo nhóm và ghi tác động trong PR.
- Không commit mật khẩu, khóa API, dữ liệu khách hàng thật, file build hoặc đường dẫn riêng của máy. Cung cấp cấu hình mẫu không chứa bí mật khi cần.

## Phối hợp

- Bảng task: Todo → Doing → Review → Done. Việc chưa đủ yêu cầu hoặc còn phụ thuộc giữ ở Todo và ghi rõ lý do.
- Cập nhật Issue khi đổi trạng thái, phát hiện trở ngại hoặc thay đổi yêu cầu.
- Mỗi tuần demo bản chung ít nhất một lần. Các quyết định quan trọng được ghi trong `docs/decisions/` và liên kết Issue liên quan.
- Đánh giá đóng góp qua đầu ra, chất lượng, review và phối hợp; không chỉ đếm số commit.
<<<<<<< HEAD
=======


## Điều kiện Ready và Done

- Đọc [lối vào tài liệu](docs/README.md), [quy tắc nghiệp vụ](docs/business-rules.md) và [kế hoạch](docs/implementation-plan.md) trước khi nhận task.
- Ready khi có scope, HC-UC/BR hoặc lý do không áp dụng, phụ thuộc, hợp đồng dữ liệu/lỗi, đầu ra và cách kiểm tra. Chưa chốt database thì không để các thành viên tự chọn database khác nhau.
- Khi thay hành vi, cập nhật use case/BR, thiết kế dữ liệu và nghiệm thu cùng PR. Không suy ra một feature có quyền chỉ từ việc có menu.
- Khi đổi tiền, quyền, kho, trạng thái hoặc chống ghi lặp, cần kiểm tra nghiệp vụ và trường hợp lỗi thích hợp. Không tạo test chỉ phản chiếu một getter hoặc màu của nút.
- Không thêm Redis, RabbitMQ, framework hoặc thay cách triển khai nếu chưa có Issue và quyết định kiến trúc. Tham khảo điều kiện mở rộng trong architecture.md.
- Chỉ stage file thuộc task; xem diff trước commit. Không gom file người khác đang làm, bài học cá nhân, secret, database thật hoặc file build.
- Test/CI phải được mô tả đúng: `verify` hiện chủ yếu biên dịch/đóng gói khi chưa có test nghiệp vụ. Số tiêu chí trong use case là kế hoạch nghiệm thu, không phải số test đã pass.
- Task chỉ Done sau nghiệm thu, review/Approve, merge và kiểm tra bản chung; không tự merge PR của mình để rút ngắn quy trình.
>>>>>>> origin/main
