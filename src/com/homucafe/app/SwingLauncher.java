package com.homucafe.app;

import com.homucafe.entity.TaiKhoan;
import com.homucafe.shared.ui.Theme;
import com.homucafe.util.DataStore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Điểm khởi chạy chính ứng dụng HomuCafe POS & Pager System.
 * Tích hợp toàn diện giao diện phẳng hiện đại FlatLaf cùng thanh JMenuBar chuẩn IUH
 * và Màn hình Đăng nhập xác thực tài khoản thật, phân quyền động theo VaiTro.
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

            // Mở Dialog Đăng nhập thật
            DialogDangNhap loginDlg = new DialogDangNhap(frame);
            loginDlg.setVisible(true);

            if (!loginDlg.isDangNhapThanhCong()) {
                System.exit(0);
                return;
            }

            WorkspaceView workspaceView = new WorkspaceView();

            // Phân quyền theo vai trò tài khoản đăng nhập
            workspaceView.applyAccountPermissions(DataStore.getTaiKhoanDangNhap());

            frame.setJMenuBar(createMenuBar(workspaceView, frame, workspaceView.getCurrentVaiTro()));
            frame.setContentPane(workspaceView);
            frame.setVisible(true);
        });
    }

    public static void updateMenuBarForRole(JFrame frame, WorkspaceView workspaceView, VaiTro vaiTro) {
        if (frame != null) {
            frame.setJMenuBar(createMenuBar(workspaceView, frame, vaiTro));
            frame.revalidate();
            frame.repaint();
        }
    }

    public static JMenuBar createMenuBar(WorkspaceView workspaceView, JFrame frame, VaiTro vaiTro) {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Theme.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE));

        if (vaiTro == null) {
            vaiTro = VaiTro.QUAN_LY;
        }

        // 1. MENU HỆ THỐNG (Dành cho mọi vai trò)
        JMenu menuHeThong = new JMenu("Hệ thống");
        menuHeThong.setMnemonic(KeyEvent.VK_H);

        JMenuItem mniDangNhap = new JMenuItem("Đăng nhập tài khoản khác...");
        mniDangNhap.setMnemonic(KeyEvent.VK_L);
        mniDangNhap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        mniDangNhap.addActionListener(e -> {
            DialogDangNhap loginDlg = new DialogDangNhap(frame);
            loginDlg.setVisible(true);
            if (loginDlg.isDangNhapThanhCong()) {
                workspaceView.applyAccountPermissions(DataStore.getTaiKhoanDangNhap());
            }
        });

        JMenuItem mniDoiMatKhau = new JMenuItem("Đổi mật khẩu...");
        mniDoiMatKhau.setMnemonic(KeyEvent.VK_P);
        mniDoiMatKhau.addActionListener(e -> {
            DialogDoiMatKhau dlg = new DialogDoiMatKhau(frame);
            dlg.setVisible(true);
        });

        JMenuItem mniDangXuat = new JMenuItem("Đăng xuất");
        mniDangXuat.setMnemonic(KeyEvent.VK_Q);
        mniDangXuat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        mniDangXuat.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, "Bạn có chắc chắn muốn đăng xuất phiên làm việc hiện tại?", "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                DialogDangNhap loginDlg = new DialogDangNhap(frame);
                loginDlg.setVisible(true);
                if (loginDlg.isDangNhapThanhCong()) {
                    workspaceView.applyAccountPermissions(DataStore.getTaiKhoanDangNhap());
                }
            }
        });

        JMenuItem mniThoat = new JMenuItem("Thoát ứng dụng");
        mniThoat.setMnemonic(KeyEvent.VK_X);
        mniThoat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
        mniThoat.addActionListener(e -> System.exit(0));

        menuHeThong.add(mniDangNhap);
        menuHeThong.add(mniDoiMatKhau);
        menuHeThong.add(mniDangXuat);
        menuHeThong.addSeparator();
        menuHeThong.add(mniThoat);

        menuBar.add(menuHeThong);

        // 2. MENU NGHIỆP VỤ (Lọc theo vai trò)
        JMenu menuNghiepVu = new JMenu("Nghiệp vụ");
        menuNghiepVu.setMnemonic(KeyEvent.VK_N);

        if (vaiTro.screens().contains("Bán hàng")) {
            JMenuItem mniBanHang = new JMenuItem("Bán hàng tại quầy (POS)");
            mniBanHang.setMnemonic(KeyEvent.VK_B);
            mniBanHang.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            mniBanHang.addActionListener(e -> workspaceView.showScreen("Bán hàng"));
            menuNghiepVu.add(mniBanHang);
        }

        if (vaiTro.screens().contains("Sơ đồ bàn")) {
            JMenuItem mniSoDoBan = new JMenuItem("Sơ đồ bàn phục vụ");
            mniSoDoBan.setMnemonic(KeyEvent.VK_S);
            mniSoDoBan.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            mniSoDoBan.addActionListener(e -> workspaceView.showScreen("Sơ đồ bàn"));
            menuNghiepVu.add(mniSoDoBan);
        }

        if (vaiTro.screens().contains("Quầy pha chế")) {
            JMenuItem mniKitchen = new JMenuItem("Quầy pha chế & Thẻ rung");
            mniKitchen.setMnemonic(KeyEvent.VK_P);
            mniKitchen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            mniKitchen.addActionListener(e -> workspaceView.showScreen("Quầy pha chế"));
            menuNghiepVu.add(mniKitchen);
        }

        if (vaiTro.screens().contains("Quản lý hóa đơn")) {
            JMenuItem mniHoaDon = new JMenuItem("Quản lý hóa đơn");
            mniHoaDon.setMnemonic(KeyEvent.VK_H);
            mniHoaDon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            mniHoaDon.addActionListener(e -> workspaceView.showScreen("Quản lý hóa đơn"));
            menuNghiepVu.add(mniHoaDon);
        }

        if (menuNghiepVu.getItemCount() > 0) {
            menuBar.add(menuNghiepVu);
        }

        // 3. MENU DANH MỤC (Lọc theo vai trò)
        JMenu menuDanhMuc = new JMenu("Danh mục");
        menuDanhMuc.setMnemonic(KeyEvent.VK_D);

        if (vaiTro.screens().contains("Thực đơn món")) {
            JMenuItem mniThucDon = new JMenuItem("Thực đơn món & Bảng giá");
            mniThucDon.setMnemonic(KeyEvent.VK_M);
            mniThucDon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            mniThucDon.addActionListener(e -> workspaceView.showScreen("Thực đơn món"));
            menuDanhMuc.add(mniThucDon);
        }

        if (vaiTro.screens().contains("Voucher & ưu đãi")) {
            JMenuItem mniVoucher = new JMenuItem("Voucher & Khuyến mãi");
            mniVoucher.setMnemonic(KeyEvent.VK_V);
            mniVoucher.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            mniVoucher.addActionListener(e -> workspaceView.showScreen("Voucher & ưu đãi"));
            menuDanhMuc.add(mniVoucher);
        }

        if (vaiTro.screens().contains("Khách hàng")) {
            JMenuItem mniKhachHang = new JMenuItem("Khách hàng quen");
            mniKhachHang.setMnemonic(KeyEvent.VK_C);
            mniKhachHang.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            mniKhachHang.addActionListener(e -> workspaceView.showScreen("Khách hàng"));
            menuDanhMuc.add(mniKhachHang);
        }

        if (vaiTro.screens().contains("Nhân viên & ca")) {
            JMenuItem mniNhanVien = new JMenuItem("Hồ sơ nhân viên & Ca làm");
            mniNhanVien.setMnemonic(KeyEvent.VK_E);
            mniNhanVien.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            mniNhanVien.addActionListener(e -> workspaceView.showScreen("Nhân viên & ca"));
            menuDanhMuc.add(mniNhanVien);
        }

        if (menuDanhMuc.getItemCount() > 0) {
            menuBar.add(menuDanhMuc);
        }

        // 4. MENU BÁO BIỂU (Lọc theo vai trò)
        if (vaiTro.screens().contains("Báo cáo")) {
            JMenu menuBaoBieu = new JMenu("Báo biểu");
            menuBaoBieu.setMnemonic(KeyEvent.VK_B);

            JMenuItem mniBaoCao = new JMenuItem("Báo cáo kinh doanh đa chiều");
            mniBaoCao.setMnemonic(KeyEvent.VK_R);
            mniBaoCao.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            mniBaoCao.addActionListener(e -> workspaceView.showScreen("Báo cáo"));

            menuBaoBieu.add(mniBaoCao);
            menuBar.add(menuBaoBieu);
        }

        // 5. MENU TRỢ GIÚP (Dành cho mọi vai trò)
        JMenu menuTroGiup = new JMenu("Trợ giúp");
        menuTroGiup.setMnemonic(KeyEvent.VK_T);

        JMenuItem mniHuongDan = new JMenuItem("Hướng dẫn sử dụng");
        mniHuongDan.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        mniHuongDan.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame,
                    "QUẢN LÝ BÁN HÀNG COFFEE TỰ PHỤC VỤ (COFFEE POS & PAGER SYSTEM)\n" +
                            "• Bán hàng: Chọn món -> Tùy biến Size/Đường/Đá/Topping -> Gán thẻ rung Pager -> Thanh toán\n" +
                            "• Quầy pha chế: Theo dõi hàng đợi FIFO, pha chế đồ uống và kích hoạt báo rung thẻ khi hoàn tất\n" +
                            "• Sơ đồ bàn: Theo dõi 12 bàn, Check-in đặt bàn, Dọn bàn\n" +
                            "• Báo biểu: Phân tích doanh thu, giờ cao điểm, phương thức thanh toán và đối soát ca tiền mặt",
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

        menuBar.add(menuTroGiup);

        return menuBar;
    }
}
