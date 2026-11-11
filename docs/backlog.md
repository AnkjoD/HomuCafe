<<<<<<< HEAD
# Backlog khởi động

Danh sách này là đầu vào để tạo Issues, chưa phải phân công hoặc cam kết deadline. Task triển khai tính năng chỉ tạo sau khi nhóm chốt phạm vi và phụ thuộc.

Bốn task chuẩn bị đã được tạo, chưa gán thành viên hoặc deadline:

- [#1 Chốt phạm vi và cách triển khai](https://github.com/AnkjoD/HomuCafe/issues/1)
- [#2 Chốt luồng và quy tắc nghiệp vụ](https://github.com/AnkjoD/HomuCafe/issues/2)
- [#3 Phác thảo màn hình](https://github.com/AnkjoD/HomuCafe/issues/3)
- [#4 Thiết kế database](https://github.com/AnkjoD/HomuCafe/issues/4)

Dùng Issues để theo dõi trạng thái; bảng dưới chỉ mô tả thứ tự và phụ thuộc dự kiến.

| Thứ tự | Task | Đầu ra / nghiệm thu | Phụ thuộc |
| --- | --- | --- | --- |
| 1 | Chốt phạm vi và cách triển khai | Cập nhật `scope.md`, chốt một/nhiều máy, bán tại bàn/mang đi và chức năng ưu tiên | Thảo luận nhóm |
| 2 | Vẽ luồng và quy tắc nghiệp vụ | Luồng bán hàng, trạng thái đơn, quyền, cách tính tiền/sửa/hủy được nhóm duyệt | 1 |
| 3 | Phác thảo màn hình | Màn hình chính và điều hướng khớp các luồng, có trạng thái trống/lỗi | 2 |
| 4 | Thiết kế database | ERD, từ điển dữ liệu, ràng buộc và script tạo dữ liệu mẫu được review | 2 |
| 5 | Chốt hợp đồng giữa các phần | Dữ liệu đầu vào/đầu ra, lỗi và chữ ký phương thức chung cho luồng bán hàng | 2–4 |
| 6 | Làm một lát cắt bán hàng | Chọn món → tạo đơn → lưu → mở lại; phạm vi nhỏ được chạy trên máy khác | 3–5 |
| 7 | Chia task tính năng còn lại | Issues nhỏ có người làm, nghiệm thu, phụ thuộc và mốc dự kiến | 6 |

Trước task đầu tiên, mỗi thành viên chạy khung trên máy mình và báo kết quả. Trưởng nhóm thiết lập cộng tác viên, bảng task và bảo vệ nhánh theo `github-setup.md`.
=======
# Backlog theo feature

Cập nhật 22/10/2026. Chủ dự án đã đồng ý triển khai theo cách gom 14 feature từ 53 use case. Issue tổng theo dõi feature; task con là đơn vị nhận việc. Chưa tự chọn database/cách triển khai hoặc gán thành viên, deadline. Các quyết định còn mở vẫn là điều kiện của task liên quan.

## Chuẩn bị dùng chung

- [#1 Phạm vi và cách triển khai](https://github.com/AnkjoD/HomuCafe/issues/1)
- [#2 Quy tắc nghiệp vụ](https://github.com/AnkjoD/HomuCafe/issues/2)
- [#3 Rà giao diện](https://github.com/AnkjoD/HomuCafe/issues/3)
- [#4 Schema, migration và seed](https://github.com/AnkjoD/HomuCafe/issues/4)
- [#7 Hợp đồng Service và transaction](https://github.com/AnkjoD/HomuCafe/issues/7)
- [#8 Demo tích hợp](https://github.com/AnkjoD/HomuCafe/issues/8) và [#9 Nghiệm thu](https://github.com/AnkjoD/HomuCafe/issues/9) theo dõi xuyên feature, không thay task con.

## Feature và task

### [F01 Tài khoản và quyền](https://github.com/AnkjoD/HomuCafe/issues/10) — M1

- [F01-A Đăng nhập, đăng xuất và đổi mật khẩu](https://github.com/AnkjoD/HomuCafe/issues/24) — phụ thuộc: #4, #7.
- [F01-B Quản trị tài khoản và kiểm tra quyền tại Service](https://github.com/AnkjoD/HomuCafe/issues/25) — phụ thuộc: [#24](https://github.com/AnkjoD/HomuCafe/issues/24).

### [F02 Ca thu ngân và đối soát](https://github.com/AnkjoD/HomuCafe/issues/11) — M1–M2

- [F02-A Mở ca và xem ca/lịch của bản thân](https://github.com/AnkjoD/HomuCafe/issues/26) — phụ thuộc: [#25](https://github.com/AnkjoD/HomuCafe/issues/25), #2.
- [F02-B Nạp rút quỹ, chốt ca và đối soát](https://github.com/AnkjoD/HomuCafe/issues/27) — phụ thuộc: [#26](https://github.com/AnkjoD/HomuCafe/issues/26), [#33](https://github.com/AnkjoD/HomuCafe/issues/33).

### [F03 Đơn hàng và bàn](https://github.com/AnkjoD/HomuCafe/issues/12) — M1

- [F03-A Danh sách bàn, khu vực và đơn mở](https://github.com/AnkjoD/HomuCafe/issues/28) — phụ thuộc: [#25](https://github.com/AnkjoD/HomuCafe/issues/25), #4.
- [F03-B Tạo, sửa và mở lại đơn chưa gửi](https://github.com/AnkjoD/HomuCafe/issues/29) — phụ thuộc: [#28](https://github.com/AnkjoD/HomuCafe/issues/28), [#36](https://github.com/AnkjoD/HomuCafe/issues/36), [#26](https://github.com/AnkjoD/HomuCafe/issues/26), #7.
- [F03-C Gửi đợt pha chế tại bàn](https://github.com/AnkjoD/HomuCafe/issues/30) — phụ thuộc: [#29](https://github.com/AnkjoD/HomuCafe/issues/29), #7.

### [F04 Thanh toán và xử lý sau bán](https://github.com/AnkjoD/HomuCafe/issues/13) — M1–M2

- [F04-A Thu đủ một lần và tạo chứng từ](https://github.com/AnkjoD/HomuCafe/issues/31) — phụ thuộc: [#29](https://github.com/AnkjoD/HomuCafe/issues/29), [#26](https://github.com/AnkjoD/HomuCafe/issues/26), #2, #7.
- [F04-B Tra lịch sử, xem chứng từ và in lại](https://github.com/AnkjoD/HomuCafe/issues/32) — phụ thuộc: [#31](https://github.com/AnkjoD/HomuCafe/issues/31).
- [F04-C Yêu cầu/duyệt hủy và hoàn toàn đơn](https://github.com/AnkjoD/HomuCafe/issues/33) — phụ thuộc: [#31](https://github.com/AnkjoD/HomuCafe/issues/31), [#34](https://github.com/AnkjoD/HomuCafe/issues/34), #2.

### [F05 Điều phối pha chế](https://github.com/AnkjoD/HomuCafe/issues/14) — M1–M2

- [F05-A Hàng đợi, bắt đầu pha, sẵn sàng và bàn giao](https://github.com/AnkjoD/HomuCafe/issues/34) — phụ thuộc: [#30](https://github.com/AnkjoD/HomuCafe/issues/30), [#25](https://github.com/AnkjoD/HomuCafe/issues/25).
- [F05-B Đánh dấu món tạm hết và cập nhật cho bán hàng](https://github.com/AnkjoD/HomuCafe/issues/35) — phụ thuộc: [#36](https://github.com/AnkjoD/HomuCafe/issues/36), [#34](https://github.com/AnkjoD/HomuCafe/issues/34).

### [F06 Thực đơn và công thức](https://github.com/AnkjoD/HomuCafe/issues/15) — M1–M3

- [F06-A Đọc menu từ DB cho màn hình bán hàng](https://github.com/AnkjoD/HomuCafe/issues/36) — phụ thuộc: #4, #7, [#25](https://github.com/AnkjoD/HomuCafe/issues/25).
- [F06-B Quản trị nhóm, món, biến thể và giá](https://github.com/AnkjoD/HomuCafe/issues/37) — phụ thuộc: [#36](https://github.com/AnkjoD/HomuCafe/issues/36).
- [F06-C Thiết lập công thức theo biến thể và tùy chọn](https://github.com/AnkjoD/HomuCafe/issues/38) — phụ thuộc: [#37](https://github.com/AnkjoD/HomuCafe/issues/37), [#39](https://github.com/AnkjoD/HomuCafe/issues/39).

### [F07 Kiểm soát nguyên liệu](https://github.com/AnkjoD/HomuCafe/issues/16) — M3

- [F07-A Danh mục nguyên liệu, đơn vị và số tồn](https://github.com/AnkjoD/HomuCafe/issues/39) — phụ thuộc: #4, #7, [#25](https://github.com/AnkjoD/HomuCafe/issues/25).
- [F07-B Kiểm kê, điều chỉnh và ghi hao hụt](https://github.com/AnkjoD/HomuCafe/issues/40) — phụ thuộc: [#39](https://github.com/AnkjoD/HomuCafe/issues/39), [#41](https://github.com/AnkjoD/HomuCafe/issues/41), #2.
- [F07-C Tích hợp giữ, xuất dùng và giải phóng nguyên liệu](https://github.com/AnkjoD/HomuCafe/issues/41) — phụ thuộc: [#39](https://github.com/AnkjoD/HomuCafe/issues/39), [#38](https://github.com/AnkjoD/HomuCafe/issues/38), [#30](https://github.com/AnkjoD/HomuCafe/issues/30), [#34](https://github.com/AnkjoD/HomuCafe/issues/34), [#33](https://github.com/AnkjoD/HomuCafe/issues/33), #7.

### [F08 Nhà cung cấp và nhập hàng](https://github.com/AnkjoD/HomuCafe/issues/17) — M3

- [F08-A Nhà cung cấp và phiếu nhập nháp](https://github.com/AnkjoD/HomuCafe/issues/42) — phụ thuộc: [#39](https://github.com/AnkjoD/HomuCafe/issues/39), [#25](https://github.com/AnkjoD/HomuCafe/issues/25).
- [F08-B Xác nhận nhập và ghi sổ kho](https://github.com/AnkjoD/HomuCafe/issues/43) — phụ thuộc: [#42](https://github.com/AnkjoD/HomuCafe/issues/42), #7.

### [F09 Voucher](https://github.com/AnkjoD/HomuCafe/issues/18) — M3

- [F09-A Quản trị chính sách voucher](https://github.com/AnkjoD/HomuCafe/issues/44) — phụ thuộc: [#25](https://github.com/AnkjoD/HomuCafe/issues/25), #2, #4.
- [F09-B Xem trước và áp dụng voucher khi thu](https://github.com/AnkjoD/HomuCafe/issues/45) — phụ thuộc: [#44](https://github.com/AnkjoD/HomuCafe/issues/44), [#31](https://github.com/AnkjoD/HomuCafe/issues/31), #7.

### [F10 Hồ sơ và lịch nhân sự](https://github.com/AnkjoD/HomuCafe/issues/19) — M3

- [F10-A Quản lý hồ sơ nhân viên](https://github.com/AnkjoD/HomuCafe/issues/46) — phụ thuộc: [#25](https://github.com/AnkjoD/HomuCafe/issues/25), #4.
- [F10-B Xếp lịch và xem lịch theo quyền](https://github.com/AnkjoD/HomuCafe/issues/47) — phụ thuộc: [#46](https://github.com/AnkjoD/HomuCafe/issues/46), [#26](https://github.com/AnkjoD/HomuCafe/issues/26).

### [F11 Cấu hình và quản trị dữ liệu](https://github.com/AnkjoD/HomuCafe/issues/20) — M4

- [F11-A Cấu hình quán và thiết bị](https://github.com/AnkjoD/HomuCafe/issues/48) — phụ thuộc: [#25](https://github.com/AnkjoD/HomuCafe/issues/25), #1.
- [F11-B Sao lưu và khôi phục thử trên môi trường sạch](https://github.com/AnkjoD/HomuCafe/issues/49) — phụ thuộc: [#48](https://github.com/AnkjoD/HomuCafe/issues/48), #4, #9.
- [F11-C Tra nhật ký thao tác và đóng gói bản chạy thử](https://github.com/AnkjoD/HomuCafe/issues/50) — phụ thuộc: [#49](https://github.com/AnkjoD/HomuCafe/issues/49), [#55](https://github.com/AnkjoD/HomuCafe/issues/55), #9.

### [F12 Hồ sơ khách hàng nội bộ](https://github.com/AnkjoD/HomuCafe/issues/21) — M3

- [F12-A Chủ quán quản lý hồ sơ khách](https://github.com/AnkjoD/HomuCafe/issues/51) — phụ thuộc: [#25](https://github.com/AnkjoD/HomuCafe/issues/25), #4.
- [F12-B Tìm kiếm, ngừng sử dụng và kiểm chứng riêng tư](https://github.com/AnkjoD/HomuCafe/issues/52) — phụ thuộc: [#51](https://github.com/AnkjoD/HomuCafe/issues/51).

### [F13 Sổ tiền và dòng tiền](https://github.com/AnkjoD/HomuCafe/issues/22) — M3

- [F13-A Ghi và điều chỉnh khoản thu chi ngoài bán hàng](https://github.com/AnkjoD/HomuCafe/issues/53) — phụ thuộc: [#25](https://github.com/AnkjoD/HomuCafe/issues/25), #2, #4.
- [F13-B Báo cáo dòng tiền dành cho chủ quán](https://github.com/AnkjoD/HomuCafe/issues/54) — phụ thuộc: [#53](https://github.com/AnkjoD/HomuCafe/issues/53), [#33](https://github.com/AnkjoD/HomuCafe/issues/33).

### [F14 Báo cáo vận hành và xuất số liệu](https://github.com/AnkjoD/HomuCafe/issues/23) — M2–M3

- [F14-A Báo cáo doanh thu, món và ca từ giao dịch thật](https://github.com/AnkjoD/HomuCafe/issues/55) — phụ thuộc: [#33](https://github.com/AnkjoD/HomuCafe/issues/33), [#27](https://github.com/AnkjoD/HomuCafe/issues/27), #2, #9.
- [F14-B Báo cáo kho và xuất dữ liệu theo quyền](https://github.com/AnkjoD/HomuCafe/issues/56) — phụ thuộc: [#55](https://github.com/AnkjoD/HomuCafe/issues/55), [#40](https://github.com/AnkjoD/HomuCafe/issues/40), [#43](https://github.com/AnkjoD/HomuCafe/issues/43).

## Cách nhận việc

1. Hoàn thành quyết định/schema/hợp đồng chung liên quan. Bắt đầu F01-A, tiếp F01-B; sau đó có thể làm F06-A, F02-A và F03-A song song khi đủ đầu vào. Tiếp F03-B/C, F05-A và F04-A/B để có luồng demo đầu.
2. Trưởng nhóm gán Assignee sau khi thành viên nhận việc; mỗi người ưu tiên một task đang làm. Task chưa đủ điều kiện giữ chờ, không coi mọi Issue mở là có thể code ngay.
3. Làm xuyên các tầng cho đầu ra task. Schema/hợp đồng chung review tập trung; không chia cố định mỗi người một tầng. Task còn lớn có thể chia tiếp qua PR/Issue theo đầu ra và ước lượng của người nhận.
4. Nhánh `feature/<issue>-<ten-ngan>` → PR liên kết `Closes #<task>` → review/Approve → build/test phù hợp → trưởng nhóm merge. Không dùng `Closes` cho issue tổng khi chỉ xong một task.
5. Cập nhật checklist issue tổng khi task đã tích hợp; đóng feature sau demo và nghiệm thu chung. Dữ liệu mẫu và thiết kế chưa phải nghiệp vụ đã chạy.

Đây là danh mục liên kết; trạng thái hiện hành và người phụ trách nằm trên GitHub Issues. Xem [kế hoạch](implementation-plan.md), [quy tắc nghiệp vụ](business-rules.md) và [quy định nhóm](../CONTRIBUTING.md).
>>>>>>> origin/main
