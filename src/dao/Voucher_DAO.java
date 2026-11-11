package dao;

import connectDB.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Lớp truy xuất dữ liệu Bảng Voucher khuyến mãi (VOUCHER_DAO) - Chuẩn kiến trúc 3 lớp IUH.
 */
public class Voucher_DAO {

    public boolean kiemTraVoucher(String maVoucher) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "SELECT maVoucher FROM VOUCHER WHERE maVoucher = ? AND trangThai = 1 AND soLuotDung < gioiHanLuot AND GETDATE() BETWEEN ngayBatDau AND ngayKetThuc";
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
}
