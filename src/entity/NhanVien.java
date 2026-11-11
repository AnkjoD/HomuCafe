package entity;

import java.util.Objects;

/**
 * Thực thể Nhân viên (NHAN_VIEN)
 */
public class NhanVien {
    private String maNV;
    private String hoTen;
    private String soDienThoai;
    private String cccd;
    private String chucVu; // Thu ngân, Pha chế, Quản lý
    private boolean trangThaiLamViec; // true: Đang làm, false: Đã nghỉ

    public NhanVien() {
    }

    public NhanVien(String maNV, String hoTen, String soDienThoai, String cccd, String chucVu, boolean trangThaiLamViec) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.cccd = cccd;
        this.chucVu = chucVu;
        this.trangThaiLamViec = trangThaiLamViec;
    }

    public boolean isQuanLy() {
        return chucVu != null && (chucVu.equalsIgnoreCase("Quản lý") || chucVu.equalsIgnoreCase("Chủ quán"));
    }

    public boolean isThuNgan() {
        return chucVu != null && chucVu.equalsIgnoreCase("Thu ngân");
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public boolean isTrangThaiLamViec() {
        return trangThaiLamViec;
    }

    public void setTrangThaiLamViec(boolean trangThaiLamViec) {
        this.trangThaiLamViec = trangThaiLamViec;
    }

    public boolean isDangLamViec() {
        return trangThaiLamViec;
    }

    public void setDangLamViec(boolean dangLamViec) {
        this.trangThaiLamViec = dangLamViec;
    }

    @Override
    public String toString() {
        return maNV + " - " + hoTen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NhanVien)) return false;
        NhanVien nhanVien = (NhanVien) o;
        return Objects.equals(maNV, nhanVien.maNV);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNV);
    }
}

