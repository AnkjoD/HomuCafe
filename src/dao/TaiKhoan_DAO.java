package dao;

import connectDB.ConnectDB;
import entity.NhanVien;
import entity.TaiKhoan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Lớp truy xuất dữ liệu Bảng Tài khoản người dùng (TAI_KHOAN_DAO) - Chuẩn kiến trúc 3 lớp IUH.
 */
public class TaiKhoan_DAO {

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
}
