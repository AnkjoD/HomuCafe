package gui;

import entity.ChiTietHoaDon;
import entity.HoaDon;
import util.DataStore;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Điều phối Quầy Bar theo thời gian thực (FrmManHinhBar - HomuCafe)
 * Tông màu Dark Cafe sang trọng: Nền #1C1917, Thẻ #292524, Cảnh báo thời gian chờ trực quan.
 */
public class FrmManHinhBar extends JPanel {
    private JLabel lblThongKeHangDoi;
    private JPanel pnlCardsGrid;

    public FrmManHinhBar() {
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BAR_BG);
        setBorder(new EmptyBorder(0, 0, 16, 0));

        initComponents();
        lamMoiDanhSachDonBar();

        DataStore.addDataChangeListener(this::lamMoiDanhSachDonBar);

        Timer timer = new Timer(30000, e -> lamMoiDanhSachDonBar());
        timer.start();
    }

    private void initComponents() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.HOMU_HEADER);
        topBar.setBorder(new EmptyBorder(14, 22, 14, 22));

        JLabel lblTitle = new JLabel("BAR MONITOR - MÀN HÌNH ĐIỀU PHỐI QUẦY PHA CHẾ (REAL-TIME QUEUE)");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblTitle.setForeground(new Color(250, 246, 239));

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightBox.setOpaque(false);

        lblThongKeHangDoi = new JLabel("Đang chờ pha: 0 đơn (0 ly) | Đã gọi rung: 0");
        lblThongKeHangDoi.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblThongKeHangDoi.setForeground(new Color(134, 239, 172)); // Xanh matcha sáng

        JButton btnDualScreen = UITheme.createRoundedButton("Mở Cửa Sổ Bar Rời", UITheme.HOMU_CARAMEL, Color.WHITE, 8);
        btnDualScreen.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnDualScreen.setBorder(new EmptyBorder(6, 12, 6, 12));
        btnDualScreen.addActionListener(e -> moCuaSoBarRoi());

        rightBox.add(lblThongKeHangDoi);
        rightBox.add(btnDualScreen);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(rightBox, BorderLayout.EAST);

        pnlCardsGrid = new JPanel(new GridLayout(0, 3, 20, 20));
        pnlCardsGrid.setOpaque(false);
        pnlCardsGrid.setBorder(new EmptyBorder(10, 22, 20, 22));

        JPanel gridWrap = new JPanel(new BorderLayout());
        gridWrap.setOpaque(false);
        gridWrap.add(pnlCardsGrid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(gridWrap);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void lamMoiDanhSachDonBar() {
        pnlCardsGrid.removeAll();

        List<HoaDon> dsDangLam = new ArrayList<>();
        List<HoaDon> dsChoGiao = new ArrayList<>();
        int tongLyChoPha = 0;

        for (HoaDon hd : DataStore.getDsHoaDon()) {
            if ("DANG_PHA_CHE".equals(hd.getTrangThaiDon())) {
                dsDangLam.add(hd);
                tongLyChoPha += hd.getTongSoLy();
            } else if ("CHO_GIAO".equals(hd.getTrangThaiDon())) {
                dsChoGiao.add(hd);
            }
        }

        lblThongKeHangDoi.setText(String.format("Đang chờ pha: %d đơn (%d ly) | Đã gọi rung: %d",
                dsDangLam.size(), tongLyChoPha, dsChoGiao.size()));

        for (HoaDon hd : dsDangLam) {
            pnlCardsGrid.add(taoTheDonBar(hd));
        }
        for (HoaDon hd : dsChoGiao) {
            pnlCardsGrid.add(taoTheDonBar(hd));
        }

        pnlCardsGrid.revalidate();
        pnlCardsGrid.repaint();
    }

    private JPanel taoTheDonBar(HoaDon hd) {
        long phutCho = Math.max(1, Duration.between(hd.getNgayLap(), LocalDateTime.now()).toMinutes());
        boolean isDangLam = "DANG_PHA_CHE".equals(hd.getTrangThaiDon());
        boolean isTaiQuan = "TAI_QUAN".equals(hd.getLoaiDon());

        Color headerColor;
        String badgeText;
        String subTitleText;

        if (isDangLam) {
            if (phutCho < 5) {
                headerColor = UITheme.HOMU_GREEN; // Xanh matcha (<5 phút)
                badgeText = "ĐANG LÀM";
            } else if (phutCho <= 10) {
                headerColor = new Color(217, 119, 6); // Cam hổ phách (5-10 phút)
                badgeText = "SẮP TRỄ";
            } else {
                headerColor = new Color(220, 38, 38); // Đỏ (>10 phút)
                badgeText = "ƯU TIÊN GẤP";
            }
            subTitleText = (isTaiQuan ? "UỐNG TẠI QUÁN" : "MANG VỀ (TAKE-AWAY)") + " - Chờ: " + phutCho + " phút";
        } else {
            headerColor = new Color(124, 58, 237); // Tím gọi rung
            badgeText = isTaiQuan ? "ĐANG RUNG CHUÔNG" : "CHỜ KHÁCH LẤY";
            subTitleText = (isTaiQuan ? "UỐNG TẠI QUÁN - Đã bấm gọi: 1 phút trước" : "MANG VỀ - Đã gọi loa: 3 phút trước");
        }

        String mainTitle = isTaiQuan
                ? String.format("THẺ RUNG #%02d", hd.getSoTheRung())
                : String.format("SỐ THỨ TỰ #%02d", hd.getSoThuTuNgay());

        UITheme.RoundedPanel card = new UITheme.RoundedPanel(16, UITheme.BAR_CARD, headerColor, 2);
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(360, 330));

        JPanel cardHeader = new JPanel(new BorderLayout(4, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(headerColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 14, 14, 14);
                g2.fillRect(0, getHeight() - 14, getWidth(), 14);
                g2.dispose();
            }
        };
        cardHeader.setOpaque(false);
        cardHeader.setBorder(new EmptyBorder(12, 16, 10, 16));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel lblMainId = new JLabel(mainTitle);
        lblMainId.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblMainId.setForeground(Color.WHITE);

        JLabel lblBadge = new JLabel(badgeText);
        lblBadge.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblBadge.setForeground(Color.WHITE);

        topRow.add(lblMainId, BorderLayout.WEST);
        topRow.add(lblBadge, BorderLayout.EAST);

        JLabel lblSub = new JLabel(subTitleText);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSub.setForeground(new Color(244, 239, 230));

        cardHeader.add(topRow, BorderLayout.NORTH);
        cardHeader.add(lblSub, BorderLayout.SOUTH);

        JPanel itemsPanel = new JPanel();
        itemsPanel.setOpaque(false);
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBorder(new EmptyBorder(14, 16, 10, 16));

        for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
            String line = String.format("<html><div style='width:270px;color:#FAF6EF;font-size:13px;'>"
                            + "• %dx <b>%s</b> [Size %s, <span style='color:#D6D3D1;'>%s</span>]</div></html>",
                    ct.getSoLuong(),
                    ct.getMon().getTenMon(),
                    ct.getKichCo().getKyHieu(),
                    ct.getMoTaTuyBien());
            JLabel lblItem = new JLabel(line);
            lblItem.setAlignmentX(Component.LEFT_ALIGNMENT);
            itemsPanel.add(lblItem);
            itemsPanel.add(Box.createVerticalStrut(10));
        }

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 16, 14, 16));

        if (isDangLam) {
            JButton btnGoiRung = UITheme.createRoundedButton(
                    "🔔 HOÀN THÀNH (BẤM GỌI THẺ RUNG)",
                    new Color(217, 119, 6), Color.WHITE, 10);
            btnGoiRung.setPreferredSize(new Dimension(0, 44));
            btnGoiRung.addActionListener(e -> {
                hd.capNhatTrangThai("CHO_GIAO");
                DataStore.notifyDataChanged();
                String msg = isTaiQuan
                        ? "Đã phát sóng kích hoạt THẺ RUNG #" + String.format("%02d", hd.getSoTheRung()) + "!\nThẻ trên tay khách đang rung và nhấp nháy đèn LED."
                        : "Đã gọi SỐ THỨ TỰ #" + String.format("%02d", hd.getSoThuTuNgay()) + " ra quầy nhận nước!";
                JOptionPane.showMessageDialog(this, msg, "Kích hoạt gọi khách (UC12)", JOptionPane.INFORMATION_MESSAGE);
            });
            footer.add(btnGoiRung, BorderLayout.CENTER);
        } else {
            JButton btnDaGiao = UITheme.createRoundedButton(
                    "✔ ĐÃ GIAO NƯỚC (THU HỒI THẺ)",
                    UITheme.HOMU_GREEN, Color.WHITE, 10);
            btnDaGiao.setPreferredSize(new Dimension(0, 44));
            btnDaGiao.addActionListener(e -> {
                hd.capNhatTrangThai("HOAN_TAT");
                DataStore.notifyDataChanged();
                String msg = isTaiQuan
                        ? "Đã giao nước và thu hồi THẺ RUNG #" + String.format("%02d", hd.getSoTheRung()) + " về khay sạc!\nThẻ đã sẵn sàng cho lượt khách tiếp theo."
                        : "Đã bàn giao đơn Mang đi STT #" + String.format("%02d", hd.getSoThuTuNgay()) + " hoàn tất!";
                JOptionPane.showMessageDialog(this, msg, "Hoàn tất & Thu hồi thẻ (UC13)", JOptionPane.INFORMATION_MESSAGE);
            });
            footer.add(btnDaGiao, BorderLayout.CENTER);
        }

        card.add(cardHeader, BorderLayout.NORTH);
        card.add(itemsPanel, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private void moCuaSoBarRoi() {
        JFrame frame = new JFrame("Bar Monitor - Quầy Pha Chế HomuCafe (Dual Screen)");
        frame.setSize(1220, 760);
        frame.setLocationRelativeTo(this);
        frame.setContentPane(new FrmManHinhBar());
        frame.setVisible(true);
    }
}
