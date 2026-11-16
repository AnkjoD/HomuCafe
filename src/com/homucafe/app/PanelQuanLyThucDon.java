package com.homucafe.app;

import com.homucafe.entity.LoaiMon;
import com.homucafe.entity.Mon;
import com.homucafe.shared.ui.Theme;
import com.homucafe.util.DataStore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Màn hình 6: Quản lý thực đơn món & Bảng giá.
 * Tra cứu theo tên/mã/gõ tắt, thêm món mới (tự sinh mã theo nhóm),
 * sửa trực tiếp trên bảng (chỉ Quản lý), và bật/tắt trạng thái sử dụng của món.
 */
public class PanelQuanLyThucDon extends JPanel {
    private boolean quanLyMode = true;
    private final JButton btnThem = Theme.button("+ Thêm món mới", true);
    private final JButton btnToggle = Theme.button("Đổi tình trạng sử dụng", false);

    public PanelQuanLyThucDon() {
        setLayout(new BorderLayout());
        setOpaque(false);
        initUI();
    }

    public void setQuanLyMode(boolean isQuanLy) {
        this.quanLyMode = isQuanLy;
        btnThem.setVisible(isQuanLy);
    }

    private void initUI() {
        JPanel page = Theme.page("Thực đơn quán", "Món ngon mỗi ngày.",
                "Quản lý danh mục đồ uống. Nhấp đúp vào ô để sửa trực tiếp · Nhấp cột Tình trạng để bật/tắt Đang sử dụng / Không sử dụng.");
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        String[] cols = {"MÃ MÓN", "TÊN MÓN ĐỒ UỐNG", "NHÓM PHÂN LOẠI", "GIÁ SIZE S", "GIÁ SIZE M", "GIÁ SIZE L", "TÌNH TRẠNG"};
        final boolean[] updatingMenuFromStore = {false};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return quanLyMode && column > 0 && column != 6;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column);
                if (updatingMenuFromStore[0]) return;
                try {
                    String ma = (String) getValueAt(row, 0);
                    Mon m = DataStore.timMonTheoMa(ma);
                    if (m != null && aValue != null) {
                        String text = aValue.toString().trim();
                        switch (column) {
                            case 1 -> m.setTenMon(text);
                            case 2 -> {
                                if (m.getLoaiMon() != null) {
                                    m.getLoaiMon().setTenLoai(text);
                                }
                            }
                            case 3 -> {
                                double gia = DataStore.parseCurrency(text);
                                if (gia > 0) {
                                    m.setDonGiaGoc(gia);
                                    updatingMenuFromStore[0] = true;
                                    setValueAt(String.format("%,.0f đ", gia), row, 3);
                                    setValueAt(String.format("%,.0f đ", gia + 6000), row, 4);
                                    setValueAt(String.format("%,.0f đ", gia + 12000), row, 5);
                                    updatingMenuFromStore[0] = false;
                                }
                            }
                        }
                        DataStore.saveMon(m);
                    }
                } catch (Exception ignored) { }
            }
        };
        JTextField txtSearchMenu = new JTextField();
        txtSearchMenu.putClientProperty("JTextField.placeholderText", "Tìm món theo tên, mã hoặc gõ tắt (cf, bx, td)...");
        txtSearchMenu.putClientProperty("FlatLaf.style", "arc: 12; margin: 8,12,8,12");

        JTable table = Theme.table(cols, new Object[0][]);
        table.setModel(model);

        Runnable loadMenu = () -> {
            updatingMenuFromStore[0] = true;
            model.setRowCount(0);
            String q = txtSearchMenu.getText();
            for (Mon m : DataStore.getDsMon()) {
                if (!DataStore.khopTimKiemMon(m, q)) continue;
                double g = m.getDonGiaGoc();
                model.addRow(new Object[]{
                        m.getMaMon(),
                        m.getTenMon(),
                        m.getLoaiMon() != null ? m.getLoaiMon().getTenLoai() : "Cà phê",
                        String.format("%,.0f đ", g),
                        String.format("%,.0f đ", g + 6000),
                        String.format("%,.0f đ", g + 12000),
                        m.isDangKinhDoanh() ? "Đang sử dụng" : "Không sử dụng"
                });
            }
            updatingMenuFromStore[0] = false;
        };
        DataStore.addDataChangeListener(loadMenu);
        loadMenu.run();

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 6) {
                    String ma = (String) model.getValueAt(row, 0);
                    Mon m = DataStore.timMonTheoMa(ma);
                    if (m != null) {
                        m.setDangKinhDoanh(!m.isDangKinhDoanh());
                        DataStore.saveMon(m);
                    }
                }
            }
        });

        btnThem.addActionListener(e -> {
            JTextField txtTenMon = new JTextField();
            JComboBox<LoaiMon> cbLoaiMon = new JComboBox<>();
            for (LoaiMon lm : DataStore.getDsLoaiMon()) {
                cbLoaiMon.addItem(lm);
            }
            JTextField txtGiaGoc = new JTextField("39000");
            JComboBox<String> cbMinhHoa = new JComboBox<>(new String[]{
                    "0 - Cà phê truyền thống",
                    "1 - Latte / Espresso",
                    "2 - Trà trái cây",
                    "3 - Trà sữa / Matcha",
                    "4 - Bánh ngọt / Cacao",
                    "5 - Bạc xỉu / Đá lớp"
            });

            JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
            form.add(new JLabel("Tên món đồ uống (*):")); form.add(txtTenMon);
            form.add(new JLabel("Nhóm phân loại:")); form.add(cbLoaiMon);
            form.add(new JLabel("Đơn giá gốc Size S (VND):")); form.add(txtGiaGoc);
            form.add(new JLabel("Kiểu minh họa ly:")); form.add(cbMinhHoa);

            int opt = JOptionPane.showConfirmDialog(page, form, "Thêm món mới vào thực đơn", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                String ten = txtTenMon.getText().trim();
                if (ten.isEmpty()) {
                    JOptionPane.showMessageDialog(page, "Vui lòng nhập tên món!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                double gia = DataStore.parseCurrency(txtGiaGoc.getText());
                if (gia <= 0) {
                    JOptionPane.showMessageDialog(page, "Đơn giá gốc phải lớn hơn 0 đ!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                LoaiMon loai = (LoaiMon) cbLoaiMon.getSelectedItem();
                if (loai == null) {
                    loai = !DataStore.getDsLoaiMon().isEmpty() ? DataStore.getDsLoaiMon().get(0) : new LoaiMon("LM01", "Cà phê");
                }
                String ma = DataStore.nextMaMon(loai.getTenLoai());
                String hinhAnh = String.valueOf(Math.max(0, cbMinhHoa.getSelectedIndex()));
                Mon newMon = new Mon(ma, ten, gia, hinhAnh, true, loai);
                DataStore.themMonVaoDB(newMon);
                JOptionPane.showMessageDialog(page, "Đã thêm món mới [" + ma + "] " + ten + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnToggle.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(page, "Vui lòng chọn 1 món từ bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String ma = (String) table.getValueAt(row, 0);
            Mon m = DataStore.timMonTheoMa(ma);
            if (m != null) {
                m.setDangKinhDoanh(!m.isDangKinhDoanh());
                DataStore.saveMon(m);
            }
        });

        txtSearchMenu.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadMenu.run(); }
        });

        JPanel menuToolbar = new JPanel(new BorderLayout(12, 0));
        menuToolbar.setOpaque(false);
        menuToolbar.add(txtSearchMenu, BorderLayout.CENTER);
        menuToolbar.add(Theme.row(btnThem, btnToggle), BorderLayout.EAST);

        body.add(menuToolbar, BorderLayout.NORTH);
        body.add(Theme.tablePanel("Bảng giá thực đơn HomuCafe (Nhấp đúp ô để sửa trực tiếp · Nhấp cột Tình trạng để bật/tắt)", table), BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);
    }
}
