package com.homucafe.dao;

import com.homucafe.connectdb.ConnectDB;
import com.homucafe.entity.NhanVien;
import com.homucafe.entity.TaiKhoan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu Bảng Tài khoản người dùng (TAI_KHOAN_DAO) - Chuẩn kiến trúc 3 lớp IUH.
 */
public class TaiKhoan_DAO {

    public List<TaiKhoan> getAllTaiKhoan() {
        List<TaiKhoan> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return ds;

        String sql = "SELECT tk.tenDangNhap, tk.matKhau, tk.phanQuyen, " +
                     "nv.maNV, nv.hoTen, nv.soDienThoai, nv.cccd, nv.trangThaiLamViec, cv.tenChucVu " +
                     "FROM TAI_KHOAN tk " +
                     "INNER JOIN NHAN_VIEN nv ON tk.maNV = nv.maNV " +
                     "LEFT JOIN CHUC_VU cv ON nv.maChucVu = cv.maChucVu";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String maNV = rs.getString("maNV");
                String ten = rs.getString("hoTen");
                String sdt = rs.getString("soDienThoai");
                String cccd = rs.getString("cccd");
                boolean tt = rs.getBoolean("trangThaiLamViec");
                String chucVu = rs.getString("tenChucVu");
                if (chucVu == null) chucVu = "Thu ngân";

                NhanVien nv = new NhanVien(maNV, ten, sdt, cccd, chucVu, tt);
                ds.add(new TaiKhoan(
                        rs.getString("tenDangNhap"),
                        rs.getString("matKhau"),
                        rs.getString("phanQuyen"),
                        nv
                ));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAllTaiKhoan: " + e.getMessage());
        }
        return ds;
    }

    public TaiKhoan kiemTraDangNhap(String user, String pass) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return null;

        String sql = "SELECT tk.tenDangNhap, tk.matKhau, tk.phanQuyen, " +
                     "nv.maNV, nv.hoTen, nv.soDienThoai, nv.cccd, nv.trangThaiLamViec, cv.tenChucVu " +
                     "FROM TAI_KHOAN tk " +
                     "INNER JOIN NHAN_VIEN nv ON tk.maNV = nv.maNV " +
                     "LEFT JOIN CHUC_VU cv ON nv.maChucVu = cv.maChucVu " +
                     "WHERE tk.tenDangNhap = ? AND tk.matKhau = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, user);
            stmt.setString(2, pass);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String maNV = rs.getString("maNV");
                    String ten = rs.getString("hoTen");
                    String sdt = rs.getString("soDienThoai");
                    String cccd = rs.getString("cccd");
                    boolean tt = rs.getBoolean("trangThaiLamViec");
                    String chucVu = rs.getString("tenChucVu");
                    if (chucVu == null) chucVu = "Thu ngân";

                    NhanVien nv = new NhanVien(maNV, ten, sdt, cccd, chucVu, tt);
                    return new TaiKhoan(
                            rs.getString("tenDangNhap"),
                            rs.getString("matKhau"),
                            rs.getString("phanQuyen"),
                            nv
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiemTraDangNhap: " + e.getMessage());
        }
        return null;
    }

    public boolean doiMatKhau(String tenDangNhap, String matKhauMoi) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "UPDATE TAI_KHOAN SET matKhau = ? WHERE tenDangNhap = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, matKhauMoi);
            stmt.setString(2, tenDangNhap);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi doiMatKhau: " + e.getMessage());
            return false;
        }
    }

    public boolean luuHoacCapNhatTaiKhoan(TaiKhoan tk) {
        Connection con = ConnectDB.getConnection();
        if (con == null || tk == null || tk.getNhanVien() == null) return false;

        String checkSql = "SELECT tenDangNhap FROM TAI_KHOAN WHERE maNV = ? OR tenDangNhap = ?";
        try (PreparedStatement check = con.prepareStatement(checkSql)) {
            check.setString(1, tk.getNhanVien().getMaNV());
            check.setString(2, tk.getTenDangNhap());
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    String updateSql = "UPDATE TAI_KHOAN SET tenDangNhap = ?, matKhau = ?, phanQuyen = ? WHERE maNV = ?";
                    try (PreparedStatement upd = con.prepareStatement(updateSql)) {
                        upd.setString(1, tk.getTenDangNhap());
                        upd.setString(2, tk.getMatKhau());
                        upd.setString(3, tk.getPhanQuyen());
                        upd.setString(4, tk.getNhanVien().getMaNV());
                        return upd.executeUpdate() > 0;
                    }
                } else {
                    String insertSql = "INSERT INTO TAI_KHOAN (tenDangNhap, matKhau, phanQuyen, maNV) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement ins = con.prepareStatement(insertSql)) {
                        ins.setString(1, tk.getTenDangNhap());
                        ins.setString(2, tk.getMatKhau());
                        ins.setString(3, tk.getPhanQuyen());
                        ins.setString(4, tk.getNhanVien().getMaNV());
                        return ins.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi luuHoacCapNhatTaiKhoan: " + e.getMessage());
            return false;
        }
    }
}
