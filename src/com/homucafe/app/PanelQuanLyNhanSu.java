package com.homucafe.app;

import com.homucafe.entity.HoaDon;
import com.homucafe.entity.NhanVien;
import com.homucafe.entity.TaiKhoan;
import com.homucafe.shared.ui.Theme;
import com.homucafe.util.DataStore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Màn hình 7: Quản lý nhân sự, ca làm & phân quyền tài khoản (THU_NGAN, PHA_CHE, QUAN_LY),
 * kèm tính năng Đối soát két tiền mặt kết ca tự động.
 */
public class PanelQuanLyNhanSu extends JPanel {

    public PanelQuanLyNhanSu() {
        setLayout(new BorderLayout());
        setOpaque(false);
        initUI();
    }

    private void initUI() {
        JPanel page = Theme.page("Nhân sự & ca làm", "Cùng nhau vận hành Homu.",
                "Hồ sơ nhân viên, phân quyền 3 vai trò (Thu ngân, Pha chế, Quản lý) & Đối soát tiền mặt kết ca.");
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        String[] cols = {"MÃ NV", "HỌ TÊN", "CHỨC VỤ", "SỐ ĐIỆN THOẠI", "CĂN CƯỚC CÔNG DÂN", "TÀI KHOẢN", "TRẠNG THÁI"};
        final boolean[] updatingStaffFromStore = {false};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 1 && column <= 4;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column);
                if (updatingStaffFromStore[0]) return;
                try {
                    String ma = (String) getValueAt(row, 0);
                    for (NhanVien nv : DataStore.getDsNhanVien()) {
                        if (nv.getMaNV().equals(ma) && aValue != null) {
                            String text = aValue.toString().trim();
                            switch (column) {
                                case 1 -> nv.setHoTen(text);
                                case 2 -> nv.setChucVu(text);
                                case 3 -> nv.setSoDienThoai(text);
                                case 4 -> nv.setCccd(text);
                            }
                            DataStore.saveNhanVien(nv);
                            break;
                        }
                    }
                } catch (Exception ignored) { }
            }
        };
        JTable table = Theme.table(cols, new Object[0][]);
        table.setModel(model);

        Runnable loadStaff = () -> {
            updatingStaffFromStore[0] = true;
            model.setRowCount(0);
            for (NhanVien nv : DataStore.getDsNhanVien()) {
                TaiKhoan tk = DataStore.timTaiKhoanTheoMaNV(nv.getMaNV());
                String tkInfo = tk != null ? tk.getTenDangNhap() + " (" + tk.getPhanQuyen() + ")" : "Chưa cấp";
                model.addRow(new Object[]{
                        nv.getMaNV(),
                        nv.getHoTen(),
                        nv.getChucVu(),
                        nv.getSoDienThoai(),
                        nv.getCccd(),
                        tkInfo,
                        nv.isDangLamViec() ? "Đang sử dụng" : "Không sử dụng"
                });
            }
            updatingStaffFromStore[0] = false;
        };
        DataStore.addDataChangeListener(loadStaff);
        loadStaff.run();

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 6) {
                    String ma = (String) model.getValueAt(row, 0);
                    for (NhanVien nv : DataStore.getDsNhanVien()) {
                        if (nv.getMaNV().equals(ma)) {
                            nv.setDangLamViec(!nv.isDangLamViec());
                            DataStore.saveNhanVien(nv);
                            break;
                        }
                    }
                }
            }
        });

        JButton btnThemNV = Theme.button("+ Thêm nhân viên mới", true);
        JButton btnCapTaiKhoan = Theme.button("Cấp / Đổi tài khoản", false);
        JButton btnToggleNV = Theme.button("Đổi trạng thái sử dụng", false);
        JButton btnDoiSoatCa = Theme.button("Đối soát két tiền kết ca", false);

        btnCapTaiKhoan.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(page, "Vui lòng chọn 1 nhân viên trên bảng để cấp hoặc đổi tài khoản!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String maNV = (String) table.getValueAt(row, 0);
            NhanVien targetNV = null;
            for (NhanVien nv : DataStore.getDsNhanVien()) {
                if (nv.getMaNV().equals(maNV)) {
                    targetNV = nv;
                    break;
                }
            }
            if (targetNV == null) return;

            TaiKhoan existingTK = DataStore.timTaiKhoanTheoMaNV(maNV);
            JTextField txtUsername = new JTextField(existingTK != null ? existingTK.getTenDangNhap() : "");
            JPasswordField txtMatKhau = new JPasswordField(existingTK != null ? existingTK.getMatKhau() : "123456");
            JComboBox<String> cbQuyen = new JComboBox<>(new String[]{
                    "THU_NGAN (Nhân viên Thu ngân)",
                    "PHA_CHE (Nhân viên Pha chế)",
                    "QUAN_LY (Quản lý / Chủ quán)"
            });
            if (existingTK != null) {
                String pq = existingTK.getPhanQuyen();
                if ("ADMIN".equalsIgnoreCase(pq) || "QUAN_LY".equalsIgnoreCase(pq)) {
                    cbQuyen.setSelectedIndex(2);
                } else if ("PHA_CHE".equalsIgnoreCase(pq)) {
                    cbQuyen.setSelectedIndex(1);
                } else {
                    cbQuyen.setSelectedIndex(0);
                }
            } else if (targetNV.getChucVu() != null) {
                String cv = targetNV.getChucVu().toLowerCase();
                if (cv.contains("quản lý") || cv.contains("chủ")) {
                    cbQuyen.setSelectedIndex(2);
                } else if (cv.contains("pha chế") || cv.contains("bar")) {
                    cbQuyen.setSelectedIndex(1);
                } else {
                    cbQuyen.setSelectedIndex(0);
                }
            }

            JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
            form.add(new JLabel("Nhân viên:")); form.add(new JLabel(targetNV.getMaNV() + " - " + targetNV.getHoTen()));
            form.add(new JLabel("Tên đăng nhập (*):")); form.add(txtUsername);
            form.add(new JLabel("Mật khẩu (>= 6 ký tự):")); form.add(txtMatKhau);
            form.add(new JLabel("Vai trò phân quyền:")); form.add(cbQuyen);

            int opt = JOptionPane.showConfirmDialog(page, form, "Cấp / Cập nhật tài khoản đăng nhập", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                String user = txtUsername.getText().trim();
                String pass = new String(txtMatKhau.getPassword()).trim();
                if (user.isEmpty() || user.contains(" ")) {
                    JOptionPane.showMessageDialog(page, "Tên đăng nhập không được trống hoặc chứa khoảng trắng!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (pass.length() < 6) {
                    JOptionPane.showMessageDialog(page, "Mật khẩu phải có ít nhất 6 ký tự!", "Lỗi bảo mật", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                TaiKhoan trung = DataStore.timTaiKhoanTheoTen(user);
                if (trung != null && trung.getNhanVien() != null && !trung.getNhanVien().getMaNV().equalsIgnoreCase(maNV)) {
                    JOptionPane.showMessageDialog(page, "Tên đăng nhập '" + user + "' đã được cấp cho nhân viên khác!", "Trùng tên đăng nhập", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String phanQuyen = switch (cbQuyen.getSelectedIndex()) {
                    case 1 -> "PHA_CHE";
                    case 2 -> "QUAN_LY";
                    default -> "THU_NGAN";
                };
                DataStore.saveTaiKhoan(new TaiKhoan(user, pass, phanQuyen, targetNV));
                JOptionPane.showMessageDialog(page,
                        "Đã lưu tài khoản '" + user + "' (" + phanQuyen + ") cho nhân viên " + targetNV.getHoTen() + "!\nCó thể dùng để đăng nhập ngay.",
                        "Cấp tài khoản thành công", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnThemNV.addActionListener(e -> {
            JTextField txtTen = new JTextField();
            JTextField txtSDT = new JTextField();
            JTextField txtCCCD = new JTextField();
            JComboBox<String> cbChucVu = new JComboBox<>(new String[]{"Thu ngân", "Pha chế", "Quản lý"});

            JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
            form.add(new JLabel("Họ tên:")); form.add(txtTen);
            form.add(new JLabel("SĐT (10 số):")); form.add(txtSDT);
            form.add(new JLabel("CCCD (12 số):")); form.add(txtCCCD);
            form.add(new JLabel("Chức vụ:")); form.add(cbChucVu);

            int opt = JOptionPane.showConfirmDialog(page, form, "Thêm nhân viên mới", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                String sdt = txtSDT.getText().trim();
                String cccd = txtCCCD.getText().trim();
                String ten = txtTen.getText().trim();

                if (ten.isEmpty()) {
                    JOptionPane.showMessageDialog(page, "Vui lòng nhập họ tên nhân viên!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!sdt.matches("^0[3|5|7|8|9][0-9]{8}$")) {
                    JOptionPane.showMessageDialog(page, "Số điện thoại không hợp lệ! (Phải là số di động VN 10 chữ số)", "Lỗi Regex BR05", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!cccd.matches("^[0-9]{12}$")) {
                    JOptionPane.showMessageDialog(page, "Căn cước công dân không hợp lệ! (Phải gồm đúng 12 chữ số)", "Lỗi Regex BR05", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String maNV = DataStore.nextMaNhanVien();
                NhanVien nvMoi = new NhanVien(maNV, ten, sdt, cccd, (String) cbChucVu.getSelectedItem(), true);
                DataStore.themNhanVienVaoDB(nvMoi);
                JOptionPane.showMessageDialog(page, "Đã thêm nhân viên [" + maNV + "] " + ten + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnToggleNV.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(page, "Vui lòng chọn 1 nhân viên từ bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String ma = (String) table.getValueAt(row, 0);
            for (NhanVien nv : DataStore.getDsNhanVien()) {
                if (nv.getMaNV().equals(ma)) {
                    nv.setDangLamViec(!nv.isDangLamViec());
                    DataStore.saveNhanVien(nv);
                    break;
                }
            }
        });

        btnDoiSoatCa.addActionListener(e -> {
            LocalDate activeDate = DataStore.getActiveBusinessDate();
            double tongTienMat = 0;
            double tongCK = 0;
            int soDon = 0;
            for (HoaDon hd : DataStore.getDsHoaDon()) {
                boolean sameDay = hd.getNgayLap() != null && hd.getNgayLap().toLocalDate().isEqual(activeDate);
                if (!"DA_HUY".equals(hd.getTrangThaiDon()) && sameDay) {
                    soDon++;
                    if (DataStore.isTienMat(hd)) {
                        tongTienMat += hd.getTongTien();
                    } else {
                        tongCK += hd.getTongTien();
                    }
                }
            }

            String ngayStr = activeDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            JTextField txtDauCa = new JTextField("1000000");
            JTextField txtChiCa = new JTextField("0");
            JTextField txtThucDem = new JTextField(String.format("%.0f", 1000000 + tongTienMat));

            JPanel pnl = new JPanel(new GridLayout(0, 2, 10, 10));
            pnl.add(new JLabel("Ngày đối soát:")); pnl.add(new JLabel(ngayStr));
            pnl.add(new JLabel("Tiền mặt đầu ca (VND):")); pnl.add(txtDauCa);
            pnl.add(new JLabel("Tiền chi trong ca (mua vặt...):")); pnl.add(txtChiCa);
            pnl.add(new JLabel("Doanh thu Tiền mặt trong ngày:")); pnl.add(new JLabel(String.format("%,.0f đ (%d đơn)", tongTienMat, soDon)));
            pnl.add(new JLabel("Doanh thu Chuyển khoản (VietQR):")); pnl.add(new JLabel(String.format("%,.0f đ", tongCK)));
            pnl.add(new JLabel("Tiền mặt THỰC ĐẾM cuối ca:")); pnl.add(txtThucDem);

            int opt = JOptionPane.showConfirmDialog(page, pnl, "Đối soát két tiền kết ca (" + ngayStr + ") - HomuCafe", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                double dauCa = DataStore.parseCurrency(txtDauCa.getText());
                double chiCa = DataStore.parseCurrency(txtChiCa.getText());
                double thucDem = DataStore.parseCurrency(txtThucDem.getText());
                double lyThuyet = dauCa + tongTienMat - chiCa;
                double chenhLech = thucDem - lyThuyet;

                String ketQua = String.format(
                        "BIÊN BẢN ĐỐI SOÁT TIỀN MẶT KẾT CA (%s)\n" +
                        "========================================\n" +
                        "• Tiền đầu ca:           %,12.0f đ\n" +
                        "• Thu tiền mặt trong ca:+%,12.0f đ\n" +
                        "• Chi phí trong ca:     -%,12.0f đ\n" +
                        "----------------------------------------\n" +
                        "• TIỀN MẶT LÝ THUYẾT:    %,12.0f đ\n" +
                        "• TIỀN MẶT THỰC ĐẾM:     %,12.0f đ\n" +
                        "========================================\n" +
                        "• ĐỘ LỆCH (Thừa / Thiếu):%,12.0f đ (%s)\n",
                        ngayStr, dauCa, tongTienMat, chiCa, lyThuyet, thucDem,
                        chenhLech, chenhLech == 0 ? "Khớp hoàn hảo 100%" : (chenhLech > 0 ? "Thừa tiền két" : "Thiếu tiền két")
                );
                JOptionPane.showMessageDialog(page, ketQua, "Kết quả đối soát ca", chenhLech == 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
            }
        });

        body.add(Theme.row(btnThemNV, btnCapTaiKhoan, btnToggleNV, btnDoiSoatCa), BorderLayout.NORTH);
        body.add(Theme.tablePanel("Danh sách nhân sự (Nhấp đúp ô để sửa trực tiếp · Nhấp cột Trạng thái để bật/tắt)", table), BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);
    }
}
