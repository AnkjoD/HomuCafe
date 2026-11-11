package dao;

import connectDB.ConnectDB;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.KhachHang;
import entity.NhanVien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu Bảng Hóa đơn (HOA_DON_DAO) - Chuẩn kiến trúc 3 lớp IUH.
 */
public class HoaDon_DAO {
    private final ChiTietHoaDon_DAO chiTietDAO = new ChiTietHoaDon_DAO();

    public List<HoaDon> getAllHoaDon() {
        List<HoaDon> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return ds;

        String sql = "SELECT hd.maHD, hd.soThuTuNgay, hd.ngayLap, hd.soTheRung, hd.loaiDon, " +
                     "hd.phuongThucThanhToan, hd.giamGia, hd.tongTien, hd.trangThaiDon, hd.maBan, " +
                     "nv.maNV, nv.hoTen AS tenNV, nv.soDienThoai AS sdtNV, nv.cccd AS cccdNV, nv.trangThaiLamViec, " +
                     "kh.maKH, kh.hoTen AS tenKH, kh.soDienThoai AS sdtKH, kh.diemTichLuy " +
                     "FROM HOA_DON hd " +
                     "INNER JOIN NHAN_VIEN nv ON hd.maNV = nv.maNV " +
                     "LEFT JOIN KHACH_HANG kh ON hd.maKH = kh.maKH " +
                     "ORDER BY hd.ngayLap DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String maHD = rs.getString("maHD");
                int stt = rs.getInt("soThuTuNgay");
                Timestamp ts = rs.getTimestamp("ngayLap");
                LocalDateTime ngayLap = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                int soTheRung = rs.getInt("soTheRung");
                String loaiDon = rs.getString("loaiDon");
                String phuongThuc = rs.getString("phuongThucThanhToan");
                double giamGia = rs.getDouble("giamGia");
                double tongTien = rs.getDouble("tongTien");
                String trangThai = rs.getString("trangThaiDon");
                String maBan = rs.getString("maBan");

                // NhanVien
                String maNV = rs.getString("maNV");
                String tenNV = rs.getString("tenNV");
                String sdtNV = rs.getString("sdtNV");
                String cccdNV = rs.getString("cccdNV");
                boolean ttNV = rs.getBoolean("trangThaiLamViec");
                NhanVien nv = new NhanVien(maNV, tenNV, sdtNV, cccdNV, "Thu ngân", ttNV);

                // KhachHang
                KhachHang kh = null;
                String maKH = rs.getString("maKH");
                if (maKH != null) {
                    kh = new KhachHang(maKH, rs.getString("tenKH"), rs.getString("sdtKH"), rs.getInt("diemTichLuy"));
                }

                String banPhucVu = maBan != null && !maBan.isEmpty() ? "Bàn " + maBan.replaceAll("[^0-9]", "") : "Mang đi";

                HoaDon hd = new HoaDon(
                        maHD, ngayLap, nv, kh, banPhucVu, soTheRung,
                        loaiDon, phuongThuc, trangThai, tongTien, giamGia,
                        tongTien, 0, null
                );
                hd.setSoThuTuNgay(stt);

                // Nạp chi tiết các ly nước
                List<ChiTietHoaDon> chiTietList = chiTietDAO.getChiTietTheoHoaDon(maHD);
                for (ChiTietHoaDon ct : chiTietList) {
                    hd.themChiTiet(ct);
                }

                ds.add(hd);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAllHoaDon: " + e.getMessage());
        }
        return ds;
    }

    public boolean themHoaDon(HoaDon hd) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String maBan = null;
        if (hd.getBanPhucVu() != null && hd.getBanPhucVu().contains("Bàn")) {
            String digits = hd.getBanPhucVu().replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                maBan = String.format("B%02d", Integer.parseInt(digits));
            }
        }

        String sql = "INSERT INTO HOA_DON (maHD, soThuTuNgay, ngayLap, soTheRung, loaiDon, phuongThucThanhToan, giamGia, tongTien, trangThaiDon, maNV, maKH, maVoucher, maBan) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, hd.getMaHoaDon());
            stmt.setInt(2, hd.getSoThuTuNgay() > 0 ? hd.getSoThuTuNgay() : 1);
            stmt.setTimestamp(3, Timestamp.valueOf(hd.getNgayLap() != null ? hd.getNgayLap() : LocalDateTime.now()));
            if (hd.getSoTheRung() > 0) stmt.setInt(4, hd.getSoTheRung());
            else stmt.setNull(4, java.sql.Types.INTEGER);
            stmt.setString(5, hd.getLoaiDon() != null ? hd.getLoaiDon() : "TAI_QUAN");
            stmt.setString(6, hd.getPhuongThucThanhToan() != null ? hd.getPhuongThucThanhToan() : "TIEN_MAT");
            stmt.setDouble(7, hd.getGiamGia());
            stmt.setDouble(8, hd.getTongTien());
            stmt.setString(9, hd.getTrangThaiDon() != null ? hd.getTrangThaiDon() : "DANG_PHA_CHE");
            stmt.setString(10, hd.getNhanVien() != null ? hd.getNhanVien().getMaNV() : "NV01");
            if (hd.getKhachHang() != null) stmt.setString(11, hd.getKhachHang().getMaKH());
            else stmt.setNull(11, java.sql.Types.NVARCHAR);
            stmt.setNull(12, java.sql.Types.NVARCHAR); // maVoucher
            if (maBan != null) stmt.setString(13, maBan);
            else stmt.setNull(13, java.sql.Types.NVARCHAR);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
                    chiTietDAO.themChiTiet(ct, hd.getMaHoaDon());
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi themHoaDon: " + e.getMessage());
        }
        return false;
    }

    public boolean capNhatTrangThaiDon(String maHD, String trangThai) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "UPDATE HOA_DON SET trangThaiDon = ? WHERE maHD = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, trangThai);
            stmt.setString(2, maHD);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatTrangThaiDon: " + e.getMessage());
            return false;
        }
    }

    public boolean huyHoaDon(String maHD) {
        return capNhatTrangThaiDon(maHD, "DA_HUY");
    }
}
