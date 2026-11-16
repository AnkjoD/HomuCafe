package com.homucafe.entity;

/**
 * Thực thể Chi tiết phiếu đặt bàn (CHI_TIET_PHIEU_DAT_BAN).
 */
public class ChiTietPhieuDatBan {
    private String maPhieu;
    private Ban ban;
    private String ghiChu;

    public ChiTietPhieuDatBan() {
    }

    public ChiTietPhieuDatBan(String maPhieu, Ban ban, String ghiChu) {
        this.maPhieu = maPhieu;
        this.ban = ban;
        this.ghiChu = ghiChu;
    }

    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }

    public Ban getBan() { return ban; }
    public void setBan(Ban ban) { this.ban = ban; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
