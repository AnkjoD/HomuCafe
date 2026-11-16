package com.homucafe.dao;

import com.homucafe.connectdb.ConnectDB;
import com.homucafe.entity.ChiTietHoaDon;
import com.homucafe.entity.HoaDon;
import com.homucafe.entity.KhachHang;
import com.homucafe.entity.NhanVien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu Bảng Hóa đơn (HOA_DON_DAO) - Chuẩn kiến trúc 3 lớp IUH.
 */
public class HoaDon_DAO {
    private final ChiTietHoaDon_DAO chiTietDAO = new ChiTietHoaDon_DAO();

    public List<HoaDon> getAllHoaDon() {
        List<HoaDon> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return ds;

        String sql = "SELECT hd.maHD, hd.ngayLap, hd.loaiDon, hd.soTheRung, " +
                     "hd.giamGia, hd.trangThaiDon, hd.maBan, hd.maVoucher, " +
                     "ISNULL(pt.tenPhuongThuc, N'Tiền mặt') AS phuongThucThanhToan, " +
                     "ISNULL((SELECT SUM(ct.soLuong * ct.donGiaThucTe) FROM CHI_TIET_HOA_DON ct WHERE ct.maHD = hd.maHD), 0) AS tongTien, " +
                     "ROW_NUMBER() OVER (PARTITION BY CAST(hd.ngayLap AS DATE) ORDER BY hd.ngayLap) AS soThuTuNgay, " +
                     "nv.maNV, nv.hoTen AS tenNV, nv.soDienThoai AS sdtNV, nv.cccd AS cccdNV, nv.trangThaiLamViec, " +
                     "kh.maKH, kh.hoTen AS tenKH, kh.soDienThoai AS sdtKH, kh.diemTichLuy " +
                     "FROM HOA_DON hd " +
                     "INNER JOIN NHAN_VIEN nv ON hd.maNV = nv.maNV " +
                     "LEFT JOIN KHACH_HANG kh ON hd.maKH = kh.maKH " +
                     "LEFT JOIN PHUONG_THUC_THANH_TOAN pt ON hd.maPhuongThuc = pt.maPhuongThuc " +
                     "ORDER BY hd.ngayLap DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String maHD = rs.getString("maHD");
                int stt = rs.getInt("soThuTuNgay");
                Timestamp ts = rs.getTimestamp("ngayLap");
                LocalDateTime ngayLap = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                int soTheRung = rs.getInt("soTheRung");
                String loaiDon = rs.getString("loaiDon");
                String phuongThuc = rs.getString("phuongThucThanhToan");
                double giamGia = rs.getDouble("giamGia");
                double tongTien = rs.getDouble("tongTien");
                String trangThai = rs.getString("trangThaiDon");
                String maBan = rs.getString("maBan");
                String maVoucher = rs.getString("maVoucher");

                // NhanVien
                String maNV = rs.getString("maNV");
                String tenNV = rs.getString("tenNV");
                String sdtNV = rs.getString("sdtNV");
                String cccdNV = rs.getString("cccdNV");
                boolean ttNV = rs.getBoolean("trangThaiLamViec");
                NhanVien nv = new NhanVien(maNV, tenNV, sdtNV, cccdNV, "Thu ngân", ttNV);

                // KhachHang
                KhachHang kh = null;
                String maKH = rs.getString("maKH");
                if (maKH != null) {
                    kh = new KhachHang(maKH, rs.getString("tenKH"), rs.getString("sdtKH"), rs.getInt("diemTichLuy"));
                }

                String banPhucVu = maBan != null && !maBan.isEmpty() ? "Bàn " + maBan.replaceAll("[^0-9]", "") : "Mang đi";

                HoaDon hd = new HoaDon(
                        maHD, ngayLap, nv, kh, banPhucVu, soTheRung,
                        loaiDon, phuongThuc, trangThai, tongTien, giamGia,
                        tongTien, 0, maVoucher
                );
                hd.setMaVoucher(maVoucher);
                hd.setSoThuTuNgay(stt);

                // Nạp chi tiết các ly nước
                List<ChiTietHoaDon> chiTietList = chiTietDAO.getChiTietTheoHoaDon(maHD);
                for (ChiTietHoaDon ct : chiTietList) {
                    hd.themChiTiet(ct);
                }

                ds.add(hd);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAllHoaDon: " + e.getMessage());
        }
        return ds;
    }

    public boolean themHoaDon(HoaDon hd) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String maBan = null;
        if (hd.getBanPhucVu() != null && hd.getBanPhucVu().contains("Bàn")) {
            String digits = hd.getBanPhucVu().replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                maBan = String.format("B%02d", Integer.parseInt(digits));
            }
        }

        String maPT = "PT01";
        if (hd.getPhuongThucThanhToan() != null) {
            String pt = hd.getPhuongThucThanhToan().toUpperCase();
            if (pt.contains("QR") || pt.contains("CHUYEN_KHOAN") || pt.contains("VIETQR")) {
                maPT = "PT02";
            } else if (pt.contains("VI_DIEN_TU") || pt.contains("MOMO") || pt.contains("ZALOPAY")) {
                maPT = "PT03";
            }
        }

        String sql = "INSERT INTO HOA_DON (maHD, ngayLap, loaiDon, soTheRung, giamGia, trangThaiDon, maNV, maKH, maVoucher, maBan, maPhuongThuc) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, hd.getMaHoaDon());
            stmt.setTimestamp(2, Timestamp.valueOf(hd.getNgayLap() != null ? hd.getNgayLap() : LocalDateTime.now()));
            stmt.setString(3, hd.getLoaiDon() != null ? hd.getLoaiDon() : "TAI_QUAN");
            if (hd.getSoTheRung() > 0) {
                stmt.setInt(4, hd.getSoTheRung());
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }
            stmt.setDouble(5, hd.getGiamGia());
            stmt.setString(6, hd.getTrangThaiDon() != null ? hd.getTrangThaiDon() : "DANG_PHA_CHE");
            stmt.setString(7, hd.getNhanVien() != null ? hd.getNhanVien().getMaNV() : "NV01");
            if (hd.getKhachHang() != null && hd.getKhachHang().getMaKH() != null) {
                stmt.setString(8, hd.getKhachHang().getMaKH());
            } else {
                stmt.setNull(8, java.sql.Types.NVARCHAR);
            }
            if (hd.getMaVoucher() != null && !hd.getMaVoucher().trim().isEmpty()) {
                stmt.setString(9, hd.getMaVoucher().trim());
            } else {
                stmt.setNull(9, java.sql.Types.NVARCHAR);
            }
            if (maBan != null) {
                stmt.setString(10, maBan);
            } else {
                stmt.setNull(10, java.sql.Types.NVARCHAR);
            }
            stmt.setString(11, maPT);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
                    chiTietDAO.themChiTiet(ct, hd.getMaHoaDon());
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi themHoaDon: " + e.getMessage());
        }
        return false;
    }

    public boolean capNhatTrangThaiDon(String maHD, String trangThai) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "UPDATE HOA_DON SET trangThaiDon = ? WHERE maHD = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, trangThai);
            stmt.setString(2, maHD);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatTrangThaiDon: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatBanHoaDon(String maHD, String maBanMoi) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "UPDATE HOA_DON SET maBan = ? WHERE maHD = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            if (maBanMoi != null && !maBanMoi.trim().isEmpty()) {
                stmt.setString(1, maBanMoi.trim());
            } else {
                stmt.setNull(1, java.sql.Types.NVARCHAR);
            }
            stmt.setString(2, maHD);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatBanHoaDon: " + e.getMessage());
            return false;
        }
    }

    public boolean huyHoaDon(String maHD, String lyDoHuy, String maNVXacNhan) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "UPDATE HOA_DON SET trangThaiDon = 'DA_HUY' WHERE maHD = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maHD);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi huyHoaDon: " + e.getMessage());
            return false;
        }
    }

    public boolean huyHoaDon(String maHD) {
        return huyHoaDon(maHD, "Hủy đơn hàng", "NV01");
    }
}
