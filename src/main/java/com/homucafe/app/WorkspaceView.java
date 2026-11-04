package com.homucafe.app;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.homucafe.inventory.InventoryView;
import com.homucafe.shared.ui.Theme;
import com.homucafe.voucher.VoucherView;
import com.homucafe.report.ReportView;

/** Khung ứng dụng: sidebar, thanh trên và vùng nội dung chuyển bằng CardLayout. */
public final class WorkspaceView extends JPanel {
    private final JPanel sidebar = new JPanel();
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel breadcrumb = Theme.label("Không gian làm việc", 12, Theme.MUTED);
    private final JComboBox<PreviewRole> rolePicker = new JComboBox<>(PreviewRole.values());
    private final Map<String, JButton> navigation = new LinkedHashMap<>();
    private final InventoryView inventoryView = new InventoryView();
    private final VoucherView voucherView = new VoucherView();
    private final ReportView reportView = new ReportView();
    private PreviewRole role = PreviewRole.OWNER;

    public WorkspaceView() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAPER);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.WHITE);
        sidebar.setBorder(new EmptyBorder(26, 16, 20, 16));
        JScrollPane menuScroll = new JScrollPane(sidebar);
        menuScroll.setPreferredSize(new Dimension(232, 0));
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(menuScroll, BorderLayout.WEST);

        // Tạo sẵn mỗi View một lần; chuyển menu không làm mất nội dung đang nhập.
        for (String name : PreviewRole.OWNER.screens()) {
            JComponent screen = switch (name) {
                case "Kho nguyên liệu" -> inventoryView;
                case "Voucher & ưu đãi" -> voucherView;
                case "Báo cáo" -> reportView;
                default -> SampleScreens.create(name);
            };
            content.add(screen, name);
        }
        content.add(SampleScreens.create("Ca của tôi"), "Ca của tôi");
        JPanel right = new JPanel(new BorderLayout());
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(Theme.WHITE);
        topbar.setBorder(new EmptyBorder(17, 30, 17, 24));
        topbar.add(breadcrumb, BorderLayout.WEST);
        rolePicker.setToolTipText("Chọn giao diện mẫu; chưa phải đăng nhập hoặc phân quyền thật.");
        rolePicker.getAccessibleContext().setAccessibleName("Vai trò xem giao diện mẫu");
        topbar.add(Theme.row(Theme.label("XEM MẪU THEO VAI TRÒ", 10, Theme.ORANGE), rolePicker), BorderLayout.EAST);
        right.add(topbar, BorderLayout.NORTH);
        right.add(content, BorderLayout.CENTER);
        JLabel demo = Theme.label("Bản xem trước giao diện  ·  Dữ liệu minh họa  ·  Chưa xử lý giao dịch", 11, Theme.MUTED);
        demo.setBorder(new EmptyBorder(12, 30, 14, 20));
        right.add(demo, BorderLayout.SOUTH);
        add(right, BorderLayout.CENTER);
        // Đây là sự kiện điều hướng giao diện, không thực hiện nghiệp vụ hoặc lưu dữ liệu.
        rolePicker.addActionListener(event -> setPreviewRole((PreviewRole) rolePicker.getSelectedItem()));
        setPreviewRole(role);
    }

    public void setPreviewRole(PreviewRole selected) {
        if (rolePicker.getSelectedItem() != selected) {
            rolePicker.setSelectedItem(selected);
            return; // Sự kiện của ComboBox sẽ gọi lại với giá trị đã đồng bộ.
        }
        role = selected;
        reportView.setOwnerPreview(role == PreviewRole.OWNER);
        sidebar.removeAll();
        navigation.clear();
        JPanel brand = new JPanel(new BorderLayout(12,0));brand.setOpaque(false);
        JLabel mark=new JLabel(new Theme.LineIcon("brand",25,Color.WHITE));mark.setOpaque(true);mark.setBackground(Theme.GREEN);mark.setHorizontalAlignment(SwingConstants.CENTER);mark.setPreferredSize(new Dimension(43,46));
        brand.add(mark,BorderLayout.WEST);brand.add(Theme.stack(1,Theme.title("homu."),Theme.label("CAFE MANAGEMENT",9,Theme.MUTED)));
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE,58));brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(32));
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
        JPanel profile=Theme.surface(new BorderLayout(10,0));profile.setBorder(new EmptyBorder(13,12,13,12));
        profile.add(Theme.stack(4,Theme.label("Homu · Nguyễn Văn Bảo",11,Theme.INK),Theme.label("●  Một quán · 12 bàn",10,Theme.MUTED)));
        profile.setMaximumSize(new Dimension(Integer.MAX_VALUE,65));profile.setAlignmentX(Component.LEFT_ALIGNMENT);sidebar.add(profile);
        sidebar.revalidate();
        sidebar.repaint();
        showScreen(role.screens().getFirst());
    }

    public void showScreen(String name) {
        // Chặn điều hướng sai vai trò ngay cả khi gọi trực tiếp phương thức.
        // Backend vẫn phải kiểm tra quyền thật khi triển khai đăng nhập sau này.
        if (!role.screens().contains(name)) throw new IllegalArgumentException("Vai trò không có màn hình " + name);
        cards.show(content, name);
        breadcrumb.setText("Không gian làm việc   /   " + name);
        navigation.forEach((title, button) -> {
            button.setBackground(title.equals(name) ? Theme.GREEN : Theme.WHITE);
            button.setForeground(title.equals(name) ? Color.WHITE : Theme.INK);
        });
    }

    public InventoryView getInventoryView() { return inventoryView; }
    public VoucherView getVoucherView() { return voucherView; }
}
