-- ========================================================
-- CƠ SỞ DỮ LIỆU: QUẢN LÝ QUÁN CÀ PHÊ HOMUCAFE (POS & TABLE RESERVATION)
-- HỆ QUẢN TRỊ CSDL: MICROSOFT SQL SERVER 2019 / 2022
-- CHUẨN HÓA DỮ LIỆU: DẠNG CHUẨN 3 (3NF)
-- ĐỒNG BỘ 1-1 VỚI CLASS DIAGRAM & ERD PHYSICAL MODEL (12 THỰC THỂ)
-- ========================================================

USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = 'QuanLyCoffeePOS_HomuCafe')
BEGIN
    ALTER DATABASE QuanLyCoffeePOS_HomuCafe SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLyCoffeePOS_HomuCafe;
END
GO

CREATE DATABASE QuanLyCoffeePOS_HomuCafe;
GO

USE QuanLyCoffeePOS_HomuCafe;
GO

-- ========================================================
-- 1. BẢNG CHỨC VỤ
-- ========================================================
CREATE TABLE CHUC_VU (
    maChucVu NVARCHAR(10) PRIMARY KEY,
    tenChucVu NVARCHAR(50) NOT NULL
);
GO

-- ========================================================
-- 2. BẢNG NHÂN VIÊN
-- ========================================================
CREATE TABLE NHAN_VIEN (
    maNV NVARCHAR(10) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    soDienThoai VARCHAR(10) NOT NULL UNIQUE,
    cccd VARCHAR(12) NOT NULL UNIQUE,
    trangThaiLamViec BIT NOT NULL DEFAULT 1,
    maChucVu NVARCHAR(10) NOT NULL FOREIGN KEY REFERENCES CHUC_VU(maChucVu)
);
GO

-- ========================================================
-- 3. BẢNG TÀI KHOẢN (Quan hệ 1-1 với NHAN_VIEN)
-- ========================================================
CREATE TABLE TAI_KHOAN (
    tenDangNhap NVARCHAR(50) PRIMARY KEY,
    matKhau NVARCHAR(255) NOT NULL,
    phanQuyen NVARCHAR(20) NOT NULL CHECK (phanQuyen IN ('ADMIN', 'STAFF')),
    maNV NVARCHAR(10) NOT NULL UNIQUE FOREIGN KEY REFERENCES NHAN_VIEN(maNV) ON DELETE CASCADE
);
GO

-- ========================================================
-- 4. BẢNG MÓN ĐỒ UỐNG / BÁNH NGỌT
-- ========================================================
CREATE TABLE MON (
    maMon NVARCHAR(10) PRIMARY KEY,
    tenMon NVARCHAR(100) NOT NULL,
    donGiaGoc DECIMAL(18,2) NOT NULL CHECK (donGiaGoc > 0),
    hinhAnh NVARCHAR(255) NULL,
    trangThai BIT NOT NULL DEFAULT 1,
    loaiMon NVARCHAR(50) NOT NULL
);
GO

-- ========================================================
-- 5. BẢNG KHÁCH HÀNG THÀNH VIÊN
-- ========================================================
CREATE TABLE KHACH_HANG (
    maKH NVARCHAR(10) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    soDienThoai VARCHAR(10) NOT NULL UNIQUE,
    diemTichLuy INT NOT NULL DEFAULT 0 CHECK (diemTichLuy >= 0)
);
GO

-- ========================================================
-- 6. BẢNG VOUCHER KHUYẾN MÃI
-- ========================================================
CREATE TABLE VOUCHER (
    maVoucher NVARCHAR(20) PRIMARY KEY,
    hinhThucGiam NVARCHAR(20) NOT NULL CHECK (hinhThucGiam IN (N'PHAN_TRAM', N'TIEN_MAT')),
    giaTriGiam DECIMAL(18,2) NOT NULL CHECK (giaTriGiam > 0),
    giamToiDa DECIMAL(18,2) NOT NULL CHECK (giamToiDa >= 0),
    donToiThieu DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (donToiThieu >= 0),
    soLuotDung INT NOT NULL DEFAULT 0,
    gioiHanLuot INT NOT NULL DEFAULT 100,
    ngayBatDau DATETIME NOT NULL DEFAULT GETDATE(),
    ngayKetThuc DATETIME NOT NULL,
    trangThai BIT NOT NULL DEFAULT 1
);
GO

-- ========================================================
-- 7. BẢNG BÀN PHỤC VỤ (12 bàn & Nghiệp vụ Giữ/Đặt bàn trước)
-- ========================================================
CREATE TABLE BAN (
    maBan NVARCHAR(10) PRIMARY KEY,
    tenBan NVARCHAR(50) NOT NULL,
    soChoNgoi INT NOT NULL CHECK (soChoNgoi > 0),
    trangThai NVARCHAR(30) NOT NULL CHECK (trangThai IN (N'TRONG', N'DANG_PHUC_VU', N'DA_DAT'))
);
GO

-- ========================================================
-- 8. BẢNG PHƯƠNG THỨC THANH TOÁN (Tách riêng bảng theo góp ý GV)
-- ========================================================
CREATE TABLE PHUONG_THUC_THANH_TOAN (
    maPhuongThuc NVARCHAR(10) PRIMARY KEY,
    tenPhuongThuc NVARCHAR(50) NOT NULL,
    trangThai BIT NOT NULL DEFAULT 1
);
GO

-- ========================================================
-- 9. BẢNG PHIẾU ĐẶT BÀN (Nghiệp vụ Đặt bàn trước)
-- ========================================================
CREATE TABLE PHIEU_DAT_BAN (
    maPhieu NVARCHAR(20) PRIMARY KEY,
    ngayDat DATETIME NOT NULL DEFAULT GETDATE(),
    ngayDen DATETIME NOT NULL,
    soLuongKhach INT NOT NULL CHECK (soLuongKhach > 0),
    tienCoc DECIMAL(18,2) NOT NULL DEFAULT 0,
    ghiChu NVARCHAR(255) NULL,
    trangThai NVARCHAR(30) NOT NULL CHECK (trangThai IN (N'DA_DAT', N'DA_NHAN', N'DA_HUY')),
    maKH NVARCHAR(10) NOT NULL FOREIGN KEY REFERENCES KHACH_HANG(maKH),
    maNV NVARCHAR(10) NOT NULL FOREIGN KEY REFERENCES NHAN_VIEN(maNV)
);
GO

-- ========================================================
-- 10. BẢNG CHI TIẾT PHIẾU ĐẶT BÀN (Khóa chính kết hợp maPhieu, maBan)
-- ========================================================
CREATE TABLE CHI_TIET_PHIEU_DAT_BAN (
    maPhieu NVARCHAR(20) NOT NULL FOREIGN KEY REFERENCES PHIEU_DAT_BAN(maPhieu) ON DELETE CASCADE,
    maBan NVARCHAR(10) NOT NULL FOREIGN KEY REFERENCES BAN(maBan),
    ghiChu NVARCHAR(255) NULL,
    PRIMARY KEY (maPhieu, maBan)
);
GO

-- ========================================================
-- 11. BẢNG HÓA ĐƠN (soTheRung 1-20; liên kết PHUONG_THUC_THANH_TOAN)
-- ========================================================
CREATE TABLE HOA_DON (
    maHD NVARCHAR(20) PRIMARY KEY,
    ngayLap DATETIME NOT NULL DEFAULT GETDATE(),
    loaiDon NVARCHAR(20) NOT NULL CHECK (loaiDon IN (N'TAI_QUAN', N'MANG_DI')),
    soTheRung INT NULL CHECK (soTheRung IS NULL OR soTheRung BETWEEN 1 AND 20),
    giamGia DECIMAL(18,2) NOT NULL DEFAULT 0,
    trangThaiDon NVARCHAR(30) NOT NULL CHECK (trangThaiDon IN (N'DANG_PHA_CHE', N'CHO_GIAO', N'HOAN_TAT', N'DA_HUY')),
    maBan NVARCHAR(10) NULL FOREIGN KEY REFERENCES BAN(maBan),
    maNV NVARCHAR(10) NOT NULL FOREIGN KEY REFERENCES NHAN_VIEN(maNV),
    maKH NVARCHAR(10) NULL FOREIGN KEY REFERENCES KHACH_HANG(maKH),
    maVoucher NVARCHAR(20) NULL FOREIGN KEY REFERENCES VOUCHER(maVoucher),
    maPhuongThuc NVARCHAR(10) NOT NULL FOREIGN KEY REFERENCES PHUONG_THUC_THANH_TOAN(maPhuongThuc)
);
GO

-- ========================================================
-- 12. BẢNG CHI TIẾT HÓA ĐƠN (Đã có maCTHD IDENTITY để cho phép cùng món/size khác topping/đường/đá)
-- ========================================================
CREATE TABLE CHI_TIET_HOA_DON (
    maCTHD INT IDENTITY(1,1) PRIMARY KEY,
    maHD NVARCHAR(20) NOT NULL FOREIGN KEY REFERENCES HOA_DON(maHD) ON DELETE CASCADE,
    maMon NVARCHAR(10) NOT NULL FOREIGN KEY REFERENCES MON(maMon),
    kichCo VARCHAR(5) NOT NULL CHECK (kichCo IN ('S', 'M', 'L')),
    soLuong INT NOT NULL CHECK (soLuong > 0),
    donGiaThucTe DECIMAL(18,2) NOT NULL,
    ghiChu NVARCHAR(255) NULL         -- Ghi chú mức đường/đá/topping tùy biến
);
GO

-- ========================================================
-- DỮ LIỆU MẪU BAN ĐẦU (SEED DATA)
-- ========================================================

-- 1. Chức vụ
INSERT INTO CHUC_VU (maChucVu, tenChucVu) VALUES 
('CV01', N'Quản lý'),
('CV02', N'Thu ngân'),
('CV03', N'Pha chế');

-- 2. Nhân viên
INSERT INTO NHAN_VIEN (maNV, hoTen, soDienThoai, cccd, trangThaiLamViec, maChucVu) VALUES 
('NV01', N'Trần Văn An', '0901234567', '079204001234', 1, 'CV02'),
('NV02', N'Lê Thị Hoa', '0912345678', '079204005678', 1, 'CV03'),
('NV03', N'Phạm Minh Vũ', '0987654321', '079204009012', 1, 'CV02'),
('NV04', N'Nguyễn Bảo Trọng', '0938112233', '079204003456', 1, 'CV01'),
('NV05', N'Võ Hiếu Tài', '0977889900', '079204007890', 1, 'CV01');

-- 3. Tài khoản
INSERT INTO TAI_KHOAN (tenDangNhap, matKhau, phanQuyen, maNV) VALUES 
('admin', '123456', 'ADMIN', 'NV04'),
('thungan01', '123456', 'STAFF', 'NV01'),
('bar01', '123456', 'STAFF', 'NV02'),
('quanly01', '123456', 'ADMIN', 'NV05');

-- 4. Thực đơn món đồ uống
INSERT INTO MON (maMon, tenMon, donGiaGoc, hinhAnh, trangThai, loaiMon) VALUES 
('CF001', N'Cà Phê Đen Đá', 29000, 'images/cf_den_da.jpg', 1, N'Cà phê'),
('CF002', N'Cà Phê Sữa Đá', 35000, 'images/cf_sua_da.jpg', 1, N'Cà phê'),
('CF003', N'Bạc Xỉu 3 Tầng', 39000, 'images/bac_xiu.jpg', 1, N'Cà phê'),
('CF004', N'Caramel Macchiato', 52000, 'images/caramel_macchiato.jpg', 1, N'Cà phê'),
('TEA01', N'Trà Đào Cam Sả', 45000, 'images/tra_dao_cam_sa.jpg', 1, N'Trà trái cây'),
('TEA02', N'Trà Sen Vàng Kem', 49000, 'images/tra_sen_vang.jpg', 1, N'Trà trái cây'),
('TEA03', N'Trà Vải Hoa Hồng', 46000, 'images/tra_vai_hoa_hong.jpg', 1, N'Trà trái cây'),
('MILK01', N'Trà Sữa Trân Châu', 42000, 'images/ts_tran_chau.jpg', 1, N'Trà sữa'),
('MILK02', N'Trà Sữa Ô Long Nướng', 45000, 'images/ts_olong.jpg', 1, N'Trà sữa'),
('ICE01', N'Homu Matcha Đá Xay', 55000, 'images/matcha_da_xay.jpg', 1, N'Đá xay'),
('ICE02', N'Cookie & Cream Đá Xay', 58000, 'images/cookie_da_xay.jpg', 1, N'Đá xay'),
('CAKE01', N'Bánh Tiramisu Homu', 39000, 'images/tiramisu.jpg', 1, N'Bánh ngọt'),
('CAKE02', N'Croissant Trứng Muối', 35000, 'images/croissant.jpg', 1, N'Bánh ngọt');

-- 5. Khách hàng thành viên
INSERT INTO KHACH_HANG (maKH, hoTen, soDienThoai, diemTichLuy) VALUES 
('KH0001', N'Nguyễn Hoàng Nam', '0987654321', 35),
('KH0002', N'Trần Ngọc Ánh', '0909123456', 50),
('KH0003', N'Lê Minh Khôi', '0918889999', 15);

-- 6. Voucher khuyến mãi
INSERT INTO VOUCHER (maVoucher, hinhThucGiam, giaTriGiam, giamToiDa, donToiThieu, soLuotDung, gioiHanLuot, ngayBatDau, ngayKetThuc, trangThai) VALUES 
('HOMU10', N'PHAN_TRAM', 10, 30000, 50000, 5, 100, '2026-01-01', '2026-12-31', 1),
('HOMU20', N'TIEN_MAT', 20000, 20000, 100000, 2, 50, '2026-01-01', '2026-12-31', 1),
('HOMUWELCOME', N'PHAN_TRAM', 15, 50000, 0, 12, 200, '2026-01-01', '2026-12-31', 1);

-- 7. Danh mục 12 Bàn phục vụ
INSERT INTO BAN (maBan, tenBan, soChoNgoi, trangThai) VALUES 
('B01', N'Bàn 01', 4, N'DANG_PHUC_VU'),
('B02', N'Bàn 02', 2, N'TRONG'),
('B03', N'Bàn 03', 4, N'DANG_PHUC_VU'),
('B04', N'Bàn 04', 6, N'DA_DAT'),      -- Bàn được khách đặt trước
('B05', N'Bàn 05', 4, N'DANG_PHUC_VU'),
('B06', N'Bàn 06', 2, N'TRONG'),
('B07', N'Bàn 07', 4, N'TRONG'),
('B08', N'Bàn 08', 4, N'DANG_PHUC_VU'),
('B09', N'Bàn 09', 2, N'TRONG'),
('B10', N'Bàn 10', 4, N'DANG_PHUC_VU'),
('B11', N'Bàn 11', 2, N'TRONG'),
('B12', N'Bàn 12', 6, N'DA_DAT');      -- Bàn được khách đặt trước

-- 8. Phương thức thanh toán
INSERT INTO PHUONG_THUC_THANH_TOAN (maPhuongThuc, tenPhuongThuc, trangThai) VALUES 
('PT01', N'Tiền mặt', 1),
('PT02', N'Chuyển khoản (VietQR)', 1),
('PT03', N'Ví điện tử (Momo/ZaloPay)', 1);

-- 9. Phiếu đặt bàn mẫu
INSERT INTO PHIEU_DAT_BAN (maPhieu, ngayDat, ngayDen, soLuongKhach, tienCoc, ghiChu, trangThai, maKH, maNV) VALUES 
('PDB001', '2026-11-04 08:00:00', '2026-11-04 18:30:00', 6, 100000, N'Họp mặt sinh nhật, cần chỗ yên tĩnh', N'DA_DAT', 'KH0001', 'NV01'),
('PDB002', '2026-11-04 09:30:00', '2026-11-04 19:00:00', 6, 100000, N'Đặt bàn tiệc gia đình', N'DA_DAT', 'KH0002', 'NV01');

-- 10. Chi tiết phiếu đặt bàn mẫu
INSERT INTO CHI_TIET_PHIEU_DAT_BAN (maPhieu, maBan, ghiChu) VALUES 
('PDB001', 'B04', N'Bàn 6 chỗ khu máy lạnh'),
('PDB002', 'B12', N'Bàn 6 chỗ view cửa sổ');

-- 11. Hóa đơn mẫu
INSERT INTO HOA_DON (maHD, ngayLap, loaiDon, soTheRung, giamGia, trangThaiDon, maNV, maKH, maVoucher, maBan, maPhuongThuc) VALUES 
('HD20261104-001', '2026-11-04 08:30:00', N'TAI_QUAN', 1, 0, N'HOAN_TAT', 'NV01', 'KH0001', NULL, 'B03', 'PT01'),
('HD20261104-002', '2026-11-04 09:15:00', N'MANG_DI', 2, 10000, N'HOAN_TAT', 'NV01', 'KH0002', 'HOMU10', NULL, 'PT02'),
('HD20261104-003', '2026-11-04 10:20:00', N'TAI_QUAN', 3, 0, N'DANG_PHA_CHE', 'NV01', 'KH0003', NULL, 'B01', 'PT01');

-- 12. Chi tiết hóa đơn mẫu (Không lưu thanhTien tính động)
INSERT INTO CHI_TIET_HOA_DON (maHD, maMon, kichCo, soLuong, donGiaThucTe, ghiChu) VALUES 
('HD20261104-001', 'CF002', 'M', 1, 35000, N'50% đường, ít đá'),
('HD20261104-001', 'CF003', 'L', 1, 39000, N'Chuẩn vị'),
('HD20261104-002', 'TEA01', 'L', 2, 50000, N'70% đường, nhiều đào'),
('HD20261104-003', 'MILK01', 'M', 2, 45000, N'50% đường, 50% đá, trân châu đen'),
('HD20261104-003', 'CAKE02', 'S', 1, 35000, N'Hâm nóng');
GO
