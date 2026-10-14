# Thiết lập GitHub

## Mã nguồn và sản phẩm phát hành

Repo HomuCafe được kiểm tra ở chế độ **private** khi khởi tạo. Giữ repo private để nhóm làm việc; phát hành bản cài đặt cho người dùng là một quyết định riêng, không đòi hỏi chuyển repo sang public. Khi cần phân phối công khai, chọn kênh tải riêng phù hợp và không đưa bí mật vào bản cài đặt.

Rule kỹ thuật, hướng dẫn đóng góp và task chức năng thông thường có thể công khai nếu nhóm chọn mở mã nguồn sau này. Thông tin cá nhân, đánh giá thành viên, dữ liệu khách hàng, mật khẩu và trao đổi nhạy cảm cần giữ ngoài nội dung công khai. Kiểm tra cả lịch sử commit, Issues và PR trước khi thay đổi visibility.

## Repo giữ gì, task giữ ở đâu?

- Trong Git: mã nguồn, README, quy định, thiết kế được thống nhất, mẫu Issue/PR và CI.
- Trong GitHub Issues: task đang làm, người phụ trách, hạn dự kiến, tiêu chí nghiệm thu và trao đổi cụ thể.
- Trong GitHub Project: bảng theo dõi Issues/PR. Không tạo task độc lập chỉ trong chat.
- `docs/backlog.md` chỉ là danh sách khởi động; khi tạo Issue, dùng Issue làm nguồn theo dõi trạng thái, tránh cập nhật trùng ở nhiều nơi.

## Checklist cho người quản lý repo

### Kết quả thiết lập ngày 14/10/2026

- Đã push khung ứng dụng, quy định, mẫu Issue/PR và workflow build lên `main`; giữ repo private.
- Đã tạo bốn [Issues chuẩn bị](https://github.com/AnkjoD/HomuCafe/issues), chưa gán thành viên/deadline.
- [Build đầu tiên trên GitHub Actions](https://github.com/AnkjoD/HomuCafe/actions/runs/38525119893) đã thành công.
- Chưa bật được bảo vệ nhánh: GitHub API trả HTTP 403 và yêu cầu nâng cấp GitHub Pro hoặc đổi repo sang public. Giữ private theo định hướng hiện tại; nhóm cần tuân thủ PR/review thủ công cho tới khi có gói hỗ trợ. Không có cơ chế kỹ thuật chặn push trực tiếp trong cấu hình hiện tại.
- Chưa mời cộng tác viên vì chưa có username; chưa tạo bảng Project. Có thể giao việc trước bằng Assignee và trạng thái Issue.

Các mục dưới đây là hướng dẫn. Chỉ đánh dấu sau khi đã áp dụng và kiểm tra trên GitHub.

- [ ] Settings → Collaborators: thêm từng thành viên bằng tài khoản cá nhân, quyền đủ làm việc; hạn chế số người có quyền quản trị.
- [ ] Tạo Project dạng Board: Todo / Doing / Review / Done; liên kết Issues của repo.
- [ ] Tạo labels khi cần: `feature`, `bug`, `documentation`, `blocked`, `needs-decision`.
- [ ] Settings → Branches hoặc Rulesets: bảo vệ `main`, yêu cầu PR và một người khác approve, giải quyết trao đổi trước khi merge; chặn force push/xóa nhánh.
- [ ] Sau khi CI chạy lần đầu, yêu cầu hai checks `Build (ubuntu-latest)` và `Build (windows-latest)` trước khi merge. Đối chiếu tên hiển thị thực tế.
- [ ] Áp dụng quy tắc cho người quản trị nếu gói/cấu hình hỗ trợ; hạn chế quyền bỏ qua.
- [ ] Settings → General → Pull Requests: dùng Squash merge và tự xóa nhánh sau merge nếu nhóm thống nhất.
- [ ] Thống nhất thời hạn review và lịch demo; ghi người điều phối trong Issue hoặc tài liệu nhóm.
- [ ] Chọn giấy phép và chính sách dữ liệu trước khi phát hành bên ngoài.

Nếu gói GitHub không hỗ trợ bảo vệ nhánh cho loại repo đang dùng, ghi rõ giới hạn và duy trì review thủ công; không coi việc có file quy định là đã bật bảo vệ kỹ thuật.

## Tạo và xử lý task

Issues → New issue → chọn mẫu Task hoặc Bug. Điền đủ nội dung, gán một người phụ trách chính và thêm vào Project. Tạo nhánh từ `main`; PR ghi `Closes #<số issue>` khi hoàn thành toàn bộ task. Nếu chỉ đóng góp một phần, dùng `Refs #<số issue>`.

Mỗi PR cần có người review và kết quả kiểm tra; Issues không tự kiểm chứng việc phần mềm chạy đúng.

Tham khảo: [bảo vệ nhánh](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches), [review PR](https://docs.github.com/en/pull-requests/reference/pull-request-reviews).
