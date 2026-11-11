package com.homucafe.report;

/**
 * Bộ dữ liệu mẫu cho hệ thống báo cáo kinh doanh HomuCafe.
 */
public final class ReportSamples {
    private ReportSamples() { }

    public static final String[] DAYS = {"15/10", "16/10", "17/10", "18/10", "19/10", "20/10", "21/10"};
    public static final double[] COLLECTED = {2.70, 3.50, 3.10, 3.90, 4.20, 3.60, 3.80};
    public static final double[] REFUNDED = {0, .05, 0, 0, .10, 0, .05};
    public static final double[] NET = {2.70, 3.45, 3.10, 3.90, 4.10, 3.60, 3.75};

    public static final String[] PRODUCTS = {"Cà phê sữa", "Trà đào", "Latte", "Bạc xỉu", "Món khác"};
    public static final double[] CUPS = {168, 124, 96, 82, 98};

    public static final String[] HOURS = {"07–09", "09–11", "11–13", "13–15", "15–17", "17–19", "19–21"};
    public static final double[] ORDERS = {42, 56, 30, 35, 48, 62, 39};

    public static final String[] GROUPS = {"Cà phê", "Trà", "Khác"};
    public static final double[] GROUP_RECEIPTS = {14.88, 6.20, 3.72};

    public static final String[] METHODS = {"Tiền mặt", "Chuyển khoản"};
    public static final double[] PAYMENTS = {10.40, 14.40};

    public static final String[] COSTS = {"Mặt bằng", "Lương nhân viên", "Vận hành điện nước", "Vật tư ly/ống hút"};
    public static final double[] PAID = {7.20, 5.00, 3.00, 1.40};
}

