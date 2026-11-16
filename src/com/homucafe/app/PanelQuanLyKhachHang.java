package com.homucafe.app;

import com.homucafe.entity.KhachHang;
import com.homucafe.shared.ui.Theme;
import com.homucafe.util.DataStore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Màn hình 8: Quản lý khách hàng thân thiết & Hạng VIP (Bạc, Vàng, Kim Cương).
 * Tìm kiếm theo tên/SĐT/mã, Đăng ký thành viên mới (tặng điểm chào mừng),
 * sửa trực tiếp trên bảng và tự động quy đổi giá trị điểm thưởng & hạng thành viên.
 */
public class PanelQuanLyKhachHang extends JPanel {

    public PanelQuanLyKhachHang() {
        setLayout(new BorderLayout());
        setOpaque(false);
        initUI();
    }

    private void initUI() {
        JPanel page = Theme.page("Khách hàng quen", "Khách quen của Homu.",
                "Tra cứu lịch sử mua hàng, hạng thành viên VIP và tích lũy điểm thưởng. Nhấp đúp ô để sửa trực tiếp thông tin.");
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        String[] cols = {"MÃ KH", "HỌ TÊN KHÁCH HÀNG", "ĐIỆN THOẠI", "HẠNG THÀNH VIÊN", "ĐIỂM TÍCH LŨY", "GIÁ TRỊ QUY ĐỔI"};
        final boolean[] updatingCustFromStore = {false};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1 || column == 2 || column == 4;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column);
                if (updatingCustFromStore[0]) return;
                try {
                    String ma = (String) getValueAt(row, 0);
                    for (KhachHang kh : DataStore.getDsKhachHang()) {
                        if (kh.getMaKH().equals(ma) && aValue != null) {
                            String text = aValue.toString().trim();
                            switch (column) {
                                case 1 -> kh.setHoTen(text);
                                case 2 -> kh.setSoDienThoai(text);
                                case 4 -> {
                                    int pts = (int) DataStore.parseCurrency(text);
                                    kh.setDiemTichLuy(pts);
                                    updatingCustFromStore[0] = true;
                                    setValueAt(kh.getHangThanhVien(), row, 3);
                                    setValueAt(pts + " điểm", row, 4);
                                    setValueAt(String.format("%,.0f đ", (double) (pts * 1000)), row, 5);
                                    updatingCustFromStore[0] = false;
                                }
                            }
                            DataStore.saveKhachHang(kh);
                            break;
                        }
                    }
                } catch (Exception ignored) { }
            }
        };
        JTextField txtSearchCust = new JTextField();
        txtSearchCust.putClientProperty("JTextField.placeholderText", "Tìm khách hàng theo tên, SĐT hoặc mã KH...");
        txtSearchCust.putClientProperty("FlatLaf.style", "arc: 12; margin: 8,12,8,12");

        JTable table = Theme.table(cols, new Object[0][]);
        table.setModel(model);

        Runnable loadCust = () -> {
            updatingCustFromStore[0] = true;
            model.setRowCount(0);
            String q = DataStore.boDauTiengViet(txtSearchCust.getText().trim().toLowerCase());
            for (KhachHang kh : DataStore.getDsKhachHang()) {
                if (!q.isEmpty()) {
                    String s = DataStore.boDauTiengViet((kh.getMaKH() + " " + kh.getHoTen() + " " + kh.getSoDienThoai()).toLowerCase());
                    if (!s.contains(q)) continue;
                }
                model.addRow(new Object[]{
                        kh.getMaKH(),
                        kh.getHoTen(),
                        kh.getSoDienThoai(),
                        kh.getHangThanhVien(),
                        kh.getDiemTichLuy() + " điểm",
                        String.format("%,.0f đ", (double) (kh.getDiemTichLuy() * 1000))
                });
            }
            updatingCustFromStore[0] = false;
        };
        DataStore.addDataChangeListener(loadCust);
        loadCust.run();

        JButton btnThem = Theme.button("+ Đăng ký thành viên mới", true);
        btnThem.addActionListener(e -> {
            String ten = JOptionPane.showInputDialog(page, "Nhập họ tên khách hàng:");
            if (ten == null || ten.trim().isEmpty()) return;
            String sdt = JOptionPane.showInputDialog(page, "Nhập số điện thoại (10 số):");
            if (sdt != null && sdt.trim().matches("^0[3|5|7|8|9][0-9]{8}$")) {
                String ma = DataStore.nextMaKhachHang();
                KhachHang khMoi = new KhachHang(ma, ten.trim(), sdt.trim(), 10);
                DataStore.themKhachHangVaoDB(khMoi);
                JOptionPane.showMessageDialog(page,
                        "Đã tạo tài khoản thành viên [" + ma + "] " + ten.trim() + " (" + khMoi.getHangThanhVien() + ") thành công!\n(Tặng 10 điểm chào mừng)",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else if (sdt != null) {
                JOptionPane.showMessageDialog(page, "Số điện thoại không hợp lệ! (Phải gồm 10 chữ số di động VN)", "Lỗi Regex", JOptionPane.ERROR_MESSAGE);
            }
        });

        txtSearchCust.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadCust.run(); }
        });

        JPanel custToolbar = new JPanel(new BorderLayout(12, 0));
        custToolbar.setOpaque(false);
        custToolbar.add(txtSearchCust, BorderLayout.CENTER);
        custToolbar.add(btnThem, BorderLayout.EAST);

        body.add(custToolbar, BorderLayout.NORTH);
        body.add(Theme.tablePanel("Danh sách thành viên HomuCafe (Nhấp đúp ô để sửa trực tiếp)", table), BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);
    }
}
