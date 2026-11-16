package com.homucafe.app;

import com.homucafe.entity.ChiTietHoaDon;
import com.homucafe.entity.HoaDon;
import com.homucafe.entity.KichCo;
import com.homucafe.entity.LoaiMon;
import com.homucafe.entity.Mon;
import com.homucafe.shared.ui.Theme;
import com.homucafe.util.DataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Màn hình 4: Quầy pha chế & Phục vụ (Kitchen Display System - KDS).
 * Hàng đợi FIFO trực quan, đếm thời gian chờ kèm cảnh báo (>10p ưu tiên gấp),
 * Kích hoạt thẻ rung khi pha chế xong, Hoàn tất & Thu hồi thẻ rung,
 * và Hộp thoại báo hết món nhanh (86) có tìm kiếm và lọc danh mục.
 */
public class PanelQuayPhaChe extends JPanel {

    public PanelQuayPhaChe() {
        setLayout(new BorderLayout());
        setOpaque(false);
        initUI();
    }

    private void initUI() {
        JPanel page = Theme.page("Pha chế & phục vụ", "Từng món, đúng nhịp.",
                "Hàng đợi FIFO thời gian thực · Cảnh báo thời gian chờ quá 10 phút · Báo hết món nhanh 86");
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);

        // Header toolbar: Nút báo hết món nhanh (86) có tìm kiếm & lọc nhóm
        JButton btnBaoHetMon = Theme.button("Báo hết món nhanh (86)", false);
        btnBaoHetMon.addActionListener(e -> showQuick86Dialog(SwingUtilities.getWindowAncestor(page)));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(Theme.label("Theo dõi tiến độ đơn hàng và thông báo thẻ rung", 12, Theme.MUTED), BorderLayout.WEST);
        topBar.add(btnBaoHetMon, BorderLayout.EAST);
        body.add(topBar, BorderLayout.NORTH);

        JPanel columns = new JPanel(new GridLayout(1, 2, 20, 0));
        columns.setOpaque(false);

        JPanel pnlChoPha = new JPanel(); pnlChoPha.setLayout(new BoxLayout(pnlChoPha, BoxLayout.Y_AXIS)); pnlChoPha.setOpaque(false);
        JPanel pnlSanSang = new JPanel(); pnlSanSang.setLayout(new BoxLayout(pnlSanSang, BoxLayout.Y_AXIS)); pnlSanSang.setOpaque(false);

        JScrollPane sc1 = new JScrollPane(pnlChoPha); sc1.setBorder(BorderFactory.createEmptyBorder()); sc1.setOpaque(false); sc1.getViewport().setOpaque(false);
        JScrollPane sc3 = new JScrollPane(pnlSanSang); sc3.setBorder(BorderFactory.createEmptyBorder()); sc3.setOpaque(false); sc3.getViewport().setOpaque(false);

        Runnable refreshBar = () -> {
            pnlChoPha.removeAll();
            pnlSanSang.removeAll();

            for (HoaDon hd : DataStore.getDsHoaDon()) {
                if ("HOAN_TAT".equals(hd.getTrangThaiDon()) || "DA_HUY".equals(hd.getTrangThaiDon())) continue;

                JPanel card = Theme.surface(new BorderLayout(0, 10));
                card.setBorder(new EmptyBorder(14, 14, 14, 14));
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

                long phutCho = Math.max(0, Duration.between(hd.getNgayLap(), LocalDateTime.now()).toMinutes());
                String timeStr = hd.getNgayLap().format(DateTimeFormatter.ofPattern("HH:mm"));
                String headerStr = hd.getMaHoaDon() + " · " + hd.getBanPhucVu() + (hd.getSoTheRung() > 0 ? " (Thẻ #" + hd.getSoTheRung() + ")" : "");

                JPanel ticketHeader = new JPanel(new BorderLayout());
                ticketHeader.setOpaque(false);
                ticketHeader.add(Theme.label(headerStr, 13, Theme.INK), BorderLayout.WEST);

                JLabel lblTime = Theme.label(timeStr + " (Chờ " + phutCho + "p)", 11, Theme.MUTED);
                ticketHeader.add(lblTime, BorderLayout.EAST);

                // Cảnh báo màu theo thời gian chờ (Xanh < 5p, Cam 5-10p, Đỏ > 10p)
                JLabel lblWarning = null;
                if ("DANG_PHA_CHE".equals(hd.getTrangThaiDon())) {
                    if (phutCho > 10) {
                        lblWarning = Theme.label("[!] ƯU TIÊN GẤP: Đã chờ hơn " + phutCho + " phút!", 11, new Color(220, 38, 38));
                        card.setBackground(new Color(254, 242, 242)); // Đỏ nhạt
                    } else if (phutCho >= 5) {
                        lblWarning = Theme.label("[!] SẮP TRỄ: Đã chờ " + phutCho + " phút", 11, new Color(217, 119, 6));
                        card.setBackground(new Color(255, 251, 235)); // Vàng cam
                    } else {
                        card.setBackground(Theme.WHITE);
                    }
                }

                JPanel itemsList = new JPanel();
                itemsList.setLayout(new BoxLayout(itemsList, BoxLayout.Y_AXIS));
                itemsList.setOpaque(false);

                if (lblWarning != null) {
                    itemsList.add(lblWarning);
                    itemsList.add(Box.createVerticalStrut(4));
                }

                for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
                    String line = ct.getSoLuong() + " × " + ct.getMon().getTenMon() + " (" + ct.getKichCo().name() + ")";
                    itemsList.add(Theme.label(line, 13, Theme.INK));
                    if (ct.getGhiChu() != null && !ct.getGhiChu().isEmpty()) {
                        itemsList.add(Theme.label("   ↳ " + ct.getGhiChu(), 11, Theme.ORANGE));
                    }
                }

                if ("DANG_PHA_CHE".equals(hd.getTrangThaiDon())) {
                    JButton btnStart = Theme.button("Hoàn thành & Báo rung thẻ #" + hd.getSoTheRung(), true);
                    btnStart.addActionListener(e -> {
                        hd.setTrangThaiDon("CHO_GIAO");
                        DataStore.saveTrangThaiHoaDon(hd.getMaHoaDon(), "CHO_GIAO");
                        JOptionPane.showMessageDialog(page,
                                "ĐÃ BẬT BÁO RUNG THẺ #" + hd.getSoTheRung() + "!\nKhách hàng đang di chuyển tới quầy nhận nước.",
                                "Kích hoạt thẻ rung", JOptionPane.INFORMATION_MESSAGE);
                    });
                    card.add(ticketHeader, BorderLayout.NORTH);
                    card.add(itemsList, BorderLayout.CENTER);
                    card.add(btnStart, BorderLayout.SOUTH);
                    pnlChoPha.add(card);
                    pnlChoPha.add(Box.createVerticalStrut(12));
                } else if ("CHO_GIAO".equals(hd.getTrangThaiDon())) {
                    card.setBackground(Theme.PALE);
                    JButton btnDone = Theme.button("✓ Đã giao khách & Thu hồi thẻ", false);
                    btnDone.addActionListener(e -> {
                        hd.setTrangThaiDon("HOAN_TAT");
                        DataStore.saveTrangThaiHoaDon(hd.getMaHoaDon(), "HOAN_TAT");

                        // Nếu là đơn mang đi: hoàn tất đơn
                        String msg = hd.getSoTheRung() > 0
                                ? "Đã thu hồi THẺ RUNG #" + hd.getSoTheRung() + " về khay sạc an toàn!\nĐơn hàng hoàn tất."
                                : "Đơn hàng Mang đi đã được giao hoàn tất!";
                        JOptionPane.showMessageDialog(page, msg, "Hoàn tất & Thu hồi thẻ", JOptionPane.INFORMATION_MESSAGE);
                    });
                    card.add(ticketHeader, BorderLayout.NORTH);
                    card.add(itemsList, BorderLayout.CENTER);
                    card.add(btnDone, BorderLayout.SOUTH);
                    pnlSanSang.add(card);
                    pnlSanSang.add(Box.createVerticalStrut(12));
                }
            }

            pnlChoPha.revalidate(); pnlChoPha.repaint();
            pnlSanSang.revalidate(); pnlSanSang.repaint();
        };

        DataStore.addDataChangeListener(refreshBar);
        refreshBar.run();
        new javax.swing.Timer(5000, e -> {
            if (page.isShowing()) {
                if (com.homucafe.connectdb.ConnectDB.getConnection() != null) {
                    DataStore.reloadFromDatabase();
                } else {
                    refreshBar.run();
                }
            }
        }).start();

        JPanel col1 = Theme.surface(new BorderLayout(0, 10)); col1.setBorder(new EmptyBorder(14, 14, 14, 14));
        col1.add(Theme.label("CHỜ PHA CHẾ (FIFO)", 12, Theme.ORANGE), BorderLayout.NORTH); col1.add(sc1, BorderLayout.CENTER);

        JPanel col2 = Theme.surface(new BorderLayout(0, 10)); col2.setBorder(new EmptyBorder(14, 14, 14, 14));
        col2.add(Theme.label("SẴN SÀNG GIAO (ĐÃ BÁO RUNG)", 12, Theme.GREEN), BorderLayout.NORTH); col2.add(sc3, BorderLayout.CENTER);

        columns.add(col1);
        columns.add(col2);
        body.add(columns, BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);
    }

    public static void showQuick86Dialog(Window owner) {
        JDialog dialog = new JDialog(owner, "Báo hết món nhanh (86) · HomuCafe", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Theme.WHITE);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        JTextField txtSearch86 = new JTextField();
        txtSearch86.putClientProperty("JTextField.placeholderText", "Tìm nhanh món theo tên, mã hoặc gõ tắt (cf, bx, td)...");
        txtSearch86.putClientProperty("FlatLaf.style", "arc: 12; margin: 8,12,8,12");

        JComboBox<String> cbCat86 = new JComboBox<>();
        cbCat86.addItem("Tất cả nhóm món");
        for (LoaiMon lm : DataStore.getDsLoaiMon()) {
            cbCat86.addItem(lm.getTenLoai());
        }
        cbCat86.putClientProperty("FlatLaf.style", "arc: 12");

        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);
        header.add(Theme.stack(3,
                Theme.title("Báo hết nguyên liệu / Tạm ngưng món (86)"),
                Theme.label("Nhấp đúp vào món hoặc bấm nút đổi trạng thái để khóa ngay trên quầy POS.", 12, Theme.MUTED)
        ), BorderLayout.NORTH);

        JPanel filterBar = new JPanel(new BorderLayout(10, 0));
        filterBar.setOpaque(false);
        filterBar.add(txtSearch86, BorderLayout.CENTER);
        filterBar.add(cbCat86, BorderLayout.EAST);
        header.add(filterBar, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        String[] cols = {"MÃ MÓN", "TÊN MÓN", "NHÓM MÓN", "TRẠNG THÁI"};
        DefaultTableModel model86 = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tbl86 = Theme.table(cols, new Object[0][]);
        tbl86.setModel(model86);
        tbl86.getColumnModel().getColumn(0).setPreferredWidth(80);
        tbl86.getColumnModel().getColumn(1).setPreferredWidth(210);
        tbl86.getColumnModel().getColumn(2).setPreferredWidth(115);
        tbl86.getColumnModel().getColumn(3).setPreferredWidth(120);

        Runnable reload86 = () -> {
            int selectedRow = tbl86.getSelectedRow();
            String selectedMa = (selectedRow >= 0 && selectedRow < model86.getRowCount())
                    ? (String) model86.getValueAt(selectedRow, 0) : null;
            model86.setRowCount(0);
            String q = txtSearch86.getText();
            String cat = (String) cbCat86.getSelectedItem();

            for (Mon m : DataStore.getDsMon()) {
                String tenLoai = m.getLoaiMon() != null ? m.getLoaiMon().getTenLoai() : "Khác";
                if (!"Tất cả nhóm món".equals(cat) && !tenLoai.equalsIgnoreCase(cat)) continue;
                if (!DataStore.khopTimKiemMon(m, q)) continue;
                model86.addRow(new Object[]{
                        m.getMaMon(),
                        m.getTenMon(),
                        tenLoai,
                        m.isDangKinhDoanh() ? "Đang bán" : "TẠM HẾT (86)"
                });
            }
            if (selectedMa != null) {
                for (int r = 0; r < model86.getRowCount(); r++) {
                    if (selectedMa.equals(model86.getValueAt(r, 0))) {
                        tbl86.setRowSelectionInterval(r, r);
                        break;
                    }
                }
            }
        };

        Runnable toggleSelected86 = () -> {
            int row = tbl86.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn 1 món trong danh sách!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String ma = (String) model86.getValueAt(row, 0);
            Mon m = DataStore.timMonTheoMa(ma);
            if (m != null) {
                m.setDangKinhDoanh(!m.isDangKinhDoanh());
                DataStore.saveMon(m);
                reload86.run();
            }
        };

        txtSearch86.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { reload86.run(); }
        });
        cbCat86.addActionListener(e -> reload86.run());
        tbl86.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    toggleSelected86.run();
                }
            }
        });

        reload86.run();
        root.add(new JScrollPane(tbl86), BorderLayout.CENTER);

        JButton btnToggle86 = Theme.button("Đổi trạng thái (Đang bán / Tạm hết 86)", true);
        btnToggle86.addActionListener(e -> toggleSelected86.run());

        JButton btnResetAll = Theme.button("Mở bán lại tất cả", false);
        btnResetAll.addActionListener(e -> {
            for (Mon m : DataStore.getDsMon()) {
                if (!m.isDangKinhDoanh()) {
                    m.setDangKinhDoanh(true);
                    DataStore.saveMon(m);
                }
            }
            reload86.run();
            JOptionPane.showMessageDialog(dialog, "Đã mở bán lại toàn bộ món trong thực đơn!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnClose = Theme.button("Đóng", false);
        btnClose.addActionListener(e -> dialog.dispose());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(btnResetAll, BorderLayout.WEST);
        footer.add(Theme.row(btnToggle86, btnClose), BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setSize(620, 500);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }
}
