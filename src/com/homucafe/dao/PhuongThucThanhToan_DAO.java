package com.homucafe.dao;

import com.homucafe.connectdb.ConnectDB;
import com.homucafe.entity.PhuongThucThanhToan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu Bảng Phương thức thanh toán (PHUONG_THUC_THANH_TOAN_DAO) - Chuẩn IUH.
 */
public class PhuongThucThanhToan_DAO {

    public List<PhuongThucThanhToan> getAllPhuongThuc() {
        List<PhuongThucThanhToan> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return ds;

        String sql = "SELECT maPhuongThuc, tenPhuongThuc, trangThai FROM PHUONG_THUC_THANH_TOAN";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maPhuongThuc");
                String ten = rs.getString("tenPhuongThuc");
                boolean tt = rs.getBoolean("trangThai");
                ds.add(new PhuongThucThanhToan(ma, ten, tt));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAllPhuongThuc: " + e.getMessage());
        }
        return ds;
    }

    public PhuongThucThanhToan timTheoMa(String maPT) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return null;

        String sql = "SELECT maPhuongThuc, tenPhuongThuc, trangThai FROM PHUONG_THUC_THANH_TOAN WHERE maPhuongThuc = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maPT);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new PhuongThucThanhToan(rs.getString("maPhuongThuc"), rs.getString("tenPhuongThuc"), rs.getBoolean("trangThai"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi timTheoMa PhuongThuc: " + e.getMessage());
        }
        return null;
    }
}
