package entity;

/**
 * Thực thể Tài khoản người dùng (TAI_KHOAN)
 */
public class TaiKhoan {
    private String tenDangNhap;
    private String matKhau;
    private String phanQuyen; // ADMIN hoặc STAFF
    private NhanVien nhanVien;

    public TaiKhoan() {
    }

    public TaiKhoan(String tenDangNhap, String matKhau, String phanQuyen, NhanVien nhanVien) {
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.phanQuyen = phanQuyen;
        this.nhanVien = nhanVien;
    }

    public boolean kiemTraDangNhap(String user, String pass) {
        return tenDangNhap != null && tenDangNhap.equalsIgnoreCase(user)
                && matKhau != null && matKhau.equals(pass)
                && (nhanVien == null || nhanVien.isTrangThaiLamViec());
    }

    public boolean doiMatKhau(String matKhauCu, String matKhauMoi) {
        if (this.matKhau != null && this.matKhau.equals(matKhauCu)) {
            this.matKhau = matKhauMoi;
            return true;
        }
        return false;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getPhanQuyen() {
        return phanQuyen;
    }

    public void setPhanQuyen(String phanQuyen) {
        this.phanQuyen = phanQuyen;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }
}

