package com.homucafe.dao;

import com.homucafe.connectdb.ConnectDB;
import com.homucafe.entity.ChiTietHoaDon;
import com.homucafe.entity.KichCo;
import com.homucafe.entity.LoaiMon;
import com.homucafe.entity.Mon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu Bảng Chi tiết hóa đơn (CHI_TIET_HOA_DON_DAO) - Chuẩn kiến trúc 3 lớp IUH.
 */
public class ChiTietHoaDon_DAO {

    public List<ChiTietHoaDon> getChiTietTheoHoaDon(String maHD) {
        List<ChiTietHoaDon> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return ds;

        String sql = "SELECT ct.maHD, ct.maMon, ct.kichCo, ct.soLuong, ct.donGiaThucTe, ct.ghiChu, " +
                     "m.tenMon, m.donGiaGoc, m.hinhAnh, m.trangThai, m.loaiMon " +
                     "FROM CHI_TIET_HOA_DON ct " +
                     "INNER JOIN MON m ON ct.maMon = m.maMon " +
                     "WHERE ct.maHD = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maHD);
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 1;
                while (rs.next()) {
                    String maMon = rs.getString("maMon");
                    String tenMon = rs.getString("tenMon");
                    double giaGoc = rs.getDouble("donGiaGoc");
                    String anh = rs.getString("hinhAnh");
                    boolean tt = rs.getBoolean("trangThai");
                    String loaiStr = rs.getString("loaiMon");
                    LoaiMon loai = new LoaiMon("L_" + loaiStr, loaiStr);
                    Mon mon = new Mon(maMon, tenMon, giaGoc, anh, tt, loai);

                    String sizeStr = rs.getString("kichCo");
                    KichCo size = "L".equalsIgnoreCase(sizeStr) ? KichCo.L : "S".equalsIgnoreCase(sizeStr) ? KichCo.S : KichCo.M;
                    int sl = rs.getInt("soLuong");
                    double dg = rs.getDouble("donGiaThucTe");
                    String ghiChu = rs.getString("ghiChu");

                    int mucDuong = 100;
                    String mucDa = "BINH_THUONG";
                    String topping = "";

                    // Parse ghi chú đường đá và tách riêng phần topping nếu có
                    if (ghiChu != null && !ghiChu.trim().isEmpty()) {
                        String lower = ghiChu.toLowerCase();
                        if (lower.contains("0% đường") && !lower.contains("100% đường") && !lower.contains("70% đường") && !lower.contains("50% đường") && !lower.contains("30% đường")) {
                            mucDuong = 0;
                        } else if (lower.contains("30% đường")) {
                            mucDuong = 30;
                        } else if (lower.contains("50% đường")) {
                            mucDuong = 50;
                        } else if (lower.contains("70% đường")) {
                            mucDuong = 70;
                        }

                        if (lower.contains("không đá")) mucDa = "KHONG_DA";
                        else if (lower.contains("ít đá")) mucDa = "IT_DA";
                        else if (lower.contains("uống nóng") || lower.contains("nóng")) mucDa = "NONG";

                        List<String> extraParts = new ArrayList<>();
                        for (String part : ghiChu.split(",")) {
                            String trimmed = part.trim();
                            if (trimmed.isEmpty()) continue;
                            String pLower = trimmed.toLowerCase();
                            if (pLower.endsWith("% đường")
                                    || pLower.equals("không đá")
                                    || pLower.equals("ít đá")
                                    || pLower.equals("bình thường")
                                    || pLower.equals("đá thường")
                                    || pLower.equals("uống nóng")) {
                                continue;
                            }
                            extraParts.add(trimmed);
                        }
                        topping = String.join(", ", extraParts);
                    }

                    ChiTietHoaDon item = new ChiTietHoaDon(count++, mon, size, sl, dg, mucDuong, mucDa, topping);
                    ds.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getChiTietTheoHoaDon: " + e.getMessage());
        }
        return ds;
    }

    public boolean themChiTiet(ChiTietHoaDon ct, String maHD) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        String sql = "INSERT INTO CHI_TIET_HOA_DON (maHD, maMon, kichCo, soLuong, donGiaThucTe, ghiChu) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maHD);
            stmt.setString(2, ct.getMon() != null ? ct.getMon().getMaMon() : "CF001");
            stmt.setString(3, ct.getKichCo() != null ? ct.getKichCo().name() : "M");
            stmt.setInt(4, ct.getSoLuong());
            stmt.setDouble(5, ct.getDonGiaThucTe());
            stmt.setString(6, ct.getGhiChu());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi themChiTiet: " + e.getMessage());
            return false;
        }
    }
}
