package com.homucafe.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể Đơn hàng tạm giữ tại quầy POS (Hold Order / Split Bill).
 */
public class DonTamGiu {
    private String maDonTam;
    private String tenGoiNho;
    private LocalDateTime thoiGianGiu;
    private String banChon;
    private KhachHang khachHang;
    private List<ChiTietHoaDon> dsMon;

    public DonTamGiu() {
        this.thoiGianGiu = LocalDateTime.now();
        this.dsMon = new ArrayList<>();
    }

    public DonTamGiu(String maDonTam, String tenGoiNho, LocalDateTime thoiGianGiu, String banChon, KhachHang khachHang, List<ChiTietHoaDon> dsMon) {
        this.maDonTam = maDonTam;
        this.tenGoiNho = tenGoiNho;
        this.thoiGianGiu = thoiGianGiu != null ? thoiGianGiu : LocalDateTime.now();
        this.banChon = banChon != null ? banChon : "Mang đi";
        this.khachHang = khachHang;
        this.dsMon = dsMon != null ? new ArrayList<>(dsMon) : new ArrayList<>();
    }

    public double getTongTienTamTinh() {
        if (dsMon == null) return 0;
        return dsMon.stream().mapToDouble(ChiTietHoaDon::tinhThanhTien).sum();
    }

    public int getTongSoLuongMon() {
        if (dsMon == null) return 0;
        return dsMon.stream().mapToInt(ChiTietHoaDon::getSoLuong).sum();
    }

    public String getMaDonTam() {
        return maDonTam;
    }

    public void setMaDonTam(String maDonTam) {
        this.maDonTam = maDonTam;
    }

    public String getTenGoiNho() {
        return tenGoiNho;
    }

    public void setTenGoiNho(String tenGoiNho) {
        this.tenGoiNho = tenGoiNho;
    }

    public LocalDateTime getThoiGianGiu() {
        return thoiGianGiu;
    }

    public void setThoiGianGiu(LocalDateTime thoiGianGiu) {
        this.thoiGianGiu = thoiGianGiu;
    }

    public String getBanChon() {
        return banChon;
    }

    public void setBanChon(String banChon) {
        this.banChon = banChon;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public List<ChiTietHoaDon> getDsMon() {
        return dsMon;
    }

    public void setDsMon(List<ChiTietHoaDon> dsMon) {
        this.dsMon = dsMon;
    }
}
