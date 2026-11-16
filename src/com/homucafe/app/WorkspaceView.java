package com.homucafe.app;

import com.homucafe.entity.TaiKhoan;
import com.homucafe.report.ReportView;
import com.homucafe.shared.ui.Theme;
import com.homucafe.util.DataStore;
import com.homucafe.voucher.VoucherView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Khung làm việc chính: Sidebar thương hiệu HomuCafe, Topbar thông tin ca trực & đổi ca nhanh,
 * và CardLayout quản lý các màn hình độc lập phân quyền theo VaiTro (QUAN_LY, THU_NGAN, PHA_CHE).
 */
public final class WorkspaceView extends JPanel {
    private final JPanel sidebar = new JPanel();
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel breadcrumb = Theme.label("Không gian làm việc", 12, Theme.MUTED);
    private final JLabel lblCurrentShiftUser = Theme.label("Ca trực: Chưa đăng nhập  ·  Vai trò: Thu ngân", 12, Theme.INK);
    private final JButton btnDoiCaDangXuat = Theme.button("Đổi ca / Đăng xuất", false);

    private final Map<String, JButton> navigation = new LinkedHashMap<>();

    // 8 Màn hình nghiệp vụ độc lập + 2 Module nâng cao
    private final PanelTongQuan panelTongQuan = new PanelTongQuan();
    private final PanelBanHangPOS panelBanHang = new PanelBanHangPOS();
    private final PanelSoDoBan panelSoDoBan = new PanelSoDoBan();
    private final PanelQuayPhaChe panelQuayPhaChe = new PanelQuayPhaChe();
    private final PanelQuanLyHoaDon panelQuanLyHoaDon = new PanelQuanLyHoaDon();
    private final PanelQuanLyThucDon panelQuanLyThucDon = new PanelQuanLyThucDon();
    private final PanelQuanLyNhanSu panelQuanLyNhanSu = new PanelQuanLyNhanSu();
    private final PanelQuanLyKhachHang panelQuanLyKhachHang = new PanelQuanLyKhachHang();
    private final VoucherView voucherView = new VoucherView();
    private final ReportView reportView = new ReportView();

    private final JLabel lblProfileName = Theme.label("HomuCafe · IUH", 12, Theme.INK);
    private final JLabel lblProfileStatus = Theme.label("● Quán đang mở · 12 bàn", 10, Theme.MUTED);
    private VaiTro currentVaiTro = VaiTro.QUAN_LY;
    private String currentScreenName = "Tổng quan";
    private static WorkspaceView currentInstance;

    public static WorkspaceView getCurrentInstance() {
        return currentInstance;
    }

    public WorkspaceView() {
        currentInstance = this;
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);

        // Sidebar bên trái
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.WHITE);
        sidebar.setBorder(new EmptyBorder(26, 16, 20, 16));

        JScrollPane menuScroll = new JScrollPane(sidebar);
        menuScroll.setPreferredSize(new Dimension(236, 0));
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(menuScroll, BorderLayout.WEST);

        // Đăng ký toàn bộ 10 màn hình vào CardLayout
        content.add(panelTongQuan, "Tổng quan");
        content.add(panelBanHang, "Bán hàng");
        content.add(panelSoDoBan, "Sơ đồ bàn");
        content.add(panelQuayPhaChe, "Quầy pha chế");
        content.add(panelQuanLyHoaDon, "Quản lý hóa đơn");
        content.add(panelQuanLyThucDon, "Thực đơn món");
        content.add(voucherView, "Voucher & ưu đãi");
        content.add(panelQuanLyNhanSu, "Nhân viên & ca");
        content.add(panelQuanLyKhachHang, "Khách hàng");
        content.add(reportView, "Báo cáo");

        // Topbar phía trên: Breadcrumb bên trái, Thông tin ca trực & Nút Đổi ca bên phải
        JPanel right = new JPanel(new BorderLayout());
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(Theme.WHITE);
        topbar.setBorder(new EmptyBorder(14, 28, 14, 24));
        topbar.add(breadcrumb, BorderLayout.WEST);

        lblCurrentShiftUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDoiCaDangXuat.addActionListener(e -> xuLyDoiCaHoacDangXuat());

        JPanel pnlUserShift = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        pnlUserShift.setOpaque(false);
        pnlUserShift.add(lblCurrentShiftUser);
        pnlUserShift.add(btnDoiCaDangXuat);
        topbar.add(pnlUserShift, BorderLayout.EAST);

        right.add(topbar, BorderLayout.NORTH);
        right.add(content, BorderLayout.CENTER);

        JLabel footer = Theme.label("HomuCafe · Coffee POS & Pager System · Bản quyền Đồ án Lập trình Hướng sự kiện (IUH)", 11, Theme.MUTED);
        footer.setBorder(new EmptyBorder(12, 30, 14, 20));
        right.add(footer, BorderLayout.SOUTH);
        add(right, BorderLayout.CENTER);

        setVaiTro(currentVaiTro);
    }

    public void xuLyDoiCaHoacDangXuat() {
        Window win = SwingUtilities.getWindowAncestor(this);
        DialogDangNhap loginDlg = new DialogDangNhap(win);
        loginDlg.setVisible(true);
        if (loginDlg.isDangNhapThanhCong()) {
            applyAccountPermissions(DataStore.getTaiKhoanDangNhap());
        }
    }

    public void applyAccountPermissions(TaiKhoan tk) {
        VaiTro vaiTro = VaiTro.tuTaiKhoan(tk);
        String tenNV = (tk != null && tk.getNhanVien() != null) ? tk.getNhanVien().getHoTen() : (tk != null ? tk.getTenDangNhap() : "Khách");
        String chucVu = (tk != null && tk.getNhanVien() != null) ? tk.getNhanVien().getChucVu() : vaiTro.getLabel();

        lblCurrentShiftUser.setText("Ca trực: " + tenNV + "  ·  Vai trò: " + vaiTro.getLabel());
        lblProfileName.setText(tenNV);
        lblProfileStatus.setText("● " + chucVu + (tk != null ? " (" + tk.getTenDangNhap() + ")" : ""));

        setVaiTro(vaiTro);

        // Cập nhật lại thanh JMenuBar của cửa sổ chính
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win instanceof JFrame frame) {
            SwingLauncher.updateMenuBarForRole(frame, this, vaiTro);
        }
    }

    public void setVaiTro(VaiTro selected) {
        this.currentVaiTro = selected;
        reportView.setOwnerPreview(selected == VaiTro.QUAN_LY);
        panelQuanLyThucDon.setQuanLyMode(selected == VaiTro.QUAN_LY);

        sidebar.removeAll();
        navigation.clear();

        // Logo thương hiệu HomuCafe
        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        JLabel mark = new JLabel(new Theme.LineIcon("brand", 25, Color.WHITE));
        mark.setOpaque(true);
        mark.setBackground(Theme.GREEN);
        mark.setHorizontalAlignment(SwingConstants.CENTER);
        mark.setPreferredSize(new Dimension(44, 46));

        brand.add(mark, BorderLayout.WEST);
        brand.add(Theme.stack(2, Theme.title("homu."), Theme.label("COFFEE POS & PAGER", 9, Theme.MUTED)));
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);

        sidebar.add(Box.createVerticalStrut(26));
        sidebar.add(Theme.label("VAI TRÒ: " + selected.getLabel().toUpperCase(), 10, Theme.MUTED));
        sidebar.add(Box.createVerticalStrut(10));

        // Nạp chính xác các màn hình mà Role này được phép truy cập
        for (String name : selected.screens()) {
            JButton button = Theme.navigationButton(name);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            button.addActionListener(event -> showScreen(name));
            navigation.put(name, button);
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());

        // Hồ sơ người dùng góc dưới sidebar
        JPanel profile = Theme.surface(new BorderLayout(10, 0));
        profile.setBorder(new EmptyBorder(12, 12, 12, 12));
        profile.add(Theme.stack(4, lblProfileName, lblProfileStatus));
        profile.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        profile.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(profile);

        sidebar.revalidate();
        sidebar.repaint();

        // Nếu màn hình hiện tại không nằm trong quyền truy cập của vai trò mới, chuyển về màn hình mặc định
        if (!selected.screens().contains(currentScreenName)) {
            showScreen(selected.screens().get(0));
        } else {
            showScreen(currentScreenName);
        }
    }

    public void showScreen(String name) {
        if (!currentVaiTro.screens().contains(name)) {
            return;
        }
        currentScreenName = name;
        cards.show(content, name);
        breadcrumb.setText("Không gian làm việc   /   " + name);
        navigation.forEach((title, button) -> {
            boolean active = title.equals(name);
            if (button instanceof Theme.NavButton nb) {
                nb.setActive(active);
            }
        });
    }

    public VaiTro getCurrentVaiTro() { return currentVaiTro; }
    public VoucherView getVoucherView() { return voucherView; }
    public ReportView getReportView() { return reportView; }
}
