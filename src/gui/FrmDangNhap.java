package gui;

import entity.TaiKhoan;
import util.DataStore;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Giao diện Đăng nhập hệ thống (FrmDangNhap - UC01)
 * Phong cách thiết kế chuẩn HOMU CAFE:
 * Tone kem ấm (#FAF6EF), Header nâu cafe rang (#43352C), nút xanh matcha (#536B4F).
 */
public class FrmDangNhap extends JFrame {
    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JRadioButton radThuNganBar;
    private JRadioButton radQuanLy;

    public FrmDangNhap() {
        setTitle("HomuCafe - Đăng Nhập Hệ Thống Quản Lý Bán Hàng");
        setSize(920, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UITheme.HOMU_BG_CREAM);
        setContentPane(root);

        // Card trung tâm bo góc với bóng đổ mềm mại
        JPanel cardContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Bóng đổ nhẹ tông ấm
                g2.setColor(new Color(214, 204, 190, 160));
                g2.fill(new RoundRectangle2D.Float(6, 6, getWidth() - 6, getHeight() - 6, 24, 24));
                // Nền trắng card
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 6, getHeight() - 6, 24, 24));
                g2.dispose();
            }
        };
        cardContainer.setOpaque(false);
        cardContainer.setPreferredSize(new Dimension(440, 500));
        cardContainer.setBorder(new EmptyBorder(0, 0, 6, 6));

        // Header nâu cafe rang đậm phía trên Card
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.HOMU_HEADER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 24, 24, 24);
                g2.fillRect(0, getHeight() - 24, getWidth(), 24);
                g2.dispose();
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(434, 120));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(26, 24, 18, 24));

        JLabel lblEyebrow = new JLabel("COFFEE POS & PAGER SYSTEM");
        lblEyebrow.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblEyebrow.setForeground(new Color(230, 222, 200));
        lblEyebrow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("HOMU CAFE");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitle.setForeground(new Color(250, 246, 239));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("ĐĂNG NHẬP HỆ THỐNG");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblSub.setForeground(new Color(220, 210, 195));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblEyebrow);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblSub);

        // Thân Form đăng nhập
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(24, 38, 24, 38));

        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(UITheme.HOMU_TEXT);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtTenDangNhap = UITheme.createStyledTextField("admin");
        txtTenDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtTenDangNhap.setPreferredSize(new Dimension(350, 42));
        txtTenDangNhap.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtTenDangNhap.setToolTipText("admin (Quản lý) / thungan01 (Thu ngân) / bar01 (Pha chế)");

        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(UITheme.FONT_BOLD);
        lblPass.setForeground(UITheme.HOMU_TEXT);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtMatKhau = new JPasswordField("123456");
        txtMatKhau.setFont(new Font("SansSerif", Font.BOLD, 15));
        txtMatKhau.setForeground(UITheme.HOMU_TEXT);
        txtMatKhau.setBackground(Color.WHITE);
        txtMatKhau.setBorder(new UITheme.RoundedBorder(UITheme.HOMU_BORDER, 8, 1, new Insets(6, 12, 6, 12)));
        txtMatKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtMatKhau.setPreferredSize(new Dimension(350, 42));
        txtMatKhau.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Vai trò truy cập
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rolePanel.setOpaque(false);
        rolePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rolePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lblRole = new JLabel("Vai trò:");
        lblRole.setFont(UITheme.FONT_SMALL);
        lblRole.setForeground(UITheme.HOMU_MUTED);

        radThuNganBar = new JRadioButton("Thu ngân / Quầy Bar");
        radThuNganBar.setFont(UITheme.FONT_SMALL);
        radThuNganBar.setForeground(UITheme.HOMU_TEXT);
        radThuNganBar.setOpaque(false);

        radQuanLy = new JRadioButton("Quản lý quán", true);
        radQuanLy.setFont(UITheme.FONT_SMALL);
        radQuanLy.setForeground(UITheme.HOMU_TEXT);
        radQuanLy.setOpaque(false);

        ButtonGroup bgRole = new ButtonGroup();
        bgRole.add(radThuNganBar);
        bgRole.add(radQuanLy);

        radThuNganBar.addActionListener(e -> {
            if ("admin".equalsIgnoreCase(txtTenDangNhap.getText().trim())) {
                txtTenDangNhap.setText("thungan01");
            }
        });
        radQuanLy.addActionListener(e -> {
            if ("thungan01".equalsIgnoreCase(txtTenDangNhap.getText().trim())) {
                txtTenDangNhap.setText("admin");
            }
        });

        rolePanel.add(lblRole);
        rolePanel.add(radThuNganBar);
        rolePanel.add(radQuanLy);

        // Nút ĐĂNG NHẬP xanh Matcha
        JButton btnLogin = UITheme.createRoundedButton("ĐĂNG NHẬP (Enter)", UITheme.HOMU_GREEN, Color.WHITE, 10);
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnLogin.setPreferredSize(new Dimension(350, 46));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> xuLyDangNhap());

        // Nút Thoát ứng dụng
        JButton btnExit = UITheme.createRoundedButton("Thoát ứng dụng", UITheme.HOMU_LIGHT_PANEL, UITheme.HOMU_TEXT, 10);
        btnExit.setFont(UITheme.FONT_REGULAR);
        btnExit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnExit.setPreferredSize(new Dimension(350, 38));
        btnExit.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnExit.addActionListener(e -> System.exit(0));

        JLabel lblHint = new JLabel("Gợi ý: admin / thungan01 / bar01 (Mật khẩu: 123456)");
        lblHint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblHint.setForeground(UITheme.HOMU_MUTED);
        lblHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(lblUser);
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(txtTenDangNhap);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(lblPass);
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(txtMatKhau);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(rolePanel);
        formPanel.add(Box.createVerticalStrut(14));
        formPanel.add(btnLogin);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(btnExit);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(lblHint);

        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    xuLyDangNhap();
                }
            }
        };
        txtTenDangNhap.addKeyListener(enterListener);
        txtMatKhau.addKeyListener(enterListener);

        cardContainer.add(headerPanel, BorderLayout.NORTH);
        cardContainer.add(formPanel, BorderLayout.CENTER);
        root.add(cardContainer);
    }

    private void xuLyDangNhap() {
        String user = txtTenDangNhap.getText().trim();
        String pass = new String(txtMatKhau.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (user.contains("/")) {
            user = radQuanLy.isSelected() ? "admin" : "thungan01";
        }

        for (TaiKhoan tk : DataStore.getDsTaiKhoan()) {
            if (tk.getTenDangNhap().equalsIgnoreCase(user) && tk.getMatKhau().equals(pass)) {
                if (tk.getNhanVien() != null && !tk.getNhanVien().isTrangThaiLamViec()) {
                    JOptionPane.showMessageDialog(this,
                            "Tài khoản nhân viên này đã ngừng làm việc! Vui lòng liên hệ Quản lý.",
                            "Từ chối truy cập", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                DataStore.setTaiKhoanDangNhap(tk);
                FrmChinh frmChinh = new FrmChinh();
                frmChinh.setVisible(true);
                this.dispose();
                return;
            }
        }

        JOptionPane.showMessageDialog(this,
                "Sai tên đăng nhập hoặc mật khẩu!\n(Tài khoản mẫu: admin / thungan01 / bar01 - Mật khẩu: 123456)",
                "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
    }
}
