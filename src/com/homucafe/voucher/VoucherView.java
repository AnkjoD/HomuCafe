package com.homucafe.voucher;

import com.homucafe.entity.Voucher;
import com.homucafe.shared.ui.Theme;
import com.homucafe.util.DataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Giao diện quản lý Voucher & Mã giảm giá HomuCafe.
 * Kết nối đồng bộ 100% với DataStore & Voucher_DAO để POS sử dụng tức thì.
 */
public final class VoucherView extends JPanel {
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{
            "Tất cả trạng thái", "Đang sử dụng", "Không sử dụng"});
    private final JButton createButton = Theme.button("+ Tạo mã giảm giá", true);
    private final JButton toggleButton = Theme.button("Đổi trạng thái sử dụng", false);

    private final String[] cols = {"MÃ VOUCHER", "HÌNH THỨC", "GIÁ TRỊ GIẢM", "ĐƠN TỐI THIỂU", "GIẢM TỐI ĐA", "ĐÃ DÙNG / GIỚI HẠN", "TRẠNG THÁI"};
    private final DefaultTableModel voucherModel = new DefaultTableModel(cols, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable voucherTable;

    public VoucherView() {
        setLayout(new BorderLayout());
        voucherTable = Theme.table(cols, new Object[0][]);
        voucherTable.setModel(voucherModel);

        JPanel page = Theme.page("Ưu đãi & khuyến mãi", "Một ưu đãi, thêm một lần ghé.",
                "Quản lý voucher theo mã, điều kiện đơn hàng. Nhấp cột Trạng thái để bật/tắt nhanh mã giảm giá.");
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);

        JPanel statistics = Theme.cards(
                Theme.card("MÃ ĐANG HOẠT ĐỘNG", "4", "Được áp dụng trực tiếp tại POS"),
                Theme.card("HÌNH THỨC GIẢM", "% hoặc Tiền mặt", "Có trần giảm tối đa"),
                Theme.card("ĐIỀU KIỆN ÁP DỤNG", "Đơn tối thiểu", "Tự động kiểm tra tại POS"));
        statistics.setPreferredSize(new Dimension(700, 120));
        body.add(statistics, BorderLayout.NORTH);

        JPanel list = new JPanel(new BorderLayout(0, 16));
        list.setOpaque(false);
        searchField.putClientProperty("JTextField.placeholderText", "Tìm mã voucher…");
        searchField.putClientProperty("FlatLaf.style", "arc: 12; margin: 9,12,9,12");

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.add(searchField, BorderLayout.CENTER);
        toolbar.add(Theme.row(statusFilter, createButton), BorderLayout.EAST);
        list.add(toolbar, BorderLayout.NORTH);
        list.add(Theme.tablePanel("Danh sách mã giảm giá HomuCafe (Nhấp vào Trạng thái để đổi nhanh)", voucherTable));

        JPanel bottom = new JPanel(new BorderLayout(10, 12));
        bottom.setOpaque(false);
        bottom.add(Theme.row(toggleButton), BorderLayout.NORTH);
        JLabel note = Theme.label("Chính sách: Áp dụng 1 mã cho mỗi hóa đơn; kiểm tra điều kiện đơn tối thiểu và hạn dùng tự động.", 11, Theme.MUTED);
        bottom.add(note, BorderLayout.SOUTH);
        list.add(bottom, BorderLayout.SOUTH);
        body.add(list, BorderLayout.CENTER);
        page.add(body);
        add(page);

        // Nạp dữ liệu từ DataStore
        Runnable loadVouchers = () -> {
            voucherModel.setRowCount(0);
            String q = searchField.getText().trim().toLowerCase();
            String st = (String) statusFilter.getSelectedItem();

            for (Voucher v : DataStore.getDsVoucher()) {
                if (!q.isEmpty() && !v.getMaVoucher().toLowerCase().contains(q)) continue;
                if ("Đang sử dụng".equals(st) && !v.isTrangThai()) continue;
                if ("Không sử dụng".equals(st) && v.isTrangThai()) continue;

                String hinhThucStr = "PHAN_TRAM".equalsIgnoreCase(v.getHinhThucGiam()) ? "Phần trăm (%)" : "Số tiền cố định";
                String giaTriStr = "PHAN_TRAM".equalsIgnoreCase(v.getHinhThucGiam())
                        ? String.format("%.0f%%", v.getGiaTriGiam())
                        : String.format("%,.0f đ", v.getGiaTriGiam());
                String tranStr = v.getGiamToiDa() > 0 ? String.format("%,.0f đ", v.getGiamToiDa()) : "—";
                String limitStr = v.getGioiHanLuot() > 0 ? (v.getSoLuotDung() + " / " + v.getGioiHanLuot()) : (v.getSoLuotDung() + " / ∞");

                voucherModel.addRow(new Object[]{
                        v.getMaVoucher(),
                        hinhThucStr,
                        giaTriStr,
                        String.format("%,.0f đ", v.getDonToiThieu()),
                        tranStr,
                        limitStr,
                        v.isTrangThai() ? "Đang sử dụng" : "Không sử dụng"
                });
            }
        };

        DataStore.addDataChangeListener(loadVouchers);
        loadVouchers.run();

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { loadVouchers.run(); }
        });
        statusFilter.addActionListener(e -> loadVouchers.run());

        // Click trực tiếp vào cột Trạng thái để bật/tắt sử dụng
        voucherTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = voucherTable.rowAtPoint(e.getPoint());
                int col = voucherTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 6) {
                    toggleVoucherStatus(row);
                }
            }
        });

        toggleButton.addActionListener(e -> {
            int row = voucherTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng voucher trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            toggleVoucherStatus(row);
        });

        createButton.addActionListener(event -> showEditor());
    }

    private void toggleVoucherStatus(int row) {
        String ma = (String) voucherModel.getValueAt(row, 0);
        Voucher v = DataStore.timVoucherTheoMa(ma);
        if (v != null) {
            boolean next = !v.isTrangThai();
            DataStore.capNhatTrangThaiVoucher(ma, next);
        }
    }

    private void showEditor() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Tạo mã giảm giá mới", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBorder(new EmptyBorder(24, 26, 24, 26));
        content.setBackground(Theme.WHITE);

        content.add(Theme.stack(5, Theme.title("Tạo mã voucher ưu đãi mới"),
                Theme.label("Nhập thông tin mã giảm giá để áp dụng ngay vào màn hình Bán hàng (POS)", 12, Theme.MUTED)), BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(0, 2, 16, 14));
        fields.setOpaque(false);

        JTextField code = input("Ví dụ: HOMU50");
        JComboBox<String> type = new JComboBox<>(new String[]{"Giảm theo phần trăm (%)", "Giảm số tiền cố định (VND)"});
        JTextField val = input("10 (cho %) hoặc 20000 (cho VND)");
        JTextField cap = input("50000 (Trần giảm tối đa)");
        JTextField minOrder = input("50000 (Đơn tối thiểu)");
        JTextField limit = input("100 (Tổng số lượt)");

        fields.add(field("Mã giảm giá (*)", code));
        fields.add(field("Hình thức giảm", type));
        fields.add(field("Giá trị giảm (*)", val));
        fields.add(field("Giảm tối đa (VND, với %)", cap));
        fields.add(field("Giá trị đơn tối thiểu (VND)", minOrder));
        fields.add(field("Tổng số lượt được dùng", limit));

        content.add(fields, BorderLayout.CENTER);

        type.addActionListener(event -> cap.setEnabled(type.getSelectedIndex() == 0));

        JButton cancel = Theme.button("Đóng", false);
        cancel.addActionListener(event -> dialog.dispose());

        JButton save = Theme.button("✓ Lưu mã ưu đãi", true);
        save.addActionListener(event -> {
            String c = code.getText().trim().toUpperCase();
            if (c.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập mã giảm giá!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (DataStore.timVoucherTheoMa(c) != null) {
                JOptionPane.showMessageDialog(dialog, "Mã voucher " + c + " đã tồn tại!", "Trùng mã", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double giaTri = 10;
            double giamMax = 0;
            double donMin = 0;
            int tongLuot = 100;

            try {
                giaTri = Double.parseDouble(val.getText().replaceAll("[^0-9]", ""));
            } catch (Exception ignored) { }
            try {
                giamMax = Double.parseDouble(cap.getText().replaceAll("[^0-9]", ""));
            } catch (Exception ignored) { }
            try {
                donMin = Double.parseDouble(minOrder.getText().replaceAll("[^0-9]", ""));
            } catch (Exception ignored) { }
            try {
                tongLuot = Integer.parseInt(limit.getText().replaceAll("[^0-9]", ""));
            } catch (Exception ignored) { }

            String hinhThuc = type.getSelectedIndex() == 0 ? "PHAN_TRAM" : "TIEN_MAT";
            LocalDate start = LocalDate.now();
            LocalDate end = start.plusMonths(2);

            Voucher newVoucher = new Voucher(c, hinhThuc, giaTri, giamMax, donMin, 0, tongLuot, start, end, true);
            DataStore.saveVoucher(newVoucher);

            JOptionPane.showMessageDialog(dialog, "Đã lưu mã giảm giá " + c + " thành công!\nMã đã sẵn sàng áp dụng ngay tại quầy POS.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(Theme.label("Áp dụng trực tiếp tại quầy thanh toán POS.", 11, Theme.MUTED));
        footer.add(Theme.row(cancel, save), BorderLayout.EAST);
        content.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private static JPanel field(String label, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel lbl = Theme.label(label, 12, Theme.INK);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    private static JTextField input(String placeholder) {
        JTextField field = new JTextField();
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.putClientProperty("FlatLaf.style", "arc: 10; margin: 8,10,8,10");
        return field;
    }
}
