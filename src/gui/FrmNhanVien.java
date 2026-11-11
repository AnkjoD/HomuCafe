package gui;

import entity.NhanVien;
import entity.TaiKhoan;
import util.DataStore;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Màn hình Quản lý Nhân sự & Phân quyền (FrmNhanVien - HomuCafe)
 * Kiểm tra chuẩn biểu thức chính quy Regex: SĐT 10 số (^0[3|5|7|8|9][0-9]{8}$), CCCD 12 số (^[0-9]{12}$).
 */
public class FrmNhanVien extends JPanel {
    private JTextField txtMaNV;
    private JTextField txtHoTen;
    private JTextField txtSDT;
    private JTextField txtCCCD;
    private JComboBox<String> cboChucVu;
    private JRadioButton radDangLam;
    private JRadioButton radDaNghi;

    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JComboBox<String> cboPhanQuyen;

    private DefaultTableModel modelNV;
    private JTable tblNV;

    public FrmNhanVien() {
        setLayout(new BorderLayout(18, 0));
        setBackground(UITheme.HOMU_BG_CREAM);
        setBorder(new EmptyBorder(14, 18, 18, 18));

        initComponents();
        napBangNhanVien();
        if (tblNV.getRowCount() > 0) {
            tblNV.setRowSelectionInterval(0, 0);
            hienThiLenForm();
        }
    }

    private void initComponents() {
        // Cột trái: Hồ sơ nhân viên
        JPanel leftCard = new UITheme.RoundedPanel(16, Color.WHITE, UITheme.HOMU_BORDER, 1);
        leftCard.setPreferredSize(new Dimension(420, 0));
        leftCard.setLayout(new BorderLayout(0, 12));
        leftCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblFormTitle = new JLabel("HỒ SƠ NHÂN SỰ & CẤP TÀI KHOẢN");
        lblFormTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblFormTitle.setForeground(UITheme.HOMU_TEXT);

        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        txtMaNV = UITheme.createStyledTextField("NV01");
        txtHoTen = UITheme.createStyledTextField("Trần Văn An");
        txtSDT = UITheme.createStyledTextField("0901234567");
        txtCCCD = UITheme.createStyledTextField("079204001234");

        cboChucVu = new JComboBox<>(new String[]{"Thu ngân", "Pha chế", "Quản lý"});
        cboChucVu.setFont(UITheme.FONT_REGULAR);
        cboChucVu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cboChucVu.setAlignmentX(Component.LEFT_ALIGNMENT);

        addField(formPanel, "Mã nhân viên (VD: NV01):", txtMaNV);
        addField(formPanel, "Họ và tên nhân viên:", txtHoTen);
        addField(formPanel, "Số điện thoại (Regex 10 số):", txtSDT);
        addField(formPanel, "Số CCCD (Regex 12 chữ số):", txtCCCD);
        addComp(formPanel, "Chức vụ công tác:", cboChucVu);

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        statusRow.setOpaque(false);
        statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblSt = new JLabel("Trạng thái:");
        lblSt.setFont(UITheme.FONT_BOLD);
        lblSt.setForeground(UITheme.HOMU_TEXT);

        radDangLam = new JRadioButton("Đang làm việc", true);
        radDaNghi = new JRadioButton("Đã nghỉ việc");
        radDangLam.setOpaque(false);
        radDaNghi.setOpaque(false);
        radDangLam.setForeground(UITheme.HOMU_TEXT);
        radDaNghi.setForeground(UITheme.HOMU_TEXT);

        ButtonGroup bg = new ButtonGroup();
        bg.add(radDangLam);
        bg.add(radDaNghi);
        statusRow.add(lblSt);
        statusRow.add(radDangLam);
        statusRow.add(radDaNghi);
        formPanel.add(statusRow);
        formPanel.add(Box.createVerticalStrut(8));

        // Khung cấp tài khoản (UC18)
        JPanel accBox = new UITheme.RoundedPanel(12, UITheme.HOMU_LIGHT_PANEL, UITheme.HOMU_BORDER, 1);
        accBox.setLayout(new BoxLayout(accBox, BoxLayout.Y_AXIS));
        accBox.setBorder(new EmptyBorder(10, 12, 10, 12));
        accBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAccTitle = new JLabel("TÀI KHOẢN ĐĂNG NHẬP & PHÂN QUYỀN (UC18)");
        lblAccTitle.setFont(UITheme.FONT_BOLD);
        lblAccTitle.setForeground(UITheme.HOMU_CARAMEL);
        lblAccTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        accBox.add(lblAccTitle);
        accBox.add(Box.createVerticalStrut(6));

        txtTenDangNhap = UITheme.createStyledTextField("thungan01");
        txtMatKhau = new JPasswordField("123456");
        txtMatKhau.setBorder(new UITheme.RoundedBorder(UITheme.HOMU_BORDER, 8, 1, new Insets(6, 10, 6, 10)));
        txtMatKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtMatKhau.setAlignmentX(Component.LEFT_ALIGNMENT);

        cboPhanQuyen = new JComboBox<>(new String[]{"STAFF", "ADMIN"});
        cboPhanQuyen.setFont(UITheme.FONT_BOLD);
        cboPhanQuyen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cboPhanQuyen.setAlignmentX(Component.LEFT_ALIGNMENT);

        addField(accBox, "Tên đăng nhập:", txtTenDangNhap);
        addComp(accBox, "Mật khẩu:", txtMatKhau);
        addComp(accBox, "Phân quyền (ADMIN / STAFF):", cboPhanQuyen);

        formPanel.add(accBox);

        JPanel btnGroup = new JPanel(new GridLayout(1, 3, 10, 0));
        btnGroup.setOpaque(false);
        btnGroup.setPreferredSize(new Dimension(0, 42));

        JButton btnThem = UITheme.createRoundedButton("Thêm NV", UITheme.HOMU_GREEN, Color.WHITE, 8);
        JButton btnSua = UITheme.createRoundedButton("Cập nhật", UITheme.HOMU_CARAMEL, Color.WHITE, 8);
        JButton btnKhoa = UITheme.createRoundedButton("Khóa / Mở", UITheme.COLOR_DANGER, Color.WHITE, 8);

        btnThem.addActionListener(e -> xuLyThemNhanVien());
        btnSua.addActionListener(e -> xuLyCapNhatNhanVien());
        btnKhoa.addActionListener(e -> xuLyDoiTrangThaiLamViec());

        btnGroup.add(btnThem);
        btnGroup.add(btnSua);
        btnGroup.add(btnKhoa);

        leftCard.add(lblFormTitle, BorderLayout.NORTH);
        leftCard.add(formPanel, BorderLayout.CENTER);
        leftCard.add(btnGroup, BorderLayout.SOUTH);

        // Cột phải: Danh sách nhân viên
        JPanel rightCard = new UITheme.RoundedPanel(16, Color.WHITE, UITheme.HOMU_BORDER, 1);
        rightCard.setLayout(new BorderLayout(0, 12));
        rightCard.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel headerRight = new JPanel(new BorderLayout());
        headerRight.setOpaque(false);
        JLabel lblListTitle = new JLabel("DANH SÁCH NHÂN SỰ & TÀI KHOẢN HỆ THỐNG");
        lblListTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblListTitle.setForeground(UITheme.HOMU_TEXT);

        JButton btnClear = UITheme.createRoundedButton("Tạo mã NV mới", UITheme.HOMU_LIGHT_PANEL, UITheme.HOMU_TEXT, 8);
        btnClear.addActionListener(e -> xoaTrangForm());
        headerRight.add(lblListTitle, BorderLayout.WEST);
        headerRight.add(btnClear, BorderLayout.EAST);

        String[] cols = {"Mã NV", "Họ và tên", "Số điện thoại", "Số CCCD", "Chức vụ", "Tài khoản", "Quyền", "Trạng thái"};
        modelNV = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblNV = new JTable(modelNV);
        tblNV.setRowHeight(40);
        tblNV.setFont(UITheme.FONT_REGULAR);
        tblNV.setGridColor(UITheme.HOMU_LIGHT_PANEL);
        tblNV.setShowVerticalLines(false);
        tblNV.setSelectionBackground(new Color(244, 239, 230));
        tblNV.setSelectionForeground(UITheme.HOMU_TEXT);
        tblNV.getTableHeader().setFont(UITheme.FONT_BOLD);
        tblNV.getTableHeader().setBackground(UITheme.HOMU_LIGHT_PANEL);
        tblNV.getTableHeader().setForeground(UITheme.HOMU_TEXT);
        tblNV.getTableHeader().setPreferredSize(new Dimension(0, 36));

        tblNV.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(UITheme.FONT_BOLD);
                c.setForeground("ADMIN".equals(value) ? new Color(124, 58, 237) : UITheme.HOMU_CARAMEL);
                return c;
            }
        });

        tblNV.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(UITheme.FONT_BOLD);
                c.setForeground("Đang làm".equals(value) ? UITheme.HOMU_GREEN : UITheme.COLOR_DANGER);
                return c;
            }
        });

        tblNV.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) hienThiLenForm();
        });

        JScrollPane scroll = new JScrollPane(tblNV);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.HOMU_BORDER));

        rightCard.add(headerRight, BorderLayout.NORTH);
        rightCard.add(scroll, BorderLayout.CENTER);

        add(leftCard, BorderLayout.WEST);
        add(rightCard, BorderLayout.CENTER);
    }

    private void addField(JPanel p, String label, JTextField tf) {
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        addComp(p, label, tf);
    }

    private void addComp(JPanel p, String label, JComponent comp) {
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.HOMU_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(3));
        p.add(comp);
        p.add(Box.createVerticalStrut(7));
    }

    private TaiKhoan timTaiKhoanCuaNV(NhanVien nv) {
        for (TaiKhoan tk : DataStore.getDsTaiKhoan()) {
            if (tk.getNhanVien() != null && tk.getNhanVien().getMaNV().equalsIgnoreCase(nv.getMaNV())) {
                return tk;
            }
        }
        return null;
    }

    private void napBangNhanVien() {
        modelNV.setRowCount(0);
        for (NhanVien nv : DataStore.getDsNhanVien()) {
            TaiKhoan tk = timTaiKhoanCuaNV(nv);
            modelNV.addRow(new Object[]{
                    nv.getMaNV(),
                    nv.getHoTen(),
                    nv.getSoDienThoai(),
                    nv.getCccd(),
                    nv.getChucVu(),
                    tk != null ? tk.getTenDangNhap() : "(Chưa cấp)",
                    tk != null ? tk.getPhanQuyen() : "-",
                    nv.isTrangThaiLamViec() ? "Đang làm" : "Đã nghỉ"
            });
        }
    }

    private void hienThiLenForm() {
        int row = tblNV.getSelectedRow();
        if (row < 0 || row >= DataStore.getDsNhanVien().size()) return;
        NhanVien nv = DataStore.getDsNhanVien().get(row);
        txtMaNV.setText(nv.getMaNV());
        txtHoTen.setText(nv.getHoTen());
        txtSDT.setText(nv.getSoDienThoai());
        txtCCCD.setText(nv.getCccd());
        cboChucVu.setSelectedItem(nv.getChucVu());
        if (nv.isTrangThaiLamViec()) radDangLam.setSelected(true);
        else radDaNghi.setSelected(true);

        TaiKhoan tk = timTaiKhoanCuaNV(nv);
        if (tk != null) {
            txtTenDangNhap.setText(tk.getTenDangNhap());
            txtMatKhau.setText(tk.getMatKhau());
            cboPhanQuyen.setSelectedItem(tk.getPhanQuyen());
        } else {
            txtTenDangNhap.setText("");
            txtMatKhau.setText("123456");
            cboPhanQuyen.setSelectedItem("STAFF");
        }
    }

    private void xoaTrangForm() {
        txtMaNV.setText(String.format("NV%02d", DataStore.getDsNhanVien().size() + 1));
        txtHoTen.setText("");
        txtSDT.setText("");
        txtCCCD.setText("");
        cboChucVu.setSelectedIndex(0);
        radDangLam.setSelected(true);
        txtTenDangNhap.setText("");
        txtMatKhau.setText("123456");
        cboPhanQuyen.setSelectedItem("STAFF");
        tblNV.clearSelection();
        txtHoTen.requestFocus();
    }

    private boolean kiemTraHopLeRegex() {
        if (txtMaNV.getText().trim().isEmpty() || txtHoTen.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã nhân viên và Họ tên không được để trống!");
            return false;
        }
        String sdt = txtSDT.getText().trim();
        if (!sdt.matches("^0[35789][0-9]{8}$")) {
            JOptionPane.showMessageDialog(this,
                    "Số điện thoại không hợp lệ (BR05)!\nPhải gồm đúng 10 chữ số chuẩn Việt Nam (^0[3|5|7|8|9][0-9]{8}$).",
                    "Lỗi kiểm tra Regex SĐT", JOptionPane.ERROR_MESSAGE);
            txtSDT.requestFocus();
            return false;
        }
        String cccd = txtCCCD.getText().trim();
        if (!cccd.matches("^[0-9]{12}$")) {
            JOptionPane.showMessageDialog(this,
                    "Căn cước công dân không hợp lệ (BR05)!\nPhải gồm đúng 12 chữ số (^[0-9]{12}$).",
                    "Lỗi kiểm tra Regex CCCD", JOptionPane.ERROR_MESSAGE);
            txtCCCD.requestFocus();
            return false;
        }
        return true;
    }

    private void xuLyThemNhanVien() {
        if (!kiemTraHopLeRegex()) return;
        String ma = txtMaNV.getText().trim().toUpperCase();
        for (NhanVien n : DataStore.getDsNhanVien()) {
            if (n.getMaNV().equalsIgnoreCase(ma)) {
                JOptionPane.showMessageDialog(this, "Mã nhân viên '" + ma + "' đã tồn tại!");
                return;
            }
        }
        NhanVien nvMoi = new NhanVien(
                ma,
                txtHoTen.getText().trim(),
                txtSDT.getText().trim(),
                txtCCCD.getText().trim(),
                String.valueOf(cboChucVu.getSelectedItem()),
                radDangLam.isSelected()
        );
        DataStore.getDsNhanVien().add(nvMoi);

        String user = txtTenDangNhap.getText().trim();
        if (!user.isEmpty()) {
            TaiKhoan tkMoi = new TaiKhoan(user, new String(txtMatKhau.getPassword()),
                    String.valueOf(cboPhanQuyen.getSelectedItem()), nvMoi);
            DataStore.getDsTaiKhoan().add(tkMoi);
        }

        napBangNhanVien();
        JOptionPane.showMessageDialog(this, "Đã thêm hồ sơ nhân sự HomuCafe thành công!");
    }

    private void xuLyCapNhatNhanVien() {
        if (!kiemTraHopLeRegex()) return;
        String ma = txtMaNV.getText().trim();
        for (NhanVien nv : DataStore.getDsNhanVien()) {
            if (nv.getMaNV().equalsIgnoreCase(ma)) {
                nv.setHoTen(txtHoTen.getText().trim());
                nv.setSoDienThoai(txtSDT.getText().trim());
                nv.setCccd(txtCCCD.getText().trim());
                nv.setChucVu(String.valueOf(cboChucVu.getSelectedItem()));
                nv.setTrangThaiLamViec(radDangLam.isSelected());

                TaiKhoan tk = timTaiKhoanCuaNV(nv);
                String user = txtTenDangNhap.getText().trim();
                if (!user.isEmpty()) {
                    if (tk == null) {
                        DataStore.getDsTaiKhoan().add(new TaiKhoan(user,
                                new String(txtMatKhau.getPassword()),
                                String.valueOf(cboPhanQuyen.getSelectedItem()), nv));
                    } else {
                        tk.setTenDangNhap(user);
                        tk.setMatKhau(new String(txtMatKhau.getPassword()));
                        tk.setPhanQuyen(String.valueOf(cboPhanQuyen.getSelectedItem()));
                    }
                }
                napBangNhanVien();
                JOptionPane.showMessageDialog(this, "Đã cập nhật hồ sơ & tài khoản nhân viên!");
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Không tìm thấy mã nhân viên '" + ma + "'!");
    }

    private void xuLyDoiTrangThaiLamViec() {
        int row = tblNV.getSelectedRow();
        if (row < 0 || row >= DataStore.getDsNhanVien().size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên trong bảng!");
            return;
        }
        NhanVien nv = DataStore.getDsNhanVien().get(row);
        nv.setTrangThaiLamViec(!nv.isTrangThaiLamViec());
        napBangNhanVien();
        hienThiLenForm();
        JOptionPane.showMessageDialog(this,
                "Đã chuyển trạng thái làm việc của '" + nv.getHoTen() + "' sang: "
                        + (nv.isTrangThaiLamViec() ? "Đang làm việc" : "Đã nghỉ việc (Khóa tài khoản)"));
    }
}
