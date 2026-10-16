package com.homucafe.app;

import java.util.List;

/** Chỉ chọn giao diện mẫu. Không phải đăng nhập hoặc cơ chế bảo mật. */
public enum PreviewRole {
    OWNER("Chủ quán", List.of("Tổng quan", "Bán hàng", "Sơ đồ bàn", "Quầy pha chế", "Kho nguyên liệu", "Nhập hàng", "Công thức món", "Nhân viên & ca", "Khách hàng", "Thu chi", "Báo cáo")),
    MANAGER("Quản lý", List.of("Tổng quan", "Bán hàng", "Sơ đồ bàn", "Quầy pha chế", "Kho nguyên liệu", "Nhập hàng", "Công thức món", "Nhân viên & ca", "Báo cáo")),
    CASHIER("Thu ngân", List.of("Bán hàng", "Sơ đồ bàn", "Ca của tôi")),
    BARISTA("Pha chế", List.of("Quầy pha chế"));

    private final String label;
    private final List<String> screens;
    PreviewRole(String label, List<String> screens) { this.label = label; this.screens = screens; }
    public List<String> screens() { return screens; }
    @Override public String toString() { return label; }
}
