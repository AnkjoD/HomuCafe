package com.homucafe.app;

import com.homucafe.entity.ChiTietHoaDon;
import com.homucafe.entity.KichCo;
import com.homucafe.entity.Mon;
import com.homucafe.shared.ui.DrinkIllustration;
import com.homucafe.shared.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Hộp thoại tùy biến đồ uống: Chọn kích cỡ (S/M/L), Mức đường, Mức đá, Topping, Số lượng.
 * Hỗ trợ mở mới hoặc sửa tùy chọn cho dòng món đang chọn trong giỏ hàng.
 */
public class DialogTuyBienMon extends JDialog {
    private Mon mon;
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
    private JCheckBox chkSenMem;
    private JTextField txtGhiChuRieng;
    private JSpinner spnSoLuong;
    private JLabel lblTongTien;

    private int soLuong = 1;
    private double donGiaMotLy;
    private double tongGia;
    private String customDetail;
    private int mucDuong = 100;
    private String mucDa = "BINH_THUONG";
    private String topping = "";

    public DialogTuyBienMon(Window owner, Mon mon, int drinkKind) {
        super(owner, "Tùy biến món · " + mon.getTenMon(), ModalityType.APPLICATION_MODAL);
        this.mon = mon;
        this.tenMon = mon.getTenMon();
        this.giaGoc = mon.getDonGiaGoc();
        this.drinkKind = drinkKind;
        initUI(owner);
    }

    public DialogTuyBienMon(Window owner, String tenMon, double giaGoc, int drinkKind) {
        super(owner, "Tùy biến món · " + tenMon, ModalityType.APPLICATION_MODAL);
        this.tenMon = tenMon;
        this.giaGoc = giaGoc;
        this.drinkKind = drinkKind;
        initUI(owner);
    }

    /**
     * Khởi tạo với dữ liệu có sẵn để sửa tùy chọn dòng món
     */
    public DialogTuyBienMon(Window owner, ChiTietHoaDon ct, int drinkKind) {
        super(owner, "Sửa tùy biến · " + ct.getMon().getTenMon(), ModalityType.APPLICATION_MODAL);
        this.mon = ct.getMon();
        this.tenMon = ct.getMon().getTenMon();
        this.giaGoc = ct.getMon().getDonGiaGoc();
        this.drinkKind = drinkKind;
        this.soLuong = ct.getSoLuong();
        initUI(owner);

        // Thiết lập giá trị cũ
        if (ct.getKichCo() == KichCo.S) cbSize.setSelectedIndex(0);
        else if (ct.getKichCo() == KichCo.M) cbSize.setSelectedIndex(1);
        else if (ct.getKichCo() == KichCo.L) cbSize.setSelectedIndex(2);

        int md = ct.getMucDuong();
        if (md == 100) cbDuong.setSelectedIndex(0);
        else if (md == 70) cbDuong.setSelectedIndex(1);
        else if (md == 50) cbDuong.setSelectedIndex(2);
        else if (md == 30) cbDuong.setSelectedIndex(3);
        else if (md == 0) cbDuong.setSelectedIndex(4);

        String da = ct.getMucDa();
        if ("BINH_THUONG".equalsIgnoreCase(da)) cbDa.setSelectedIndex(0);
        else if ("IT_DA".equalsIgnoreCase(da)) cbDa.setSelectedIndex(1);
        else if ("KHONG_DA".equalsIgnoreCase(da)) cbDa.setSelectedIndex(2);
        else if ("NONG".equalsIgnoreCase(da)) cbDa.setSelectedIndex(3);

        String gh = ct.getGhiChuTopping();
        if (gh != null) {
            if (gh.contains("Trân châu") || gh.contains("trân châu")) chkTranChauTrang.setSelected(true);
            if (gh.contains("Đào") || gh.contains("đào")) chkThachDao.setSelected(true);
            if (gh.contains("Cheese") || gh.contains("cheese")) chkKemCheese.setSelected(true);
            if (gh.contains("Sen") || gh.contains("sen")) chkSenMem.setSelected(true);
        }

        spnSoLuong.setValue(ct.getSoLuong());
        updatePrice();
    }

    private void initUI(Window owner) {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.WHITE);

        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout(15, 0));
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(18, 24, 12, 24));

        DrinkIllustration illustration = new DrinkIllustration(drinkKind);
        illustration.setPreferredSize(new Dimension(75, 75));
        pnlHeader.add(illustration, BorderLayout.WEST);

        JLabel lblTitle = Theme.label(tenMon, 18, Theme.INK);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel lblSub = Theme.label("Giá cơ bản (Size S): " + String.format("%,.0f đ", giaGoc), 12, Theme.ORANGE);
        pnlHeader.add(Theme.stack(4, lblTitle, lblSub), BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        // Options form
        JPanel pnlForm = new JPanel(new GridLayout(0, 2, 16, 12));
        pnlForm.setOpaque(false);
        pnlForm.setBorder(new EmptyBorder(8, 24, 12, 24));

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

        spnSoLuong = new JSpinner(new SpinnerNumberModel(soLuong, 1, 50, 1));
        spnSoLuong.setFont(new Font("Segoe UI", Font.BOLD, 15));

        pnlForm.add(field("Kích cỡ (Size)", cbSize));
        pnlForm.add(field("Số lượng ly", spnSoLuong));
        pnlForm.add(field("Mức đường", cbDuong));
        pnlForm.add(field("Mức đá / Nhiệt độ", cbDa));

        // Toppings
        chkTranChauTrang = new JCheckBox("Trân châu trắng (+8.000 đ)");
        chkThachDao = new JCheckBox("Thạch đào giòn (+8.000 đ)");
        chkKemCheese = new JCheckBox("Kem cheese béo (+10.000 đ)");
        chkSenMem = new JCheckBox("Hạt sen mềm (+8.000 đ)");

        JPanel pnlTopping = new JPanel();
        pnlTopping.setLayout(new BoxLayout(pnlTopping, BoxLayout.Y_AXIS));
        pnlTopping.setOpaque(false);
        pnlTopping.add(chkTranChauTrang);
        pnlTopping.add(chkThachDao);
        pnlTopping.add(chkKemCheese);
        pnlTopping.add(chkSenMem);

        pnlForm.add(field("Topping thêm", pnlTopping));

        txtGhiChuRieng = new JTextField();
        txtGhiChuRieng.putClientProperty("JTextField.placeholderText", "Ghi chú đặc biệt cho Barista...");
        txtGhiChuRieng.putClientProperty("FlatLaf.style", "arc: 10");
        pnlForm.add(field("Ghi chú riêng", txtGhiChuRieng));

        add(pnlForm, BorderLayout.CENTER);

        // Footer
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(Theme.PAPER);
        pnlFooter.setBorder(new EmptyBorder(14, 24, 14, 24));

        lblTongTien = Theme.label("Tổng: 0 đ", 18, Theme.GREEN);
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pnlFooter.add(lblTongTien, BorderLayout.WEST);

        JButton btnHuy = Theme.button("Hủy bỏ", false);
        btnHuy.addActionListener(e -> dispose());

        JButton btnXacNhan = Theme.button("✓ Xác nhận", true);
        btnXacNhan.addActionListener(e -> {
            confirmed = true;
            buildCustomString();
            dispose();
        });

        pnlFooter.add(Theme.row(btnHuy, btnXacNhan), BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);

        // Listeners
        cbSize.addActionListener(e -> updatePrice());
        spnSoLuong.addChangeListener(e -> updatePrice());
        chkTranChauTrang.addActionListener(e -> updatePrice());
        chkThachDao.addActionListener(e -> updatePrice());
        chkKemCheese.addActionListener(e -> updatePrice());
        chkSenMem.addActionListener(e -> updatePrice());

        updatePrice();
        pack();
        setMinimumSize(new Dimension(540, 480));
        setLocationRelativeTo(owner);
    }

    private void updatePrice() {
        double unit = giaGoc;
        int sizeIdx = cbSize.getSelectedIndex();
        if (sizeIdx == 1) unit += 6000;
        else if (sizeIdx == 2) unit += 12000;

        if (chkTranChauTrang.isSelected()) unit += 8000;
        if (chkThachDao.isSelected()) unit += 8000;
        if (chkKemCheese.isSelected()) unit += 10000;
        if (chkSenMem.isSelected()) unit += 8000;

        this.donGiaMotLy = unit;
        int sl = (int) spnSoLuong.getValue();
        this.soLuong = sl;
        this.tongGia = unit * sl;
        lblTongTien.setText(String.format("%,.0f đ  (%s/ly)", tongGia, String.format("%,.0f đ", unit)));
    }

    private void buildCustomString() {
        int sizeIdx = cbSize.getSelectedIndex();
        String size = sizeIdx == 0 ? "S" : sizeIdx == 1 ? "M" : "L";

        int duongIdx = cbDuong.getSelectedIndex();
        this.mucDuong = duongIdx == 0 ? 100 : duongIdx == 1 ? 70 : duongIdx == 2 ? 50 : duongIdx == 3 ? 30 : 0;

        int daIdx = cbDa.getSelectedIndex();
        this.mucDa = daIdx == 0 ? "BINH_THUONG" : daIdx == 1 ? "IT_DA" : daIdx == 2 ? "KHONG_DA" : "NONG";

        List<String> listTop = new ArrayList<>();
        if (chkTranChauTrang.isSelected()) listTop.add("+Trân châu");
        if (chkThachDao.isSelected()) listTop.add("+Thạch đào");
        if (chkKemCheese.isSelected()) listTop.add("+Cheese");
        if (chkSenMem.isSelected()) listTop.add("+Sen");
        String note = txtGhiChuRieng.getText().trim();
        if (!note.isEmpty()) listTop.add(note);

        this.topping = String.join(", ", listTop);

        StringBuilder sb = new StringBuilder();
        sb.append("Size ").append(size).append(" · ").append(mucDuong).append("% Đường · ").append(ChiTietHoaDon.formatMucDa(mucDa));
        if (!topping.isEmpty()) {
            sb.append(" · ").append(topping);
        }
        this.customDetail = sb.toString();
    }

    private static JPanel field(String name, JComponent input) {
        JLabel label = Theme.label(name, 12, Theme.INK);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return Theme.stack(5, label, input);
    }

    public boolean isConfirmed() { return confirmed; }
    public double getDonGiaMotLy() { return donGiaMotLy; }
    public double getTongGia() { return tongGia; }
    public int getSoLuong() { return soLuong; }
    public String getCustomDetail() { return customDetail; }
    public String getSizeName() {
        return cbSize.getSelectedIndex() == 0 ? "S" : cbSize.getSelectedIndex() == 1 ? "M" : "L";
    }
    public int getMucDuong() { return mucDuong; }
    public String getMucDa() { return mucDa; }
    public String getTopping() { return topping; }
}
