package gui;

import entity.ChiTietHoaDon;
import entity.KichCo;
import entity.Mon;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Hộp thoại Tùy biến ly nước (DialogTuyBienMon - HomuCafe)
 * Tông màu ấm: Header nâu cafe #43352C, Nút xác nhận xanh matcha #536B4F.
 */
public class DialogTuyBienMon extends JDialog {
    private final Mon mon;
    private ChiTietHoaDon ketQua;

    private JRadioButton radSizeS, radSizeM, radSizeL;
    private final List<JRadioButton> radDuongList = new ArrayList<>();
    private JRadioButton radDaBinhThuong, radDaIt, radDaKhong, radNong;
    private JCheckBox chkTranChau, chkDaoMieng, chkKemCheese, chkSenMem;
    private JTextField txtGhiChuRieng;
    private JSpinner spnSoLuong;
    private JLabel lblDonGiaThucTe;

    public DialogTuyBienMon(Window owner, Mon mon) {
        super(owner, "HomuCafe - Tùy Biến Ly Nước (" + mon.getTenMon() + ")", ModalityType.APPLICATION_MODAL);
        this.mon = mon;
        setSize(530, 570);
        setLocationRelativeTo(owner);
        initComponents();
        capNhatGiaTamTinh();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(UITheme.HOMU_BG_CREAM);
        root.setBorder(new EmptyBorder(16, 20, 16, 20));
        setContentPane(root);

        // Header món
        JPanel topPanel = new UITheme.RoundedPanel(12, UITheme.HOMU_HEADER, null, 0);
        topPanel.setLayout(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblTenMon = new JLabel(mon.getMaMon() + " - " + mon.getTenMon());
        lblTenMon.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblTenMon.setForeground(new Color(250, 246, 239));

        JLabel lblGiaGoc = new JLabel("Size S: " + UITheme.formatMoney(mon.getDonGiaGoc()));
        lblGiaGoc.setFont(UITheme.FONT_BOLD);
        lblGiaGoc.setForeground(new Color(254, 240, 138)); // Vàng kem

        topPanel.add(lblTenMon, BorderLayout.WEST);
        topPanel.add(lblGiaGoc, BorderLayout.EAST);

        // Nội dung tùy biến
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // 1. Kích cỡ Size
        JPanel pnlSize = createSectionPanel("1. Kích cỡ ly (Size) - BR04:");
        radSizeS = new JRadioButton("Size S (+0đ)", true);
        radSizeM = new JRadioButton("Size M (+6.000đ)");
        radSizeL = new JRadioButton("Size L (+12.000đ)");
        ButtonGroup bgSize = new ButtonGroup();
        for (JRadioButton r : new JRadioButton[]{radSizeS, radSizeM, radSizeL}) {
            r.setOpaque(false);
            r.setFont(UITheme.FONT_BOLD);
            r.setForeground(UITheme.HOMU_TEXT);
            r.addActionListener(e -> capNhatGiaTamTinh());
            bgSize.add(r);
            pnlSize.add(r);
        }
        body.add(pnlSize);
        body.add(Box.createVerticalStrut(8));

        // 2. Mức đường (%)
        JPanel pnlDuong = createSectionPanel("2. Độ ngọt (% Đường):");
        int[] mucDuongs = {100, 70, 50, 30, 0};
        ButtonGroup bgDuong = new ButtonGroup();
        for (int md : mucDuongs) {
            JRadioButton rad = new JRadioButton(md + "%", md == 100);
            rad.putClientProperty("mucDuong", md);
            rad.setOpaque(false);
            rad.setFont(UITheme.FONT_REGULAR);
            rad.setForeground(UITheme.HOMU_TEXT);
            bgDuong.add(rad);
            radDuongList.add(rad);
            pnlDuong.add(rad);
        }
        body.add(pnlDuong);
        body.add(Box.createVerticalStrut(8));

        // 3. Mức đá
        JPanel pnlDa = createSectionPanel("3. Mức đá / Nhiệt độ:");
        radDaBinhThuong = new JRadioButton("Bình thường", true);
        radDaIt = new JRadioButton("Ít đá");
        radDaKhong = new JRadioButton("Không đá");
        radNong = new JRadioButton("Uống nóng");
        ButtonGroup bgDa = new ButtonGroup();
        for (JRadioButton r : new JRadioButton[]{radDaBinhThuong, radDaIt, radDaKhong, radNong}) {
            r.setOpaque(false);
            r.setFont(UITheme.FONT_REGULAR);
            r.setForeground(UITheme.HOMU_TEXT);
            bgDa.add(r);
            pnlDa.add(r);
        }
        body.add(pnlDa);
        body.add(Box.createVerticalStrut(8));

        // 4. Topping
        JPanel pnlTopping = createSectionPanel("4. Topping thêm HomuCafe:");
        pnlTopping.setLayout(new GridLayout(2, 2, 8, 6));
        chkTranChau = new JCheckBox("+Trân châu trắng (+6.000đ)");
        chkDaoMieng = new JCheckBox("+Đào miếng (+6.000đ)");
        chkKemCheese = new JCheckBox("+Kem cheese (+10.000đ)");
        chkSenMem = new JCheckBox("+Sen mềm (+8.000đ)");
        for (JCheckBox chk : new JCheckBox[]{chkTranChau, chkDaoMieng, chkKemCheese, chkSenMem}) {
            chk.setOpaque(false);
            chk.setFont(UITheme.FONT_REGULAR);
            chk.setForeground(UITheme.HOMU_TEXT);
            chk.addActionListener(e -> capNhatGiaTamTinh());
            pnlTopping.add(chk);
        }
        body.add(pnlTopping);
        body.add(Box.createVerticalStrut(8));

        // 5. Ghi chú & Số lượng
        JPanel pnlExtra = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlExtra.setOpaque(false);

        JPanel pnlGhiChu = createSectionPanel("Ghi chú pha chế:");
        pnlGhiChu.setLayout(new BorderLayout());
        txtGhiChuRieng = UITheme.createStyledTextField("");
        pnlGhiChu.add(txtGhiChuRieng, BorderLayout.CENTER);

        JPanel pnlSL = createSectionPanel("Số lượng ly:");
        spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        spnSoLuong.setFont(new Font("SansSerif", Font.BOLD, 16));
        spnSoLuong.setPreferredSize(new Dimension(90, 36));
        spnSoLuong.addChangeListener(e -> capNhatGiaTamTinh());
        pnlSL.add(spnSoLuong);

        pnlExtra.add(pnlGhiChu);
        pnlExtra.add(pnlSL);
        body.add(pnlExtra);

        // Footer
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 0, 0, 0));

        lblDonGiaThucTe = new JLabel("Thành tiền: 0đ");
        lblDonGiaThucTe.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblDonGiaThucTe.setForeground(new Color(185, 28, 28));

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnGroup.setOpaque(false);

        JButton btnHuy = UITheme.createRoundedButton("Hủy (Esc)", UITheme.HOMU_LIGHT_PANEL, UITheme.HOMU_TEXT, 8);
        btnHuy.addActionListener(e -> dispose());

        JButton btnXacNhan = UITheme.createRoundedButton("+ THÊM VÀO GIỎ HÀNG", UITheme.HOMU_GREEN, Color.WHITE, 8);
        btnXacNhan.addActionListener(e -> xacNhanThemMon());

        btnGroup.add(btnHuy);
        btnGroup.add(btnXacNhan);

        footer.add(lblDonGiaThucTe, BorderLayout.WEST);
        footer.add(btnGroup, BorderLayout.EAST);

        root.add(topPanel, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
    }

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.HOMU_BORDER),
                title,
                0, 0, UITheme.FONT_BOLD, UITheme.HOMU_CARAMEL
        ));
        return p;
    }

    private KichCo getSelectedSize() {
        if (radSizeM.isSelected()) return KichCo.M;
        if (radSizeL.isSelected()) return KichCo.L;
        return KichCo.S;
    }

    private double tinhPhuThuTopping() {
        double pt = 0;
        if (chkTranChau.isSelected()) pt += 6000;
        if (chkDaoMieng.isSelected()) pt += 6000;
        if (chkKemCheese.isSelected()) pt += 10000;
        if (chkSenMem.isSelected()) pt += 8000;
        return pt;
    }

    private void capNhatGiaTamTinh() {
        KichCo kc = getSelectedSize();
        double donGia = mon.tinhGiaTheoSize(kc) + tinhPhuThuTopping();
        int sl = (int) spnSoLuong.getValue();
        lblDonGiaThucTe.setText("Thành tiền: " + UITheme.formatMoney(donGia * sl) + " (" + UITheme.formatMoney(donGia) + "/ly)");
    }

    private void xacNhanThemMon() {
        KichCo kc = getSelectedSize();
        double donGia = mon.tinhGiaTheoSize(kc) + tinhPhuThuTopping();
        int sl = (int) spnSoLuong.getValue();

        int mucDuong = 100;
        for (JRadioButton r : radDuongList) {
            if (r.isSelected()) {
                mucDuong = (int) r.getClientProperty("mucDuong");
                break;
            }
        }

        String mucDa = "BINH_THUONG";
        if (radDaIt.isSelected()) mucDa = "IT_DA";
        else if (radDaKhong.isSelected()) mucDa = "KHONG_DA";
        else if (radNong.isSelected()) mucDa = "NONG";

        List<String> toppings = new ArrayList<>();
        if (chkTranChau.isSelected()) toppings.add("+Trân châu trắng");
        if (chkDaoMieng.isSelected()) toppings.add("+Đào miếng");
        if (chkKemCheese.isSelected()) toppings.add("+Kem cheese");
        if (chkSenMem.isSelected()) toppings.add("+Sen mềm");
        if (!txtGhiChuRieng.getText().trim().isEmpty()) {
            toppings.add(txtGhiChuRieng.getText().trim());
        }

        String ghiChu = String.join(", ", toppings);
        ketQua = new ChiTietHoaDon(0, mon, kc, sl, donGia, mucDuong, mucDa, ghiChu);
        dispose();
    }

    public ChiTietHoaDon getKetQua() {
        return ketQua;
    }
}
