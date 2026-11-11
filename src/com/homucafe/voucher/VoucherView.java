package com.homucafe.voucher;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import com.homucafe.shared.ui.Theme;

/**
 * Giao diện quản lý Voucher & Mã giảm giá theo phong cách HomuCafe.
 * Cho phép chỉnh sửa trực tiếp trên bảng và nhấp chuyển đổi trạng thái "Đang sử dụng" / "Không sử dụng".
 */
public final class VoucherView extends JPanel {
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{
            "Tất cả trạng thái", "Đang sử dụng", "Không sử dụng"});
    private final JButton createButton = Theme.button("+ Tạo mã giảm giá", true);
    private final JButton editButton = Theme.button("Xem / sửa mã", false);
    private final JButton toggleButton = Theme.button("Đổi trạng thái sử dụng", false);

    private final String[] cols = {"MÃ VOUCHER", "ƯU ĐÃI", "ĐƠN TỐI THIỂU", "GIẢM TỐI ĐA", "ĐÃ DÙNG / GIỚI HẠN", "THỜI GIAN", "TRẠNG THÁI"};
    private final DefaultTableModel voucherModel = new DefaultTableModel(
            new Object[][]{
                    {"HOMU10", "Giảm 10%", "100.000 đ", "30.000 đ", "15 / 100", "01–31/10/2026", "Đang sử dụng"},
                    {"CHIEUVUI", "Giảm 15.000 đ", "80.000 đ", "—", "30 / 200", "01–31/10/2026", "Đang sử dụng"},
                    {"WELCOME20", "Giảm 20%", "150.000 đ", "40.000 đ", "0 / 80", "01–30/11/2026", "Đang sử dụng"},
                    {"HOMUOLD", "Giảm 10.000 đ", "60.000 đ", "—", "80 / 80", "01–30/09/2026", "Không sử dụng"}
            }, cols) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column > 0 && column != 6; // Cột mã khóa, cột trạng thái click toggle, các cột khác sửa trực tiếp
        }
    };
    private final JTable voucherTable;

    public VoucherView() {
        setLayout(new BorderLayout());
        voucherTable = Theme.table(cols, new Object[0][]);
        voucherTable.setModel(voucherModel);

        JPanel page = Theme.page("Ưu đãi & khuyến mãi", "Một ưu đãi, thêm một lần ghé.",
                "Quản lý voucher theo mã, điều kiện đơn hàng. Nhấp đúp ô để sửa trực tiếp hoặc nhấp cột Trạng thái để bật/tắt nhanh.");
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        JPanel statistics = Theme.cards(
                Theme.card("MÃ ĐANG HOẠT ĐỘNG", "3", "Số liệu để áp dụng đơn hàng"),
                Theme.card("LƯỢT SỬ DỤNG", "125", "45 lượt ở các mã đang hoạt động"),
                Theme.card("HÌNH THỨC ƯU ĐÃI", "% / số tiền", "Chọn một mã cho mỗi đơn"));
        statistics.setPreferredSize(new Dimension(700, 120));
        body.add(statistics, BorderLayout.NORTH);

        JPanel list = new JPanel(new BorderLayout(0, 16));
        list.setOpaque(false);
        searchField.putClientProperty("JTextField.placeholderText", "Tìm mã voucher…");
        searchField.putClientProperty("JTextField.leadingIcon", new Theme.LineIcon("search", 16, Theme.MUTED));
        searchField.putClientProperty("FlatLaf.style", "arc: 12; margin: 9,12,9,12");
        searchField.getAccessibleContext().setAccessibleName("Tìm mã voucher");
        statusFilter.getAccessibleContext().setAccessibleName("Lọc trạng thái voucher");
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.add(searchField, BorderLayout.CENTER);
        toolbar.add(Theme.row(statusFilter, createButton), BorderLayout.EAST);
        list.add(toolbar, BorderLayout.NORTH);
        list.add(Theme.tablePanel("Danh sách mã giảm giá HomuCafe (Nhấp vào Trạng thái để đổi nhanh)", voucherTable));

        JPanel bottom = new JPanel(new BorderLayout(10, 12));
        bottom.setOpaque(false);
        bottom.add(Theme.row(editButton, toggleButton), BorderLayout.NORTH);
        JLabel note = Theme.label("Chính sách: 1 mã / đơn; tự động chiết khấu vào tổng tiền thanh toán.", 11, Theme.MUTED);
        bottom.add(note, BorderLayout.SOUTH);
        list.add(bottom, BorderLayout.SOUTH);
        body.add(list, BorderLayout.CENTER);
        page.add(body);
        add(page);
        voucherTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        voucherTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        voucherTable.getColumnModel().getColumn(6).setPreferredWidth(145);

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
        String current = (String) voucherModel.getValueAt(row, 6);
        String next = "Đang sử dụng".equals(current) ? "Không sử dụng" : "Đang sử dụng";
        voucherModel.setValueAt(next, row, 6);
        voucherTable.repaint();
        new Thread(() -> {
            try {
                String ma = (String) voucherModel.getValueAt(row, 0);
                new com.homucafe.dao.Voucher_DAO().capNhatTrangThai(ma, "Đang sử dụng".equals(next));
            } catch (Throwable ignored) { }
        }).start();
    }

    private void showEditor() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Tạo mã giảm giá mới", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBorder(new EmptyBorder(24, 26, 24, 26));
        content.add(Theme.stack(7, Theme.title("Tạo một ưu đãi mới"),
                Theme.label("Nhập thông tin mã giảm giá để áp dụng vào hệ thống POS", 12, Theme.MUTED)), BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridLayout(0, 2, 16, 14));
        JTextField code = input("Ví dụ: HOMU10");
        JComboBox<String> type = new JComboBox<>(new String[]{"Giảm theo phần trăm", "Giảm số tiền cố định"});
        JTextField val = input("Ví dụ: 10");
        fields.add(field("Mã giảm giá", code));
        fields.add(field("Hình thức giảm", type));
        fields.add(field("Giá trị giảm (% hoặc VND)", val));
        JTextField cap = input("Ví dụ: 30000");
        fields.add(field("Giảm tối đa (VND, với mã %)", cap));
        JTextField minOrder = input("Ví dụ: 100000");
        fields.add(field("Giá trị đơn tối thiểu (VND)", minOrder));
        JTextField limit = input("Ví dụ: 100");
        fields.add(field("Tổng số lượt được dùng", limit));
        fields.add(field("Bắt đầu (ngày/tháng/năm giờ:phút)", input("01/11/2026 00:00")));
        fields.add(field("Kết thúc (ngày/tháng/năm giờ:phút)", input("30/11/2026 23:59")));
        content.add(fields);
        type.addActionListener(event -> cap.setEnabled(type.getSelectedIndex() == 0));
        JButton cancel = Theme.button("Đóng", false);
        cancel.addActionListener(event -> dialog.dispose());
        JButton save = Theme.button("Lưu mã", true);
        save.addActionListener(event -> {
            String c = code.getText().trim();
            if (!c.isEmpty()) {
                String giamStr = type.getSelectedIndex() == 0 ? "Giảm " + val.getText().trim() + "%" : "Giảm " + val.getText().trim() + " đ";
                voucherModel.addRow(new Object[]{
                        c, giamStr, minOrder.getText().trim() + " đ", cap.getText().trim() + " đ",
                        "0 / " + limit.getText().trim(), "01–30/11/2026", "Đang sử dụng"
                });
                JOptionPane.showMessageDialog(dialog, "Đã lưu mã giảm giá " + c + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            }
        });
        JPanel footer = new JPanel(new BorderLayout());
        footer.add(Theme.label("Áp dụng trực tiếp tại quầy thanh toán POS.", 11, Theme.MUTED));
        footer.add(Theme.row(cancel, save), BorderLayout.EAST);
        content.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private static JPanel field(String label, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.add(Theme.label(label, 12, Theme.INK), BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    private static JTextField input(String placeholder) {
        JTextField field = new JTextField();
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.putClientProperty("FlatLaf.style", "arc: 12; margin: 9,12,9,12");
        return field;
    }
}
