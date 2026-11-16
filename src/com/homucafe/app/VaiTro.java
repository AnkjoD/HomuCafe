package com.homucafe.app;

import com.homucafe.entity.TaiKhoan;
import java.util.List;

/**
 * 3 Vai trò nghiệp vụ thực tế độc lập của hệ thống HomuCafe POS:
 * 1. QUAN_LY: Quản lý / Chủ quán (Toàn quyền mọi chức năng, cấu hình, báo cáo đa chiều)
 * 2. THU_NGAN: Nhân viên Thu ngân (Bán hàng, Sơ đồ bàn, Hóa đơn, Khách hàng, Báo cáo ca)
 * 3. PHA_CHE: Nhân viên Pha chế - Barista (Quầy pha chế FIFO, Sơ đồ bàn, Thực đơn món)
 */
public enum VaiTro {
    QUAN_LY("Quản lý / Chủ quán", List.of(
            "Tổng quan",
            "Bán hàng",
            "Sơ đồ bàn",
            "Quầy pha chế",
            "Quản lý hóa đơn",
            "Thực đơn món",
            "Voucher & ưu đãi",
            "Nhân viên & ca",
            "Khách hàng",
            "Báo cáo"
    )),
    THU_NGAN("Nhân viên Thu ngân", List.of(
            "Bán hàng",
            "Sơ đồ bàn",
            "Quản lý hóa đơn",
            "Khách hàng",
            "Báo cáo"
    )),
    PHA_CHE("Nhân viên Pha chế (Barista)", List.of(
            "Quầy pha chế",
            "Sơ đồ bàn",
            "Thực đơn món"
    ));

    private final String label;
    private final List<String> screens;

    VaiTro(String label, List<String> screens) {
        this.label = label;
        this.screens = screens;
    }

    public String getLabel() {
        return label;
    }

    public List<String> screens() {
        return screens;
    }

    @Override
    public String toString() {
        return label;
    }

    /**
     * Xác định VaiTro từ đối tượng TaiKhoan đăng nhập thực tế.
     */
    public static VaiTro tuTaiKhoan(TaiKhoan tk) {
        if (tk == null) return THU_NGAN;
        String pq = tk.getPhanQuyen() != null ? tk.getPhanQuyen().trim().toUpperCase() : "";
        if ("ADMIN".equals(pq) || "QUAN_LY".equals(pq) || "OWNER".equals(pq) || "MANAGER".equals(pq)) {
            return QUAN_LY;
        }
        if ("PHA_CHE".equals(pq) || "BARISTA".equals(pq)) {
            return PHA_CHE;
        }
        if ("THU_NGAN".equals(pq) || "CASHIER".equals(pq)) {
            return THU_NGAN;
        }

        // Nhận diện theo chức vụ nhân viên hoặc tên đăng nhập
        String cv = tk.getNhanVien() != null && tk.getNhanVien().getChucVu() != null
                ? tk.getNhanVien().getChucVu().toLowerCase() : "";
        String un = tk.getTenDangNhap() != null ? tk.getTenDangNhap().toLowerCase() : "";

        if (cv.contains("pha chế") || cv.contains("bar") || un.contains("barista") || un.contains("phache")) {
            return PHA_CHE;
        }
        if (cv.contains("quản lý") || cv.contains("chủ") || un.contains("admin") || un.contains("quanly")) {
            return QUAN_LY;
        }
        return THU_NGAN;
    }
}
