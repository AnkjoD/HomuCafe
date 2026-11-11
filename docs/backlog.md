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
