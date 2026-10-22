# Kế hoạch triển khai HomuCafe

Ngày cập nhật 22/10/2026. Kế hoạch đi từ 53 mục tiêu người dùng đã mô tả đến lát cắt chạy được, rồi mới mở rộng quản lý. Chủ dự án đã đồng ý triển khai theo 14 feature; [backlog](backlog.md) liên kết issue tổng và task con tương ứng. Chưa có đủ thông tin số thành viên, thời gian rảnh và deadline để cam kết lịch theo ngày. Các mốc dưới đây dùng điều kiện vào/ra; trưởng nhóm gán người và hạn sau khi có ước lượng.

## 🎯 Các mốc và điều kiện nghiệm thu

| Mốc | Đầu vào | Đầu ra bắt buộc trước chuyển mốc |
| --- | --- | --- |
| M0 Baseline và hợp đồng | Bộ use case 0.3, quy tắc BR01–BR15, OPEN01–OPEN07 | Chốt bản đầu, database/triển khai; ERD và migration nền; hợp đồng đầu vào/kết quả/lỗi; danh mục test và quyết định còn hoãn |
| M1 Một luồng bán có lưu | M0, khung Swing và build chung | Tài khoản/quyền tối thiểu, mở ca, menu mẫu từ DB, tạo/lưu/mở lại đơn, phiếu pha chế và thu tiền; chạy lại app vẫn còn dữ liệu |
| M2 Bán hàng tin cậy | M1 và hợp đồng xử lý ngoại lệ | Hủy/hoàn/in lại/chốt ca, báo cáo từ giao dịch thật; test thu lặp, xung đột, timeout và lịch sử giá; không lấy dữ liệu giả làm nguồn |
| M3 Quản lý quán | M2 và từng feature đủ điều kiện | Công thức/kho/nhập/kiểm kê/hao hụt; voucher; hồ sơ/lịch; khách nội bộ, thu chi và báo cáo tương ứng theo thứ tự phụ thuộc |
| M4 Phát hành thử | Luồng và quyền đã nghiệm thu | Backup/restore đã thử, dữ liệu demo sạch, bản cài/chạy máy khác, hướng dẫn cập nhật, nhật ký lỗi và checklist phát hành |

Không cần xong mọi phần M3 để demo lát cắt M1. Muốn đưa ra quán dùng thật phải xác định phạm vi phát hành và nghiệm thu đầy đủ phạm vi đó, bao gồm ngoại lệ và phục hồi. Phần chưa làm được ẩn/vô hiệu hóa có nhãn rõ, không cho người dùng tưởng đã ghi thành công.

## 🧩 Thứ tự và phụ thuộc công việc

| Mã việc | Phạm vi / use case | Phụ thuộc | Tiêu chí ra |
| --- | --- | --- | --- |
| PLAN01 | Chốt phạm vi và cách triển khai | Bộ use case | OPEN01 có quyết định; P1/P2 được điều chỉnh thành release scope |
| PLAN02 | Chốt quy tắc, quyền, tiền/ca/kho | PLAN01; làm song song phần độc lập | OPEN02–OPEN06 có quyết định cho lát cắt, sơ đồ trạng thái không mâu thuẫn |
| PLAN03 | Rà View và các màn hình thiếu | PLAN02 | Thanh toán, lịch sử, xác thực/menu/ca có bản mẫu và trạng thái lỗi; đối chiếu quyền |
| PLAN04 | ERD, migration và fixture | PLAN01–02 | Khóa lịch sử, unique thu/hoàn, requestId, version, đơn vị và dữ liệu mẫu có thể tạo lại |
| PLAN05 | Hợp đồng giữa các phần | PLAN02–04 | Các kiểu input/result/error, nơi sở hữu transaction và test hợp đồng được review |
| IMPL01 | Đăng nhập/quyền và ca tối thiểu; UC001–005,038–039 | PLAN04–05 | Tài khoản thật; từ chối vượt quyền; một ca mở/quầy; kiểm chứng không chỉ bằng menu |
| IMPL02 | Menu và đơn; UC008–011,025–026 | IMPL01; schema/hợp đồng đã chốt | Tạo/sửa hợp lệ, snapshot giá, lưu/mở lại, gửi đúng đợt; kho chưa bật không giả trừ kho |
| IMPL03 | Pha chế và bàn giao; UC020–023 | IMPL02 | Chuyển trạng thái hợp lệ, không lặp; đóng đơn/bàn đúng điều kiện tiền và phục vụ |
| IMPL04 | Thu và chứng từ; UC013–015 | IMPL02; phối hợp IMPL03 | Thu một lần, phiếu bền vững, in lại không thu lại; mang đi tạo đợt đầu cùng transaction |
| IMPL05 | Hủy/hoàn và đối soát; UC005–007,016–019 | IMPL03–04 | Quyền, lý do, giữ lịch sử và ca thực thu/hoàn; test lỗi và xung đột |
| IMPL06 | Báo cáo thật; UC048–051,053 theo phạm vi | IMPL04–05, chỉ tiêu đã chốt | Bảng/biểu đồ khớp nguồn và bộ lọc; quyền xuất không vượt quyền xem |
| IMPL07 | Thực đơn/kho/nhập; UC027,029–034,050 | M2; chốt đơn vị, công thức và kiểm kê | Tích hợp giữ khi gửi/xuất khi bắt đầu; nhập một lần; kiểm kê không ghi đè biến động |
| IMPL08 | Voucher; UC012,035 | OPEN02 và IMPL04 | Tính đúng; kiểm tra và ghi lượt cùng thu; test hết lượt/đồng thời/hoàn đơn |
| IMPL09 | Lịch/hồ sơ/khách/thu chi; UC028,036–037,044–047,052 | Quyền và nguồn dữ liệu tương ứng | Quyền khách chủ quán; phân biệt hồ sơ/quyền; không cộng trùng payment/chi nhập |
| REL01 | Quản trị và phát hành; UC040–043 | Bản release scope hoạt động | Restore thử, chạy máy khác, cấu hình mẫu, hướng dẫn và đánh giá đo thực tế |

Các nhãn IMPL là gói công việc, không phải giao nguyên gói lớn cho một người. Backlog đã chia task con theo F01–F14; mã IMPL giữ để giải thích thứ tự tích hợp, không tạo một bộ task trùng. Mỗi task nên có đầu ra độc lập khoảng 1–3 ngày công ước lượng; người nhận chia tiếp nếu còn lớn. Đó là hướng dẫn kích thước, không phải hạn bắt buộc. Không tạo 53 task theo đúng 53 use case nếu nhiều ca cần một lát cắt chung.

## 👥 Phân công sau khi có thành viên

| Trách nhiệm | Người phụ trách | Cách phối hợp |
| --- | --- | --- |
| Điều phối phạm vi, review và merge | AnkjoD | Chốt người làm từng Issue và ưu tiên |
| Giao diện/Controller | Chưa gán | Theo hợp đồng; mock dữ liệu rõ; không tự viết SQL |
| Service và transaction | Chưa gán | Cùng người dữ liệu xác định khóa, ràng buộc và lỗi |
| Database/Repository | Chưa gán | Migration review chung; fixture tái lập được |
| Kiểm thử/nghiệm thu | Luân phiên, chưa gán tên | Reviewer khác tác giả; bám các ca lỗi của use case |

Một người có thể kiêm nhiều trách nhiệm. Mỗi Issue vẫn có một người chịu trách nhiệm chính. Không chia cố định mỗi người một tầng rồi đợi toàn bộ tầng kia hoàn tất; sau khi hợp đồng ổn, ưu tiên hoàn thành chức năng xuyên View–Service–DB.

## ✅ Điều kiện nhận và hoàn thành task

Ready: có mã HC-UC/BR, đầu ra quan sát được, scope, dependencies, input/result/error, dữ liệu/thiết kế cần dùng và cách kiểm tra. Task chuẩn bị chưa gắn một UC có thể ghi lý do thay vì gán mã sai.

Done: đạt nghiệm thu, có test phù hợp cho tiền/quyền/trạng thái/ghi lặp hoặc lý do không áp dụng, build đạt, tài liệu/migration được cập nhật, review/Approve đúng quy trình, merge và kiểm tra trên bản chung. Giao diện đẹp hoặc `verify` thành công khi chưa có test nghiệp vụ chưa đủ để Done một task nghiệp vụ.

## 🗓️ Nhịp làm việc đề xuất

Mỗi người cập nhật Issue khi bắt đầu, gặp blocker và gửi PR. Mỗi tuần có một buổi demo lát cắt chung do nhóm thống nhất giờ. Review mục tiêu trong một ngày làm việc khi có thể; nếu chưa kịp thì báo trên Issue. Chỉ gán deadline thật sau khi người làm xác nhận ước lượng và lịch học/thi.

Issues là nguồn trạng thái hiện hành; [backlog](backlog.md) giữ liên kết và các gói còn chờ. Không ghi “đã hoàn thành” khi việc mới có tài liệu hoặc chưa merge.
