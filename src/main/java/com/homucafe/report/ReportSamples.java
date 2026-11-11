package com.homucafe.report;

/** Dữ liệu giả để duyệt giao diện; Controller/Service sẽ cung cấp dữ liệu thật sau. */
final class ReportSamples {
    private ReportSamples() { }
    static final String[] DAYS = {"15/10", "16/10", "17/10", "18/10", "19/10", "20/10", "21/10"};
    // Đơn vị triệu đồng. Các màn hình cùng đọc một bộ mẫu để tránh số liệu mâu thuẫn.
    static final double[] COLLECTED = {2.70, 3.50, 3.10, 3.90, 4.20, 3.60, 3.80};
    static final double[] REFUNDED = {0, .05, 0, 0, .10, 0, .05};
    static final double[] NET = {2.70, 3.45, 3.10, 3.90, 4.10, 3.60, 3.75};
    static final String[] PRODUCTS = {"Cà phê sữa", "Trà đào", "Latte", "Bạc xỉu", "Món khác"};
    static final double[] CUPS = {168, 124, 96, 82, 98};
    static final String[] HOURS = {"07–09", "09–11", "11–13", "13–15", "15–17", "17–19", "19–21"};
    static final double[] ORDERS = {42, 56, 30, 35, 48, 62, 39};
    static final String[] GROUPS = {"Cà phê", "Trà", "Khác"};
    static final double[] GROUP_RECEIPTS = {14.88, 6.20, 3.72};
    static final String[] METHODS = {"Tiền mặt", "Chuyển khoản"};
    static final double[] PAYMENTS = {10.40, 14.40};
    static final String[] COSTS = {"Nhập nguyên liệu", "Lương đã trả", "Thuê mặt bằng", "Chi vận hành khác"};
    static final double[] PAID = {7.20, 5.00, 3.00, 1.40};
}
