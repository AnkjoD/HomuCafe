package com.homucafe.dao;

import com.homucafe.connectdb.ConnectDB;
import com.homucafe.entity.Voucher;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu Bảng Voucher khuyến mãi (VOUCHER_DAO) - Chuẩn kiến trúc 3 lớp IUH.
 */
public class Voucher_DAO {

    public List<Voucher> getAllVoucher() {
        List<Voucher> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return ds;

        String sql = "SELECT maVoucher, hinhThucGiam, giaTriGiam, giamToiDa, donToiThieu, soLuotDung, gioiHanLuot, ngayBatDau, ngayKetThuc, trangThai FROM VOUCHER";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maVoucher");
                String hinhThuc = rs.getString("hinhThucGiam");
                double giaTri = rs.getDouble("giaTriGiam");
                double giamToiDa = rs.getDouble("giamToiDa");
                double donToiThieu = rs.getDouble("donToiThieu");
                int daDung = rs.getInt("soLuotDung");
                int gioiHan = rs.getInt("gioiHanLuot");
                Date dStart = rs.getDate("ngayBatDau");
                Date dEnd = rs.getDate("ngayKetThuc");
                LocalDate start = dStart != null ? dStart.toLocalDate() : null;
                LocalDate end = dEnd != null ? dEnd.toLocalDate() : null;
                boolean tt = rs.getBoolean("trangThai");

                ds.add(new Voucher(ma, hinhThuc, giaTri, giamToiDa, donToiThieu, daDung, gioiHan, start, end, tt));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAllVoucher: " + e.getMessage());
        }
        return ds;
    }

    public boolean kiemTraVoucher(String maVoucher) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "SELECT maVoucher FROM VOUCHER WHERE maVoucher = ? AND trangThai = 1 AND (gioiHanLuot = 0 OR soLuotDung < gioiHanLuot) AND CAST(GETDATE() AS DATE) BETWEEN ngayBatDau AND ngayKetThuc";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maVoucher);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiemTraVoucher: " + e.getMessage());
            return false;
        }
    }

    public boolean themVoucher(Voucher v) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "INSERT INTO VOUCHER (maVoucher, hinhThucGiam, giaTriGiam, giamToiDa, donToiThieu, soLuotDung, gioiHanLuot, ngayBatDau, ngayKetThuc, trangThai) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, v.getMaVoucher());
            stmt.setString(2, v.getHinhThucGiam() != null ? v.getHinhThucGiam() : "PHAN_TRAM");
            stmt.setDouble(3, v.getGiaTriGiam());
            stmt.setDouble(4, v.getGiamToiDa());
            stmt.setDouble(5, v.getDonToiThieu());
            stmt.setInt(6, v.getSoLuotDung());
            stmt.setInt(7, v.getGioiHanLuot());
            stmt.setDate(8, v.getNgayBatDau() != null ? Date.valueOf(v.getNgayBatDau()) : Date.valueOf(LocalDate.now()));
            stmt.setDate(9, v.getNgayKetThuc() != null ? Date.valueOf(v.getNgayKetThuc()) : Date.valueOf(LocalDate.now().plusMonths(1)));
            stmt.setBoolean(10, v.isTrangThai());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi themVoucher: " + e.getMessage());
            return false;
        }
    }

    public boolean tangLuotDung(String maVoucher) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "UPDATE VOUCHER SET soLuotDung = soLuotDung + 1 WHERE maVoucher = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maVoucher);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi tangLuotDung: " + e.getMessage());
            return false;
        }
    }

    public boolean giamSoLuot(String maVoucher) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "UPDATE VOUCHER SET soLuotDung = CASE WHEN soLuotDung > 0 THEN soLuotDung - 1 ELSE 0 END WHERE maVoucher = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maVoucher);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi giamSoLuot Voucher: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatTrangThai(String maVoucher, boolean trangThai) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "UPDATE VOUCHER SET trangThai = ? WHERE maVoucher = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setBoolean(1, trangThai);
            stmt.setString(2, maVoucher);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatTrangThai Voucher: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatTrangThai(String maVoucher, String trangThai) {
        boolean tt = "DANG_AP_DUNG".equalsIgnoreCase(trangThai) || "1".equals(trangThai) || "Đang sử dụng".equalsIgnoreCase(trangThai);
        return capNhatTrangThai(maVoucher, tt);
    }
}
