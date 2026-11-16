package com.homucafe.entity;

/**
 * Thực thể Bàn phục vụ (BAN) trong hệ thống HomuCafe.
 */
public class Ban {
    private String maBan;     // B01..B12
    private String tenBan;    // Bàn 01..Bàn 12
    private int soChoNgoi;    // 2, 4, 6 chỗ
    private String trangThai; // TRONG, DANG_PHUC_VU, DA_DAT

    public Ban() {
        this.trangThai = "TRONG";
    }

    public Ban(String maBan, String tenBan, int soChoNgoi, String trangThai) {
        this.maBan = maBan;
        this.tenBan = tenBan;
        this.soChoNgoi = soChoNgoi;
        this.trangThai = trangThai;
    }

    public int getSoBan() {
        if (maBan == null) return 0;
        try {
            return Integer.parseInt(maBan.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    public String getMaBan() { return maBan; }
    public void setMaBan(String maBan) { this.maBan = maBan; }

    public String getTenBan() { return tenBan; }
    public void setTenBan(String tenBan) { this.tenBan = tenBan; }

    public int getSoChoNgoi() { return soChoNgoi; }
    public void setSoChoNgoi(int soChoNgoi) { this.soChoNgoi = soChoNgoi; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return tenBan != null ? tenBan : maBan;
    }
}
