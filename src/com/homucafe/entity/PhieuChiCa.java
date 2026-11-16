package com.homucafe.entity;

import java.time.LocalDateTime;

/**
 * Thực thể Phiếu chi vặt trong ca làm việc (Petty Cash).
 */
public class PhieuChiCa {
    private String maPhieuChi;
    private LocalDateTime thoiGian;
    private double soTien;
    private String lyDo;
    private String nguoiChi;

    public PhieuChiCa() {
        this.thoiGian = LocalDateTime.now();
    }

    public PhieuChiCa(String maPhieuChi, LocalDateTime thoiGian, double soTien, String lyDo, String nguoiChi) {
        this.maPhieuChi = maPhieuChi;
        this.thoiGian = thoiGian != null ? thoiGian : LocalDateTime.now();
        this.soTien = soTien;
        this.lyDo = lyDo;
        this.nguoiChi = nguoiChi;
    }

    public String getMaPhieuChi() {
        return maPhieuChi;
    }

    public void setMaPhieuChi(String maPhieuChi) {
        this.maPhieuChi = maPhieuChi;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }

    public double getSoTien() {
        return soTien;
    }

    public void setSoTien(double soTien) {
        this.soTien = soTien;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public String getNguoiChi() {
        return nguoiChi;
    }

    public void setNguoiChi(String nguoiChi) {
        this.nguoiChi = nguoiChi;
    }
}
