package dao;

import connectDB.ConnectDB;
import entity.KichCo;
import entity.LoaiMon;
import entity.Mon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu Bảng Món đồ uống (MON_DAO) - Chuẩn kiến trúc 3 lớp IUH.
 */
public class Mon_DAO {

    public List<Mon> getAllMon() {
        List<Mon> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return ds;

        String sql = "SELECT maMon, tenMon, donGiaGoc, hinhAnh, trangThai, loaiMon FROM MON ORDER BY maMon ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maMon");
                String ten = rs.getString("tenMon");
                double gia = rs.getDouble("donGiaGoc");
                String anh = rs.getString("hinhAnh");
                boolean tt = rs.getBoolean("trangThai");
                String loaiStr = rs.getString("loaiMon");
                LoaiMon loai = new LoaiMon("L_" + loaiStr, loaiStr);
                ds.add(new Mon(ma, ten, gia, anh, tt, loai));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAllMon: " + e.getMessage());
        }
        return ds;
    }

    public boolean themMon(Mon m) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "INSERT INTO MON (maMon, tenMon, donGiaGoc, hinhAnh, trangThai, loaiMon) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, m.getMaMon());
            stmt.setString(2, m.getTenMon());
            stmt.setDouble(3, m.getDonGiaGoc());
            stmt.setString(4, m.getHinhAnh() != null ? m.getHinhAnh() : "images/default.jpg");
            stmt.setBoolean(5, m.isDangKinhDoanh());
            stmt.setString(6, m.getLoaiMon() != null ? m.getLoaiMon().getTenLoai() : "Cà phê");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi themMon: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatMon(Mon m) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "UPDATE MON SET tenMon = ?, donGiaGoc = ?, loaiMon = ?, trangThai = ? WHERE maMon = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, m.getTenMon());
            stmt.setDouble(2, m.getDonGiaGoc());
            stmt.setString(3, m.getLoaiMon() != null ? m.getLoaiMon().getTenLoai() : "Cà phê");
            stmt.setBoolean(4, m.isDangKinhDoanh());
            stmt.setString(5, m.getMaMon());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatMon: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatTrangThai(String maMon, boolean dangKinhDoanh) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "UPDATE MON SET trangThai = ? WHERE maMon = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setBoolean(1, dangKinhDoanh);
            stmt.setString(2, maMon);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatTrangThai: " + e.getMessage());
            return false;
        }
    }
}
