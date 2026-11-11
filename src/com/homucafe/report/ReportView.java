package com.homucafe.report;

import com.homucafe.shared.ui.Theme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import static com.homucafe.report.ReportChart.Kind.*;
import static com.homucafe.report.ReportSamples.*;

/**
 * Màn hình Báo cáo kinh doanh đa chiều: Tổng quan, Bán hàng, Món bán, Ca làm, Tài chính.
 * Đã loại bỏ phần Kho nguyên liệu theo yêu cầu phạm vi Coffee POS.
 */
public final class ReportView extends JPanel {
    private final JTabbedPane tabs = new JTabbedPane();
    private static final String FINANCE = "Tài chính";

    public ReportView() {
        setLayout(new BorderLayout());
        JPanel page = Theme.page("Báo cáo kinh doanh", "Hiểu quán qua từng con số.",
                "Dữ liệu phân tích đa chiều · Bán hàng, Món chạy, Ca làm và Dòng tiền.");
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabs.setBackground(Theme.PAPER);
        tabs.putClientProperty("JTabbedPane.tabHeight", 42);
        tabs.addTab("Tổng quan", overview());
        tabs.addTab("Bán hàng", sales());
        tabs.addTab("Món bán", products());
        tabs.addTab("Ca làm", shifts());
        tabs.addTab(FINANCE, finance());

        page.add(tabs, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);
    }

    public void setOwnerPreview(boolean owner) {
        int index = tabs.indexOfTab(FINANCE);
        if (owner && index < 0) tabs.addTab(FINANCE, finance());
        if (!owner && index >= 0) tabs.removeTabAt(index);
    }

    private JComponent overview() {
        return report("Tổng quan hoạt động", "Tiền thu theo lúc thanh toán; tiền hoàn theo thời điểm hoàn trong kỳ.",
                Theme.cards(Theme.card("TIỀN THU SAU HOÀN", "24.600.000 đ", "24,8 triệu thu − 0,2 triệu hoàn"),
                        Theme.card("ĐƠN ĐÃ THANH TOÁN", "312 đơn", "Dữ liệu mẫu trong 7 ngày"),
                        Theme.card("SỐ LY ĐÃ BÁN", "568 ly", "Đã loại món hủy")),
                pair(chart("Tiền thu sau hoàn", "Triệu đồng · Theo ngày", LINE, DAYS, NET, "triệu đ"),
                        chart("Món bán chạy", "Số ly bán trong kỳ", BARS, PRODUCTS, CUPS, "ly")),
                "Chi tiết từng ngày · Đơn vị đồng", new String[]{"NGÀY", "THU THÀNH CÔNG", "TIỀN HOÀN", "THU SAU HOÀN"}, dailyRows());
    }

    private JComponent sales() {
        return report("Bán hàng & thanh toán", "Khung giờ theo lúc tạo đơn; thanh toán theo lúc xác nhận thu.",
                Theme.cards(Theme.card("THU THÀNH CÔNG", "24.800.000 đ", "Tiền mặt + chuyển khoản"),
                        Theme.card("GIẢM GIÁ ĐÃ ÁP", "620.000 đ", "Đã phản ánh trong tiền khách trả"),
                        Theme.card("TIỀN HOÀN", "200.000 đ", "4 giao dịch hoàn mẫu")),
                pair(chart("Giờ cao điểm", "Tổng đơn theo khung giờ · 7 ngày", COLUMNS, HOURS, ORDERS, "đơn"),
                        chart("Phương thức thanh toán", "Tiền thu thành công · Trước hoàn", DONUT, METHODS, PAYMENTS, "triệu đ")),
                "Đối soát thanh toán · Đơn vị đồng", new String[]{"PHƯƠNG THỨC", "THU THÀNH CÔNG", "HOÀN", "THU SAU HOÀN"},
                new Object[][]{
                        {"Tiền mặt", "10.400.000", "150.000", "10.250.000"},
                        {"Chuyển khoản VietQR", "14.400.000", "50.000", "14.350.000"},
                        {"Tổng", "24.800.000", "200.000", "24.600.000"}
                },
                "Lượng đơn theo khung giờ", new String[]{"KHUNG GIỜ", "SỐ ĐƠN"}, hourlyRows());
    }

    private JComponent products() {
        return report("Hiệu quả thực đơn", "Số ly đã loại món hủy; tỷ trọng bên phải dựa trên tiền thu thành công.",
                Theme.cards(Theme.card("TỔNG LY BÁN", "568 ly", "4 món nổi bật và nhóm còn lại"),
                        Theme.card("MÓN BÁN NHIỀU NHẤT", "Cà phê sữa", "168 ly trong kỳ"),
                        Theme.card("NHÓM THU CAO NHẤT", "Cà phê · 60%", "14.880.000 đ tiền thu")),
                pair(chart("So sánh lượng bán", "Món khác gộp các món còn lại", BARS, PRODUCTS, CUPS, "ly"),
                        chart("Tiền thu theo nhóm món", "Triệu đồng · Trước hoàn", DONUT, GROUPS, GROUP_RECEIPTS, "triệu đ")),
                "Cơ cấu nhóm món", new String[]{"NHÓM MÓN", "SỐ LY", "TIỀN THU", "TỶ TRỌNG TIỀN THU"},
                new Object[][]{
                        {"Cà phê", 346, "14.880.000 đ", "60%"},
                        {"Trà", 124, "6.200.000 đ", "25%"},
                        {"Khác", 98, "3.720.000 đ", "15%"}
                });
    }

    private JComponent shifts() {
        JPanel comparison = new ReportChart(COMPARE, new String[]{"Ca sáng", "Ca chiều", "Ca tối"},
                new double[]{1.10, 1.25, .90}, new double[]{1.08, 1.25, .90}, "triệu đ");
        return report("Đối soát tiền mặt theo ca", "Mẫu ngày 21/10 · Dự kiến = đầu ca + thu tiền mặt − hoàn tiền mặt − tiền đã nộp.",
                Theme.cards(Theme.card("CA ĐÃ CHỐT", "3 / 3 ca", "Quầy 01 · Ngày 21/10/2026"),
                        Theme.card("CA CÓ CHÊNH LỆCH", "1 ca", "Ca sáng cần kiểm tra"),
                        Theme.card("TỔNG CHÊNH LỆCH", "−20.000 đ", "Thực đếm − tiền dự kiến")),
                pair(chartCard("Dự kiến và thực đếm", "Xanh: dự kiến · Cam: thực đếm · Triệu đồng", comparison),
                        chart("Thiếu tiền theo ca", "Giá trị thiếu so với dự kiến · Nghìn đồng", COLUMNS,
                                new String[]{"Ca sáng", "Ca chiều", "Ca tối"}, new double[]{20, 0, 0}, "nghìn đ")),
                "Đối soát ngày 21/10 · Đơn vị đồng", new String[]{"CA / QUẦY", "ĐẦU CA", "THU", "HOÀN", "ĐÃ NỘP", "DỰ KIẾN", "THỰC ĐẾM", "LỆCH"},
                new Object[][]{
                        {"Sáng / 01", "300.000", "800.000", "0", "0", "1.100.000", "1.080.000", "−20.000"},
                        {"Chiều / 01", "300.000", "1.000.000", "50.000", "0", "1.250.000", "1.250.000", "0"},
                        {"Tối / 01", "300.000", "650.000", "0", "50.000", "900.000", "900.000", "0"}
                });
    }

    private JComponent finance() {
        return report("Dòng tiền của quán", "Chủ quán xem · Chênh lệch thu chi vận hành.",
                Theme.cards(Theme.card("THU SAU HOÀN", "24.600.000 đ", "Từ thanh toán bán hàng"),
                        Theme.card("ĐÃ CHI TRONG KỲ", "16.600.000 đ", "Theo ngày thực trả"),
                        Theme.card("CHÊNH LỆCH THU CHI", "+8.000.000 đ", "Thặng dư vận hành")),
                pair(chart("Tiền vào và tiền ra", "Triệu đồng · Không gồm chuyển tiền nội bộ", COLUMNS,
                                new String[]{"Thu sau hoàn", "Đã chi"}, new double[]{24.6, 16.6}, "triệu đ"),
                        chart("Cơ cấu tiền đã chi", "Theo nhóm khoản chi · Triệu đồng", DONUT, COSTS, PAID, "triệu đ")),
                "Chi tiết tiền đã chi", new String[]{"NHÓM CHI", "SỐ TIỀN", "CƠ SỞ GHI NHẬN"},
                new Object[][]{
                        {COSTS[0], "7.200.000 đ", "Tiền thuê mặt bằng trong kỳ"},
                        {COSTS[1], "5.000.000 đ", "Tiền lương nhân viên thực trả"},
                        {COSTS[2], "3.000.000 đ", "Điện, nước, internet vận hành"},
                        {COSTS[3], "1.400.000 đ", "Vật tư ly giấy, ống hút, túi mang đi"}
                });
    }

    private JComponent report(String title, String note, JPanel cards, JPanel charts, String tableTitle, String[] cols, Object[][] rows) {
        return report(title, note, cards, charts, tableTitle, cols, rows, null, null, null);
    }

    private JComponent report(String title, String note, JPanel cards, JPanel charts, String tableTitle, String[] cols, Object[][] rows,
                              String secondTableTitle, String[] secondCols, Object[][] secondRows) {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(Theme.PAPER);
        panel.setBorder(new EmptyBorder(18, 0, 18, 0));
        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel heading = Theme.label(title, 18, Theme.INK);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(heading, BorderLayout.NORTH);
        header.add(Theme.label(note, 12, Theme.MUTED), BorderLayout.SOUTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(header);
        content.add(Box.createVerticalStrut(14));
        content.add(cards);
        content.add(Box.createVerticalStrut(16));
        content.add(charts);
        content.add(Box.createVerticalStrut(16));

        if (secondTableTitle != null) {
            JPanel tables = new JPanel(new GridLayout(1, 2, 16, 0));
            tables.setOpaque(false);
            tables.add(Theme.tablePanel(tableTitle, Theme.table(cols, rows)));
            tables.add(Theme.tablePanel(secondTableTitle, Theme.table(secondCols, secondRows)));
            content.add(tables);
        } else {
            content.add(Theme.tablePanel(tableTitle, Theme.table(cols, rows)));
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.PAPER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel pair(JPanel left, JPanel right) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 0));
        panel.setOpaque(false);
        panel.add(left);
        panel.add(right);
        return panel;
    }

    private static JPanel chart(String title, String subtitle, ReportChart.Kind kind, String[] labels, double[] values, String unit) {
        return chartCard(title, subtitle, new ReportChart(kind, labels, values, unit));
    }

    private static JPanel chartCard(String title, String subtitle, JComponent chart) {
        JPanel card = Theme.surface(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel heading = Theme.label(title, 14, Theme.INK);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.add(heading, BorderLayout.NORTH);
        header.add(Theme.label(subtitle, 11, Theme.MUTED), BorderLayout.SOUTH);
        card.add(header, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private static Object[][] dailyRows() {
        Object[][] rows = new Object[DAYS.length][4];
        for (int i = 0; i < DAYS.length; i++) {
            rows[i] = new Object[]{DAYS[i] + "/2026", money(COLLECTED[i]), money(REFUNDED[i]), money(NET[i])};
        }
        return rows;
    }

    private static Object[][] hourlyRows() {
        Object[][] rows = new Object[HOURS.length][2];
        for (int i = 0; i < HOURS.length; i++) {
            rows[i] = new Object[]{HOURS[i] + " giờ", (int) ORDERS[i] + " đơn"};
        }
        return rows;
    }

    private static String money(double million) {
        return String.format(java.util.Locale.forLanguageTag("vi-VN"), "%,.0f đ", million * 1_000_000);
    }
}

