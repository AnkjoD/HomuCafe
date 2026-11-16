package com.homucafe.dao;

import com.homucafe.connectdb.ConnectDB;
import com.homucafe.entity.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu Bảng Phiếu đặt bàn (PHIEU_DAT_BAN_DAO) - Chuẩn IUH.
 */
public class PhieuDatBan_DAO {

    public List<PhieuDatBan> getAllPhieuDatBan() {
        List<PhieuDatBan> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return ds;

        String sql = "SELECT p.maPhieu, p.ngayDat, p.ngayDen, p.soLuongKhach, p.tienCoc, p.ghiChu, p.trangThai, " +
                     "kh.maKH, kh.hoTen AS tenKH, kh.soDienThoai AS sdtKH, kh.diemTichLuy, " +
                     "nv.maNV, nv.hoTen AS tenNV, nv.soDienThoai AS sdtNV, nv.cccd AS cccdNV, nv.trangThaiLamViec " +
                     "FROM PHIEU_DAT_BAN p " +
                     "INNER JOIN KHACH_HANG kh ON p.maKH = kh.maKH " +
                     "INNER JOIN NHAN_VIEN nv ON p.maNV = nv.maNV " +
                     "ORDER BY p.ngayDen DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String maPhieu = rs.getString("maPhieu");
                Timestamp tsDat = rs.getTimestamp("ngayDat");
                Timestamp tsDen = rs.getTimestamp("ngayDen");
                LocalDateTime ngayDat = tsDat != null ? tsDat.toLocalDateTime() : LocalDateTime.now();
                LocalDateTime ngayDen = tsDen != null ? tsDen.toLocalDateTime() : LocalDateTime.now();
                int soKhach = rs.getInt("soLuongKhach");
                double tienCoc = rs.getDouble("tienCoc");
                String ghiChu = rs.getString("ghiChu");
                String trangThai = rs.getString("trangThai");

                KhachHang kh = new KhachHang(rs.getString("maKH"), rs.getString("tenKH"), rs.getString("sdtKH"), rs.getInt("diemTichLuy"));
                NhanVien nv = new NhanVien(rs.getString("maNV"), rs.getString("tenNV"), rs.getString("sdtNV"), rs.getString("cccdNV"), "Nhân viên", rs.getBoolean("trangThaiLamViec"));

                PhieuDatBan pdb = new PhieuDatBan(maPhieu, ngayDat, ngayDen, soKhach, tienCoc, ghiChu, trangThai, kh, nv);

                // Lấy chi tiết bàn
                List<ChiTietPhieuDatBan> ctList = getChiTietPhieu(con, maPhieu);
                for (ChiTietPhieuDatBan ct : ctList) {
                    pdb.themChiTiet(ct);
                }

                ds.add(pdb);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAllPhieuDatBan: " + e.getMessage());
        }
        return ds;
    }

    private List<ChiTietPhieuDatBan> getChiTietPhieu(Connection con, String maPhieu) {
        List<ChiTietPhieuDatBan> list = new ArrayList<>();
        String sql = "SELECT ct.maBan, ct.ghiChu, b.tenBan, b.soChoNgoi, b.trangThai " +
                     "FROM CHI_TIET_PHIEU_DAT_BAN ct " +
                     "INNER JOIN BAN b ON ct.maBan = b.maBan " +
                     "WHERE ct.maPhieu = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maPhieu);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String maBan = rs.getString("maBan");
                    String tenBan = rs.getString("tenBan");
                    int soCho = rs.getInt("soChoNgoi");
                    String tt = rs.getString("trangThai");
                    Ban ban = new Ban(maBan, tenBan, soCho, tt);
                    list.add(new ChiTietPhieuDatBan(maPhieu, ban, rs.getString("ghiChu")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getChiTietPhieu: " + e.getMessage());
        }
        return list;
    }

    public boolean themPhieuDatBan(PhieuDatBan pdb) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "INSERT INTO PHIEU_DAT_BAN (maPhieu, ngayDat, ngayDen, soLuongKhach, tienCoc, ghiChu, trangThai, maKH, maNV) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, pdb.getMaPhieu());
            stmt.setTimestamp(2, Timestamp.valueOf(pdb.getNgayDat() != null ? pdb.getNgayDat() : LocalDateTime.now()));
            stmt.setTimestamp(3, Timestamp.valueOf(pdb.getNgayDen() != null ? pdb.getNgayDen() : LocalDateTime.now().plusHours(1)));
            stmt.setInt(4, pdb.getSoLuongKhach());
            stmt.setDouble(5, pdb.getTienCoc());
            stmt.setString(6, pdb.getGhiChu());
            stmt.setString(7, pdb.getTrangThai() != null ? pdb.getTrangThai() : "DA_DAT");
            stmt.setString(8, pdb.getKhachHang() != null ? pdb.getKhachHang().getMaKH() : "KH0001");
            stmt.setString(9, pdb.getNhanVien() != null ? pdb.getNhanVien().getMaNV() : "NV01");

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                // Thêm chi tiết bàn
                String sqlCT = "INSERT INTO CHI_TIET_PHIEU_DAT_BAN (maPhieu, maBan, ghiChu) VALUES (?, ?, ?)";
                for (ChiTietPhieuDatBan ct : pdb.getDsChiTiet()) {
                    try (PreparedStatement stmtCT = con.prepareStatement(sqlCT)) {
                        stmtCT.setString(1, pdb.getMaPhieu());
                        stmtCT.setString(2, ct.getBan() != null ? ct.getBan().getMaBan() : "B01");
                        stmtCT.setString(3, ct.getGhiChu());
                        stmtCT.executeUpdate();
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi themPhieuDatBan: " + e.getMessage());
        }
        return false;
    }

    public boolean capNhatTrangThai(String maPhieu, String trangThai) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "UPDATE PHIEU_DAT_BAN SET trangThai = ? WHERE maPhieu = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, trangThai);
            stmt.setString(2, maPhieu);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatTrangThai PhieuDatBan: " + e.getMessage());
            return false;
        }
    }
}
