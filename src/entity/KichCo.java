package entity;

/**
 * Enum Kích cỡ ly đồ uống (BR04: Size S giữ nguyên giá gốc, M +6.000đ, L +12.000đ)
 */
public enum KichCo {
    S("S", 0.0, "Size Nhỏ (Tiêu chuẩn)"),
    M("M", 6000.0, "Size Vừa (+6.000đ)"),
    L("L", 12000.0, "Size Lớn (+12.000đ)");

    private final String kyHieu;
    private final double phuThu;
    private final String moTa;

    KichCo(String kyHieu, double phuThu, String moTa) {
        this.kyHieu = kyHieu;
        this.phuThu = phuThu;
        this.moTa = moTa;
    }

    public String getKyHieu() {
        return kyHieu;
    }

    public double getPhuThu() {
        return phuThu;
    }

    public String getMoTa() {
        return moTa;
    }

    public static KichCo fromString(String text) {
        if (text == null) return S;
        for (KichCo kc : KichCo.values()) {
            if (kc.kyHieu.equalsIgnoreCase(text.trim())) {
                return kc;
            }
        }
        return S;
    }
}

