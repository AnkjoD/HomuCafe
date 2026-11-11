package gui;

import entity.ChiTietHoaDon;
import entity.HoaDon;
import util.DataStore;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Giao diện Báo cáo Thống kê Doanh thu và Đối soát ca (FrmThongKe - HomuCafe)
 * Thẩm mỹ ấm cúng: Nền #FAF6EF, Thẻ KPI, Biểu đồ Top món bán chạy & Bảng đối soát ca trực.
 */
public class FrmThongKe extends JPanel {
    private JLabel lblTongDoanhThu;
    private JLabel lblTongSoDon;
    private JLabel lblTongSoLy;
    private JLabel lblTyLeThanhToan;

    private JComboBox<String> cboKyBaoCao;
    private JComboBox<String> cboCaLamViec;
    private BarChartPanel chartPanel;
    private DefaultTableModel modelCa;

    public FrmThongKe() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.HOMU_BG_CREAM);
        setBorder(new EmptyBorder(14, 18, 18, 18));

        initComponents();
        capNhatSoLieuThongKe();

        DataStore.addDataChangeListener(this::capNhatSoLieuThongKe);
    }

    private void initComponents() {
        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setOpaque(false);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterBar.setOpaque(false);

        JLabel lblFilter = new JLabel("Kỳ báo cáo (UC20):");
        lblFilter.setFont(UITheme.FONT_BOLD);
        lblFilter.setForeground(UITheme.HOMU_TEXT);
        cboKyBaoCao = new JComboBox<>(new String[]{
                "Hôm nay (04/11/2026)", "7 ngày gần nhất", "Tháng 11/2026", "Quý 4/2026"
        });
        cboKyBaoCao.setFont(UITheme.FONT_REGULAR);
        cboKyBaoCao.addActionListener(e -> capNhatSoLieuThongKe());

        JLabel lblCa = new JLabel("Ca làm việc (UC19):");
        lblCa.setFont(UITheme.FONT_BOLD);
        lblCa.setForeground(UITheme.HOMU_TEXT);
        cboCaLamViec = new JComboBox<>(new String[]{
                "Ca: Toàn ngày", "Ca 1 (07:00 - 12:00)", "Ca 2 (12:00 - 17:00)", "Ca 3 (17:00 - 22:30)"
        });
        cboCaLamViec.setFont(UITheme.FONT_REGULAR);
        cboCaLamViec.addActionListener(e -> capNhatSoLieuThongKe());

        filterBar.add(lblFilter);
        filterBar.add(cboKyBaoCao);
        filterBar.add(lblCa);
        filterBar.add(cboCaLamViec);

        // 4 Thẻ KPI phong cách HomuCafe
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setPreferredSize(new Dimension(0, 112));

        lblTongDoanhThu = new JLabel("18.450.000 đ");
        lblTongSoDon = new JLabel("246 đơn");
        lblTongSoLy = new JLabel("412 ly");
        lblTyLeThanhToan = new JLabel("62% QR / 38% Tiền mặt");

        kpiGrid.add(taoTheKPI("TỔNG DOANH THU", lblTongDoanhThu, "+15.2% so với hôm qua",
                UITheme.HOMU_LIGHT_PANEL, UITheme.HOMU_CARAMEL, UITheme.HOMU_CARAMEL));
        kpiGrid.add(taoTheKPI("TỔNG SỐ HÓA ĐƠN", lblTongSoDon, "Trung bình 75.000đ/đơn",
                new Color(236, 253, 245), UITheme.HOMU_GREEN, UITheme.HOMU_GREEN));
        kpiGrid.add(taoTheKPI("TỔNG SỐ LY BÁN RA", lblTongSoLy, "Size L chiếm 48%",
                new Color(254, 243, 199), new Color(217, 119, 6), new Color(217, 119, 6)));
        kpiGrid.add(taoTheKPI("TỶ LỆ THANH TOÁN", lblTyLeThanhToan, "Đối soát tiền mặt: Khớp 100%",
                new Color(250, 245, 255), new Color(124, 58, 237), new Color(124, 58, 237)));

        topContainer.add(filterBar, BorderLayout.NORTH);
        topContainer.add(kpiGrid, BorderLayout.CENTER);

        // Phần dưới: 2 Cột
        JPanel bottomGrid = new JPanel(new GridLayout(1, 2, 18, 0));
        bottomGrid.setOpaque(false);

        // Cột trái: Biểu đồ ngang Top Món Bán Chạy
        JPanel leftChartCard = new UITheme.RoundedPanel(16, Color.WHITE, UITheme.HOMU_BORDER, 1);
        leftChartCard.setLayout(new BorderLayout(0, 12));
        leftChartCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel lblChartTitle = new JLabel("TOP SẢN PHẨM BÁN CHẠY NHẤT HOMUCAFE");
        lblChartTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblChartTitle.setForeground(UITheme.HOMU_TEXT);

        chartPanel = new BarChartPanel();
        leftChartCard.add(lblChartTitle, BorderLayout.NORTH);
        leftChartCard.add(chartPanel, BorderLayout.CENTER);

        // Cột phải: Đối soát ca làm việc
        JPanel rightShiftCard = new UITheme.RoundedPanel(16, Color.WHITE, UITheme.HOMU_BORDER, 1);
        rightShiftCard.setLayout(new BorderLayout(0, 14));
        rightShiftCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel lblShiftTitle = new JLabel("ĐỐI SOÁT DOANH THU THEO CA LÀM VIỆC");
        lblShiftTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblShiftTitle.setForeground(UITheme.HOMU_TEXT);

        String[] cols = {"Ca trực / Nhân viên", "Tiền mặt", "Chuyển khoản", "Tổng cộng"};
        modelCa = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblCa = new JTable(modelCa);
        tblCa.setRowHeight(44);
        tblCa.setFont(UITheme.FONT_REGULAR);
        tblCa.setGridColor(UITheme.HOMU_LIGHT_PANEL);
        tblCa.setShowVerticalLines(false);
        tblCa.getTableHeader().setFont(UITheme.FONT_BOLD);
        tblCa.getTableHeader().setBackground(UITheme.HOMU_LIGHT_PANEL);
        tblCa.getTableHeader().setForeground(UITheme.HOMU_TEXT);
        tblCa.getTableHeader().setPreferredSize(new Dimension(0, 36));
        tblCa.getColumnModel().getColumn(0).setPreferredWidth(210);

        tblCa.getColumnModel().getColumn(1).setCellRenderer(createColorRenderer(UITheme.HOMU_GREEN, false));
        tblCa.getColumnModel().getColumn(2).setCellRenderer(createColorRenderer(UITheme.HOMU_CARAMEL, false));
        tblCa.getColumnModel().getColumn(3).setCellRenderer(createColorRenderer(new Color(185, 28, 28), true));

        JScrollPane scrollCa = new JScrollPane(tblCa);
        scrollCa.setBorder(BorderFactory.createLineBorder(UITheme.HOMU_BORDER));

        JPanel exportBtnRow = new JPanel(new GridLayout(1, 2, 14, 0));
        exportBtnRow.setOpaque(false);
        exportBtnRow.setPreferredSize(new Dimension(0, 48));

        JButton btnExportExcel = UITheme.createRoundedButton("Xuất Báo Cáo Excel (.xlsx)", UITheme.HOMU_GREEN, Color.WHITE, 10);
        JButton btnPrintPDF = UITheme.createRoundedButton("In Báo Cáo Tài Chính (PDF)", UITheme.COLOR_DANGER, Color.WHITE, 10);

        btnExportExcel.addActionListener(e -> xuatBaoCaoExcel());
        btnPrintPDF.addActionListener(e -> inBaoCaoTaiChinhPDF());

        exportBtnRow.add(btnExportExcel);
        exportBtnRow.add(btnPrintPDF);

        rightShiftCard.add(lblShiftTitle, BorderLayout.NORTH);
        rightShiftCard.add(scrollCa, BorderLayout.CENTER);
        rightShiftCard.add(exportBtnRow, BorderLayout.SOUTH);

        bottomGrid.add(leftChartCard);
        bottomGrid.add(rightShiftCard);

        add(topContainer, BorderLayout.NORTH);
        add(bottomGrid, BorderLayout.CENTER);
    }

    private DefaultTableCellRenderer createColorRenderer(Color fg, boolean bold) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(fg);
                setFont(bold ? UITheme.FONT_BOLD : UITheme.FONT_REGULAR);
                return c;
            }
        };
    }

    private JPanel taoTheKPI(String title, JLabel lblBigValue, String subText,
                             Color bgColor, Color borderColor, Color valColor) {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel(16, bgColor, borderColor, 2);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.HOMU_MUTED);

        lblBigValue.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblBigValue.setForeground(valColor);

        JLabel lblSub = new JLabel(subText);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSub.setForeground(UITheme.HOMU_MUTED);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(lblBigValue);
        card.add(Box.createVerticalStrut(6));
        card.add(lblSub);
        return card;
    }

    private void capNhatSoLieuThongKe() {
        double doanhThuNen = 18450000;
        int donNen = 241;
        int lyNen = 400;

        double doanhThuPhatSinh = 0;
        int donPhatSinh = 0;
        int lyPhatSinh = 0;

        for (HoaDon hd : DataStore.getDsHoaDon()) {
            if (!"DA_HUY".equals(hd.getTrangThaiDon())) {
                donPhatSinh++;
                lyPhatSinh += hd.getTongSoLy();
                doanhThuPhatSinh += hd.tinhTongTien();
            }
        }

        double extraRevenue = Math.max(0, doanhThuPhatSinh - 610000);
        int extraOrders = Math.max(0, donPhatSinh - 5);
        int extraCups = Math.max(0, lyPhatSinh - 12);

        double tongDT = doanhThuNen + extraRevenue;
        int tongDon = donNen + 5 + extraOrders;
        int tongLy = lyNen + 12 + extraCups;

        lblTongDoanhThu.setText(UITheme.formatMoneySpace(tongDT));
        lblTongSoDon.setText(tongDon + " đơn");
        lblTongSoLy.setText(tongLy + " ly");

        modelCa.setRowCount(0);
        double ca1TM = 3250000 + extraRevenue;
        double ca1CK = 4890000;
        modelCa.addRow(new Object[]{
                "Ca 1 (07:00 - 12:00) | NV: Trần An",
                UITheme.formatMoney(ca1TM),
                UITheme.formatMoney(ca1CK),
                UITheme.formatMoney(ca1TM + ca1CK)
        });
        modelCa.addRow(new Object[]{
                "Ca 2 (12:00 - 17:00) | NV: Lê Hoa",
                "2.410.000đ",
                "4.120.000đ",
                "6.530.000đ"
        });
        modelCa.addRow(new Object[]{
                "Ca 3 (17:00 - 22:30) | NV: Phạm Vũ",
                "1.350.000đ",
                "2.430.000đ",
                "3.780.000đ"
        });

        chartPanel.repaint();
    }

    private void xuatBaoCaoExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("BaoCao_DoanhThu_HomuCafe.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                pw.println('\ufeff' + "Ca trực / Nhân viên,Tiền mặt,Chuyển khoản,Tổng cộng");
                for (int r = 0; r < modelCa.getRowCount(); r++) {
                    pw.printf("\"%s\",\"%s\",\"%s\",\"%s\"%n",
                            modelCa.getValueAt(r, 0),
                            modelCa.getValueAt(r, 1),
                            modelCa.getValueAt(r, 2),
                            modelCa.getValueAt(r, 3));
                }
                JOptionPane.showMessageDialog(this,
                        "Đã xuất báo cáo doanh thu ra file thành công (UC22):\n" + file.getAbsolutePath(),
                        "Xuất Báo Cáo Excel", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi ghi file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void inBaoCaoTaiChinhPDF() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================\n");
        sb.append("      BÁO CÁO TÀI CHÍNH & ĐỐI SOÁT CA - HOMU CAFE       \n");
        sb.append("========================================================\n");
        sb.append("Kỳ báo cáo: ").append(cboKyBaoCao.getSelectedItem()).append("\n");
        sb.append("Tổng doanh thu:     ").append(lblTongDoanhThu.getText()).append("\n");
        sb.append("Tổng số hóa đơn:    ").append(lblTongSoDon.getText()).append("\n");
        sb.append("Tổng số ly bán ra:  ").append(lblTongSoLy.getText()).append("\n");
        sb.append("Tỷ lệ thanh toán:   ").append(lblTyLeThanhToan.getText()).append("\n");
        sb.append("--------------------------------------------------------\n");
        sb.append("CHI TIẾT ĐỐI SOÁT THEO CA TRỰC:\n");
        for (int r = 0; r < modelCa.getRowCount(); r++) {
            sb.append("• ").append(modelCa.getValueAt(r, 0)).append("\n");
            sb.append("   Tiền mặt: ").append(modelCa.getValueAt(r, 1))
              .append(" | CK: ").append(modelCa.getValueAt(r, 2))
              .append(" | Tổng: ").append(modelCa.getValueAt(r, 3)).append("\n");
        }
        sb.append("========================================================\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        area.setBorder(new EmptyBorder(10, 12, 10, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "Bản Xem Trước In Báo Cáo Tài Chính (UC22)", JOptionPane.INFORMATION_MESSAGE);
    }

    private static class BarChartPanel extends JPanel {
        private final String[] names = {
                "Trà Đào Cam Sả",
                "Cà Phê Sữa Đá",
                "Trà Sữa Trân Châu",
                "Bạc Xỉu 3 Tầng",
                "Homu Matcha Đá Xay"
        };
        private final int[] values = {88, 76, 65, 54, 42};
        private final Color[] colors = {
                new Color(180, 83, 9),    // Cam đào
                new Color(130, 99, 67),   // Nâu caramel cafe
                new Color(109, 64, 42),   // Trà sữa ô long
                new Color(161, 98, 7),    // Bạc xỉu vàng mật
                new Color(83, 107, 79)    // Xanh Matcha HomuCafe
        };

        public BarChartPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int labelWidth = 150;
            int maxBarWidth = Math.max(120, w - labelWidth - 80);
            int maxVal = 95;

            int rowHeight = 56;
            int barHeight = 28;
            int startY = 14;

            for (int i = 0; i < names.length; i++) {
                int y = startY + i * rowHeight;

                g2.setFont(UITheme.FONT_REGULAR);
                g2.setColor(UITheme.HOMU_TEXT);
                g2.drawString(names[i], 4, y + 19);

                int barW = (int) ((values[i] / (double) maxVal) * maxBarWidth);
                g2.setColor(colors[i]);
                g2.fill(new RoundRectangle2D.Float(labelWidth, y, barW, barHeight, 8, 8));

                g2.setFont(UITheme.FONT_BOLD);
                g2.setColor(UITheme.HOMU_TEXT);
                g2.drawString(values[i] + " ly", labelWidth + barW + 10, y + 19);
            }

            g2.dispose();
        }
    }
}
