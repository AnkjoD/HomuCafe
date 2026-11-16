package com.homucafe.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Thực thể Voucher khuyến mãi (VOUCHER) chuẩn 12 thực thể IUH.
 */
public class Voucher {
    private String maVoucher;
    private String hinhThucGiam; // PHAN_TRAM hoặc TIEN_MAT
    private double giaTriGiam;   // % (ví dụ 10) hoặc số tiền (ví dụ 20000)
    private double giamToiDa;    // Áp dụng trần với PHAN_TRAM (0 nếu không giới hạn)
    private double donToiThieu;  // Đơn tối thiểu để áp dụng
    private int soLuotDung;      // Số lượt đã sử dụng
    private int gioiHanLuot;     // Tổng số lượt tối đa
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private boolean trangThai;    // true = Đang áp dụng (1), false = Ngừng (0)

    public Voucher() {
        this.trangThai = true;
    }

    public Voucher(String maVoucher, String hinhThucGiam, double giaTriGiam, double giamToiDa,
                   double donToiThieu, int soLuotDung, int gioiHanLuot,
                   LocalDate ngayBatDau, LocalDate ngayKetThuc, boolean trangThai) {
        this.maVoucher = maVoucher;
        this.hinhThucGiam = hinhThucGiam;
        this.giaTriGiam = giaTriGiam;
        this.giamToiDa = giamToiDa;
        this.donToiThieu = donToiThieu;
        this.soLuotDung = soLuotDung;
        this.gioiHanLuot = gioiHanLuot;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai = trangThai;
    }

    public boolean isHopLe(double tongTienDon) {
        if (!trangThai) return false;
        if (gioiHanLuot > 0 && soLuotDung >= gioiHanLuot) return false;
        LocalDate now = LocalDate.now();
        if (ngayBatDau != null && now.isBefore(ngayBatDau)) return false;
        if (ngayKetThuc != null && now.isAfter(ngayKetThuc)) return false;
        return !(tongTienDon < donToiThieu);
    }

    public double tinhTienGiam(double tongTienDon) {
        if (!isHopLe(tongTienDon)) return 0;
        double giam = 0;
        if ("PHAN_TRAM".equalsIgnoreCase(hinhThucGiam)) {
            giam = tongTienDon * (giaTriGiam / 100.0);
            if (giamToiDa > 0 && giam > giamToiDa) {
                giam = giamToiDa;
            }
        } else {
            giam = giaTriGiam;
        }
        return Math.min(giam, tongTienDon);
    }

    public int getSoLuotConLai() {
        return Math.max(0, gioiHanLuot - soLuotDung);
    }

    // Getters and Setters
    public String getMaVoucher() { return maVoucher; }
    public void setMaVoucher(String maVoucher) { this.maVoucher = maVoucher; }

    public String getHinhThucGiam() { return hinhThucGiam; }
    public void setHinhThucGiam(String hinhThucGiam) { this.hinhThucGiam = hinhThucGiam; }

    public double getGiaTriGiam() { return giaTriGiam; }
    public void setGiaTriGiam(double giaTriGiam) { this.giaTriGiam = giaTriGiam; }

    public double getGiamToiDa() { return giamToiDa; }
    public void setGiamToiDa(double giamToiDa) { this.giamToiDa = giamToiDa; }

    public double getDonToiThieu() { return donToiThieu; }
    public void setDonToiThieu(double donToiThieu) { this.donToiThieu = donToiThieu; }

    public int getSoLuotDung() { return soLuotDung; }
    public void setSoLuotDung(int soLuotDung) { this.soLuotDung = soLuotDung; }

    public int getGioiHanLuot() { return gioiHanLuot; }
    public void setGioiHanLuot(int gioiHanLuot) { this.gioiHanLuot = gioiHanLuot; }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDate ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return maVoucher;
    }
}
