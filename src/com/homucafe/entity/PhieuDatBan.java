package com.homucafe.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể Phiếu đặt bàn trước (PHIEU_DAT_BAN).
 */
public class PhieuDatBan {
    private String maPhieu;
    private LocalDateTime ngayDat;
    private LocalDateTime ngayDen;
    private int soLuongKhach;
    private double tienCoc;
    private String ghiChu;
    private String trangThai; // DA_DAT, DA_NHAN, DA_HUY
    private KhachHang khachHang;
    private NhanVien nhanVien;
    private List<ChiTietPhieuDatBan> dsChiTiet = new ArrayList<>();

    public PhieuDatBan() {
        this.ngayDat = LocalDateTime.now();
        this.trangThai = "DA_DAT";
    }

    public PhieuDatBan(String maPhieu, LocalDateTime ngayDat, LocalDateTime ngayDen,
                       int soLuongKhach, double tienCoc, String ghiChu, String trangThai,
                       KhachHang khachHang, NhanVien nhanVien) {
        this.maPhieu = maPhieu;
        this.ngayDat = ngayDat;
        this.ngayDen = ngayDen;
        this.soLuongKhach = soLuongKhach;
        this.tienCoc = tienCoc;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
        this.khachHang = khachHang;
        this.nhanVien = nhanVien;
    }

    public void themChiTiet(ChiTietPhieuDatBan ct) {
        this.dsChiTiet.add(ct);
    }

    public String getDanhSachBanStr() {
        if (dsChiTiet.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dsChiTiet.size(); i++) {
            Ban b = dsChiTiet.get(i).getBan();
            if (b != null) {
                sb.append(b.getTenBan());
                if (i < dsChiTiet.size() - 1) sb.append(", ");
            }
        }
        return sb.toString();
    }

    // Getters & Setters
    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }

    public LocalDateTime getNgayDat() { return ngayDat; }
    public void setNgayDat(LocalDateTime ngayDat) { this.ngayDat = ngayDat; }

    public LocalDateTime getNgayDen() { return ngayDen; }
    public void setNgayDen(LocalDateTime ngayDen) { this.ngayDen = ngayDen; }

    public int getSoLuongKhach() { return soLuongKhach; }
    public void setSoLuongKhach(int soLuongKhach) { this.soLuongKhach = soLuongKhach; }

    public double getTienCoc() { return tienCoc; }
    public void setTienCoc(double tienCoc) { this.tienCoc = tienCoc; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public List<ChiTietPhieuDatBan> getDsChiTiet() { return dsChiTiet; }
    public void setDsChiTiet(List<ChiTietPhieuDatBan> dsChiTiet) { this.dsChiTiet = dsChiTiet; }
}
