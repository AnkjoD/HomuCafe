package com.homucafe.entity;

/**
 * Thực thể Chi tiết hóa đơn (CHI_TIET_HOA_DON)
 */
public class ChiTietHoaDon {
    private int maCTHD;
    private Mon mon;
    private KichCo kichCo;
    private int soLuong;
    private double donGiaThucTe;
    private int mucDuong; // 0, 30, 50, 70, 100
    private String mucDa; // KHONG_DA, IT_DA, BINH_THUONG, NONG
    private String ghiChuTopping;

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(int maCTHD, Mon mon, KichCo kichCo, int soLuong, double donGiaThucTe,
                         int mucDuong, String mucDa, String ghiChuTopping) {
        this.maCTHD = maCTHD;
        this.mon = mon;
        this.kichCo = kichCo;
        this.soLuong = soLuong;
        this.donGiaThucTe = donGiaThucTe;
        this.mucDuong = mucDuong;
        this.mucDa = mucDa;
        this.ghiChuTopping = ghiChuTopping;
    }

    public double tinhThanhTien() {
        return soLuong * donGiaThucTe;
    }

    public double getThanhTien() {
        return tinhThanhTien();
    }

    public String getMoTaTuyBien() {
        StringBuilder sb = new StringBuilder();
        sb.append(mucDuong).append("% đường, ").append(formatMucDa(mucDa));
        if (ghiChuTopping != null && !ghiChuTopping.trim().isEmpty()) {
            sb.append(", ").append(ghiChuTopping.trim());
        }
        return sb.toString();
    }

    public String getGhiChu() {
        return getMoTaTuyBien();
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChuTopping = ghiChu;
    }

    public static String formatMucDa(String mucDaCode) {
        if (mucDaCode == null) return "Bình thường";
        switch (mucDaCode) {
            case "KHONG_DA": return "Không đá";
            case "IT_DA": return "Ít đá";
            case "NONG": return "Uống nóng";
            case "BINH_THUONG":
            default:
                return "Bình thường";
        }
    }

    public int getMaCTHD() {
        return maCTHD;
    }

    public void setMaCTHD(int maCTHD) {
        this.maCTHD = maCTHD;
    }

    public Mon getMon() {
        return mon;
    }

    public void setMon(Mon mon) {
        this.mon = mon;
    }

    public KichCo getKichCo() {
        return kichCo;
    }

    public void setKichCo(KichCo kichCo) {
        this.kichCo = kichCo;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGiaThucTe() {
        return donGiaThucTe;
    }

    public void setDonGiaThucTe(double donGiaThucTe) {
        this.donGiaThucTe = donGiaThucTe;
    }

    public int getMucDuong() {
        return mucDuong;
    }

    public void setMucDuong(int mucDuong) {
        this.mucDuong = mucDuong;
    }

    public String getMucDa() {
        return mucDa;
    }

    public void setMucDa(String mucDa) {
        this.mucDa = mucDa;
    }

    public String getGhiChuTopping() {
        return ghiChuTopping;
    }

    public void setGhiChuTopping(String ghiChuTopping) {
        this.ghiChuTopping = ghiChuTopping;
    }
}
