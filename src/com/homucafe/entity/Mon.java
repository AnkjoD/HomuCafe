package com.homucafe.entity;

import java.util.Objects;

/**
 * Thực thể Món đồ uống (MON)
 */
public class Mon {
    private String maMon;
    private String tenMon;
    private double donGiaGoc;
    private String hinhAnh;
    private boolean trangThai; // true: Đang bán, false: Ngừng bán (Xóa mềm)
    private LoaiMon loaiMon;

    public Mon() {
    }

    public Mon(String maMon, String tenMon, double donGiaGoc, String hinhAnh, boolean trangThai, LoaiMon loaiMon) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.donGiaGoc = donGiaGoc;
        this.hinhAnh = hinhAnh;
        this.trangThai = trangThai;
        this.loaiMon = loaiMon;
    }

    public double tinhGiaTheoSize(KichCo size) {
        if (size == null) return donGiaGoc;
        return donGiaGoc + size.getPhuThu();
    }

    public boolean isConHang() {
        return trangThai;
    }

    public boolean isDangKinhDoanh() {
        return trangThai;
    }

    public void setDangKinhDoanh(boolean dangKinhDoanh) {
        this.trangThai = dangKinhDoanh;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public double getDonGiaGoc() {
        return donGiaGoc;
    }

    public void setDonGiaGoc(double donGiaGoc) {
        this.donGiaGoc = donGiaGoc;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public LoaiMon getLoaiMon() {
        return loaiMon;
    }

    public void setLoaiMon(LoaiMon loaiMon) {
        this.loaiMon = loaiMon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mon)) return false;
        Mon mon = (Mon) o;
        return Objects.equals(maMon, mon.maMon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maMon);
    }
}
