# Thiết lập GitHub & Quy trình Dự án

## 1. Cấu hình Repository
- Kho chứa: `HomuCafe`
- Ngôn ngữ & Nền tảng: Java 17+, Maven Wrapper, FlatLaf 3.4.1, MSSQL JDBC 12.6.1.
- Quy chuẩn nhánh:
  - `main`: Nhánh ổn định nộp bài.
  - `feature/pos-swing-iuh`: Nhánh phát triển hoàn thiện đồ án môn học IUH.

## 2. Quy trình làm việc nhóm
- Tạo nhánh từ `feature/pos-swing-iuh` theo cú pháp: `feature/<ma-task>`, `fix/<ten-loi>`.
- Mở Pull Request (PR) để review trước khi merge.
- Đảm bảo mã nguồn biên dịch sạch không có warning hoặc conflict markers trước khi tạo PR.
