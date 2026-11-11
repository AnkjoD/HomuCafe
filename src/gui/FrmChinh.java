package gui;

import entity.NhanVien;
import entity.TaiKhoan;
import util.DataStore;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Cửa sổ điều hướng trung tâm (FrmChinh - HomuCafe)
 * Tích hợp trọn vẹn yêu cầu môn Lập trình Hướng sự kiện / Phát triển ứng dụng (IUH):
 * 1. Hệ thống Menu bar (JMenuBar) chuẩn hướng chức năng & nghiệp vụ:
 *    Phím nóng Mnemonic (Alt + ?) và Phím tắt Accelerator (Ctrl + ?)
 * 2. Thanh công cụ trực quan (Toolbar) 6 phân hệ lớn:
 *    - Bán hàng quầy POS (FrmBanHang)
 *    - Điều phối pha chế quầy Bar (FrmManHinhBar)
 *    - Quản lý & Tra cứu hóa đơn (FrmQuanLyHoaDon)
 *    - Quản lý thực đơn & bảng giá (FrmQuanLyMon)
 *    - Quản lý nhân sự & phân quyền (FrmNhanVien)
 *    - Báo cáo thống kê & đối soát ca (FrmThongKe)
 * 3. Thiết kế thẩm mỹ phong cách HomuCafe (Nền kem #FAF6EF, Header nâu cafe #43352C, Nút xanh Matcha #536B4F)
 */
public class FrmChinh extends JFrame {
    private JLabel lblHeaderTitle;
    private JLabel lblStaffClock;
    private CardLayout cardLayout;
    private JPanel mainContent;
    private final List<JButton> navButtons = new ArrayList<>();

    public FrmChinh() {
        setTitle("HomuCafe - Hệ Thống Quản Lý Bán Hàng Quán Coffee Tự Phục Vụ (Coffee POS & Pager System)");
        setSize(1380, 850);
        setMinimumSize(new Dimension(1200, 750));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        taoMenuBarChuanHuongSuKien();
        initComponents();
        khoiDongDongHoThoiGianThuc();
    }

    /**
     * Khởi tạo hệ thống JMenuBar chuẩn hướng chức năng & nghiệp vụ
     * Có đầy đủ phím nóng (Mnemonic: Alt+...) và phím tắt (Accelerator: Ctrl+...)
     */
    private void taoMenuBarChuanHuongSuKien() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.HOMU_BORDER));

        TaiKhoan tk = DataStore.getTaiKhoanDangNhap();
        boolean isAdmin = tk == null || "ADMIN".equalsIgnoreCase(tk.getPhanQuyen());

        // 1. MENU HỆ THỐNG (Alt + H)
        JMenu mnuHeThong = new JMenu("Hệ thống (H)");
        mnuHeThong.setMnemonic(KeyEvent.VK_H);
        mnuHeThong.setFont(UITheme.FONT_BOLD);
        mnuHeThong.setForeground(UITheme.HOMU_TEXT);

        JMenuItem mniDoiMK = new JMenuItem("Đổi mật khẩu cá nhân...");
        mniDoiMK.setMnemonic(KeyEvent.VK_P);
        mniDoiMK.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        mniDoiMK.addActionListener(e -> moHopThoaiDoiMatKhau());

        JMenuItem mniDangXuat = new JMenuItem("Đăng xuất phiên làm việc");
        mniDangXuat.setMnemonic(KeyEvent.VK_L);
        mniDangXuat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        mniDangXuat.addActionListener(e -> xuLyDangXuat());

        JMenuItem mniThoat = new JMenuItem("Thoát ứng dụng");
        mniThoat.setMnemonic(KeyEvent.VK_X);
        mniThoat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        mniThoat.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn thoát khỏi ứng dụng?", "Thoát HomuCafe", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) System.exit(0);
        });

        mnuHeThong.add(mniDoiMK);
        mnuHeThong.addSeparator();
        mnuHeThong.add(mniDangXuat);
        mnuHeThong.add(mniThoat);

        // 2. MENU NGHIỆP VỤ / XỬ LÝ (Alt + X)
        JMenu mnuNghiepVu = new JMenu("Nghiệp vụ (X)");
        mnuNghiepVu.setMnemonic(KeyEvent.VK_X);
        mnuNghiepVu.setFont(UITheme.FONT_BOLD);
        mnuNghiepVu.setForeground(UITheme.HOMU_TEXT);

        JMenuItem mniBanHang = new JMenuItem("Bán hàng tại quầy (POS Counter)");
        mniBanHang.setMnemonic(KeyEvent.VK_B);
        mniBanHang.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        mniBanHang.addActionListener(e -> chuyenManHinh("POS", "HOMU CAFE - MÀN HÌNH BÁN HÀNG TỰ PHỤC VỤ (POS COUNTER)", navButtons.get(0)));

        JMenuItem mniBar = new JMenuItem("Điều phối pha chế (Bar Monitor)");
        mniBar.setMnemonic(KeyEvent.VK_P);
        mniBar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
        mniBar.addActionListener(e -> chuyenManHinh("BAR", "BAR MONITOR - MÀN HÌNH ĐIỀU PHỐI QUẦY PHA CHẾ (REAL-TIME QUEUE)", navButtons.get(1)));

        JMenuItem mniBarRoi = new JMenuItem("Mở cửa sổ Bar rời (Dual Screen)");
        mniBarRoi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
        mniBarRoi.addActionListener(e -> {
            JFrame f = new JFrame("Bar Monitor - Quầy Pha Chế HomuCafe (Dual Screen)");
            f.setSize(1220, 760);
            f.setLocationRelativeTo(this);
            f.setContentPane(new FrmManHinhBar());
            f.setVisible(true);
        });

        JMenuItem mniQuanLyHD = new JMenuItem("Quản lý & Tra cứu hóa đơn");
        mniQuanLyHD.setMnemonic(KeyEvent.VK_H);
        mniQuanLyHD.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
        mniQuanLyHD.addActionListener(e -> chuyenManHinh("INVOICE", "HOMU CAFE - QUẢN LÝ & TRA CỨU HÓA ĐƠN BÁN HÀNG", navButtons.get(2)));

        mnuNghiepVu.add(mniBanHang);
        mnuNghiepVu.add(mniBar);
        mnuNghiepVu.add(mniBarRoi);
        mnuNghiepVu.addSeparator();
        mnuNghiepVu.add(mniQuanLyHD);

        // 3. MENU DANH MỤC (Alt + D) - Quản lý món & Nhân sự (Admin)
        JMenu mnuDanhMuc = new JMenu("Danh mục (D)");
        mnuDanhMuc.setMnemonic(KeyEvent.VK_D);
        mnuDanhMuc.setFont(UITheme.FONT_BOLD);
        mnuDanhMuc.setForeground(UITheme.HOMU_TEXT);

        JMenuItem mniMon = new JMenuItem("Quản lý Thực đơn & Đơn giá theo Size");
        mniMon.setMnemonic(KeyEvent.VK_M);
        mniMon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
        mniMon.setEnabled(isAdmin);
        mniMon.addActionListener(e -> chuyenManHinh("MENU", "HOMU CAFE - QUẢN LÝ THỰC ĐƠN & ĐƠN GIÁ THEO SIZE", navButtons.get(3)));

        JMenuItem mniNhanVien = new JMenuItem("Quản lý Nhân sự & Phân quyền");
        mniNhanVien.setMnemonic(KeyEvent.VK_N);
        mniNhanVien.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
        mniNhanVien.setEnabled(isAdmin);
        mniNhanVien.addActionListener(e -> chuyenManHinh("STAFF", "HOMU CAFE - QUẢN LÝ NHÂN SỰ & PHÂN QUYỀN TÀI KHOẢN", navButtons.get(4)));

        mnuDanhMuc.add(mniMon);
        mnuDanhMuc.add(mniNhanVien);

        // 4. MENU BÁO BIỂU / THỐNG KÊ (Alt + B)
        JMenu mnuBaoBieu = new JMenu("Báo biểu (B)");
        mnuBaoBieu.setMnemonic(KeyEvent.VK_B);
        mnuBaoBieu.setFont(UITheme.FONT_BOLD);
        mnuBaoBieu.setForeground(UITheme.HOMU_TEXT);

        JMenuItem mniThongKe = new JMenuItem("Báo cáo doanh thu & Đối soát ca");
        mniThongKe.setMnemonic(KeyEvent.VK_T);
        mniThongKe.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        mniThongKe.setEnabled(isAdmin);
        mniThongKe.addActionListener(e -> chuyenManHinh("REPORT", "HOMU CAFE - BÁO CÁO & THỐNG KÊ DOANH THU", navButtons.get(5)));

        mnuBaoBieu.add(mniThongKe);

        // 5. MENU TRỢ GIÚP (Alt + T)
        JMenu mnuTroGiup = new JMenu("Trợ giúp (T)");
        mnuTroGiup.setMnemonic(KeyEvent.VK_T);
        mnuTroGiup.setFont(UITheme.FONT_BOLD);
        mnuTroGiup.setForeground(UITheme.HOMU_TEXT);

        JMenuItem mniHuongDan = new JMenuItem("Hướng dẫn phím tắt hệ thống");
        mniHuongDan.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        mniHuongDan.addActionListener(e -> hienThiHuongDanPhimTat());

        JMenuItem mniThongTin = new JMenuItem("Thông tin ứng dụng HomuCafe");
        mniThongTin.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
        mniThongTin.addActionListener(e -> hienThiThongTinDoAn());

        mnuTroGiup.add(mniHuongDan);
        mnuTroGiup.addSeparator();
        mnuTroGiup.add(mniThongTin);

        menuBar.add(mnuHeThong);
        menuBar.add(mnuNghiepVu);
        menuBar.add(mnuDanhMuc);
        menuBar.add(mnuBaoBieu);
        menuBar.add(mnuTroGiup);

        setJMenuBar(menuBar);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.HOMU_BG_CREAM);
        setContentPane(root);

        // ==================== HEADER NÂU CAFE HOMUCAFE ====================
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(UITheme.HOMU_HEADER);

        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);
        headerBar.setBorder(new EmptyBorder(12, 22, 10, 22));

        lblHeaderTitle = new JLabel("HOMU CAFE - MÀN HÌNH BÁN HÀNG TỰ PHỤC VỤ (POS COUNTER)");
        lblHeaderTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblHeaderTitle.setForeground(new Color(250, 246, 239));

        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightInfo.setOpaque(false);

        lblStaffClock = new JLabel();
        lblStaffClock.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblStaffClock.setForeground(new Color(230, 222, 200));
        capNhatChuoiDongHo();

        JButton btnDoiMK = UITheme.createRoundedButton("Đổi MK (UC02)", UITheme.HOMU_CARAMEL, Color.WHITE, 8);
        btnDoiMK.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnDoiMK.setBorder(new EmptyBorder(5, 10, 5, 10));
        btnDoiMK.addActionListener(e -> moHopThoaiDoiMatKhau());

        JButton btnDangXuat = UITheme.createRoundedButton("Đăng xuất (UC03)", UITheme.COLOR_DANGER, Color.WHITE, 8);
        btnDangXuat.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnDangXuat.setBorder(new EmptyBorder(5, 10, 5, 10));
        btnDangXuat.addActionListener(e -> xuLyDangXuat());

        rightInfo.add(lblStaffClock);
        rightInfo.add(btnDoiMK);
        rightInfo.add(btnDangXuat);

        headerBar.add(lblHeaderTitle, BorderLayout.WEST);
        headerBar.add(rightInfo, BorderLayout.EAST);

        // Thanh công cụ chuyển nhanh các màn hình nghiệp vụ
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        navBar.setBackground(UITheme.HOMU_NAV);
        navBar.setBorder(new EmptyBorder(2, 16, 4, 16));

        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setOpaque(false);

        FrmBanHang pnlBanHang = new FrmBanHang();
        FrmManHinhBar pnlBar = new FrmManHinhBar();
        FrmQuanLyHoaDon pnlHoaDon = new FrmQuanLyHoaDon();
        FrmQuanLyMon pnlQuanLyMon = new FrmQuanLyMon();
        FrmNhanVien pnlNhanVien = new FrmNhanVien();
        FrmThongKe pnlThongKe = new FrmThongKe();

        mainContent.add(pnlBanHang, "POS");
        mainContent.add(pnlBar, "BAR");
        mainContent.add(pnlHoaDon, "INVOICE");
        mainContent.add(pnlQuanLyMon, "MENU");
        mainContent.add(pnlNhanVien, "STAFF");
        mainContent.add(pnlThongKe, "REPORT");

        TaiKhoan tk = DataStore.getTaiKhoanDangNhap();
        boolean isAdmin = tk == null || "ADMIN".equalsIgnoreCase(tk.getPhanQuyen());

        navBar.add(taoNutDieuHuong("1. Bán Hàng Tại Quầy (FrmBanHang)", "POS",
                "HOMU CAFE - MÀN HÌNH BÁN HÀNG TỰ PHỤC VỤ (POS COUNTER)", true));
        navBar.add(taoNutDieuHuong("2. Điều Phối Pha Chế (FrmManHinhBar)", "BAR",
                "BAR MONITOR - MÀN HÌNH ĐIỀU PHỐI QUẦY PHA CHẾ (REAL-TIME QUEUE)", true));
        navBar.add(taoNutDieuHuong("3. Quản Lý Hóa Đơn (FrmQuanLyHoaDon)", "INVOICE",
                "HOMU CAFE - QUẢN LÝ & TRA CỨU HÓA ĐƠN BÁN HÀNG", true));
        navBar.add(taoNutDieuHuong("4. Quản Lý Thực Đơn (FrmQuanLyMon)", "MENU",
                "HOMU CAFE - QUẢN LÝ THỰC ĐƠN & ĐƠN GIÁ THEO SIZE", isAdmin));
        navBar.add(taoNutDieuHuong("5. Quản Lý Nhân Sự (FrmNhanVien)", "STAFF",
                "HOMU CAFE - QUẢN LÝ NHÂN SỰ & PHÂN QUYỀN TÀI KHOẢN", isAdmin));
        navBar.add(taoNutDieuHuong("6. Báo Cáo & Thống Kê (FrmThongKe)", "REPORT",
                "HOMU CAFE - BÁO CÁO & THỐNG KÊ DOANH THU", isAdmin));

        topContainer.add(headerBar, BorderLayout.NORTH);
        topContainer.add(navBar, BorderLayout.SOUTH);

        root.add(topContainer, BorderLayout.NORTH);
        root.add(mainContent, BorderLayout.CENTER);

        if (tk != null && "bar01".equalsIgnoreCase(tk.getTenDangNhap())) {
            chuyenManHinh("BAR", "BAR MONITOR - MÀN HÌNH ĐIỀU PHỐI QUẦY PHA CHẾ (REAL-TIME QUEUE)", navButtons.get(1));
        } else {
            chuyenManHinh("POS", "HOMU CAFE - MÀN HÌNH BÁN HÀNG TỰ PHỤC VỤ (POS COUNTER)", navButtons.get(0));
        }
    }

    private JButton taoNutDieuHuong(String text, String cardKey, String headerTitle, boolean hasPermission) {
        JButton btn = UITheme.createRoundedButton(text, new Color(67, 53, 44), new Color(244, 239, 230), 8);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.addActionListener(e -> {
            if (!hasPermission) {
                JOptionPane.showMessageDialog(this,
                        "Tài khoản STAFF chỉ có quyền truy cập Bán hàng POS, Quầy Bar và Tra cứu Hóa đơn!\n"
                                + "Vui lòng đăng nhập tài khoản Quản lý / Admin để vào phân hệ Quản trị.",
                        "Phân quyền truy cập", JOptionPane.WARNING_MESSAGE);
                return;
            }
            chuyenManHinh(cardKey, headerTitle, btn);
        });
        navButtons.add(btn);
        return btn;
    }

    private void chuyenManHinh(String cardKey, String headerTitle, JButton activeBtn) {
        cardLayout.show(mainContent, cardKey);
        lblHeaderTitle.setText(headerTitle);
        for (JButton b : navButtons) {
            if (b == activeBtn) {
                b.setBackground(UITheme.HOMU_GREEN);
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(new Color(67, 53, 44));
                b.setForeground(new Color(244, 239, 230));
            }
        }
    }

    private void khoiDongDongHoThoiGianThuc() {
        Timer clockTimer = new Timer(1000, e -> capNhatChuoiDongHo());
        clockTimer.start();
    }

    private void capNhatChuoiDongHo() {
        TaiKhoan tk = DataStore.getTaiKhoanDangNhap();
        NhanVien nv = tk != null ? tk.getNhanVien() : null;
        String tenNV = nv != null ? (nv.getMaNV() + " - " + nv.getHoTen()) : "NV01 - Trần Văn An";
        String role = tk != null ? tk.getPhanQuyen() : "ADMIN";
        String timeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        lblStaffClock.setText("Nhân sự: " + tenNV + " (" + role + ") | Ca sáng | " + timeStr);
    }

    private void hienThiHuongDanPhimTat() {
        String msg = "HOMU CAFE - TỔ HỢP PHÍM TẮT & PHÍM NÓNG HỆ THỐNG:\n"
                + "--------------------------------------------------------\n"
                + "1. PHÍM NÓNG MENU (Alt + ?):\n"
                + "   • Alt + H: Mở Menu Hệ Thống\n"
                + "   • Alt + X: Mở Menu Nghiệp Vụ\n"
                + "   • Alt + D: Mở Menu Danh Mục\n"
                + "   • Alt + B: Mở Menu Báo Biểu\n"
                + "   • Alt + T: Mở Menu Trợ Giúp\n\n"
                + "2. PHÍM TẮT ĐIỀU HƯỚNG NHANH:\n"
                + "   • Ctrl + N: Mở Bán hàng tại quầy POS\n"
                + "   • Ctrl + B: Mở Màn hình Quầy Bar\n"
                + "   • Ctrl + H: Mở Quản lý Hóa đơn\n"
                + "   • Ctrl + M: Mở Quản lý Thực đơn (Admin)\n"
                + "   • Ctrl + E: Mở Quản lý Nhân sự (Admin)\n"
                + "   • Ctrl + R: Mở Báo cáo Thống kê (Admin)\n\n"
                + "3. PHÍM TẮT TẠI QUẦY POS (FrmBanHang):\n"
                + "   • F1: Tạo đơn hàng mới / Xóa trắng giỏ hàng\n"
                + "   • F2: Con trỏ vào ô tìm kiếm nhanh món đồ uống\n"
                + "   • F3: In hóa đơn nhiệt & Chuyển đơn sang quầy Bar\n"
                + "   • F4: Con trỏ vào ô nhập Số thẻ rung gán cho khách\n"
                + "   • F5: Thanh toán Tiền mặt (Cash)\n"
                + "   • F6: Quét mã VietQR động\n";

        JTextArea ta = new JTextArea(msg);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        ta.setBorder(new EmptyBorder(8, 10, 8, 10));
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Hướng Dẫn Phím Tắt HomuCafe", JOptionPane.INFORMATION_MESSAGE);
    }

    private void hienThiThongTinDoAn() {
        String info = "HOMU CAFE - COFFEE POS & PAGER SYSTEM\n"
                + "Môn học: Lập trình Hướng sự kiện với Java (Phát triển ứng dụng)\n"
                + "Đơn vị: Khoa Công Nghệ Thông Tin - Đại học Công nghiệp TP.HCM (IUH)\n"
                + "Sinh viên thực hiện: Nguyễn Bảo Trọng (14096921) - Võ Hiếu Tài (14101541)\n"
                + "Lớp: ĐHKTPM14\n"
                + "Công nghệ: Java Swing / AWT Event-Driven Architecture, SQL Server 3NF";
        JOptionPane.showMessageDialog(this, info, "Thông Tin Ứng Dụng HomuCafe", JOptionPane.INFORMATION_MESSAGE);
    }

    private void moHopThoaiDoiMatKhau() {
        TaiKhoan tk = DataStore.getTaiKhoanDangNhap();
        if (tk == null) return;

        JPasswordField txtCu = new JPasswordField();
        JPasswordField txtMoi = new JPasswordField();
        JPasswordField txtXacNhan = new JPasswordField();

        Object[] fields = {
                "Tài khoản đang đăng nhập: " + tk.getTenDangNhap(),
                "Mật khẩu hiện tại:", txtCu,
                "Mật khẩu mới:", txtMoi,
                "Nhập lại mật khẩu mới:", txtXacNhan
        };

        int opt = JOptionPane.showConfirmDialog(this, fields, "Đổi Mật Khẩu Cá Nhân (UC02)", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String cu = new String(txtCu.getPassword());
            String moi = new String(txtMoi.getPassword());
            String xn = new String(txtXacNhan.getPassword());

            if (moi.isEmpty() || !moi.equals(xn)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu mới và xác nhận mật khẩu không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (tk.doiMatKhau(cu, moi)) {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công (UC02)!");
            } else {
                JOptionPane.showMessageDialog(this, "Mật khẩu hiện tại không đúng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xuLyDangXuat() {
        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn đăng xuất khỏi phiên làm việc hiện tại (UC03)?",
                "Xác nhận Đăng xuất", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            FrmDangNhap login = new FrmDangNhap();
            login.setVisible(true);
            this.dispose();
        }
    }
}
