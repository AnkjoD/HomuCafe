package com.homucafe.app;

import com.homucafe.entity.*;
import com.homucafe.shared.ui.Theme;
import com.homucafe.util.DataStore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Điểm khởi chạy chính ứng dụng HomuCafe POS & Pager System.
 * Tích hợp toàn diện giao diện phẳng hiện đại FlatLaf cùng thanh JMenuBar chuẩn IUH
 * và Màn hình Đăng nhập xác thực tài khoản thật, phân quyền động theo VaiTro,
 * kèm tính năng Sao lưu dữ liệu quán (Backup) 1-Click.
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

        // Tính năng Sao lưu dữ liệu quán (Backup) 1-Click
        JMenuItem mniBackup = new JMenuItem("Sao lưu dữ liệu quán (Backup)...");
        mniBackup.setMnemonic(KeyEvent.VK_K);
        mniBackup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK));
        mniBackup.addActionListener(e -> thucHienSaoLuuDuLieu(frame));

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
        menuHeThong.add(mniBackup);
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
                            "• Tạm giữ đơn [F4]: Lưu đơn hàng đang order để thanh toán cho khách sau, khôi phục bất cứ lúc nào\n" +
                            "• Tách bill: Tách giỏ hàng thành nhiều đợt thanh toán (chia tiền món cho nhóm khách)\n" +
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

    private static void thucHienSaoLuuDuLieu(Component parent) {
        JFileChooser chooser = new JFileChooser();
        String defaultFileName = "HomuCafe_Backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".sql";
        chooser.setSelectedFile(new java.io.File(defaultFileName));
        chooser.setDialogTitle("Chọn vị trí lưu file sao lưu CSDL HomuCafe");

        int userSelection = chooser.showSaveDialog(parent);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = chooser.getSelectedFile();
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(fileToSave), StandardCharsets.UTF_8)) {

                writer.write("-- ==========================================================\n");
                writer.write("-- BẢN SAO LƯU DỮ LIỆU TOÀN DIỆN HOMUCAFE POS & PAGER SYSTEM\n");
                writer.write("-- Ngày sao lưu: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\n");
                writer.write("-- Người thực hiện: " + (DataStore.getTaiKhoanDangNhap() != null ? DataStore.getTaiKhoanDangNhap().getTenDangNhap() : "admin") + "\n");
                writer.write("-- ==========================================================\n\n");

                // 1. Khách hàng
                writer.write("-- 1. DỮ LIỆU KHÁCH HÀNG THÂN THIẾT & ĐIỂM TÍCH LŨY\n");
                for (KhachHang kh : DataStore.getDsKhachHang()) {
                    writer.write(String.format("INSERT INTO KHACH_HANG (maKH, hoTen, soDienThoai, diemTichLuy) VALUES (N'%s', N'%s', '%s', %d);\n",
                            escapeSql(kh.getMaKH()), escapeSql(kh.getHoTen()), kh.getSoDienThoai(), kh.getDiemTichLuy()));
                }
                writer.write("\n");

                // 2. Nhân viên & Tài khoản
                writer.write("-- 2. DỮ LIỆU NHÂN VIÊN & TÀI KHOẢN HỆ THỐNG\n");
                for (NhanVien nv : DataStore.getDsNhanVien()) {
                    writer.write(String.format("INSERT INTO NHAN_VIEN (maNV, hoTen, soDienThoai, cccd, chucVu, dangLamViec) VALUES ('%s', N'%s', '%s', '%s', N'%s', %d);\n",
                            nv.getMaNV(), escapeSql(nv.getHoTen()), nv.getSoDienThoai(), nv.getCccd(), escapeSql(nv.getChucVu()), nv.isDangLamViec() ? 1 : 0));
                }
                for (TaiKhoan tk : DataStore.getDsTaiKhoan()) {
                    writer.write(String.format("INSERT INTO TAI_KHOAN (tenDangNhap, matKhau, phanQuyen, maNV) VALUES ('%s', '%s', '%s', %s);\n",
                            tk.getTenDangNhap(), tk.getMatKhau(), tk.getPhanQuyen(),
                            tk.getNhanVien() != null ? "'" + tk.getNhanVien().getMaNV() + "'" : "NULL"));
                }
                writer.write("\n");

                // 3. Thực đơn món
                writer.write("-- 3. DỮ LIỆU THỰC ĐƠN MÓN NƯỚC\n");
                for (Mon m : DataStore.getDsMon()) {
                    writer.write(String.format("INSERT INTO MON (maMon, tenMon, donGiaGoc, maLoai, dangKinhDoanh, hinhAnh) VALUES ('%s', N'%s', %.0f, '%s', %d, '%s');\n",
                            m.getMaMon(), escapeSql(m.getTenMon()), m.getDonGiaGoc(),
                            m.getLoaiMon() != null ? m.getLoaiMon().getMaLoai() : "CF",
                            m.isDangKinhDoanh() ? 1 : 0, m.getHinhAnh()));
                }
                writer.write("\n");

                // 4. Voucher
                writer.write("-- 4. DỮ LIỆU VOUCHER & KHUYẾN MÃI\n");
                for (Voucher v : DataStore.getDsVoucher()) {
                    writer.write(String.format("INSERT INTO VOUCHER (maVoucher, loaiGiam, giaTriGiam, donToiThieu, gioiHanLuot, soLuotDung, trangThai) VALUES ('%s', '%s', %.0f, %.0f, %d, %d, %d);\n",
                            v.getMaVoucher(), v.getHinhThucGiam(), v.getGiaTriGiam(), v.getDonToiThieu(), v.getGioiHanLuot(), v.getSoLuotDung(), v.isTrangThai() ? 1 : 0));
                }
                writer.write("\n");

                // 5. Hóa đơn & Chi tiết hóa đơn
                writer.write("-- 5. DỮ LIỆU HÓA ĐƠN & CHI TIẾT HÓA ĐƠN\n");
                for (HoaDon hd : DataStore.getDsHoaDon()) {
                    String ngayLapStr = hd.getNgayLap() != null ? hd.getNgayLap().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "GETDATE()";
                    writer.write(String.format("INSERT INTO HOA_DON (maHD, ngayLap, tongTien, giamGia, tienKhachDua, tienThoi, phuongThuc, hinhThuc, trangThaiDon, soTheRung, maNV, maKH, maVoucher) VALUES ('%s', '%s', %.0f, %.0f, %.0f, %.0f, '%s', '%s', '%s', %s, %s, %s, %s);\n",
                            hd.getMaHD(), ngayLapStr, hd.getTongTien(), hd.getGiamGia(), hd.getTienKhachDua(), hd.getTienThoi(),
                            hd.getPhuongThucThanhToan(), hd.getLoaiDon(), hd.getTrangThaiDon(),
                            hd.getSoTheRung() > 0 ? String.valueOf(hd.getSoTheRung()) : "NULL",
                            hd.getNhanVien() != null ? "'" + hd.getNhanVien().getMaNV() + "'" : "NULL",
                            hd.getKhachHang() != null ? "N'" + escapeSql(hd.getKhachHang().getMaKH()) + "'" : "NULL",
                            hd.getMaVoucher() != null ? "'" + hd.getMaVoucher() + "'" : "NULL"));

                    if (hd.getDsChiTiet() != null) {
                        for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
                            writer.write(String.format("INSERT INTO CHI_TIET_HOA_DON (maHD, maMon, kichCo, soLuong, donGiaThucTe, mucDuong, mucDa, ghiChuTopping) VALUES ('%s', '%s', '%s', %d, %.0f, %d, '%s', N'%s');\n",
                                    hd.getMaHD(), ct.getMon() != null ? ct.getMon().getMaMon() : "CF01",
                                    ct.getKichCo() != null ? ct.getKichCo().name() : "M",
                                    ct.getSoLuong(), ct.getDonGiaThucTe(), ct.getMucDuong(), ct.getMucDa(), escapeSql(ct.getGhiChuTopping() != null ? ct.getGhiChuTopping() : "")));
                        }
                    }
                }
                writer.write("\n");

                // 6. Phiếu chi vặt trong ca
                writer.write("-- 6. SỔ PHIẾU CHI VẶT TRONG CA (PETTY CASH LOG)\n");
                for (PhieuChiCa pc : DataStore.getDsPhieuChiCa()) {
                    String tgStr = pc.getThoiGian() != null ? pc.getThoiGian().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "GETDATE()";
                    writer.write(String.format("INSERT INTO PHIEU_CHI_CA (maPhieuChi, thoiGian, soTien, lyDo, nguoiChi) VALUES ('%s', '%s', %.0f, N'%s', N'%s');\n",
                            pc.getMaPhieuChi(), tgStr, pc.getSoTien(), escapeSql(pc.getLyDo()), escapeSql(pc.getNguoiChi())));
                }

                writer.flush();
                JOptionPane.showMessageDialog(parent,
                        "Sao lưu dữ liệu CSDL HomuCafe thành công!\nĐường dẫn: " + fileToSave.getAbsolutePath(),
                        "Sao lưu hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent,
                        "Không thể lưu file sao lưu: " + ex.getMessage(),
                        "Lỗi sao lưu", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String escapeSql(String s) {
        if (s == null) return "";
        return s.replace("'", "''");
    }
}
