package com.homucafe.app;

import com.homucafe.shared.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Điểm khởi chạy chính ứng dụng HomuCafe POS & Pager System.
 * Tích hợp toàn diện giao diện phẳng hiện đại FlatLaf cùng thanh JMenuBar chuẩn IUH
 * (đầy đủ Mnemonic Alt+... và Accelerator Ctrl+... theo barem chấm điểm đồ án).
 */
public class SwingLauncher {

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "HomuCafe");

        SwingUtilities.invokeLater(() -> {
            Theme.install();

            JFrame frame = new JFrame("HomuCafe · Quản Lý Bán Hàng Quán Coffee Tự Phục Vụ (Coffee POS & Pager System)");
            frame.setSize(1380, 880);
            frame.setMinimumSize(new Dimension(1180, 760));
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            WorkspaceView workspaceView = new WorkspaceView();
            frame.setJMenuBar(createMenuBar(workspaceView, frame));
            frame.setContentPane(workspaceView);
            frame.setVisible(true);
        });
    }

    private static JMenuBar createMenuBar(WorkspaceView workspaceView, JFrame frame) {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Theme.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE));

        // 1. MENU HỆ THỐNG
        JMenu menuHeThong = new JMenu("Hệ thống");
        menuHeThong.setMnemonic(KeyEvent.VK_H);

        JMenuItem mniDangNhap = new JMenuItem("Đăng nhập...");
        mniDangNhap.setMnemonic(KeyEvent.VK_L);
        mniDangNhap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        mniDangNhap.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Bạn có thể chuyển đổi nhanh vai trò làm việc tại mục 'VAI TRÒ LÀM VIỆC' ở góc trên bên phải!", "Thông tin đăng nhập", JOptionPane.INFORMATION_MESSAGE);
        });

        JMenuItem mniDangXuat = new JMenuItem("Đăng xuất");
        mniDangXuat.setMnemonic(KeyEvent.VK_Q);
        mniDangXuat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        mniDangXuat.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, "Bạn có chắc chắn muốn đăng xuất phiên làm việc hiện tại?", "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                workspaceView.setPreviewRole(PreviewRole.CASHIER);
            }
        });

        JMenuItem mniThoat = new JMenuItem("Thoát ứng dụng");
        mniThoat.setMnemonic(KeyEvent.VK_X);
        mniThoat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
        mniThoat.addActionListener(e -> System.exit(0));

        menuHeThong.add(mniDangNhap);
        menuHeThong.add(mniDangXuat);
        menuHeThong.addSeparator();
        menuHeThong.add(mniThoat);

        // 2. MENU NGHIỆP VỤ
        JMenu menuNghiepVu = new JMenu("Nghiệp vụ");
        menuNghiepVu.setMnemonic(KeyEvent.VK_N);

        JMenuItem mniBanHang = new JMenuItem("Bán hàng tại quầy (POS)");
        mniBanHang.setMnemonic(KeyEvent.VK_B);
        mniBanHang.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        mniBanHang.addActionListener(e -> workspaceView.showScreen("Bán hàng"));

        JMenuItem mniSoDoBan = new JMenuItem("Sơ đồ bàn phục vụ");
        mniSoDoBan.setMnemonic(KeyEvent.VK_S);
        mniSoDoBan.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        mniSoDoBan.addActionListener(e -> workspaceView.showScreen("Sơ đồ bàn"));

        JMenuItem mniKitchen = new JMenuItem("Quầy pha chế & Thẻ rung");
        mniKitchen.setMnemonic(KeyEvent.VK_P);
        mniKitchen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        mniKitchen.addActionListener(e -> workspaceView.showScreen("Quầy pha chế"));

        JMenuItem mniHoaDon = new JMenuItem("Quản lý hóa đơn");
        mniHoaDon.setMnemonic(KeyEvent.VK_H);
        mniHoaDon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        mniHoaDon.addActionListener(e -> workspaceView.showScreen("Quản lý hóa đơn"));

        menuNghiepVu.add(mniBanHang);
        menuNghiepVu.add(mniSoDoBan);
        menuNghiepVu.add(mniKitchen);
        menuNghiepVu.add(mniHoaDon);

        // 3. MENU DANH MỤC
        JMenu menuDanhMuc = new JMenu("Danh mục");
        menuDanhMuc.setMnemonic(KeyEvent.VK_D);

        JMenuItem mniThucDon = new JMenuItem("Thực đơn món & Bảng giá");
        mniThucDon.setMnemonic(KeyEvent.VK_M);
        mniThucDon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        mniThucDon.addActionListener(e -> workspaceView.showScreen("Thực đơn món"));

        JMenuItem mniVoucher = new JMenuItem("Voucher & Khuyến mãi");
        mniVoucher.setMnemonic(KeyEvent.VK_V);
        mniVoucher.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        mniVoucher.addActionListener(e -> workspaceView.showScreen("Voucher & ưu đãi"));

        JMenuItem mniKhachHang = new JMenuItem("Khách hàng quen");
        mniKhachHang.setMnemonic(KeyEvent.VK_C);
        mniKhachHang.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        mniKhachHang.addActionListener(e -> workspaceView.showScreen("Khách hàng"));

        JMenuItem mniNhanVien = new JMenuItem("Hồ sơ nhân viên & Ca làm");
        mniNhanVien.setMnemonic(KeyEvent.VK_E);
        mniNhanVien.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        mniNhanVien.addActionListener(e -> workspaceView.showScreen("Nhân viên & ca"));

        menuDanhMuc.add(mniThucDon);
        menuDanhMuc.add(mniVoucher);
        menuDanhMuc.add(mniKhachHang);
        menuDanhMuc.add(mniNhanVien);

        // 4. MENU BÁO BIỂU
        JMenu menuBaoBieu = new JMenu("Báo biểu");
        menuBaoBieu.setMnemonic(KeyEvent.VK_B);

        JMenuItem mniBaoCao = new JMenuItem("Báo cáo kinh doanh đa chiều");
        mniBaoCao.setMnemonic(KeyEvent.VK_R);
        mniBaoCao.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        mniBaoCao.addActionListener(e -> workspaceView.showScreen("Báo cáo"));

        menuBaoBieu.add(mniBaoCao);

        // 5. MENU TRỢ GIÚP
        JMenu menuTroGiup = new JMenu("Trợ giúp");
        menuTroGiup.setMnemonic(KeyEvent.VK_T);

        JMenuItem mniHuongDan = new JMenuItem("Hướng dẫn sử dụng");
        mniHuongDan.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        mniHuongDan.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame,
                    "QUẢN LÝ BÁN HÀNG COFFEE TỰ PHỤC VỤ (COFFEE POS & PAGER SYSTEM)\n" +
                            "• Bán hàng: Chọn món -> Tùy biến Size/Đường/Đá/Topping -> Gán thẻ rung Pager -> Thanh toán\n" +
                            "• Quầy pha chế: Theo dõi hàng đợi FIFO, pha chế đồ uống và kích hoạt báo rung thẻ khi hoàn tất\n" +
                            "• Báo biểu: Phân tích doanh thu, giờ cao điểm, phương thức thanh toán và dòng tiền",
                    "Hướng dẫn sử dụng", JOptionPane.INFORMATION_MESSAGE);
        });

        JMenuItem mniThongTin = new JMenuItem("Thông tin đồ án");
        mniThongTin.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame,
                    "ĐỒ ÁN MÔN HỌC: LẬP TRÌNH HƯỚNG SỰ KIỆN VỚI JAVA\n" +
                            "Đề tài: Quản lý Quán Cà phê Tự phục vụ (HomuCafe)\n" +
                            "Khoa Công nghệ Thông tin - Trường Đại học Công nghiệp TP.HCM (IUH)",
                    "Về ứng dụng HomuCafe", JOptionPane.INFORMATION_MESSAGE);
        });

        menuTroGiup.add(mniHuongDan);
        menuTroGiup.add(mniThongTin);

        menuBar.add(menuHeThong);
        menuBar.add(menuNghiepVu);
        menuBar.add(menuDanhMuc);
        menuBar.add(menuBaoBieu);
        menuBar.add(menuTroGiup);

        return menuBar;
    }
}

