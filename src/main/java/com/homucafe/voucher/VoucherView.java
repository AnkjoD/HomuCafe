package com.homucafe.voucher;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.homucafe.shared.ui.Theme;

/** Giao diện quản lý mã giảm giá; chưa kiểm tra điều kiện, lưu hoặc áp mã thật. */
public final class VoucherView extends JPanel {
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{
            "Tất cả trạng thái", "Đang hoạt động", "Sắp bắt đầu", "Hết hạn", "Đã tắt"});
    private final JButton createButton = Theme.button("+ Tạo mã giảm giá", true);
    private final JButton editButton = Theme.button("Xem / sửa mã", false);
    private final JButton disableButton = Theme.button("Tắt mã", false);
    private final JTable voucherTable = Theme.table(
            new String[]{"MÃ VOUCHER", "ƯU ĐÃI", "ĐƠN TỐI THIỂU", "GIẢM TỐI ĐA", "ĐÃ DÙNG / GIỚI HẠN", "THỜI GIAN", "TRẠNG THÁI"},
            new Object[][]{
                    {"HOMU10", "Giảm 10%", "100.000 đ", "30.000 đ", "15 / 100", "01–31/10/2026", "Đang hoạt động"},
                    {"CHIEUVUI", "Giảm 15.000 đ", "80.000 đ", "—", "30 / 200", "01–31/10/2026", "Đang hoạt động"},
                    {"WELCOME20", "Giảm 20%", "150.000 đ", "40.000 đ", "0 / 80", "01–30/11/2026", "Sắp bắt đầu"},
                    {"HOMUOLD", "Giảm 10.000 đ", "60.000 đ", "—", "80 / 80", "01–30/09/2026", "Hết hạn"}
            });

    public VoucherView() {
        setLayout(new BorderLayout());
        JPanel page = Theme.page("Ưu đãi & khuyến mãi", "Một ưu đãi, thêm một lần ghé.",
                "Quản lý voucher theo mã, điều kiện đơn hàng và thời gian áp dụng.");
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        JPanel statistics = Theme.cards(
                Theme.card("MÃ ĐANG HOẠT ĐỘNG", "2", "Số liệu mẫu để duyệt giao diện"),
                Theme.card("LƯỢT SỬ DỤNG MẪU", "125", "45 lượt ở hai mã đang hoạt động"),
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
        list.add(Theme.tablePanel("Danh sách mã giảm giá · Dữ liệu minh họa", voucherTable));

        JPanel bottom = new JPanel(new BorderLayout(10, 12));
        bottom.setOpaque(false);
        bottom.add(Theme.row(editButton, disableButton), BorderLayout.NORTH);
        JLabel note = Theme.label("Mẫu đề xuất: 1 mã / đơn; chưa cộng dồn giảm thủ công. Điều kiện và lượt dùng phải kiểm tra ở Service.", 11, Theme.MUTED);
        bottom.add(note, BorderLayout.SOUTH);
        list.add(bottom, BorderLayout.SOUTH);
        body.add(list, BorderLayout.CENTER);
        page.add(body);
        add(page);
        voucherTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        voucherTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        voucherTable.getColumnModel().getColumn(6).setPreferredWidth(145);

        // Chỉ mở form giao diện. Chưa tạo voucher hoặc cập nhật dữ liệu bảng.
        createButton.addActionListener(event -> showEditor());
        for (JButton button : new JButton[]{editButton, disableButton}) {
            button.setEnabled(false);
            Theme.markUnwired(button);
        }
    }

    private void showEditor() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Tạo mã giảm giá · Bản giao diện", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBorder(new EmptyBorder(24, 26, 24, 26));
        content.add(Theme.stack(7, Theme.title("Tạo một ưu đãi mới"),
                Theme.label("Form xem trước · Chưa lưu dữ liệu", 12, Theme.MUTED)), BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridLayout(0, 2, 16, 14));
        JTextField code = input("Ví dụ: HOMU10");
        JComboBox<String> type = new JComboBox<>(new String[]{"Giảm theo phần trăm", "Giảm số tiền cố định"});
        fields.add(field("Mã giảm giá", code));
        fields.add(field("Hình thức giảm", type));
        fields.add(field("Giá trị giảm (% hoặc VND)", input("Ví dụ: 10")));
        JTextField cap = input("Ví dụ: 30000");
        fields.add(field("Giảm tối đa (VND, với mã %)", cap));
        fields.add(field("Giá trị đơn tối thiểu (VND)", input("Ví dụ: 100000")));
        fields.add(field("Tổng số lượt được dùng", input("Ví dụ: 100")));
        fields.add(field("Bắt đầu (ngày/tháng/năm giờ:phút)", input("01/11/2026 00:00")));
        fields.add(field("Kết thúc (ngày/tháng/năm giờ:phút)", input("30/11/2026 23:59")));
        content.add(fields);
        type.addActionListener(event -> cap.setEnabled(type.getSelectedIndex() == 0));
        JButton cancel = Theme.button("Đóng", false);
        cancel.addActionListener(event -> dialog.dispose());
        JButton save = Theme.button("Lưu mã", true);
        save.setEnabled(false);
        save.setToolTipText("Chưa nối VoucherController / VoucherService để kiểm tra và lưu mã.");
        JPanel footer = new JPanel(new BorderLayout());
        footer.add(Theme.label("Chưa tạo mã thật trong bản giao diện này.", 11, Theme.MUTED));
        footer.add(Theme.row(cancel, save), BorderLayout.EAST);
        content.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(680, 480));
        dialog.setLocationRelativeTo(owner);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    private static JTextField input(String placeholder) {
        JTextField field = new JTextField(20);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }

    private static JPanel field(String name, JComponent input) {
        JLabel label = Theme.label(name, 12, Theme.INK);
        label.setLabelFor(input);
        input.getAccessibleContext().setAccessibleName(name);
        return Theme.stack(7, label, input);
    }

    // Điểm nối cho Controller ở bước viết nghiệp vụ sau.
    public JTextField getSearchField() { return searchField; }
    public JComboBox<String> getStatusFilter() { return statusFilter; }
    public JTable getVoucherTable() { return voucherTable; }
    public JButton getEditButton() { return editButton; }
    public JButton getDisableButton() { return disableButton; }
}
