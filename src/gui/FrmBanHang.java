package gui;

import entity.*;
import util.DataStore;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Giao diện Bán hàng tại quầy POS Counter 2 cột (FrmBanHang - HomuCafe)
 * Tông màu ấm cúng: Nền kem #FAF6EF, Card trắng viền #E6DEC8, Nút xanh matcha #536B4F & Caramel #826343.
 */
public class FrmBanHang extends JPanel {
    private JTextField txtTimNhanh;
    private JPanel pnlCategoryTabs;
    private JPanel pnlMenuGrid;
    private String selectedMaLoai = "ALL";

    private JLabel lblMaDonHang;
    private JRadioButton radTaiQuan;
    private JRadioButton radMangDi;
    private JTextField txtSoTheRung;
    private JLabel lblTrangThaiTheRung;
    private UITheme.RoundedPanel pnlTheRungBox;

    private DefaultTableModel cartModel;
    private JTable tblCart;
    private final List<ChiTietHoaDon> gioHang = new ArrayList<>();

    private JTextField txtSDTKhach;
    private JLabel lblThongTinKhach;
    private JCheckBox chkDungDiem;
    private KhachHang khachHangHienTai;

    private JLabel lblTongTienHangTitle;
    private JLabel lblTongTienHangVal;
    private JLabel lblChietKhauTitle;
    private JLabel lblChietKhauVal;
    private JLabel lblTongThanhToanVal;

    private String phuongThucDaChon = "TIEN_MAT";
    private boolean daXacNhanThuTien = true;
    private double tienKhachDua = 0;

    public FrmBanHang() {
        setLayout(new BorderLayout(14, 0));
        setBackground(UITheme.HOMU_BG_CREAM);
        setBorder(new EmptyBorder(12, 14, 14, 14));

        initComponents();
        napGioHangMauBanDau();
        dangKyPhimTatToanCuc();

        DataStore.addDataChangeListener(() -> {
            hienThiDanhMucVaMon();
            capNhatThongTinTheRung();
        });
    }

    private void initComponents() {
        // Cột trái: Thực đơn (~58%)
        JPanel leftCard = new UITheme.RoundedPanel(16, Color.WHITE, UITheme.HOMU_BORDER, 1);
        leftCard.setLayout(new BorderLayout(0, 10));
        leftCard.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Ô tìm kiếm nhanh [F2] viền nâu caramel
        JPanel searchWrap = new UITheme.RoundedPanel(12, UITheme.HOMU_LIGHT_PANEL, UITheme.HOMU_CARAMEL, 2);
        searchWrap.setLayout(new BorderLayout(8, 0));
        searchWrap.setBorder(new EmptyBorder(6, 12, 6, 12));

        JLabel lblSearchPrompt = new JLabel("[F2] Tìm món HomuCafe (cfd, matcha, bạc xỉu...): ");
        lblSearchPrompt.setFont(UITheme.FONT_BOLD);
        lblSearchPrompt.setForeground(UITheme.HOMU_TEXT);

        txtTimNhanh = new JTextField();
        txtTimNhanh.setFont(new Font("SansSerif", Font.BOLD, 14));
        txtTimNhanh.setForeground(UITheme.HOMU_TEXT);
        txtTimNhanh.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        txtTimNhanh.setOpaque(false);
        txtTimNhanh.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { hienThiLoiMon(); }
            @Override
            public void removeUpdate(DocumentEvent e) { hienThiLoiMon(); }
            @Override
            public void changedUpdate(DocumentEvent e) { hienThiLoiMon(); }
        });

        JButton btnClearSearch = UITheme.createRoundedButton("Xóa lọc", UITheme.HOMU_LIGHT_PANEL, UITheme.HOMU_TEXT, 8);
        btnClearSearch.setFont(UITheme.FONT_SMALL);
        btnClearSearch.setBorder(new EmptyBorder(4, 10, 4, 10));
        btnClearSearch.addActionListener(e -> {
            txtTimNhanh.setText("");
            selectedMaLoai = "ALL";
            hienThiDanhMucVaMon();
        });

        searchWrap.add(lblSearchPrompt, BorderLayout.WEST);
        searchWrap.add(txtTimNhanh, BorderLayout.CENTER);
        searchWrap.add(btnClearSearch, BorderLayout.EAST);

        // Thanh nút lọc danh mục Loại món
        pnlCategoryTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlCategoryTabs.setOpaque(false);

        JPanel topLeftBox = new JPanel(new BorderLayout(0, 10));
        topLeftBox.setOpaque(false);
        topLeftBox.add(searchWrap, BorderLayout.NORTH);
        topLeftBox.add(pnlCategoryTabs, BorderLayout.SOUTH);

        // Lưới 3 cột hiển thị Thẻ món đồ uống
        pnlMenuGrid = new JPanel(new GridLayout(0, 3, 12, 12));
        pnlMenuGrid.setOpaque(false);

        JPanel gridContainer = new JPanel(new BorderLayout());
        gridContainer.setOpaque(false);
        gridContainer.add(pnlMenuGrid, BorderLayout.NORTH);

        JScrollPane scrollMenu = new JScrollPane(gridContainer);
        scrollMenu.setBorder(null);
        scrollMenu.setOpaque(false);
        scrollMenu.getViewport().setOpaque(false);
        scrollMenu.getVerticalScrollBar().setUnitIncrement(16);

        leftCard.add(topLeftBox, BorderLayout.NORTH);
        leftCard.add(scrollMenu, BorderLayout.CENTER);

        // ==================== CỘT PHẢI: GIỎ HÀNG & THANH TOÁN ====================
        JPanel rightCard = new UITheme.RoundedPanel(16, Color.WHITE, UITheme.HOMU_BORDER, 1);
        rightCard.setLayout(new BorderLayout(0, 8));
        rightCard.setBorder(new EmptyBorder(14, 16, 14, 16));
        rightCard.setPreferredSize(new Dimension(510, 0));

        JPanel cartHeader = new JPanel();
        cartHeader.setOpaque(false);
        cartHeader.setLayout(new BoxLayout(cartHeader, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        lblMaDonHang = new JLabel("GIỎ HÀNG ĐƠN HÀNG [" + DataStore.nextMaHoaDon() + "]");
        lblMaDonHang.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblMaDonHang.setForeground(UITheme.HOMU_TEXT);

        JButton btnKetCa = UITheme.createRoundedButton("Kết ca (UC10)", UITheme.HOMU_LIGHT_PANEL, UITheme.HOMU_TEXT, 8);
        btnKetCa.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnKetCa.setBorder(new EmptyBorder(4, 8, 4, 8));
        btnKetCa.addActionListener(e -> moHopThoaiKetCa());

        titleRow.add(lblMaDonHang, BorderLayout.WEST);
        titleRow.add(btnKetCa, BorderLayout.EAST);

        // Hình thức phục vụ
        JPanel serviceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        serviceRow.setOpaque(false);
        JLabel lblHinhThuc = new JLabel("Hình thức:");
        lblHinhThuc.setFont(UITheme.FONT_BOLD);
        lblHinhThuc.setForeground(UITheme.HOMU_TEXT);

        radTaiQuan = new JRadioButton("TẠI QUÁN (Thẻ rung)", true);
        radMangDi = new JRadioButton("MANG ĐI (Số thứ tự)");
        radTaiQuan.setOpaque(false);
        radMangDi.setOpaque(false);
        radTaiQuan.setFont(UITheme.FONT_BOLD);
        radMangDi.setFont(UITheme.FONT_REGULAR);
        radTaiQuan.setForeground(UITheme.HOMU_TEXT);
        radMangDi.setForeground(UITheme.HOMU_TEXT);

        ButtonGroup bgService = new ButtonGroup();
        bgService.add(radTaiQuan);
        bgService.add(radMangDi);

        radTaiQuan.addActionListener(e -> capNhatThongTinTheRung());
        radMangDi.addActionListener(e -> capNhatThongTinTheRung());

        serviceRow.add(lblHinhThuc);
        serviceRow.add(radTaiQuan);
        serviceRow.add(radMangDi);

        // Khung gán SỐ THẺ RUNG [F4]
        pnlTheRungBox = new UITheme.RoundedPanel(10, new Color(254, 243, 199), new Color(217, 119, 6), 1);
        pnlTheRungBox.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 6));

        JLabel lblF4 = new JLabel("[F4] SỐ THẺ RUNG GÁN CHO KHÁCH:");
        lblF4.setFont(UITheme.FONT_BOLD);
        lblF4.setForeground(new Color(180, 83, 9));

        txtSoTheRung = new JTextField("08", 3);
        txtSoTheRung.setFont(new Font("SansSerif", Font.BOLD, 15));
        txtSoTheRung.setHorizontalAlignment(JTextField.CENTER);
        txtSoTheRung.setForeground(new Color(180, 83, 9));
        txtSoTheRung.setBorder(BorderFactory.createLineBorder(new Color(217, 119, 6), 1));

        lblTrangThaiTheRung = new JLabel("(Còn " + DataStore.demSoTheRungRanh() + " thẻ rảnh)");
        lblTrangThaiTheRung.setFont(UITheme.FONT_BOLD);
        lblTrangThaiTheRung.setForeground(new Color(180, 83, 9));

        JButton btnSoDoThe = UITheme.createRoundedButton("Chọn thẻ", new Color(245, 158, 11), Color.WHITE, 8);
        btnSoDoThe.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnSoDoThe.setBorder(new EmptyBorder(4, 8, 4, 8));
        btnSoDoThe.addActionListener(e -> moSoDoChonTheRung());

        pnlTheRungBox.add(lblF4);
        pnlTheRungBox.add(txtSoTheRung);
        pnlTheRungBox.add(lblTrangThaiTheRung);
        pnlTheRungBox.add(btnSoDoThe);

        cartHeader.add(titleRow);
        cartHeader.add(Box.createVerticalStrut(4));
        cartHeader.add(serviceRow);
        cartHeader.add(Box.createVerticalStrut(4));
        cartHeader.add(pnlTheRungBox);

        // Bảng chi tiết giỏ hàng
        String[] cols = {"Món đồ uống & Ghi chú ly", "SL", "Đơn giá", "T.Tiền"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCart = new JTable(cartModel);
        tblCart.setRowHeight(46);
        tblCart.setFont(UITheme.FONT_REGULAR);
        tblCart.setGridColor(UITheme.HOMU_LIGHT_PANEL);
        tblCart.setShowVerticalLines(false);
        tblCart.setSelectionBackground(new Color(244, 239, 230));
        tblCart.setSelectionForeground(UITheme.HOMU_TEXT);
        tblCart.getTableHeader().setFont(UITheme.FONT_BOLD);
        tblCart.getTableHeader().setBackground(UITheme.HOMU_LIGHT_PANEL);
        tblCart.getTableHeader().setForeground(UITheme.HOMU_TEXT);
        tblCart.getTableHeader().setPreferredSize(new Dimension(0, 32));

        tblCart.getColumnModel().getColumn(0).setPreferredWidth(235);
        tblCart.getColumnModel().getColumn(1).setPreferredWidth(38);
        tblCart.getColumnModel().getColumn(2).setPreferredWidth(75);
        tblCart.getColumnModel().getColumn(3).setPreferredWidth(85);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblCart.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tblCart.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

        DefaultTableCellRenderer redRightRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(new Color(185, 28, 28));
                setFont(UITheme.FONT_BOLD);
                setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        };
        tblCart.getColumnModel().getColumn(3).setCellRenderer(redRightRenderer);

        // Thanh công cụ nhỏ chỉnh sửa giỏ hàng
        JPanel cartTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        cartTools.setOpaque(false);
        JButton btnTangSL = UITheme.createRoundedButton("+ SL", UITheme.HOMU_LIGHT_PANEL, UITheme.HOMU_TEXT, 6);
        JButton btnGiamSL = UITheme.createRoundedButton("- SL", UITheme.HOMU_LIGHT_PANEL, UITheme.HOMU_TEXT, 6);
        JButton btnXoaDong = UITheme.createRoundedButton("Xóa món", new Color(254, 226, 226), UITheme.COLOR_DANGER, 6);
        JButton btnDoiMon = UITheme.createRoundedButton("Đổi món / Sửa bill (UC09)", UITheme.HOMU_LIGHT_PANEL, UITheme.HOMU_CARAMEL, 6);
        for (JButton b : new JButton[]{btnTangSL, btnGiamSL, btnXoaDong, btnDoiMon}) {
            b.setFont(new Font("SansSerif", Font.BOLD, 11));
            b.setBorder(new EmptyBorder(4, 8, 4, 8));
            cartTools.add(b);
        }
        btnTangSL.addActionListener(e -> thayDoiSoLuongDongChon(1));
        btnGiamSL.addActionListener(e -> thayDoiSoLuongDongChon(-1));
        btnXoaDong.addActionListener(e -> xoaDongChon());
        btnDoiMon.addActionListener(e -> moHopThoaiDoiMonUC09());

        JPanel cartCenterPanel = new JPanel(new BorderLayout(0, 4));
        cartCenterPanel.setOpaque(false);
        JScrollPane scrollCart = new JScrollPane(tblCart);
        scrollCart.setBorder(BorderFactory.createLineBorder(UITheme.HOMU_BORDER));
        cartCenterPanel.add(scrollCart, BorderLayout.CENTER);
        cartCenterPanel.add(cartTools, BorderLayout.SOUTH);

        // Khung Khách hàng & Tích điểm
        JPanel bottomBox = new JPanel();
        bottomBox.setOpaque(false);
        bottomBox.setLayout(new BoxLayout(bottomBox, BoxLayout.Y_AXIS));

        JPanel memberBox = new UITheme.RoundedPanel(10, UITheme.HOMU_LIGHT_PANEL, UITheme.HOMU_BORDER, 1);
        memberBox.setLayout(new BorderLayout(6, 0));
        memberBox.setBorder(new EmptyBorder(6, 10, 6, 10));

        JPanel sdtLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        sdtLeft.setOpaque(false);
        JLabel lblSdt = new JLabel("Homu Rewards (SĐT):");
        lblSdt.setFont(UITheme.FONT_BOLD);
        lblSdt.setForeground(UITheme.HOMU_CARAMEL);

        txtSDTKhach = new JTextField("0987654321", 9);
        txtSDTKhach.setFont(UITheme.FONT_BOLD);
        txtSDTKhach.setForeground(UITheme.HOMU_TEXT);
        txtSDTKhach.addActionListener(e -> traCuuKhachHang());

        JButton btnTimKH = UITheme.createRoundedButton("Tra cứu", UITheme.HOMU_CARAMEL, Color.WHITE, 6);
        btnTimKH.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnTimKH.setBorder(new EmptyBorder(3, 8, 3, 8));
        btnTimKH.addActionListener(e -> traCuuKhachHang());

        sdtLeft.add(lblSdt);
        sdtLeft.add(txtSDTKhach);
        sdtLeft.add(btnTimKH);

        lblThongTinKhach = new JLabel("Nguyễn Hoàng Nam (35 điểm)");
        lblThongTinKhach.setFont(UITheme.FONT_BOLD);
        lblThongTinKhach.setForeground(UITheme.HOMU_TEXT);

        chkDungDiem = new JCheckBox("Đổi 20 điểm (-20.000đ)", true);
        chkDungDiem.setOpaque(false);
        chkDungDiem.setFont(new Font("SansSerif", Font.BOLD, 11));
        chkDungDiem.setForeground(UITheme.HOMU_GREEN);
        chkDungDiem.addActionListener(e -> capNhatBangGioHang());

        JPanel memberRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        memberRight.setOpaque(false);
        memberRight.add(lblThongTinKhach);
        memberRight.add(chkDungDiem);

        memberBox.add(sdtLeft, BorderLayout.NORTH);
        memberBox.add(memberRight, BorderLayout.SOUTH);

        // Khung Tổng tiền
        JPanel summaryBox = new UITheme.RoundedPanel(10, UITheme.HOMU_LIGHT_PANEL, null, 0);
        summaryBox.setLayout(new GridLayout(3, 2, 4, 4));
        summaryBox.setBorder(new EmptyBorder(8, 12, 8, 12));

        lblTongTienHangTitle = new JLabel("Tổng tiền hàng (0 ly):");
        lblTongTienHangTitle.setFont(UITheme.FONT_REGULAR);
        lblTongTienHangTitle.setForeground(UITheme.HOMU_TEXT);
        lblTongTienHangVal = new JLabel("0 đ", SwingConstants.RIGHT);
        lblTongTienHangVal.setFont(UITheme.FONT_BOLD);
        lblTongTienHangVal.setForeground(UITheme.HOMU_TEXT);

        lblChietKhauTitle = new JLabel("Chiết khấu tích điểm (20 điểm):");
        lblChietKhauTitle.setFont(UITheme.FONT_REGULAR);
        lblChietKhauTitle.setForeground(UITheme.HOMU_TEXT);
        lblChietKhauVal = new JLabel("- 20.000 đ", SwingConstants.RIGHT);
        lblChietKhauVal.setFont(UITheme.FONT_BOLD);
        lblChietKhauVal.setForeground(UITheme.HOMU_GREEN);

        JLabel lblCanTT = new JLabel("TỔNG CẦN THANH TOÁN:");
        lblCanTT.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblCanTT.setForeground(UITheme.HOMU_TEXT);
        lblTongThanhToanVal = new JLabel("0 đ", SwingConstants.RIGHT);
        lblTongThanhToanVal.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTongThanhToanVal.setForeground(new Color(185, 28, 28));

        summaryBox.add(lblTongTienHangTitle);
        summaryBox.add(lblTongTienHangVal);
        summaryBox.add(lblChietKhauTitle);
        summaryBox.add(lblChietKhauVal);
        summaryBox.add(lblCanTT);
        summaryBox.add(lblTongThanhToanVal);

        // Cụm 4 Nút chức năng lớn phong cách HomuCafe
        JPanel actionGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        actionGrid.setOpaque(false);
        actionGrid.setPreferredSize(new Dimension(0, 108));

        JButton btnTienMat = UITheme.createRoundedButton("[F5] TIỀN MẶT (Cash)", UITheme.HOMU_GREEN, Color.WHITE, 10);
        JButton btnVietQR = UITheme.createRoundedButton("[F6] QUÉT MÃ VIETQR", UITheme.HOMU_CARAMEL, Color.WHITE, 10);
        JButton btnHuyDon = UITheme.createRoundedButton("[F1] HỦY / ĐƠN MỚI", UITheme.COLOR_DANGER, Color.WHITE, 10);
        JButton btnInBill = UITheme.createRoundedButton("[F3] IN HÓA ĐƠN & GỬI BAR", UITheme.HOMU_HEADER, Color.WHITE, 10);

        btnTienMat.addActionListener(e -> xuLyThuTienMat());
        btnVietQR.addActionListener(e -> xuLyQuetMaVietQR());
        btnHuyDon.addActionListener(e -> taoDonHangMoi());
        btnInBill.addActionListener(e -> hoanTatVaInHoaDon());

        actionGrid.add(btnTienMat);
        actionGrid.add(btnVietQR);
        actionGrid.add(btnHuyDon);
        actionGrid.add(btnInBill);

        bottomBox.add(memberBox);
        bottomBox.add(Box.createVerticalStrut(6));
        bottomBox.add(summaryBox);
        bottomBox.add(Box.createVerticalStrut(8));
        bottomBox.add(actionGrid);

        rightCard.add(cartHeader, BorderLayout.NORTH);
        rightCard.add(cartCenterPanel, BorderLayout.CENTER);
        rightCard.add(bottomBox, BorderLayout.SOUTH);

        add(leftCard, BorderLayout.CENTER);
        add(rightCard, BorderLayout.EAST);

        hienThiDanhMucVaMon();
    }

    private void napGioHangMauBanDau() {
        khachHangHienTai = DataStore.timKhachHangTheoSDT("0987654321");
        Mon traDao = DataStore.timMonTheoMa("TEA01");
        Mon cfSua = DataStore.timMonTheoMa("CF002");
        Mon matcha = DataStore.timMonTheoMa("ICE01");

        if (traDao != null) {
            gioHang.add(new ChiTietHoaDon(1, traDao, KichCo.L, 2, 57000, 70, "IT_DA", "+Đào miếng"));
        }
        if (cfSua != null) {
            gioHang.add(new ChiTietHoaDon(2, cfSua, KichCo.M, 1, 41000, 100, "BINH_THUONG", "Không đường thêm"));
        }
        if (matcha != null) {
            gioHang.add(new ChiTietHoaDon(3, matcha, KichCo.L, 1, 67000, 100, "BINH_THUONG", "+Kem cheese"));
        }
        capNhatBangGioHang();
    }

    private void hienThiDanhMucVaMon() {
        pnlCategoryTabs.removeAll();
        List<Mon> allActive = new ArrayList<>();
        for (Mon m : DataStore.getDsMon()) {
            if (m.isConHang()) allActive.add(m);
        }

        pnlCategoryTabs.add(taoNutDanhMuc("Tất cả (" + allActive.size() + ")", "ALL"));
        for (LoaiMon lm : DataStore.getDsLoaiMon()) {
            int count = 0;
            for (Mon m : allActive) {
                if (m.getLoaiMon() != null && lm.getMaLoai().equals(m.getLoaiMon().getMaLoai())) {
                    count++;
                }
            }
            pnlCategoryTabs.add(taoNutDanhMuc(lm.getTenLoai() + " (" + count + ")", lm.getMaLoai()));
        }
        pnlCategoryTabs.revalidate();
        pnlCategoryTabs.repaint();

        hienThiLoiMon();
    }

    private JButton taoNutDanhMuc(String label, String maLoai) {
        boolean active = maLoai.equals(selectedMaLoai);
        Color bg = active ? UITheme.HOMU_GREEN : UITheme.HOMU_LIGHT_PANEL;
        Color fg = active ? Color.WHITE : UITheme.HOMU_TEXT;
        JButton btn = UITheme.createRoundedButton(label, bg, fg, 8);
        btn.setFont(active ? UITheme.FONT_BOLD : UITheme.FONT_REGULAR);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.addActionListener(e -> {
            selectedMaLoai = maLoai;
            hienThiDanhMucVaMon();
        });
        return btn;
    }

    private void hienThiLoiMon() {
        pnlMenuGrid.removeAll();
        String keyword = txtTimNhanh.getText();

        for (Mon mon : DataStore.getDsMon()) {
            if (!mon.isConHang()) continue;
            if (!"ALL".equals(selectedMaLoai)) {
                if (mon.getLoaiMon() == null || !selectedMaLoai.equals(mon.getLoaiMon().getMaLoai())) {
                    continue;
                }
            }
            if (!DataStore.khopTimKiemMon(mon, keyword)) {
                continue;
            }
            pnlMenuGrid.add(taoTheMonCard(mon));
        }

        pnlMenuGrid.revalidate();
        pnlMenuGrid.repaint();
    }

    private JPanel taoTheMonCard(Mon mon) {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel(12, Color.WHITE, UITheme.HOMU_BORDER, 1);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(8, 8, 10, 8));
        card.setPreferredSize(new Dimension(190, 178));

        UITheme.RoundedPanel banner = new UITheme.RoundedPanel(10, DataStore.getCardColorForMon(mon), null, 0);
        banner.setPreferredSize(new Dimension(170, 96));
        banner.setLayout(new GridBagLayout());

        JLabel lblCode = new JLabel(mon.getMaMon());
        lblCode.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblCode.setForeground(new Color(250, 246, 239));
        banner.add(lblCode);

        JPanel infoBox = new JPanel(new BorderLayout(0, 6));
        infoBox.setOpaque(false);

        JLabel lblTen = new JLabel(mon.getTenMon());
        lblTen.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTen.setForeground(UITheme.HOMU_TEXT);

        JPanel priceRow = new JPanel(new BorderLayout());
        priceRow.setOpaque(false);

        JLabel lblGia = new JLabel("Size S: " + UITheme.formatMoney(mon.getDonGiaGoc()));
        lblGia.setFont(UITheme.FONT_BOLD);
        lblGia.setForeground(UITheme.HOMU_CARAMEL);

        JButton btnChon = UITheme.createRoundedButton("+ Chọn", UITheme.HOMU_GREEN, Color.WHITE, 8);
        btnChon.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnChon.setBorder(new EmptyBorder(4, 10, 4, 10));
        btnChon.addActionListener(e -> moTuyBienMon(mon));

        priceRow.add(lblGia, BorderLayout.WEST);
        priceRow.add(btnChon, BorderLayout.EAST);

        infoBox.add(lblTen, BorderLayout.NORTH);
        infoBox.add(priceRow, BorderLayout.SOUTH);

        card.add(banner, BorderLayout.CENTER);
        card.add(infoBox, BorderLayout.SOUTH);
        return card;
    }

    private void moTuyBienMon(Mon mon) {
        Window win = SwingUtilities.getWindowAncestor(this);
        DialogTuyBienMon dlg = new DialogTuyBienMon(win, mon);
        dlg.setVisible(true);
        ChiTietHoaDon ctMoi = dlg.getKetQua();
        if (ctMoi != null) {
            gioHang.add(ctMoi);
            capNhatBangGioHang();
        }
    }

    private void capNhatBangGioHang() {
        cartModel.setRowCount(0);
        for (ChiTietHoaDon ct : gioHang) {
            String htmlTen = "<html><b>" + ct.getMon().getTenMon() + " (Size " + ct.getKichCo().getKyHieu() + ")</b>"
                    + "<br><span style='color:#74685C;font-size:10px;'>" + ct.getMoTaTuyBien() + "</span></html>";
            cartModel.addRow(new Object[]{
                    htmlTen,
                    ct.getSoLuong(),
                    String.format("%,.0f", ct.getDonGiaThucTe()).replace(',', '.'),
                    UITheme.formatMoney(ct.tinhThanhTien())
            });
        }

        int tongLy = 0;
        double tongTienHang = 0;
        for (ChiTietHoaDon ct : gioHang) {
            tongLy += ct.getSoLuong();
            tongTienHang += ct.tinhThanhTien();
        }

        double giamGia = 0;
        if (khachHangHienTai != null && chkDungDiem.isSelected() && khachHangHienTai.getDiemTichLuy() >= 20) {
            giamGia = 20000;
        }

        double canThanhToan = Math.max(0, tongTienHang - giamGia);

        lblTongTienHangTitle.setText("Tổng tiền hàng (" + tongLy + " ly):");
        lblTongTienHangVal.setText(UITheme.formatMoneySpace(tongTienHang));
        lblChietKhauTitle.setText(giamGia > 0 ? "Chiết khấu tích điểm (20 điểm):" : "Chiết khấu tích điểm (0 điểm):");
        lblChietKhauVal.setText("- " + UITheme.formatMoneySpace(giamGia));
        lblTongThanhToanVal.setText(UITheme.formatMoneySpace(canThanhToan));
    }

    private void capNhatThongTinTheRung() {
        if (radTaiQuan.isSelected()) {
            txtSoTheRung.setEnabled(true);
            lblTrangThaiTheRung.setText("(Còn " + DataStore.demSoTheRungRanh() + " thẻ rảnh)");
        } else {
            txtSoTheRung.setEnabled(false);
            lblTrangThaiTheRung.setText("(Mang đi: Tự cấp STT)");
        }
    }

    private void thayDoiSoLuongDongChon(int delta) {
        int row = tblCart.getSelectedRow();
        if (row < 0 || row >= gioHang.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một món trong giỏ hàng!");
            return;
        }
        ChiTietHoaDon ct = gioHang.get(row);
        int slMoi = ct.getSoLuong() + delta;
        if (slMoi <= 0) {
            gioHang.remove(row);
        } else {
            ct.setSoLuong(slMoi);
        }
        capNhatBangGioHang();
    }

    private void xoaDongChon() {
        int row = tblCart.getSelectedRow();
        if (row >= 0 && row < gioHang.size()) {
            gioHang.remove(row);
            capNhatBangGioHang();
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng món trong giỏ hàng cần xóa!");
        }
    }

    private void traCuuKhachHang() {
        String sdt = txtSDTKhach.getText().trim();
        if (!sdt.matches("^0[35789][0-9]{8}$")) {
            JOptionPane.showMessageDialog(this,
                    "Số điện thoại không hợp lệ! Chuẩn Regex 10 chữ số (03, 05, 07, 08, 09).",
                    "Kiểm tra Regex (BR05)", JOptionPane.WARNING_MESSAGE);
            return;
        }
        KhachHang kh = DataStore.timKhachHangTheoSDT(sdt);
        if (kh != null) {
            khachHangHienTai = kh;
            lblThongTinKhach.setText(kh.getHoTen() + " (" + kh.getDiemTichLuy() + " điểm)");
            chkDungDiem.setEnabled(kh.getDiemTichLuy() >= 20);
            capNhatBangGioHang();
        } else {
            String tenMoi = JOptionPane.showInputDialog(this,
                    "Khách hàng mới! Nhập họ tên đăng ký Homu Rewards:",
                    "Đăng ký thành viên mới", JOptionPane.QUESTION_MESSAGE);
            if (tenMoi != null && !tenMoi.trim().isEmpty()) {
                KhachHang khMoi = new KhachHang("KH" + String.format("%04d", DataStore.getDsKhachHang().size() + 1),
                        tenMoi.trim(), sdt, 0);
                DataStore.getDsKhachHang().add(khMoi);
                khachHangHienTai = khMoi;
                lblThongTinKhach.setText(khMoi.getHoTen() + " (0 điểm)");
                chkDungDiem.setSelected(false);
                chkDungDiem.setEnabled(false);
                capNhatBangGioHang();
            }
        }
    }

    private void moSoDoChonTheRung() {
        if (!radTaiQuan.isSelected()) {
            JOptionPane.showMessageDialog(this, "Đơn Mang đi sử dụng Số thứ tự tự động, không cần cấp thẻ rung!");
            return;
        }
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Khay Thẻ Rung HomuCafe (Coaster Pager 01-20)", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(520, 380);
        dlg.setLocationRelativeTo(this);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(14, 14, 14, 14));
        main.setBackground(Color.WHITE);

        JLabel lblGuide = new JLabel("Chọn thẻ rung màu XANH (Rảnh trên khay sạc). Thẻ ĐỎ đang phục vụ khách:");
        lblGuide.setFont(UITheme.FONT_BOLD);
        lblGuide.setForeground(UITheme.HOMU_TEXT);

        JPanel grid = new JPanel(new GridLayout(4, 5, 8, 8));
        grid.setOpaque(false);

        for (int i = 1; i <= DataStore.TONG_SO_THE_RUNG; i++) {
            final int soThe = i;
            boolean ban = DataStore.isTheRungDangBan(soThe);
            Color bg = ban ? UITheme.COLOR_DANGER : UITheme.HOMU_GREEN;
            JButton btn = UITheme.createRoundedButton("<html><center><b>Thẻ #" + String.format("%02d", soThe) + "</b><br>"
                    + (ban ? "Đang bận" : "Rảnh") + "</center></html>", bg, Color.WHITE, 8);
            btn.setEnabled(!ban);
            btn.addActionListener(e -> {
                txtSoTheRung.setText(String.format("%02d", soThe));
                dlg.dispose();
            });
            grid.add(btn);
        }

        main.add(lblGuide, BorderLayout.NORTH);
        main.add(grid, BorderLayout.CENTER);
        dlg.setContentPane(main);
        dlg.setVisible(true);
    }

    private void xuLyThuTienMat() {
        if (gioHang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống!");
            return;
        }
        double canThu = tinhTongCanThanhToanHienTai();
        String input = JOptionPane.showInputDialog(this,
                "Tổng cần thanh toán: " + UITheme.formatMoney(canThu) + "\nNhập số tiền mặt khách đưa (VNĐ):",
                String.valueOf((long) canThu));
        if (input == null) return;
        try {
            double khachDua = Double.parseDouble(input.replace(".", "").replace(",", "").trim());
            if (khachDua < canThu) {
                JOptionPane.showMessageDialog(this,
                        "Số tiền khách đưa nhỏ hơn Tổng cần thanh toán (" + UITheme.formatMoney(canThu) + ")!\n"
                                + "Ràng buộc BR02: Phải thu đủ 100% trước khi in bill.",
                        "Cảnh báo thiếu tiền (BR02)", JOptionPane.ERROR_MESSAGE);
                daXacNhanThuTien = false;
                return;
            }
            phuongThucDaChon = "TIEN_MAT";
            tienKhachDua = khachDua;
            daXacNhanThuTien = true;
            double tienThoi = khachDua - canThu;
            JOptionPane.showMessageDialog(this,
                    "Xác nhận thu TIỀN MẶT thành công!\n"
                            + "• Tổng thanh toán: " + UITheme.formatMoney(canThu) + "\n"
                            + "• Tiền khách đưa: " + UITheme.formatMoney(khachDua) + "\n"
                            + "• Tiền thối lại khách: " + UITheme.formatMoney(tienThoi) + "\n\n"
                            + "Bấm [F3] IN HÓA ĐƠN & GỬI BAR để hoàn tất.",
                    "Thu tiền mặt (UC08)", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số tiền nhập vào không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyQuetMaVietQR() {
        if (gioHang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống!");
            return;
        }
        double canThu = tinhTongCanThanhToanHienTai();
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Quét Mã VietQR HomuCafe (UC08)", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(400, 480);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("QUÉT MÃ VIETQR THANH TOÁN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setForeground(UITheme.HOMU_HEADER);

        JPanel qrCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int size = 210;
                int startX = (getWidth() - size) / 2;
                int startY = 10;
                g2.setColor(Color.WHITE);
                g2.fillRect(startX - 8, startY - 8, size + 16, size + 16);
                g2.setColor(UITheme.HOMU_HEADER);
                g2.drawRect(startX - 8, startY - 8, size + 16, size + 16);

                g2.setColor(UITheme.HOMU_TEXT);
                int cell = 10;
                for (int r = 0; r < 21; r++) {
                    for (int c = 0; c < 21; c++) {
                        boolean corner = (r < 7 && c < 7) || (r < 7 && c >= 14) || (r >= 14 && c < 7);
                        if (corner) {
                            if (r == 0 || r == 6 || c == 0 || c == 6 || c == 14 || c == 20 || r == 14 || r == 20
                                    || (r >= 2 && r <= 4 && c >= 2 && c <= 4)
                                    || (r >= 2 && r <= 4 && c >= 16 && c <= 18)
                                    || (r >= 16 && r <= 18 && c >= 2 && c <= 4)) {
                                g2.fillRect(startX + c * cell, startY + r * cell, cell, cell);
                            }
                        } else if ((r * 31 + c * 17 + (int) canThu) % 3 == 0) {
                            g2.fillRect(startX + c * cell, startY + r * cell, cell, cell);
                        }
                    }
                }
                g2.dispose();
            }
        };
        qrCanvas.setOpaque(false);
        qrCanvas.setPreferredSize(new Dimension(250, 240));

        JLabel lblInfo = new JLabel("<html><center>Tài khoản: <b>Vietcombank - HOMU CAFE</b><br>"
                + "Chủ TK: <b>HOMU CAFE POS SYSTEM</b><br>"
                + "Số tiền: <b style='color:#B91C1C;font-size:15px;'>" + UITheme.formatMoney(canThu) + "</b><br>"
                + "Nội dung: <b>" + lblMaDonHang.getText().replace("GIỎ HÀNG ĐƠN HÀNG ", "") + "</b></center></html>", SwingConstants.CENTER);
        lblInfo.setFont(UITheme.FONT_REGULAR);
        lblInfo.setForeground(UITheme.HOMU_TEXT);

        JButton btnXacNhanQR = UITheme.createRoundedButton("ĐÃ NHẬN ĐỦ TIỀN CHUYỂN KHOẢN", UITheme.HOMU_GREEN, Color.WHITE, 10);
        btnXacNhanQR.addActionListener(e -> {
            phuongThucDaChon = "CHUYEN_KHOAN";
            daXacNhanThuTien = true;
            dlg.dispose();
            JOptionPane.showMessageDialog(this, "Đã xác nhận thanh toán VietQR! Bấm [F3] để In Bill & Gửi Quầy Bar.");
        });

        JPanel south = new JPanel(new BorderLayout(0, 10));
        south.setOpaque(false);
        south.add(lblInfo, BorderLayout.CENTER);
        south.add(btnXacNhanQR, BorderLayout.SOUTH);

        p.add(lblTitle, BorderLayout.NORTH);
        p.add(qrCanvas, BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private double tinhTongCanThanhToanHienTai() {
        double sum = 0;
        for (ChiTietHoaDon ct : gioHang) sum += ct.tinhThanhTien();
        double giam = (khachHangHienTai != null && chkDungDiem.isSelected() && khachHangHienTai.getDiemTichLuy() >= 20) ? 20000 : 0;
        return Math.max(0, sum - giam);
    }

    private void hoanTatVaInHoaDon() {
        if (gioHang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng chưa có món đồ uống nào!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int soTheRung = 0;
        String loaiDon = radTaiQuan.isSelected() ? "TAI_QUAN" : "MANG_DI";

        if ("TAI_QUAN".equals(loaiDon)) {
            String rawThe = txtSoTheRung.getText().trim();
            if (!rawThe.matches("^[0-9]?[1-9][0-9]?$")) {
                JOptionPane.showMessageDialog(this,
                        "Số thẻ rung phải nằm trong khoảng từ 01 đến 99 (BR05)!",
                        "Lỗi số thẻ rung", JOptionPane.ERROR_MESSAGE);
                txtSoTheRung.requestFocus();
                return;
            }
            soTheRung = Integer.parseInt(rawThe);
            if (DataStore.isTheRungDangBan(soTheRung)) {
                JOptionPane.showMessageDialog(this,
                        "Thẻ rung #" + String.format("%02d", soTheRung) + " đang phục vụ cho đơn chưa hoàn tất (BR01)!\n"
                                + "Vui lòng chọn thẻ rung khác đang rảnh.",
                        "Trùng thẻ rung (BR01)", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (!daXacNhanThuTien) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng xác nhận thu đủ 100% tiền khách (F5 Tiền mặt hoặc F6 VietQR) trước khi in bill (BR02)!",
                    "Chưa thu tiền", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int sttNgay = DataStore.nextSoThuTuNgay();
        String maHD = DataStore.nextMaHoaDon();
        double giamGia = (khachHangHienTai != null && chkDungDiem.isSelected() && khachHangHienTai.getDiemTichLuy() >= 20) ? 20000 : 0;

        NhanVien nvLap = DataStore.getTaiKhoanDangNhap() != null ? DataStore.getTaiKhoanDangNhap().getNhanVien() : null;
        HoaDon hdMoi = new HoaDon(maHD, sttNgay, LocalDateTime.now(), soTheRung, loaiDon,
                phuongThucDaChon, giamGia, "DANG_PHA_CHE", nvLap, khachHangHienTai);
        for (ChiTietHoaDon ct : gioHang) {
            hdMoi.themChiTiet(ct);
        }

        if (khachHangHienTai != null) {
            if (giamGia > 0) khachHangHienTai.truDiem(20);
            khachHangHienTai.congDiem(hdMoi.tinhTongTien());
            lblThongTinKhach.setText(khachHangHienTai.getHoTen() + " (" + khachHangHienTai.getDiemTichLuy() + " điểm)");
        }

        DataStore.getDsHoaDon().add(hdMoi);
        DataStore.notifyDataChanged();

        hienThiPhieuInHoaDonNhiet(hdMoi);
        taoDonHangMoi();
    }

    private void hienThiPhieuInHoaDonNhiet(HoaDon hd) {
        StringBuilder bill = new StringBuilder();
        bill.append("========================================\n");
        bill.append("               HOMU CAFE                \n");
        bill.append("       COFFEE POS & PAGER SYSTEM        \n");
        bill.append("   Đ/c: 12 Nguyễn Văn Bảo, Gò Vấp, HCM  \n");
        bill.append("========================================\n");
        bill.append("Mã HĐ: ").append(hd.getMaHD()).append("\n");
        bill.append("Ngày lập: ").append(hd.getNgayLap().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        bill.append("Thu ngân: ").append(hd.getNhanVien() != null ? hd.getNhanVien().getHoTen() : "NV01").append("\n");
        if ("TAI_QUAN".equals(hd.getLoaiDon())) {
            bill.append(">>> HÌNH THỨC: UỐNG TẠI QUÁN\n");
            bill.append(">>> THẺ RUNG SỐ: #").append(String.format("%02d", hd.getSoTheRung())).append("\n");
        } else {
            bill.append(">>> HÌNH THỨC: MANG ĐI (TAKE-AWAY)\n");
            bill.append(">>> SỐ THỨ TỰ: #").append(String.format("%02d", hd.getSoThuTuNgay())).append("\n");
        }
        bill.append("----------------------------------------\n");
        for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
            bill.append(String.format("%dx %s (Size %s)   %s\n",
                    ct.getSoLuong(), ct.getMon().getTenMon(), ct.getKichCo().getKyHieu(),
                    UITheme.formatMoney(ct.tinhThanhTien())));
            bill.append("   + ").append(ct.getMoTaTuyBien()).append("\n");
        }
        bill.append("----------------------------------------\n");
        bill.append("Tổng tiền hàng:      ").append(UITheme.formatMoney(hd.tinhTongTienHang())).append("\n");
        bill.append("Chiết khấu điểm:    -").append(UITheme.formatMoney(hd.getGiamGia())).append("\n");
        bill.append("TỔNG THANH TOÁN:     ").append(UITheme.formatMoney(hd.tinhTongTien())).append("\n");
        bill.append("Phương thức:         ").append("TIEN_MAT".equals(hd.getPhuongThucThanhToan()) ? "Tiền mặt (Đã thu 100%)" : "VietQR (Đã thu 100%)").append("\n");
        bill.append("========================================\n");
        bill.append("   Đơn hàng đã chuyển sang Quầy Bar!    \n");
        bill.append("   Cảm ơn Quý khách & Hẹn gặp lại!     \n");

        JTextArea area = new JTextArea(bill.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        area.setBackground(new Color(254, 252, 232));
        area.setBorder(new EmptyBorder(10, 12, 10, 12));

        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "In Hóa Đơn Nhiệt HomuCafe (UC08)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void taoDonHangMoi() {
        gioHang.clear();
        lblMaDonHang.setText("GIỎ HÀNG ĐƠN HÀNG [" + DataStore.nextMaHoaDon() + "]");
        txtSoTheRung.setText(String.format("%02d", DataStore.goiYTheRungTiepTheo()));
        capNhatThongTinTheRung();
        capNhatBangGioHang();
    }

    private void moHopThoaiDoiMonUC09() {
        String passQL = JOptionPane.showInputDialog(this,
                "Nghiệp vụ Đổi món / Điều chỉnh bill (UC09 - BR03)\n"
                        + "Yêu cầu nhập mật khẩu Quản lý để phê duyệt (Mặc định: 123456):",
                "Phê duyệt Quản lý (UC09)", JOptionPane.WARNING_MESSAGE);
        if (passQL == null) return;
        if (!"123456".equals(passQL.trim())) {
            JOptionPane.showMessageDialog(this, "Mật khẩu Quản lý không đúng!", "Từ chối", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this,
                "Quản lý đã phê duyệt quyền điều chỉnh đơn!\n"
                        + "Bạn có thể xóa món cũ và chọn món thay thế trực tiếp trên giỏ hàng, hệ thống sẽ tự tính chênh lệch.",
                "Đã mở khóa điều chỉnh bill (UC09)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void moHopThoaiKetCa() {
        double tongTienMat = 0;
        double tongCK = 0;
        int soDon = 0;
        for (HoaDon hd : DataStore.getDsHoaDon()) {
            if (!"DA_HUY".equals(hd.getTrangThaiDon())) {
                soDon++;
                if ("TIEN_MAT".equals(hd.getPhuongThucThanhToan())) {
                    tongTienMat += hd.tinhTongTien();
                } else {
                    tongCK += hd.tinhTongTien();
                }
            }
        }
        String msg = String.format(
                "BIÊN BẢN ĐỐI SOÁT & KẾT CA BÀN GIAO TIỀN MẶT - HOMU CAFE\n"
                        + "--------------------------------------------------------\n"
                        + "• Nhân viên trực ca: %s\n"
                        + "• Tổng số đơn đã lập: %d đơn\n"
                        + "• Doanh thu Tiền mặt tại két: %s\n"
                        + "• Doanh thu Chuyển khoản QR: %s\n"
                        + "• TỔNG DOANH THU CA: %s\n"
                        + "--------------------------------------------------------\n"
                        + "Xác nhận đối soát khớp 100%% tiền mặt và lưu biên bản giao ca?",
                DataStore.getTaiKhoanDangNhap() != null && DataStore.getTaiKhoanDangNhap().getNhanVien() != null
                        ? DataStore.getTaiKhoanDangNhap().getNhanVien().toString() : "NV01 - Trần Văn An",
                soDon,
                UITheme.formatMoney(tongTienMat),
                UITheme.formatMoney(tongCK),
                UITheme.formatMoney(tongTienMat + tongCK)
        );
        int opt = JOptionPane.showConfirmDialog(this, msg, "Kết Ca Bàn Giao Tiền Mặt (UC10)", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "Đã lưu biên bản kết ca và in phiếu bàn giao tiền mặt thành công!");
        }
    }

    private void dangKyPhimTatToanCuc() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke("F1"), "newOrder");
        am.put("newOrder", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { taoDonHangMoi(); }
        });

        im.put(KeyStroke.getKeyStroke("F2"), "focusSearch");
        am.put("focusSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtTimNhanh.requestFocusInWindow();
                txtTimNhanh.selectAll();
            }
        });

        im.put(KeyStroke.getKeyStroke("F3"), "printAndSendBar");
        am.put("printAndSendBar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { hoanTatVaInHoaDon(); }
        });

        im.put(KeyStroke.getKeyStroke("F4"), "focusPager");
        am.put("focusPager", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                radTaiQuan.setSelected(true);
                capNhatThongTinTheRung();
                txtSoTheRung.requestFocusInWindow();
                txtSoTheRung.selectAll();
            }
        });

        im.put(KeyStroke.getKeyStroke("F5"), "cashPay");
        am.put("cashPay", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { xuLyThuTienMat(); }
        });

        im.put(KeyStroke.getKeyStroke("F6"), "qrPay");
        am.put("qrPay", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { xuLyQuetMaVietQR(); }
        });
    }
}
