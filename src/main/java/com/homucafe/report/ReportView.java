package com.homucafe.report;

import com.homucafe.shared.ui.Theme;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import static com.homucafe.report.ReportChart.Kind.*;
import static com.homucafe.report.ReportSamples.*;

/** Màn hình duyệt mẫu báo cáo. Controller/Service và quyền dữ liệu thật sẽ được học sau. */
public final class ReportView extends JPanel {
    private final JTabbedPane tabs = new JTabbedPane();
    private static final String FINANCE = "Tài chính";

    public ReportView() {
        setLayout(new BorderLayout());
        JPanel page = Theme.page("Báo cáo kinh doanh", "Hiểu quán qua từng con số.",
                "Dữ liệu minh họa · 15–21/10/2026 · Chọn nhóm báo cáo bên dưới.");
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabs.setBackground(Theme.PAPER);
        tabs.putClientProperty("JTabbedPane.tabHeight", 42);
        tabs.addTab("Tổng quan", overview());
        tabs.addTab("Bán hàng", sales());
        tabs.addTab("Món bán", products());
        tabs.addTab("Kho", inventory());
        tabs.addTab("Ca làm", shifts());
        // Mặc định không tạo tab tài chính; Workspace cấp chế độ xem mẫu theo vai trò.
        page.add(tabs, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);
    }

    /** Chỉ phân vai giao diện mẫu. Service phải kiểm tra quyền thật khi có đăng nhập. */
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
                new Object[][]{{"Tiền mặt", "10.400.000", "150.000", "10.250.000"},
                        {"Chuyển khoản", "14.400.000", "50.000", "14.350.000"},
                        {"Tổng", "24.800.000", "200.000", "24.600.000"}},
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
                new Object[][]{{"Cà phê", 346, "14.880.000 đ", "60%"}, {"Trà", 124, "6.200.000 đ", "25%"},
                        {"Khác", 98, "3.720.000 đ", "15%"}});
    }

    private JComponent inventory() {
        return report("Nhập, xuất & hao hụt", "Tồn cuối = tồn đầu + nhập − xuất dùng − hao hụt. Không cộng kg với lít.",
                Theme.cards(Theme.card("NGUYÊN LIỆU THEO DÕI", "4 loại", "Trích mẫu báo cáo kho"),
                        Theme.card("DƯỚI MỨC TỐI THIỂU", "2 loại", "Cà phê Arabica và sữa tươi"),
                        Theme.card("GIÁ TRỊ NHẬP KHO", "7.200.000 đ", "Chưa bao gồm chi phí khác")),
                pair(chart("Mức đáp ứng tồn tối thiểu", "Tồn cuối / mức tối thiểu · Dưới 100% cần bổ sung", BARS,
                                new String[]{"Cà phê Arabica", "Sữa tươi", "Trà lài", "Syrup đào"}, new double[]{80, 90, 125, 150}, "%"),
                        chart("Tỷ lệ hao hụt", "Hao hụt / (tồn đầu + nhập) · Theo từng nguyên liệu", BARS,
                                new String[]{"Cà phê Arabica", "Sữa tươi", "Trà lài", "Syrup đào"}, new double[]{1, 2, 1.25, 2.5}, "%")),
                "Sổ nhập xuất tồn · Mỗi dòng theo đơn vị riêng", new String[]{"NGUYÊN LIỆU", "ĐVT", "ĐẦU KỲ", "NHẬP", "XUẤT DÙNG", "HAO HỤT", "CUỐI KỲ", "TỐI THIỂU"},
                new Object[][]{{"Cà phê Arabica", "kg", 5, 5, 8.3, .1, 1.6, 2}, {"Sữa tươi", "lít", 10, 20, 27.6, .6, 1.8, 2},
                        {"Trà lài", "kg", 1, 3, 2.7, .05, 1.25, 1}, {"Syrup đào", "lít", 2, 2, 2.4, .1, 1.5, 1}});
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
                new Object[][]{{"Sáng / 01", "300.000", "800.000", "0", "0", "1.100.000", "1.080.000", "−20.000"},
                        {"Chiều / 01", "300.000", "1.000.000", "50.000", "0", "1.250.000", "1.250.000", "0"},
                        {"Tối / 01", "300.000", "650.000", "0", "50.000", "900.000", "900.000", "0"}});
    }

    private JComponent finance() {
        return report("Dòng tiền của quán", "Chủ quán xem · Chênh lệch thu chi chưa phải lợi nhuận; chưa tính giá vốn và dồn tích.",
                Theme.cards(Theme.card("THU SAU HOÀN", "24.600.000 đ", "Từ thanh toán bán hàng"),
                        Theme.card("ĐÃ CHI TRONG KỲ", "16.600.000 đ", "Theo ngày thực trả"),
                        Theme.card("CHÊNH LỆCH THU CHI", "+8.000.000 đ", "Chưa phải lợi nhuận")),
                pair(chart("Tiền vào và tiền ra", "Triệu đồng · Không gồm chuyển tiền nội bộ", COLUMNS,
                                new String[]{"Thu sau hoàn", "Đã chi"}, new double[]{24.6, 16.6}, "triệu đ"),
                        chart("Cơ cấu tiền đã chi", "Theo nhóm khoản chi · Triệu đồng", DONUT, COSTS, PAID, "triệu đ")),
                "Chi tiết tiền đã chi", new String[]{"NHÓM CHI", "SỐ TIỀN", "CƠ SỞ GHI NHẬN"},
                new Object[][]{{COSTS[0], "7.200.000 đ", "Thanh toán phiếu nhập trong kỳ"},
                        {COSTS[1], "5.000.000 đ", "Tiền lương thực trả"}, {COSTS[2], "3.000.000 đ", "Tiền thuê thực trả"},
                        {COSTS[3], "1.400.000 đ", "Điện, nước và vật tư đã trả"}});
    }

    private static Object[][] dailyRows() {
        Object[][] rows = new Object[DAYS.length][4];
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));
        for (int i = 0; i < DAYS.length; i++) rows[i] = new Object[]{DAYS[i] + "/2026",
                format.format(Math.round(COLLECTED[i] * 1_000_000)), format.format(Math.round(REFUNDED[i] * 1_000_000)),
                format.format(Math.round(NET[i] * 1_000_000))};
        return rows;
    }

    private static Object[][] hourlyRows() {
        Object[][] rows = new Object[HOURS.length][2];
        for (int i = 0; i < HOURS.length; i++) rows[i] = new Object[]{HOURS[i], (int) ORDERS[i]};
        return rows;
    }

    private static JPanel pair(JPanel first, JPanel second) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 18, 0));
        panel.setOpaque(false); panel.add(first); panel.add(second);
        panel.setPreferredSize(new Dimension(0, 360));
        return panel;
    }

    private static JPanel chart(String title, String note, ReportChart.Kind kind, String[] labels, double[] values, String unit) {
        return chartCard(title, note, new ReportChart(kind, labels, values, unit));
    }

    private static JPanel chartCard(String title, String note, JPanel chart) {
        JPanel card = Theme.surface(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel heading = Theme.label(title, 15, Theme.INK);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD));
        card.add(Theme.stack(7, heading, Theme.label("<html>" + note + "</html>", 11, Theme.MUTED)), BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private static JComponent report(String title, String note, JPanel summary, JPanel charts,
                                     String tableTitle, String[] columns, Object[][] rows) {
        return report(title, note, summary, charts, tableTitle, columns, rows, null, null, null);
    }

    private static JComponent report(String title, String note, JPanel summary, JPanel charts,
                                     String tableTitle, String[] columns, Object[][] rows,
                                     String extraTitle, String[] extraColumns, Object[][] extraRows) {
        ReportBody body = new ReportBody();
        body.setBorder(new EmptyBorder(20, 2, 18, 10));
        JPanel introduction = new JPanel(new BorderLayout(16, 0)); introduction.setOpaque(false);
        introduction.add(Theme.stack(8, Theme.label(title, 18, Theme.INK),
                Theme.label("<html>" + note + "</html>", 12, Theme.MUTED)), BorderLayout.CENTER);
        JButton filter = Theme.button(title.contains("theo ca") ? "21/10/2026" : "15–21/10/2026", false);
        filter.setEnabled(false);
        filter.setToolTipText("Khoảng ngày mẫu cố định; Controller sẽ xử lý bộ lọc sau.");
        introduction.add(filter, BorderLayout.EAST);
        body.add(introduction); body.add(Box.createVerticalStrut(18));
        summary.setPreferredSize(new Dimension(0, 136)); summary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 136));
        body.add(summary); body.add(Box.createVerticalStrut(18));
        charts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));
        body.add(charts); body.add(Box.createVerticalStrut(18));
        body.add(details(tableTitle, columns, rows));
        if (extraTitle != null) {
            body.add(Box.createVerticalStrut(18)); body.add(details(extraTitle, extraColumns, extraRows));
        }
        for (Component item : body.getComponents()) if (item instanceof JComponent component) component.setAlignmentX(LEFT_ALIGNMENT);
        JScrollPane scroll = new JScrollPane(body);
        scroll.getViewport().setBackground(Theme.PAPER);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        return scroll;
    }

    private static JPanel details(String title, String[] columns, Object[][] rows) {
        JTable table = Theme.table(columns, rows); table.setRowHeight(39);
        // Bảng nhiều cột cuộn ngang để số tiền không bị ép đến mức không đọc được.
        if (columns.length >= 7) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i = 0; i < columns.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 165 : 105);
        }
        JPanel panel = Theme.tablePanel(title, table);
        panel.setPreferredSize(new Dimension(0, 112 + rows.length * 39));
        return panel;
    }

    /** Nội dung rộng bằng viewport; phần dọc cuộn theo chiều cao tự nhiên. */
    private static final class ReportBody extends JPanel implements Scrollable {
        ReportBody() { setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); setOpaque(false); }
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 24; }
        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) { return Math.max(24, r.height - 24); }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
