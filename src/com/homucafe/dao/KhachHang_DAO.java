package com.homucafe.dao;

import com.homucafe.connectdb.ConnectDB;
import com.homucafe.entity.KhachHang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu Bảng Khách hàng (KHACH_HANG_DAO) - Chuẩn kiến trúc 3 lớp IUH.
 */
public class KhachHang_DAO {

    public List<KhachHang> getAllKhachHang() {
        List<KhachHang> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return ds;

        String sql = "SELECT maKH, hoTen, soDienThoai, diemTichLuy FROM KHACH_HANG ORDER BY maKH ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maKH");
                String ten = rs.getString("hoTen");
                String sdt = rs.getString("soDienThoai");
                int diem = rs.getInt("diemTichLuy");
                ds.add(new KhachHang(ma, ten, sdt, diem));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAllKhachHang: " + e.getMessage());
        }
        return ds;
    }

    public KhachHang timTheoSDT(String sdt) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return null;

        String sql = "SELECT maKH, hoTen, soDienThoai, diemTichLuy FROM KHACH_HANG WHERE soDienThoai = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, sdt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new KhachHang(
                            rs.getString("maKH"),
                            rs.getString("hoTen"),
                            rs.getString("soDienThoai"),
                            rs.getInt("diemTichLuy")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi timTheoSDT: " + e.getMessage());
        }
        return null;
    }

    public boolean themKhachHang(KhachHang kh) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "INSERT INTO KHACH_HANG (maKH, hoTen, soDienThoai, diemTichLuy) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, kh.getMaKH());
            stmt.setString(2, kh.getHoTen());
            stmt.setString(3, kh.getSoDienThoai());
            stmt.setInt(4, kh.getDiemTichLuy());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi themKhachHang: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatKhachHang(KhachHang kh) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "UPDATE KHACH_HANG SET hoTen = ?, soDienThoai = ?, diemTichLuy = ? WHERE maKH = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, kh.getHoTen());
            stmt.setString(2, kh.getSoDienThoai());
            stmt.setInt(3, kh.getDiemTichLuy());
            stmt.setString(4, kh.getMaKH());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatKhachHang: " + e.getMessage());
            return false;
        }
    }

    public boolean congDiem(String maKH, int diemCong) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "UPDATE KHACH_HANG SET diemTichLuy = diemTichLuy + ? WHERE maKH = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, diemCong);
            stmt.setString(2, maKH);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi congDiem: " + e.getMessage());
            return false;
        }
    }
}

