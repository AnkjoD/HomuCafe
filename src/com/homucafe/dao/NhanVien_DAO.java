package com.homucafe.dao;

import com.homucafe.connectdb.ConnectDB;
import com.homucafe.entity.NhanVien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu Bảng Nhân viên (NHAN_VIEN_DAO) - Chuẩn kiến trúc 3 lớp IUH.
 */
public class NhanVien_DAO {

    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return ds;

        String sql = "SELECT nv.maNV, nv.hoTen, nv.soDienThoai, nv.cccd, nv.trangThaiLamViec, cv.tenChucVu " +
                     "FROM NHAN_VIEN nv LEFT JOIN CHUC_VU cv ON nv.maChucVu = cv.maChucVu " +
                     "ORDER BY nv.maNV ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maNV");
                String ten = rs.getString("hoTen");
                String sdt = rs.getString("soDienThoai");
                String cccd = rs.getString("cccd");
                boolean tt = rs.getBoolean("trangThaiLamViec");
                String chucVu = rs.getString("tenChucVu");
                if (chucVu == null) chucVu = "Thu ngân";
                ds.add(new NhanVien(ma, ten, sdt, cccd, chucVu, tt));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAllNhanVien: " + e.getMessage());
        }
        return ds;
    }

    public boolean themNhanVien(NhanVien nv) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String maCV = "CV02";
        if (nv.getChucVu() != null) {
            if (nv.getChucVu().equalsIgnoreCase("Quản lý") || nv.getChucVu().equalsIgnoreCase("Chủ quán")) maCV = "CV01";
            else if (nv.getChucVu().equalsIgnoreCase("Pha chế")) maCV = "CV03";
        }

        String sql = "INSERT INTO NHAN_VIEN (maNV, hoTen, soDienThoai, cccd, trangThaiLamViec, maChucVu) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, nv.getMaNV());
            stmt.setString(2, nv.getHoTen());
            stmt.setString(3, nv.getSoDienThoai());
            stmt.setString(4, nv.getCccd());
            stmt.setBoolean(5, nv.isDangLamViec());
            stmt.setString(6, maCV);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi themNhanVien: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatNhanVien(NhanVien nv) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String maCV = "CV02";
        if (nv.getChucVu() != null) {
            if (nv.getChucVu().equalsIgnoreCase("Quản lý") || nv.getChucVu().equalsIgnoreCase("Chủ quán")) maCV = "CV01";
            else if (nv.getChucVu().equalsIgnoreCase("Pha chế")) maCV = "CV03";
        }

        String sql = "UPDATE NHAN_VIEN SET hoTen = ?, soDienThoai = ?, cccd = ?, trangThaiLamViec = ?, maChucVu = ? WHERE maNV = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, nv.getHoTen());
            stmt.setString(2, nv.getSoDienThoai());
            stmt.setString(3, nv.getCccd());
            stmt.setBoolean(4, nv.isDangLamViec());
            stmt.setString(5, maCV);
            stmt.setString(6, nv.getMaNV());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatNhanVien: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatTrangThai(String maNV, boolean dangLamViec) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "UPDATE NHAN_VIEN SET trangThaiLamViec = ? WHERE maNV = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setBoolean(1, dangLamViec);
            stmt.setString(2, maNV);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatTrangThai NV: " + e.getMessage());
            return false;
        }
    }
}

