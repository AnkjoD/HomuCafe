package com.homucafe.app;

import java.util.List;

/**
 * Phân quyền và phạm vi màn hình cho từng vai trò trong hệ thống HomuCafe POS.
 * Đã loại bỏ Kho nguyên liệu, Nhập hàng, Công thức món theo yêu cầu.
 */
public enum PreviewRole {
    OWNER("Chủ quán", List.of(
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
    MANAGER("Quản lý", List.of(
            "Tổng quan",
            "Bán hàng",
            "Sơ đồ bàn",
            "Quầy pha chế",
            "Quản lý hóa đơn",
            "Thực đơn món",
            "Voucher & ưu đãi",
            "Nhân viên & ca",
            "Báo cáo"
    )),
    CASHIER("Thu ngân", List.of(
            "Bán hàng",
            "Sơ đồ bàn",
            "Quản lý hóa đơn"
    )),
    BARISTA("Pha chế", List.of(
            "Quầy pha chế"
    ));

    private final String label;
    private final List<String> screens;

    PreviewRole(String label, List<String> screens) {
        this.label = label;
        this.screens = screens;
    }

    public List<String> screens() { return screens; }

    @Override public String toString() { return label; }
}

