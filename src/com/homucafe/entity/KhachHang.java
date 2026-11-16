package com.homucafe.entity;

/**
 * Thực thể Khách hàng thành viên (KHACH_HANG)
 */
public class KhachHang {
    private String maKH;
    private String hoTen;
    private String soDienThoai;
    private int diemTichLuy;

    public KhachHang() {
    }

    public KhachHang(String maKH, String hoTen, String soDienThoai, int diemTichLuy) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.diemTichLuy = diemTichLuy;
    }

    public void congDiem(double tongTienHoaDon) {
        int diemCong = (int) (tongTienHoaDon / 10000);
        if (diemCong > 0) {
            this.diemTichLuy += diemCong;
        }
    }

    public void truDiem(int soDiemSuDung) {
        if (soDiemSuDung > 0 && this.diemTichLuy >= soDiemSuDung) {
            this.diemTichLuy -= soDiemSuDung;
        }
    }

    public String getHangThanhVien() {
        if (diemTichLuy >= 500) {
            return "Kim Cương (VIP -10%)";
        } else if (diemTichLuy >= 200) {
            return "Hạng Vàng (-5%)";
        } else if (diemTichLuy >= 50) {
            return "Hạng Bạc (-3%)";
        } else {
            return "Thành viên Mới";
        }
    }

    public double getPhanTramGiamVIP() {
        if (diemTichLuy >= 500) {
            return 10.0;
        } else if (diemTichLuy >= 200) {
            return 5.0;
        } else if (diemTichLuy >= 50) {
            return 3.0;
        } else {
            return 0.0;
        }
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
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

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }
}

