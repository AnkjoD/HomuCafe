package com.homucafe.app;

import com.homucafe.entity.ChiTietHoaDon;
import com.homucafe.entity.HoaDon;
import com.homucafe.shared.ui.Theme;
import com.homucafe.util.DataStore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Màn hình 1: Tổng quan hoạt động kinh doanh (Dashboard).
 * Hiển thị thẻ chỉ số thời gian thực, biểu đồ nhịp bán hàng theo khung giờ và top món bán chạy.
 */
public class PanelTongQuan extends JPanel {

    public PanelTongQuan() {
        setLayout(new BorderLayout());
        setOpaque(false);
        initUI();
    }

    private void initUI() {
        JPanel page = Theme.page("Một ngày ở Homu", "Chào buổi sáng, chủ quán.",
                "Tổng quan hoạt động bán hàng · Doanh thu thực tế, số bàn và thẻ rung");
        JPanel body = new JPanel(new BorderLayout(0, 22));
        body.setOpaque(false);

        JPanel pnlCardsContainer = new JPanel(new BorderLayout());
        pnlCardsContainer.setOpaque(false);

        JPanel lower = new JPanel(new GridLayout(1, 2, 22, 0));
        lower.setOpaque(false);
        lower.add(new SalesChart());

        String[] topCols = {"MÓN ĐỒ UỐNG", "SỐ LY BÁN", "DOANH THU"};
        DefaultTableModel topModel = new DefaultTableModel(topCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable topTable = Theme.table(topCols, new Object[0][]);
        topTable.setModel(topModel);
        lower.add(Theme.tablePanel("Top món bán chạy nhất hôm nay", topTable));

        Runnable refreshDashboard = () -> {
            pnlCardsContainer.removeAll();
            topModel.setRowCount(0);

            long tongThu = 0;
            int donDaThanhToan = 0;
            int theRungDangDung = 0;
            int banDangPhucVu = 0;
            LocalDate activeDate = DataStore.getActiveBusinessDate();

            Map<String, int[]> monStats = new HashMap<>(); // [soLuong, doanhThu]

            for (HoaDon hd : DataStore.getDsHoaDon()) {
                boolean isActiveDate = hd.getNgayLap() != null && hd.getNgayLap().toLocalDate().isEqual(activeDate);
                if (!"DA_HUY".equals(hd.getTrangThaiDon()) && isActiveDate) {
                    tongThu += hd.getTongTien();
                    donDaThanhToan++;

                    for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
                        String ten = ct.getMon().getTenMon();
                        monStats.putIfAbsent(ten, new int[]{0, 0});
                        monStats.get(ten)[0] += ct.getSoLuong();
                        monStats.get(ten)[1] += (int) ct.getThanhTien();
                    }
                }
                if ("DANG_PHA_CHE".equals(hd.getTrangThaiDon()) || "CHO_GIAO".equals(hd.getTrangThaiDon())) {
                    theRungDangDung++;
                }
            }
            for (String st : DataStore.getTrangThaiBan().values()) {
                if ("DANG_PHUC_VU".equals(st)) banDangPhucVu++;
            }

            String dateSuffix = activeDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            pnlCardsContainer.add(Theme.cards(
                    Theme.card("TIỀN THU NGÀY " + dateSuffix, String.format("%,.0f đ", (double) tongThu), donDaThanhToan + " đơn đã thanh toán"),
                    Theme.card("BÀN ĐANG PHỤC VỤ", banDangPhucVu + " / 12", (12 - banDangPhucVu) + " bàn sẵn sàng đón khách"),
                    Theme.card("THẺ RUNG ĐANG DÙNG", theRungDangDung + " / 20", DataStore.demSoTheRungRanh() + " thẻ sẵn sàng trên khay sạc")
            ), BorderLayout.CENTER);

            // Sắp xếp top món
            List<Map.Entry<String, int[]>> sortedMon = new ArrayList<>(monStats.entrySet());
            sortedMon.sort((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0]));

            for (int i = 0; i < Math.min(6, sortedMon.size()); i++) {
                Map.Entry<String, int[]> entry = sortedMon.get(i);
                topModel.addRow(new Object[]{
                        entry.getKey(),
                        entry.getValue()[0] + " ly",
                        String.format("%,.0f đ", (double) entry.getValue()[1])
                });
            }

            pnlCardsContainer.revalidate();
            pnlCardsContainer.repaint();
            lower.revalidate();
            lower.repaint();
        };

        DataStore.addDataChangeListener(refreshDashboard);
        refreshDashboard.run();

        body.add(pnlCardsContainer, BorderLayout.NORTH);
        body.add(lower, BorderLayout.CENTER);
        page.add(body);
        add(page, BorderLayout.CENTER);
    }

    /**
     * Biểu đồ doanh thu nhịp bán hàng theo khung giờ vẽ bằng Java2D.
     */
    private static final class SalesChart extends JPanel {
        SalesChart() {
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                Theme.smooth(g);
                g.setColor(Theme.WHITE);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g.setColor(Theme.LINE);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                g.setColor(Theme.INK);
                g.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g.drawString("Nhịp bán hàng theo khung giờ", 24, 37);

                g.setColor(Theme.MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g.drawString("Số ly nước bán ra trong ngày", 24, 61);

                int bottom = getHeight() - 70;
                int available = Math.max(30, getHeight() - 180);
                int width = Math.max(12, (getWidth() - 105) / 7);

                LocalDate activeDate = DataStore.getActiveBusinessDate();
                int[] values = new int[7];
                String[] hours = {"8h", "10h", "12h", "14h", "16h", "18h", "20h"};
                for (HoaDon hd : DataStore.getDsHoaDon()) {
                    if ("DA_HUY".equalsIgnoreCase(hd.getTrangThaiDon()) || hd.getNgayLap() == null) continue;
                    if (!hd.getNgayLap().toLocalDate().isEqual(activeDate)) continue;
                    int hour = hd.getNgayLap().getHour();
                    int idx = (hour < 10) ? 0
                            : (hour < 12) ? 1
                            : (hour < 14) ? 2
                            : (hour < 16) ? 3
                            : (hour < 18) ? 4
                            : (hour < 20) ? 5 : 6;
                    values[idx] += hd.getTongSoLy();
                }

                int maxVal = 0;
                int maxIdx = 0;
                for (int i = 0; i < 7; i++) {
                    if (values[i] > maxVal) {
                        maxVal = values[i];
                        maxIdx = i;
                    }
                }
                int scaleTop = ((Math.max(10, maxVal) + 3) / 4) * 4;

                for (int n = 0; n <= 4; n++) {
                    int y = bottom - available * n / 4;
                    g.setColor(Theme.LINE);
                    g.drawLine(44, y, getWidth() - 25, y);
                    g.setColor(Theme.MUTED);
                    g.drawString(String.valueOf(scaleTop * n / 4), 20, y + 4);
                }

                for (int i = 0; i < 7; i++) {
                    int h = available * values[i] / scaleTop;
                    int x = 55 + i * width;
                    g.setColor(maxVal > 0 && i == maxIdx ? Theme.GREEN : new Color(210, 225, 204));
                    g.fillRoundRect(x, bottom - h, Math.max(8, width - 18), h, 8, 8);
                    g.setColor(Theme.MUTED);
                    g.drawString(hours[i], x + 5, bottom + 25);
                }
            } finally {
                g.dispose();
            }
        }
    }
}
