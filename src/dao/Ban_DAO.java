package dao;

import connectDB.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Lớp truy xuất dữ liệu Bảng Bàn phục vụ (BAN_DAO) - Chuẩn kiến trúc 3 lớp IUH.
 */
public class Ban_DAO {

    public Map<Integer, String> getAllTrangThaiBan() {
        Map<Integer, String> map = new HashMap<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return map;

        String sql = "SELECT maBan, trangThai FROM BAN";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maBan"); // VD: "B01"
                int soBan = 0;
                try {
                    soBan = Integer.parseInt(ma.replaceAll("[^0-9]", ""));
                } catch (Exception ignored) { }
                String tt = rs.getString("trangThai");
                if (soBan > 0) {
                    map.put(soBan, tt);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAllTrangThaiBan: " + e.getMessage());
        }
        return map;
    }

    public boolean capNhatTrangThaiBan(int soBan, String trangThai) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String maBan = String.format("B%02d", soBan);
        String sql = "UPDATE BAN SET trangThai = ? WHERE maBan = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, trangThai);
            stmt.setString(2, maBan);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatTrangThaiBan: " + e.getMessage());
            return false;
        }
    }
}
