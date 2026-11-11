package com.homucafe.app;

import com.homucafe.shared.ui.DrinkIllustration;
import com.homucafe.shared.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Hộp thoại tùy biến đồ uống: Chọn kích cỡ (S/M/L), Mức đường, Mức đá, Topping.
 */
public class DialogTuyBienMon extends JDialog {
    private String tenMon;
    private double giaGoc;
    private int drinkKind;
    private boolean confirmed = false;

    private JComboBox<String> cbSize;
    private JComboBox<String> cbDuong;
    private JComboBox<String> cbDa;
    private JCheckBox chkTranChauTrang;
    private JCheckBox chkThachDao;
    private JCheckBox chkKemCheese;
    private JLabel lblTongTien;

    private double tongGia;
    private String customDetail;

    public DialogTuyBienMon(Window owner, String tenMon, double giaGoc, int drinkKind) {
        super(owner, "Tùy biến món · " + tenMon, ModalityType.APPLICATION_MODAL);
        this.tenMon = tenMon;
        this.giaGoc = giaGoc;
        this.drinkKind = drinkKind;
        initUI(owner);
    }

    private void initUI(Window owner) {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.WHITE);

        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout(15, 0));
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(20, 24, 15, 24));

        DrinkIllustration illustration = new DrinkIllustration(drinkKind);
        illustration.setPreferredSize(new Dimension(80, 80));
        pnlHeader.add(illustration, BorderLayout.WEST);

        JLabel lblTitle = Theme.label(tenMon, 20, Theme.INK);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JLabel lblSub = Theme.label("Giá cơ bản (Size S): " + String.format("%,.0f đ", giaGoc), 13, Theme.ORANGE);
        pnlHeader.add(Theme.stack(6, lblTitle, lblSub), BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        // Options form
        JPanel pnlForm = new JPanel(new GridLayout(0, 2, 16, 14));
        pnlForm.setOpaque(false);
        pnlForm.setBorder(new EmptyBorder(10, 24, 15, 24));

        cbSize = new JComboBox<>(new String[]{
                "Size S (+0 đ)",
                "Size M (+6.000 đ)",
                "Size L (+12.000 đ)"
        });
        cbSize.setSelectedIndex(1); // default Size M

        cbDuong = new JComboBox<>(new String[]{
                "100% Đường (Chuẩn)",
                "70% Đường",
                "50% Đường",
                "30% Đường",
                "0% Đường (Không ngọt)"
        });

        cbDa = new JComboBox<>(new String[]{
                "Đá thường (100%)",
                "Ít đá (50%)",
                "Không đá",
                "Uống nóng"
        });

        pnlForm.add(field("Kích cỡ (Size)", cbSize));
        pnlForm.add(field("Mức đường", cbDuong));
        pnlForm.add(field("Mức đá / Nhiệt độ", cbDa));

        // Toppings
        chkTranChauTrang = new JCheckBox("Trân châu trắng (+8.000 đ)");
        chkThachDao = new JCheckBox("Thạch đào giòn (+8.000 đ)");
        chkKemCheese = new JCheckBox("Kem cheese béo (+10.000 đ)");

        JPanel pnlTopping = new JPanel();
        pnlTopping.setLayout(new BoxLayout(pnlTopping, BoxLayout.Y_AXIS));
        pnlTopping.setOpaque(false);
        pnlTopping.add(chkTranChauTrang);
        pnlTopping.add(chkThachDao);
        pnlTopping.add(chkKemCheese);

        pnlForm.add(field("Topping thêm", pnlTopping));
        add(pnlForm, BorderLayout.CENTER);

        // Footer
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(Theme.PAPER);
        pnlFooter.setBorder(new EmptyBorder(16, 24, 16, 24));

        lblTongTien = Theme.label("Tổng: 0 đ", 18, Theme.GREEN);
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pnlFooter.add(lblTongTien, BorderLayout.WEST);

        JButton btnHuy = Theme.button("Hủy bỏ", false);
        btnHuy.addActionListener(e -> dispose());

        JButton btnXacNhan = Theme.button("+ Thêm vào đơn", true);
        btnXacNhan.addActionListener(e -> {
            confirmed = true;
            buildCustomString();
            dispose();
        });

        pnlFooter.add(Theme.row(btnHuy, btnXacNhan), BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);

        // Listeners for live price recalculation
        cbSize.addActionListener(e -> updatePrice());
        chkTranChauTrang.addActionListener(e -> updatePrice());
        chkThachDao.addActionListener(e -> updatePrice());
        chkKemCheese.addActionListener(e -> updatePrice());

        updatePrice();
        pack();
        setMinimumSize(new Dimension(520, 420));
        setLocationRelativeTo(owner);
    }

    private void updatePrice() {
        double total = giaGoc;
        int sizeIdx = cbSize.getSelectedIndex();
        if (sizeIdx == 1) total += 6000;
        else if (sizeIdx == 2) total += 12000;

        if (chkTranChauTrang.isSelected()) total += 8000;
        if (chkThachDao.isSelected()) total += 8000;
        if (chkKemCheese.isSelected()) total += 10000;

        this.tongGia = total;
        lblTongTien.setText(String.format("%,.0f đ", total));
    }

    private void buildCustomString() {
        String size = cbSize.getSelectedIndex() == 0 ? "S" : cbSize.getSelectedIndex() == 1 ? "M" : "L";
        String duong = cbDuong.getSelectedItem().toString().split(" ")[0];
        String da = cbDa.getSelectedItem().toString().split(" ")[0];

        StringBuilder sb = new StringBuilder();
        sb.append("Size ").append(size).append(" · ").append(duong).append(" Đường · ").append(da);

        if (chkTranChauTrang.isSelected()) sb.append(" · +Trân châu");
        if (chkThachDao.isSelected()) sb.append(" · +Thạch đào");
        if (chkKemCheese.isSelected()) sb.append(" · +Cheese");

        this.customDetail = sb.toString();
    }

    private static JPanel field(String name, JComponent input) {
        JLabel label = Theme.label(name, 12, Theme.INK);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return Theme.stack(6, label, input);
    }

    public boolean isConfirmed() { return confirmed; }
    public double getTongGia() { return tongGia; }
    public String getCustomDetail() { return customDetail; }
    public String getSizeName() {
        return cbSize.getSelectedIndex() == 0 ? "S" : cbSize.getSelectedIndex() == 1 ? "M" : "L";
    }
}
