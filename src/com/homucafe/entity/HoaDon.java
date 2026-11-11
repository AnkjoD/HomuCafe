package com.homucafe.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể Hóa đơn bán hàng (HOA_DON) theo chuẩn Báo cáo Đồ án Coffee POS & Pager System.
 */
public class HoaDon {
    private String maHD;
    private int soThuTuNgay;
    private LocalDateTime ngayLap;
    private int soTheRung; // 1..99 cho TAI_QUAN, 0 nếu MANG_DI
    private int soBan; // 1..12 cho TAI_QUAN, 0 nếu MANG_DI
    private String banPhucVu; // "Bàn 01" .. "Bàn 12", "Mang đi"
    private String loaiDon; // TAI_QUAN hoặc MANG_DI
    private String phuongThucThanhToan; // TIEN_MAT hoặc VIETQR / CHUYEN_KHOAN
    private double giamGia;
    private double tongTien;
    private double tienKhachDua;
    private double tienThoi;
    private String trangThaiDon; // DANG_PHA_CHE, CHO_GIAO, HOAN_TAT, DA_HUY
    private NhanVien nhanVien;
    private KhachHang khachHang;
    private Object voucher;
    private List<ChiTietHoaDon> dsChiTiet = new ArrayList<>();

    public HoaDon() {
        this.ngayLap = LocalDateTime.now();
        this.trangThaiDon = "DANG_PHA_CHE";
    }

    public HoaDon(String maHD, int soThuTuNgay, LocalDateTime ngayLap, int soTheRung,
                  String loaiDon, String phuongThucThanhToan, double giamGia,
                  String trangThaiDon, NhanVien nhanVien, KhachHang khachHang) {
        this.maHD = maHD;
        this.soThuTuNgay = soThuTuNgay;
        this.ngayLap = ngayLap;
        this.soTheRung = soTheRung;
        this.loaiDon = loaiDon;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.giamGia = giamGia;
        this.trangThaiDon = trangThaiDon;
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
    }

    public HoaDon(String maHD, LocalDateTime ngayLap, NhanVien nhanVien, KhachHang khachHang,
                  String banPhucVu, int soTheRung, String loaiDon, String phuongThucThanhToan,
                  String trangThaiDon, double tongTien, double giamGia, double tienKhachDua,
                  double tienThoi, Object voucher) {
        this.maHD = maHD;
        this.ngayLap = ngayLap;
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
        this.banPhucVu = banPhucVu;
        this.soTheRung = soTheRung;
        this.loaiDon = loaiDon;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.trangThaiDon = trangThaiDon;
        this.tongTien = tongTien;
        this.giamGia = giamGia;
        this.tienKhachDua = tienKhachDua;
        this.tienThoi = tienThoi;
        this.voucher = voucher;
    }

    public double tinhTongTienHang() {
        double sum = 0;
        for (ChiTietHoaDon ct : dsChiTiet) {
            sum += ct.tinhThanhTien();
        }
        return sum;
    }

    public double tinhTongTien() {
        if (tongTien > 0) return tongTien;
        double total = tinhTongTienHang() - giamGia;
        return Math.max(0, total);
    }

    public double getTongTien() {
        return tinhTongTien();
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public int getTongSoLy() {
        int count = 0;
        for (ChiTietHoaDon ct : dsChiTiet) {
            count += ct.getSoLuong();
        }
        return count;
    }

    public void themChiTiet(ChiTietHoaDon ct) {
        this.dsChiTiet.add(ct);
    }

    public void capNhatTrangThai(String trangThaiMoi) {
        this.trangThaiDon = trangThaiMoi;
    }

    public String getMaHoaDon() {
        return maHD;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHD = maHoaDon;
    }

    public String getMaHD() {
        return maHD;
    }

    public void setMaHD(String maHD) {
        this.maHD = maHD;
    }

    public int getSoThuTuNgay() {
        return soThuTuNgay;
    }

    public void setSoThuTuNgay(int soThuTuNgay) {
        this.soThuTuNgay = soThuTuNgay;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public int getSoTheRung() {
        return soTheRung;
    }

    public void setSoTheRung(int soTheRung) {
        this.soTheRung = soTheRung;
    }

    public int getSoBan() {
        return soBan;
    }

    public void setSoBan(int soBan) {
        this.soBan = soBan;
    }

    public String getBanPhucVu() {
        if (banPhucVu != null && !banPhucVu.isEmpty()) return banPhucVu;
        if (soBan > 0) return "Bàn " + String.format("%02d", soBan);
        return "Mang đi";
    }

    public void setBanPhucVu(String banPhucVu) {
        this.banPhucVu = banPhucVu;
    }

    public String getLoaiDon() {
        return loaiDon;
    }

    public void setLoaiDon(String loaiDon) {
        this.loaiDon = loaiDon;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public double getGiamGia() {
        return giamGia;
    }

    public void setGiamGia(double giamGia) {
        this.giamGia = giamGia;
    }

    public double getTienKhachDua() {
        return tienKhachDua;
    }

    public void setTienKhachDua(double tienKhachDua) {
        this.tienKhachDua = tienKhachDua;
    }

    public double getTienThoi() {
        return tienThoi;
    }

    public void setTienThoi(double tienThoi) {
        this.tienThoi = tienThoi;
    }

    public String getTrangThaiDon() {
        return trangThaiDon;
    }

    public void setTrangThaiDon(String trangThaiDon) {
        this.trangThaiDon = trangThaiDon;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public Object getVoucher() {
        return voucher;
    }

    public void setVoucher(Object voucher) {
        this.voucher = voucher;
    }

    public List<ChiTietHoaDon> getDsChiTiet() {
        return dsChiTiet;
    }

    public void setDsChiTiet(List<ChiTietHoaDon> dsChiTiet) {
        this.dsChiTiet = dsChiTiet;
    }
}
