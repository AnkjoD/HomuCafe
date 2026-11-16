package com.homucafe.app;

import com.homucafe.entity.TaiKhoan;
import com.homucafe.report.ReportView;
import com.homucafe.shared.ui.Theme;
import com.homucafe.voucher.VoucherView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Khung làm việc chính: Sidebar thương hiệu HomuCafe, Topbar chuyển đổi vai trò, và CardLayout quản lý màn hình.
 */
public final class WorkspaceView extends JPanel {
    private final JPanel sidebar = new JPanel();
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel breadcrumb = Theme.label("Không gian làm việc", 12, Theme.MUTED);
    private final JComboBox<PreviewRole> rolePicker = new JComboBox<>(PreviewRole.values());
    private final Map<String, JButton> navigation = new LinkedHashMap<>();

    private final VoucherView voucherView = new VoucherView();
    private final ReportView reportView = new ReportView();
    private final JLabel lblProfileName = Theme.label("HomuCafe · IUH", 12, Theme.INK);
    private final JLabel lblProfileStatus = Theme.label("● Quán đang mở · 12 bàn", 10, Theme.MUTED);
    private PreviewRole role = PreviewRole.OWNER;
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

        // Đăng ký toàn bộ các màn hình chức năng vào CardLayout
        for (String name : PreviewRole.OWNER.screens()) {
            JComponent screen = switch (name) {
                case "Voucher & ưu đãi" -> voucherView;
                case "Báo cáo" -> reportView;
                default -> SampleScreens.create(name);
            };
            content.add(screen, name);
        }

        // Topbar phía trên
        JPanel right = new JPanel(new BorderLayout());
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(Theme.WHITE);
        topbar.setBorder(new EmptyBorder(17, 30, 17, 24));
        topbar.add(breadcrumb, BorderLayout.WEST);

        rolePicker.setToolTipText("Chọn vai trò làm việc để tự động lọc phân quyền màn hình");
        topbar.add(Theme.row(Theme.label("VAI TRÒ LÀM VIỆC:", 10, Theme.ORANGE), rolePicker), BorderLayout.EAST);
        right.add(topbar, BorderLayout.NORTH);
        right.add(content, BorderLayout.CENTER);

        JLabel footer = Theme.label("HomuCafe · Coffee POS & Pager System · Bản quyền Đồ án Lập trình Hướng sự kiện (IUH)", 11, Theme.MUTED);
        footer.setBorder(new EmptyBorder(12, 30, 14, 20));
        right.add(footer, BorderLayout.SOUTH);
        add(right, BorderLayout.CENTER);

        rolePicker.addActionListener(event -> setPreviewRole((PreviewRole) rolePicker.getSelectedItem()));
        setPreviewRole(role);
    }

    public void applyAccountPermissions(TaiKhoan tk) {
        if (tk == null) {
            rolePicker.setEnabled(false);
            lblProfileName.setText("HomuCafe · Khách");
            lblProfileStatus.setText("● Chưa xác thực tài khoản");
            setPreviewRole(PreviewRole.CASHIER);
            return;
        }

        String tenNV = tk.getNhanVien() != null ? tk.getNhanVien().getHoTen() : tk.getTenDangNhap();
        String chucVu = tk.getNhanVien() != null ? tk.getNhanVien().getChucVu() : "Nhân viên";
        lblProfileName.setText(tenNV);
        lblProfileStatus.setText("● " + chucVu + " (" + tk.getTenDangNhap() + ")");

        if ("ADMIN".equalsIgnoreCase(tk.getPhanQuyen())) {
            rolePicker.setEnabled(true);
            rolePicker.setToolTipText("Quản lý (ADMIN): Cho phép xem trước mọi vai trò làm việc");
            setPreviewRole(PreviewRole.OWNER);
        } else {
            rolePicker.setEnabled(false);
            rolePicker.setToolTipText("Tài khoản nhân viên (" + chucVu + ") được khóa phân quyền theo vai trò");
            String cvLower = chucVu.toLowerCase();
            String userLower = tk.getTenDangNhap() != null ? tk.getTenDangNhap().toLowerCase() : "";
            if (cvLower.contains("pha chế") || cvLower.contains("bar") || userLower.contains("bar")) {
                setPreviewRole(PreviewRole.BARISTA);
            } else {
                setPreviewRole(PreviewRole.CASHIER);
            }
        }
    }

    public void setPreviewRole(PreviewRole selected) {
        if (rolePicker.getSelectedItem() != selected) {
            rolePicker.setSelectedItem(selected);
            return;
        }
        role = selected;
        reportView.setOwnerPreview(role == PreviewRole.OWNER);
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

        sidebar.add(Box.createVerticalStrut(30));
        sidebar.add(Theme.label("KHÔNG GIAN " + role.toString().toUpperCase(), 10, Theme.MUTED));
        sidebar.add(Box.createVerticalStrut(12));

        for (String name : role.screens()) {
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
        showScreen(role.screens().get(0));
    }

    public void showScreen(String name) {
        if (!role.screens().contains(name)) {
            return;
        }
        cards.show(content, name);
        breadcrumb.setText("Không gian làm việc   /   " + name);
        navigation.forEach((title, button) -> {
            boolean active = title.equals(name);
            if (button instanceof Theme.NavButton nb) {
                nb.setActive(active);
            }
        });
    }

    public VoucherView getVoucherView() { return voucherView; }
    public ReportView getReportView() { return reportView; }
}

