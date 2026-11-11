package gui;

import entity.KichCo;
import entity.LoaiMon;
import entity.Mon;
import util.DataStore;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Giao diện Quản lý Thực đơn và bảng giá theo Size (FrmQuanLyMon - HomuCafe)
 * Tông màu ấm cúng: Nền kem #FAF6EF, Thẻ viền #E6DEC8, Nút xanh Matcha #536B4F & Caramel #826343.
 */
public class FrmQuanLyMon extends JPanel {
    private JTextField txtMaMon;
    private JTextField txtTenMon;
    private JComboBox<LoaiMon> cboLoaiMon;
    private JTextField txtGiaSizeS;
    private JTextField txtPhuThuM;
    private JTextField txtPhuThuL;
    private JTextField txtHinhAnh;
    private JRadioButton radDangBan;
    private JRadioButton radTamNgung;

    private DefaultTableModel modelMon;
    private JTable tblMon;

    public FrmQuanLyMon() {
        setLayout(new BorderLayout(18, 0));
        setBackground(UITheme.HOMU_BG_CREAM);
        setBorder(new EmptyBorder(14, 18, 18, 18));

        initComponents();
        napDuLieuBangMon();

        if (tblMon.getRowCount() > 3) {
            tblMon.setRowSelectionInterval(3, 3);
            hienThiMonDangChonLenForm();
        }
    }

    private void initComponents() {
        // Cột trái: Form thông tin món
        JPanel leftCard = new UITheme.RoundedPanel(16, Color.WHITE, UITheme.HOMU_BORDER, 1);
        leftCard.setPreferredSize(new Dimension(410, 0));
        leftCard.setLayout(new BorderLayout(0, 12));
        leftCard.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel lblFormTitle = new JLabel("THÔNG TIN MÓN ĐỒ UỐNG");
        lblFormTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblFormTitle.setForeground(UITheme.HOMU_TEXT);

        JPanel formFields = new JPanel();
        formFields.setOpaque(false);
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));

        txtMaMon = UITheme.createStyledTextField("TEA01");
        txtTenMon = UITheme.createStyledTextField("Trà Đào Cam Sả");

        JPanel loaiMonRow = new JPanel(new BorderLayout(6, 0));
        loaiMonRow.setOpaque(false);
        loaiMonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        loaiMonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        cboLoaiMon = new JComboBox<>();
        cboLoaiMon.setFont(UITheme.FONT_REGULAR);
        napComboBoxLoaiMon();

        JButton btnQLLoai = UITheme.createRoundedButton("+ Loại món (UC14)", UITheme.HOMU_LIGHT_PANEL, UITheme.HOMU_TEXT, 8);
        btnQLLoai.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnQLLoai.setBorder(new EmptyBorder(6, 10, 6, 10));
        btnQLLoai.addActionListener(e -> moHopThoaiQuanLyLoaiMon());

        loaiMonRow.add(cboLoaiMon, BorderLayout.CENTER);
        loaiMonRow.add(btnQLLoai, BorderLayout.EAST);

        txtGiaSizeS = UITheme.createStyledTextField("45000");
        txtPhuThuM = UITheme.createStyledTextField("+ 6.000 đ");
        txtPhuThuM.setEditable(false);
        txtPhuThuL = UITheme.createStyledTextField("+ 12.000 đ");
        txtPhuThuL.setEditable(false);

        JPanel anhRow = new JPanel(new BorderLayout(6, 0));
        anhRow.setOpaque(false);
        anhRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        anhRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtHinhAnh = UITheme.createStyledTextField("images/tra_dao_cam_sa.jpg");
        JButton btnChonAnh = UITheme.createRoundedButton("Chọn...", UITheme.HOMU_LIGHT_PANEL, UITheme.HOMU_TEXT, 8);
        btnChonAnh.setFont(UITheme.FONT_SMALL);
        btnChonAnh.setBorder(new EmptyBorder(6, 10, 6, 10));
        btnChonAnh.addActionListener(e -> chonFileAnh());
        anhRow.add(txtHinhAnh, BorderLayout.CENTER);
        anhRow.add(btnChonAnh, BorderLayout.EAST);

        addField(formFields, "Mã món:", txtMaMon);
        addField(formFields, "Tên món đồ uống:", txtTenMon);
        addFieldComponent(formFields, "Loại món:", loaiMonRow);
        addField(formFields, "Đơn giá gốc (Size S):", txtGiaSizeS);
        addField(formFields, "Phụ thu Size M:", txtPhuThuM);
        addField(formFields, "Phụ thu Size L:", txtPhuThuL);
        addFieldComponent(formFields, "Đường dẫn ảnh:", anhRow);

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        statusRow.setOpaque(false);
        statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblStatus = new JLabel("Trạng thái kinh doanh:");
        lblStatus.setFont(UITheme.FONT_BOLD);
        lblStatus.setForeground(UITheme.HOMU_TEXT);

        radDangBan = new JRadioButton("Đang bán", true);
        radTamNgung = new JRadioButton("Tạm ngưng");
        radDangBan.setOpaque(false);
        radTamNgung.setOpaque(false);
        radDangBan.setFont(UITheme.FONT_REGULAR);
        radTamNgung.setFont(UITheme.FONT_REGULAR);
        radDangBan.setForeground(UITheme.HOMU_TEXT);
        radTamNgung.setForeground(UITheme.HOMU_TEXT);

        ButtonGroup bgSt = new ButtonGroup();
        bgSt.add(radDangBan);
        bgSt.add(radTamNgung);
        statusRow.add(lblStatus);
        statusRow.add(radDangBan);
        statusRow.add(radTamNgung);

        formFields.add(Box.createVerticalStrut(4));
        formFields.add(statusRow);

        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.setPreferredSize(new Dimension(0, 44));

        JButton btnThemMoi = UITheme.createRoundedButton("Thêm mới", UITheme.HOMU_GREEN, Color.WHITE, 8);
        JButton btnCapNhat = UITheme.createRoundedButton("Cập nhật", UITheme.HOMU_CARAMEL, Color.WHITE, 8);
        JButton btnXoaMem = UITheme.createRoundedButton("Xóa mềm", UITheme.COLOR_DANGER, Color.WHITE, 8);

        btnThemMoi.addActionListener(e -> xuLyThemMon());
        btnCapNhat.addActionListener(e -> xuLyCapNhatMon());
        btnXoaMem.addActionListener(e -> xuLyXoaMemMon());

        btnPanel.add(btnThemMoi);
        btnPanel.add(btnCapNhat);
        btnPanel.add(btnXoaMem);

        leftCard.add(lblFormTitle, BorderLayout.NORTH);
        leftCard.add(formFields, BorderLayout.CENTER);
        leftCard.add(btnPanel, BorderLayout.SOUTH);

        // Cột phải: Danh sách món
        JPanel rightCard = new UITheme.RoundedPanel(16, Color.WHITE, UITheme.HOMU_BORDER, 1);
        rightCard.setLayout(new BorderLayout(0, 12));
        rightCard.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel rightHeader = new JPanel(new BorderLayout());
        rightHeader.setOpaque(false);

        JLabel lblListTitle = new JLabel("DANH SÁCH MÓN ĐỒ UỐNG HOMUCAFE");
        lblListTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblListTitle.setForeground(UITheme.HOMU_TEXT);

        JButton btnLamMoiForm = UITheme.createRoundedButton("Làm trắng Form", UITheme.HOMU_LIGHT_PANEL, UITheme.HOMU_TEXT, 8);
        btnLamMoiForm.setFont(UITheme.FONT_SMALL);
        btnLamMoiForm.addActionListener(e -> xoaTrangForm());

        rightHeader.add(lblListTitle, BorderLayout.WEST);
        rightHeader.add(btnLamMoiForm, BorderLayout.EAST);

        String[] cols = {"Mã", "Tên món đồ uống", "Loại món", "Giá Size S", "Giá Size M", "Giá Size L", "Trạng thái"};
        modelMon = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblMon = new JTable(modelMon);
        tblMon.setRowHeight(40);
        tblMon.setFont(UITheme.FONT_REGULAR);
        tblMon.setGridColor(UITheme.HOMU_LIGHT_PANEL);
        tblMon.setShowVerticalLines(false);
        tblMon.setSelectionBackground(new Color(244, 239, 230));
        tblMon.setSelectionForeground(UITheme.HOMU_TEXT);
        tblMon.getTableHeader().setFont(UITheme.FONT_BOLD);
        tblMon.getTableHeader().setBackground(UITheme.HOMU_LIGHT_PANEL);
        tblMon.getTableHeader().setForeground(UITheme.HOMU_TEXT);
        tblMon.getTableHeader().setPreferredSize(new Dimension(0, 36));

        tblMon.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(UITheme.FONT_BOLD);
                c.setForeground(UITheme.HOMU_TEXT);
                return c;
            }
        });

        DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(UITheme.HOMU_CARAMEL);
                setFont(UITheme.FONT_BOLD);
                return c;
            }
        };
        tblMon.getColumnModel().getColumn(3).setCellRenderer(priceRenderer);
        tblMon.getColumnModel().getColumn(4).setCellRenderer(priceRenderer);
        tblMon.getColumnModel().getColumn(5).setCellRenderer(priceRenderer);

        tblMon.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String st = String.valueOf(value);
                setFont(UITheme.FONT_BOLD);
                c.setForeground("Đang bán".equals(st) ? UITheme.HOMU_GREEN : UITheme.COLOR_DANGER);
                return c;
            }
        });

        tblMon.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienThiMonDangChonLenForm();
            }
        });

        JScrollPane scroll = new JScrollPane(tblMon);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.HOMU_BORDER));

        rightCard.add(rightHeader, BorderLayout.NORTH);
        rightCard.add(scroll, BorderLayout.CENTER);

        add(leftCard, BorderLayout.WEST);
        add(rightCard, BorderLayout.CENTER);
    }

    private void addField(JPanel container, String labelText, JTextField tf) {
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        addFieldComponent(container, labelText, tf);
    }

    private void addFieldComponent(JPanel container, String labelText, JComponent comp) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_REGULAR);
        lbl.setForeground(UITheme.HOMU_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(lbl);
        container.add(Box.createVerticalStrut(4));
        container.add(comp);
        container.add(Box.createVerticalStrut(10));
    }

    private void napComboBoxLoaiMon() {
        cboLoaiMon.removeAllItems();
        for (LoaiMon lm : DataStore.getDsLoaiMon()) {
            cboLoaiMon.addItem(lm);
        }
    }

    private void napDuLieuBangMon() {
        modelMon.setRowCount(0);
        for (Mon m : DataStore.getDsMon()) {
            modelMon.addRow(new Object[]{
                    m.getMaMon(),
                    m.getTenMon(),
                    m.getLoaiMon() != null ? m.getLoaiMon().getTenLoai() : "",
                    UITheme.formatMoney(m.tinhGiaTheoSize(KichCo.S)),
                    UITheme.formatMoney(m.tinhGiaTheoSize(KichCo.M)),
                    UITheme.formatMoney(m.tinhGiaTheoSize(KichCo.L)),
                    m.isTrangThai() ? "Đang bán" : "Tạm ngưng"
            });
        }
    }

    private void hienThiMonDangChonLenForm() {
        int row = tblMon.getSelectedRow();
        if (row < 0 || row >= DataStore.getDsMon().size()) return;
        Mon m = DataStore.getDsMon().get(row);
        txtMaMon.setText(m.getMaMon());
        txtTenMon.setText(m.getTenMon());
        if (m.getLoaiMon() != null) {
            cboLoaiMon.setSelectedItem(m.getLoaiMon());
        }
        txtGiaSizeS.setText(String.valueOf((long) m.getDonGiaGoc()));
        txtHinhAnh.setText(m.getHinhAnh() != null ? m.getHinhAnh() : "");
        if (m.isTrangThai()) radDangBan.setSelected(true);
        else radTamNgung.setSelected(true);
    }

    private void xoaTrangForm() {
        txtMaMon.setText("MON" + String.format("%02d", DataStore.getDsMon().size() + 1));
        txtTenMon.setText("");
        txtGiaSizeS.setText("35000");
        txtHinhAnh.setText("images/default_drink.jpg");
        radDangBan.setSelected(true);
        tblMon.clearSelection();
        txtTenMon.requestFocus();
    }

    private void chonFileAnh() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtHinhAnh.setText(chooser.getSelectedFile().getName());
        }
    }

    private void xuLyThemMon() {
        String ma = txtMaMon.getText().trim().toUpperCase();
        String ten = txtTenMon.getText().trim();
        if (ma.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã món và Tên món không được để trống!");
            return;
        }
        if (DataStore.timMonTheoMa(ma) != null) {
            JOptionPane.showMessageDialog(this, "Mã món '" + ma + "' đã tồn tại!",
                    "Trùng mã món", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            double giaS = Double.parseDouble(txtGiaSizeS.getText().replace(".", "").replace("đ", "").trim());
            if (giaS <= 0) throw new NumberFormatException();
            LoaiMon lm = (LoaiMon) cboLoaiMon.getSelectedItem();
            Mon monMoi = new Mon(ma, ten, giaS, txtHinhAnh.getText().trim(), radDangBan.isSelected(), lm);
            DataStore.getDsMon().add(monMoi);
            napDuLieuBangMon();
            DataStore.notifyDataChanged();
            JOptionPane.showMessageDialog(this, "Đã thêm món mới '" + ten + "' vào thực đơn HomuCafe!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Đơn giá gốc Size S phải là số dương (> 0)!", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyCapNhatMon() {
        String ma = txtMaMon.getText().trim();
        Mon mon = DataStore.timMonTheoMa(ma);
        if (mon == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy món có mã '" + ma + "' để cập nhật!");
            return;
        }
        try {
            double giaS = Double.parseDouble(txtGiaSizeS.getText().replace(".", "").replace("đ", "").trim());
            if (giaS <= 0) throw new NumberFormatException();
            mon.setTenMon(txtTenMon.getText().trim());
            mon.setDonGiaGoc(giaS);
            mon.setLoaiMon((LoaiMon) cboLoaiMon.getSelectedItem());
            mon.setHinhAnh(txtHinhAnh.getText().trim());
            mon.setTrangThai(radDangBan.isSelected());
            napDuLieuBangMon();
            DataStore.notifyDataChanged();
            JOptionPane.showMessageDialog(this, "Đã cập nhật thông tin & đơn giá cho món '" + mon.getTenMon() + "'!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Đơn giá gốc Size S không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyXoaMemMon() {
        String ma = txtMaMon.getText().trim();
        Mon mon = DataStore.timMonTheoMa(ma);
        if (mon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một món trong danh sách để thực hiện Xóa mềm!");
            return;
        }
        boolean trangThaiMoi = !mon.isTrangThai();
        mon.setTrangThai(trangThaiMoi);
        radDangBan.setSelected(trangThaiMoi);
        radTamNgung.setSelected(!trangThaiMoi);
        napDuLieuBangMon();
        DataStore.notifyDataChanged();

        String msg = trangThaiMoi
                ? "Đã MỞ BÁN LẠI món '" + mon.getTenMon() + "' trên quầy bán hàng!"
                : "Đã TẠM NGƯNG KINH DOANH (Xóa mềm UC16) món '" + mon.getTenMon() + "'.\nMón sẽ tạm ẩn trên màn hình POS nhưng vẫn bảo toàn lịch sử hóa đơn cũ.";
        JOptionPane.showMessageDialog(this, msg, "Xóa mềm / Mở bán (UC16)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void moHopThoaiQuanLyLoaiMon() {
        String tenLoaiMoi = JOptionPane.showInputDialog(this,
                "Nhập tên danh mục Loại món mới (VD: Trà nhiệt đới, Bánh ngọt...):",
                "Quản lý Danh mục Loại món (UC14)", JOptionPane.PLAIN_MESSAGE);
        if (tenLoaiMoi != null && !tenLoaiMoi.trim().isEmpty()) {
            String maLoai = String.format("LM%02d", DataStore.getDsLoaiMon().size() + 1);
            LoaiMon lm = new LoaiMon(maLoai, tenLoaiMoi.trim());
            DataStore.getDsLoaiMon().add(lm);
            napComboBoxLoaiMon();
            cboLoaiMon.setSelectedItem(lm);
            DataStore.notifyDataChanged();
            JOptionPane.showMessageDialog(this, "Đã thêm danh mục loại món: " + maLoai + " - " + tenLoaiMoi.trim());
        }
    }
}
