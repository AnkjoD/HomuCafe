package com.homucafe.entity;

/**
 * Thực thể Phương thức thanh toán (PHUONG_THUC_THANH_TOAN).
 */
public class PhuongThucThanhToan {
    private String maPhuongThuc;  // PT01, PT02, PT03
    private String tenPhuongThuc; // Tiền mặt, Chuyển khoản (VietQR), Ví điện tử
    private boolean trangThai;    // true = Đang hoạt động

    public PhuongThucThanhToan() {
        this.trangThai = true;
    }

    public PhuongThucThanhToan(String maPhuongThuc, String tenPhuongThuc, boolean trangThai) {
        this.maPhuongThuc = maPhuongThuc;
        this.tenPhuongThuc = tenPhuongThuc;
        this.trangThai = trangThai;
    }

    public String getMaPhuongThuc() { return maPhuongThuc; }
    public void setMaPhuongThuc(String maPhuongThuc) { this.maPhuongThuc = maPhuongThuc; }

    public String getTenPhuongThuc() { return tenPhuongThuc; }
    public void setTenPhuongThuc(String tenPhuongThuc) { this.tenPhuongThuc = tenPhuongThuc; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return tenPhuongThuc;
    }
}
