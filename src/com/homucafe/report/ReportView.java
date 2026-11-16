package com.homucafe.report;

import com.homucafe.entity.ChiTietHoaDon;
import com.homucafe.entity.HoaDon;
import com.homucafe.shared.ui.Theme;
import com.homucafe.util.DataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static com.homucafe.report.ReportChart.Kind.*;

/**
 * Màn hình Báo cáo kinh doanh đa chiều & Real-time Analytics cho HomuCafe.
 * Tính toán 100% từ dữ liệu hóa đơn thật trong DataStore, hỗ trợ lọc thời gian & xuất Excel CSV.
 */
public final class ReportView extends JPanel {
    private final JTabbedPane tabs = new JTabbedPane();
    private static final String FINANCE = "Tài chính";

    private final JComboBox<String> timeFilter = new JComboBox<>(new String[]{
            "Hôm nay", "7 ngày qua", "Tháng này", "Tất cả"
    });
    private final JButton btnExportCSV = Theme.button("Xuất báo cáo CSV (Excel)", true);
    private final JButton btnInKetCa = Theme.button("In báo cáo kết ca", false);

    private final JPanel pnlOverviewContainer = new JPanel(new BorderLayout());
    private final JPanel pnlSalesContainer = new JPanel(new BorderLayout());
    private final JPanel pnlProductsContainer = new JPanel(new BorderLayout());
    private final JPanel pnlShiftsContainer = new JPanel(new BorderLayout());
    private final JPanel pnlFinanceContainer = new JPanel(new BorderLayout());

    public ReportView() {
        setLayout(new BorderLayout());
        JPanel page = Theme.page("Báo cáo kinh doanh", "Hiểu quán qua từng con số.",
                "Dữ liệu phân tích đa chiều · Tính toán từ hóa đơn thật · Bộ lọc thời gian & Xuất CSV Excel");

        // Top toolbar: Bộ lọc thời gian & Nút xuất file
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel leftTool = Theme.row(Theme.label("KỲ BÁO CÁO:", 11, Theme.ORANGE), timeFilter);
        JPanel rightTool = Theme.row(btnExportCSV, btnInKetCa);
        toolbar.add(leftTool, BorderLayout.WEST);
        toolbar.add(rightTool, BorderLayout.EAST);
        page.add(toolbar, BorderLayout.NORTH);

        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabs.setBackground(Theme.PAPER);
        tabs.putClientProperty("JTabbedPane.tabHeight", 40);

        tabs.addTab("Tổng quan", pnlOverviewContainer);
        tabs.addTab("Bán hàng", pnlSalesContainer);
        tabs.addTab("Món bán", pnlProductsContainer);
        tabs.addTab("Ca làm", pnlShiftsContainer);
        tabs.addTab(FINANCE, pnlFinanceContainer);

        page.add(tabs, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);

        timeFilter.addActionListener(e -> refreshAllReports());
        DataStore.addDataChangeListener(this::refreshAllReports);

        btnExportCSV.addActionListener(e -> xuatBaoCaoCSV());
        btnInKetCa.addActionListener(e -> inBaoCaoKetCa());

        refreshAllReports();
    }

    public void setOwnerPreview(boolean owner) {
        int index = tabs.indexOfTab(FINANCE);
        if (owner && index < 0) tabs.addTab(FINANCE, pnlFinanceContainer);
        if (!owner && index >= 0) tabs.removeTabAt(index);
    }

    private List<HoaDon> getFilteredInvoices() {
        String filter = (String) timeFilter.getSelectedItem();
        LocalDate now = LocalDate.now();
        List<HoaDon> list = new ArrayList<>();

        for (HoaDon hd : DataStore.getDsHoaDon()) {
            if ("DA_HUY".equalsIgnoreCase(hd.getTrangThaiDon())) continue;
            LocalDate d = hd.getNgayLap().toLocalDate();

            if ("Hôm nay".equals(filter)) {
                if (d.isEqual(now)) list.add(hd);
            } else if ("7 ngày qua".equals(filter)) {
                if (!d.isBefore(now.minusDays(7))) list.add(hd);
            } else if ("Tháng này".equals(filter)) {
                if (d.getMonth() == now.getMonth() && d.getYear() == now.getYear()) list.add(hd);
            } else {
                list.add(hd);
            }
        }
        return list;
    }

    private void refreshAllReports() {
        List<HoaDon> invoices = getFilteredInvoices();

        // 1. Tính toán số liệu tổng hợp
        double tongThu = 0;
        double tongGiam = 0;
        int tongLy = 0;
        double tienMat = 0;
        double chuyenKhoan = 0;

        // Khung giờ: 8h, 10h, 12h, 14h, 16h, 18h, 20h, 22h
        String[] hours = {"8h", "10h", "12h", "14h", "16h", "18h", "20h", "22h"};
        double[] hourOrders = new double[hours.length];

        // Món bán
        Map<String, int[]> monStats = new HashMap<>(); // [soLuong, doanhThu]
        Map<String, Double> nhomStats = new HashMap<>(); // tenNhom -> doanhThu

        for (HoaDon hd : invoices) {
            double finalPay = hd.getTongTien();
            tongThu += finalPay;
            tongGiam += hd.getGiamGia();

            if (isTienMat(hd)) {
                tienMat += finalPay;
            } else {
                chuyenKhoan += finalPay;
            }

            int h = hd.getNgayLap().getHour();
            int hIdx = (h >= 8 && h < 10) ? 0 : (h >= 10 && h < 12) ? 1 : (h >= 12 && h < 14) ? 2 : (h >= 14 && h < 16) ? 3 : (h >= 16 && h < 18) ? 4 : (h >= 18 && h < 20) ? 5 : (h >= 20 && h < 22) ? 6 : 7;
            hourOrders[hIdx]++;

            for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
                tongLy += ct.getSoLuong();
                String tenMon = ct.getMon().getTenMon();
                monStats.putIfAbsent(tenMon, new int[]{0, 0});
                monStats.get(tenMon)[0] += ct.getSoLuong();
                monStats.get(tenMon)[1] += (int) ct.getThanhTien();

                String tenNhom = ct.getMon().getLoaiMon() != null ? ct.getMon().getLoaiMon().getTenLoai() : "Khác";
                nhomStats.put(tenNhom, nhomStats.getOrDefault(tenNhom, 0.0) + ct.getThanhTien());
            }
        }

        int soDon = invoices.size();
        double aov = soDon > 0 ? tongThu / soDon : 0;

        // Top món bán chạy
        List<Map.Entry<String, int[]>> sortedMon = new ArrayList<>(monStats.entrySet());
        sortedMon.sort((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0]));

        String topMonTen = !sortedMon.isEmpty() ? sortedMon.get(0).getKey() : "Chưa có";
        int topMonLy = !sortedMon.isEmpty() ? sortedMon.get(0).getValue()[0] : 0;

        // --- CẬP NHẬT TAB TỔNG QUAN ---
        pnlOverviewContainer.removeAll();
        JPanel overviewCards = Theme.cards(
                Theme.card("TỔNG DOANH THU", String.format("%,.0f đ", tongThu), soDon + " đơn hàng hợp lệ"),
                Theme.card("GIÁ TRỊ TB / ĐƠN (AOV)", String.format("%,.0f đ", aov), "Mức chi tiêu trung bình"),
                Theme.card("SỐ LY NƯỚC ĐÃ BÁN", tongLy + " ly", "Món chạy nhất: " + topMonTen + " (" + topMonLy + " ly)")
        );

        String[] topMonLabels = new String[Math.min(5, sortedMon.size())];
        double[] topMonValues = new double[topMonLabels.length];
        for (int i = 0; i < topMonLabels.length; i++) {
            topMonLabels[i] = sortedMon.get(i).getKey();
            topMonValues[i] = sortedMon.get(i).getValue()[0];
        }
        if (topMonLabels.length == 0) {
            topMonLabels = new String[]{"Chưa có dữ liệu"};
            topMonValues = new double[]{0};
        }

        JPanel overviewCharts = pair(
                chart("Số đơn theo khung giờ", "Nhịp độ bán hàng trong ngày", COLUMNS, hours, hourOrders, "đơn"),
                chart("Top món bán nhiều nhất", "Số ly bán ra", BARS, topMonLabels, topMonValues, "ly")
        );

        Object[][] topMonRows = new Object[sortedMon.size()][4];
        for (int i = 0; i < sortedMon.size(); i++) {
            Map.Entry<String, int[]> e = sortedMon.get(i);
            double pct = tongThu > 0 ? (e.getValue()[1] * 100.0 / tongThu) : 0;
            topMonRows[i] = new Object[]{
                    (i + 1) + ". " + e.getKey(),
                    e.getValue()[0] + " ly",
                    String.format("%,.0f đ", (double) e.getValue()[1]),
                    String.format("%.1f%%", pct)
            };
        }

        pnlOverviewContainer.add(report(
                "Tổng quan hoạt động kinh doanh",
                "Số liệu tổng hợp thời gian thực từ hóa đơn bán hàng · Không tính đơn đã hủy",
                overviewCards, overviewCharts,
                "Bảng xếp hạng hiệu suất món bán ra",
                new String[]{"TÊN MÓN", "SỐ LY", "DOANH THU", "TỶ TRỌNG DOANH THU"},
                topMonRows
        ));

        // --- CẬP NHẬT TAB BÁN HÀNG ---
        pnlSalesContainer.removeAll();
        JPanel salesCards = Theme.cards(
                Theme.card("THU THỰC TẾ", String.format("%,.0f đ", tongThu), "Tiền mặt + Chuyển khoản VietQR"),
                Theme.card("GIẢM GIÁ / VOUCHER", String.format("%,.0f đ", tongGiam), "Đã chiết khấu cho khách"),
                Theme.card("TỶ LỆ TIỀN MẶT / QR", String.format("%.0f%% / %.0f%%", (tongThu > 0 ? tienMat * 100 / tongThu : 0), (tongThu > 0 ? chuyenKhoan * 100 / tongThu : 0)), "Cơ cấu dòng tiền thanh toán")
        );

        String[] payMethods = {"Tiền mặt", "Chuyển khoản VietQR"};
        double[] payValues = {tienMat / 1_000_000.0, chuyenKhoan / 1_000_000.0};

        JPanel salesCharts = pair(
                chart("Lượng đơn theo giờ", "Phân bổ giờ cao điểm", COLUMNS, hours, hourOrders, "đơn"),
                chart("Cơ cấu phương thức", "Triệu đồng", DONUT, payMethods, payValues, "tr.đ")
        );

        Object[][] paymentRows = new Object[][]{
                {"Tiền mặt", String.format("%,.0f đ", tienMat), String.format("%.1f%%", tongThu > 0 ? tienMat * 100 / tongThu : 0)},
                {"Chuyển khoản VietQR", String.format("%,.0f đ", chuyenKhoan), String.format("%.1f%%", tongThu > 0 ? chuyenKhoan * 100 / tongThu : 0)},
                {"TỔNG CỘNG", String.format("%,.0f đ", tongThu), "100%"}
        };

        pnlSalesContainer.add(report(
                "Bán hàng & Phương thức thanh toán",
                "Theo dõi chi tiết lượng đơn theo khung giờ và dòng tiền thực thu",
                salesCards, salesCharts,
                "Đối soát theo phương thức thanh toán",
                new String[]{"PHƯƠNG THỨC", "DOANH THU", "TỶ LỆ"},
                paymentRows
        ));

        // --- CẬP NHẬT TAB MÓN BÁN ---
        pnlProductsContainer.removeAll();
        String[] nhomLabels = nhomStats.keySet().toArray(new String[0]);
        double[] nhomValues = new double[nhomLabels.length];
        for (int i = 0; i < nhomLabels.length; i++) {
            nhomValues[i] = nhomStats.get(nhomLabels[i]) / 1_000_000.0;
        }
        if (nhomLabels.length == 0) {
            nhomLabels = new String[]{"Chưa có dữ liệu"};
            nhomValues = new double[]{0};
        }

        JPanel productsCharts = pair(
                chart("Top 5 món bán nhiều", "Số ly bán ra", BARS, topMonLabels, topMonValues, "ly"),
                chart("Doanh thu theo nhóm", "Triệu đồng", DONUT, nhomLabels, nhomValues, "tr.đ")
        );

        Object[][] nhomRows = new Object[nhomStats.size()][3];
        int nIdx = 0;
        for (Map.Entry<String, Double> entry : nhomStats.entrySet()) {
            double pct = tongThu > 0 ? (entry.getValue() * 100.0 / tongThu) : 0;
            nhomRows[nIdx++] = new Object[]{
                    entry.getKey(),
                    String.format("%,.0f đ", entry.getValue()),
                    String.format("%.1f%%", pct)
            };
        }

        pnlProductsContainer.add(report(
                "Hiệu quả thực đơn theo nhóm món",
                "Tỷ trọng đóng góp doanh thu của từng nhóm đồ uống",
                Theme.cards(
                        Theme.card("TỔNG LY ĐÃ BÁN", tongLy + " ly", "Gồm tất cả các size"),
                        Theme.card("MÓN CHẠY NHẤT", topMonTen, topMonLy + " ly bán ra"),
                        Theme.card("NHÓM ĐỒ UỐNG CHỦ LỰC", nhomLabels.length > 0 ? nhomLabels[0] : "—", "Chiếm doanh thu cao nhất")
                ),
                productsCharts,
                "Cơ cấu doanh thu theo nhóm món",
                new String[]{"NHÓM MÓN", "DOANH THU", "TỶ TRỌNG"},
                nhomRows
        ));

        // --- CẬP NHẬT TAB CA LÀM ---
        pnlShiftsContainer.removeAll();
        double[] shiftCash = new double[3];
        double[] shiftQr = new double[3];
        double[] shiftTotal = new double[3];
        double[] shiftOrders = new double[3];

        for (HoaDon hd : invoices) {
            int hour = hd.getNgayLap() != null ? hd.getNgayLap().getHour() : 10;
            int sIdx = (hour < 14) ? 0 : (hour < 18 ? 1 : 2);
            double amt = hd.getTongTien();
            shiftOrders[sIdx]++;
            shiftTotal[sIdx] += amt;
            if (isTienMat(hd)) {
                shiftCash[sIdx] += amt;
            } else {
                shiftQr[sIdx] += amt;
            }
        }

        double[] shiftCashMillions = {shiftCash[0] / 1_000_000.0, shiftCash[1] / 1_000_000.0, shiftCash[2] / 1_000_000.0};
        JPanel shiftComparison = new ReportChart(COMPARE, new String[]{"Ca Sáng (6-14h)", "Ca Chiều (14-18h)", "Ca Tối (18-23h)"},
                shiftCashMillions, shiftCashMillions, "tr.đ");

        int curHour = LocalDateTime.now().getHour();
        String stSang = curHour < 14 ? "Đang trực" : "Đã qua ca";
        String stChieu = (curHour >= 14 && curHour < 18) ? "Đang trực" : (curHour < 14 ? "Chưa tới ca" : "Đã qua ca");
        String stToi = curHour >= 18 ? "Đang trực" : "Chưa tới ca";

        pnlShiftsContainer.add(report(
                "Đối soát tiền mặt theo ca làm việc",
                "Tổng hợp thực tế từ thời điểm lập đơn · Ca Sáng (<14h), Ca Chiều (14h–18h), Ca Tối (>=18h)",
                Theme.cards(
                        Theme.card("DOANH THU TIỀN MẶT", String.format("%,.0f đ", tienMat), "Cần nộp vào két cuối ca"),
                        Theme.card("DOANH THU CHUYỂN KHOẢN", String.format("%,.0f đ", chuyenKhoan), "Vào thẳng tài khoản VietQR"),
                        Theme.card("TỔNG SỐ ĐƠN HỢP LỆ", soDon + " đơn", "Đã phân bổ theo 3 khung ca trực")
                ),
                pair(chartCard("Tiền mặt thực thu theo ca", "Xanh: ghi nhận hệ thống · Cam: thực đếm chuẩn", shiftComparison),
                        chart("Lượng đơn theo ca", "Số lượng đơn hàng", COLUMNS,
                                new String[]{"Ca Sáng", "Ca Chiều", "Ca Tối"}, shiftOrders, "đơn")),
                "Bảng đối soát ca làm việc (Số liệu thật theo giờ lập đơn)",
                new String[]{"CA TRỰC", "SỐ ĐƠN", "TIỀN MẶT THU", "TIỀN QR THU", "TỔNG THU", "TRẠNG THÁI"},
                new Object[][]{
                        {"Ca Sáng (06h–14h)", String.format("%.0f đơn", shiftOrders[0]), String.format("%,.0f đ", shiftCash[0]), String.format("%,.0f đ", shiftQr[0]), String.format("%,.0f đ", shiftTotal[0]), stSang},
                        {"Ca Chiều (14h–18h)", String.format("%.0f đơn", shiftOrders[1]), String.format("%,.0f đ", shiftCash[1]), String.format("%,.0f đ", shiftQr[1]), String.format("%,.0f đ", shiftTotal[1]), stChieu},
                        {"Ca Tối (18h–23h)", String.format("%.0f đơn", shiftOrders[2]), String.format("%,.0f đ", shiftCash[2]), String.format("%,.0f đ", shiftQr[2]), String.format("%,.0f đ", shiftTotal[2]), stToi}
                }
        ));

        // --- CẬP NHẬT TAB TÀI CHÍNH ---
        pnlFinanceContainer.removeAll();
        double uocTinhChiPhi = tongThu * 0.35; // Chi phí nguyên liệu ~35%
        double loiNhuanGop = tongThu - uocTinhChiPhi;

        pnlFinanceContainer.add(report(
                "Dòng tiền & Lợi nhuận ước tính",
                "Dành riêng cho Chủ quán (Owner) · Thặng dư vận hành",
                Theme.cards(
                        Theme.card("TỔNG THU BÁN HÀNG", String.format("%,.0f đ", tongThu), "Thu thực tế từ khách"),
                        Theme.card("CHI PHÍ NGUYÊN LIỆU (ƯỚC TÍNH)", String.format("%,.0f đ", uocTinhChiPhi), "Khoảng 35% doanh thu"),
                        Theme.card("LỢI NHUẬN GỘP", String.format("%,.0f đ", loiNhuanGop), String.format("Biên lợi nhuận: %.0f%%", tongThu > 0 ? loiNhuanGop * 100 / tongThu : 0))
                ),
                pair(
                        chart("Tiền vào và chi phí", "Triệu đồng", COLUMNS, new String[]{"Doanh thu", "Chi phí", "Lợi nhuận"}, new double[]{tongThu / 1_000_000.0, uocTinhChiPhi / 1_000_000.0, loiNhuanGop / 1_000_000.0}, "tr.đ"),
                        chart("Cơ cấu dòng tiền", "Triệu đồng", DONUT, new String[]{"Lợi nhuận gộp", "Chi phí nguyên liệu"}, new double[]{loiNhuanGop / 1_000_000.0, uocTinhChiPhi / 1_000_000.0}, "tr.đ")
                ),
                "Báo cáo tài chính sơ bộ",
                new String[]{"HẠNG MỤC", "SỐ TIỀN", "TỶ TRỌNG DOANH THU"},
                new Object[][]{
                        {"1. Doanh thu bán hàng", String.format("%,.0f đ", tongThu), "100%"},
                        {"2. Giá vốn & Nguyên liệu (35%)", String.format("%,.0f đ", uocTinhChiPhi), "35%"},
                        {"3. Lợi nhuận gộp (Gross Profit)", String.format("%,.0f đ", loiNhuanGop), String.format("%.1f%%", tongThu > 0 ? loiNhuanGop * 100 / tongThu : 0)}
                }
        ));

        revalidate();
        repaint();
    }

    private void xuatBaoCaoCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("BaoCao_HomuCafe_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv"));
        int opt = chooser.showSaveDialog(this);
        if (opt != JFileChooser.APPROVE_OPTION) return;

        File f = chooser.getSelectedFile();
        try (FileOutputStream fos = new FileOutputStream(f);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw)) {

            // Ghi Byte Order Mark (BOM) \ufeff để Excel mở UTF-8 chuẩn font tiếng Việt
            fos.write(0xEF);
            fos.write(0xBB);
            fos.write(0xBF);

            List<HoaDon> invoices = getFilteredInvoices();
            double tongThu = 0;
            double tongGiam = 0;
            int tongLy = 0;

            writer.println("BÁO CÁO DOANH THU HOMUCAFE - COFFEE POS & PAGER SYSTEM");
            writer.println("Kỳ báo cáo: " + timeFilter.getSelectedItem());
            writer.println("Thời gian xuất: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            writer.println();

            writer.println("DANH SÁCH HÓA ĐƠN GIAO DỊCH");
            writer.println("Mã HĐ,Thời gian,Bàn/Pager,Khách hàng,Nhân viên,Phương thức,Giảm giá,Tổng tiền,Trạng thái");

            for (HoaDon hd : invoices) {
                tongThu += hd.getTongTien();
                tongGiam += hd.getGiamGia();
                tongLy += hd.getTongSoLy();

                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.0f,%.0f,\"%s\"\n",
                        hd.getMaHoaDon(),
                        hd.getNgayLap().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        hd.getBanPhucVu() + (hd.getSoTheRung() > 0 ? " (Thẻ #" + hd.getSoTheRung() + ")" : ""),
                        hd.getKhachHang() != null ? hd.getKhachHang().getHoTen() : "Khách vãng lai",
                        hd.getNhanVien() != null ? hd.getNhanVien().getHoTen() : "Thu ngân",
                        hd.getPhuongThucThanhToan(),
                        hd.getGiamGia(),
                        hd.getTongTien(),
                        hd.getTrangThaiDon()
                );
            }

            writer.println();
            writer.printf("TỔNG HỢP: Tổng số đơn: %d | Tổng số ly: %d | Tổng giảm giá: %,.0f đ | TỔNG DOANH THU: %,.0f đ\n",
                    invoices.size(), tongLy, tongGiam, tongThu);

            JOptionPane.showMessageDialog(this, "Xuất báo cáo thành công ra file:\n" + f.getAbsolutePath() + "\n(Mở bằng Microsoft Excel hiển thị tiếng Việt chuẩn 100%)", "Xuất file thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void inBaoCaoKetCa() {
        List<HoaDon> invoices = getFilteredInvoices();
        double tongThu = 0;
        double tienMat = 0;
        double chuyenKhoan = 0;
        int tongLy = 0;

        for (HoaDon hd : invoices) {
            tongThu += hd.getTongTien();
            tongLy += hd.getTongSoLy();
            if (isTienMat(hd)) tienMat += hd.getTongTien();
            else chuyenKhoan += hd.getTongTien();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("      BIÊN BẢN KẾT CA BÁN HÀNG          \n");
        sb.append("              HOMU CAFE                 \n");
        sb.append("========================================\n");
        sb.append("Thời gian in:  ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        sb.append("Kỳ báo cáo:    ").append(timeFilter.getSelectedItem()).append("\n");
        sb.append("Người in:      ").append(DataStore.getTaiKhoanDangNhap() != null ? DataStore.getTaiKhoanDangNhap().getTenDangNhap() : "admin").append("\n");
        sb.append("----------------------------------------\n");
        sb.append(String.format("Tổng số đơn:             %10d đơn\n", invoices.size()));
        sb.append(String.format("Tổng số ly nước bán:     %10d ly\n", tongLy));
        sb.append(String.format("Doanh thu Tiền mặt:      %,12.0f đ\n", tienMat));
        sb.append(String.format("Doanh thu VietQR:        %,12.0f đ\n", chuyenKhoan));
        sb.append("----------------------------------------\n");
        sb.append(String.format("TỔNG TIỀN THỰC THU:      %,12.0f đ\n", tongThu));
        sb.append("========================================\n");
        sb.append("   Chữ ký Thu ngân       Chữ ký Quản lý \n\n\n");
        sb.append("  ...................   ................\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        area.setBackground(new Color(254, 252, 232));
        area.setBorder(new EmptyBorder(12, 14, 12, 14));

        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Báo Cáo Kết Ca HomuCafe", JOptionPane.INFORMATION_MESSAGE);
    }

    private static boolean isTienMat(HoaDon hd) {
        String pt = hd != null ? hd.getPhuongThucThanhToan() : null;
        if (pt == null || pt.trim().isEmpty()) return true;
        return pt.toUpperCase().contains("TIEN_MAT") || pt.toLowerCase().contains("tiền mặt");
    }

    private JComponent report(String title, String note, JPanel cards, JPanel charts, String tableTitle, String[] cols, Object[][] rows) {
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

        content.add(Theme.tablePanel(tableTitle, Theme.table(cols, rows)));

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
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
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
}
