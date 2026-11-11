package gui;

import entity.ChiTietHoaDon;
import entity.HoaDon;
import util.DataStore;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Quản Lý & Tra Cứu Hóa Đơn (FrmQuanLyHoaDon - HomuCafe)
 * Master / Detail view chuẩn mực: Tra cứu đa tiêu chí, xem chi tiết ly nước, in lại bill và hủy bill.
 */
public class FrmQuanLyHoaDon extends JPanel {
    private JTextField txtTimKiem;
    private JComboBox<String> cboLoaiDon;
    private JComboBox<String> cboTrangThai;
    private JComboBox<String> cboPhuongThuc;

    private DefaultTableModel modelHoaDon;
    private JTable tblHoaDon;
    private DefaultTableModel modelChiTiet;
    private JTable tblChiTiet;

    private JLabel lblTongSoHD;
    private JLabel lblTongTienLoc;
    private JLabel lblChiTietHeader;

    private final List<HoaDon> dsHienThi = new ArrayList<>();
    private HoaDon hoaDonDangChon = null;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public FrmQuanLyHoaDon() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.HOMU_BG_CREAM);
        setBorder(new EmptyBorder(14, 18, 18, 18));

        initComponents();
        taiDanhSachHoaDon();

        DataStore.addDataChangeListener(this::taiDanhSachHoaDon);
    }

    private void initComponents() {
        // Bộ lọc tra cứu
        JPanel filterCard = new UITheme.RoundedPanel(14, Color.WHITE, UITheme.HOMU_BORDER, 1);
        filterCard.setLayout(new BorderLayout(10, 8));
        filterCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel filterRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterRow1.setOpaque(false);

        JLabel lblTim = new JLabel("Tìm nhanh (Mã HĐ / SĐT / Thẻ):");
        lblTim.setFont(UITheme.FONT_BOLD);
        lblTim.setForeground(UITheme.HOMU_TEXT);

        txtTimKiem = UITheme.createStyledTextField("");
        txtTimKiem.setPreferredSize(new Dimension(220, 36));
        txtTimKiem.setToolTipText("Mã HĐ, SĐT khách hoặc số thẻ rung");
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { apDungBoLoc(); }
            @Override
            public void removeUpdate(DocumentEvent e) { apDungBoLoc(); }
            @Override
            public void changedUpdate(DocumentEvent e) { apDungBoLoc(); }
        });

        JLabel lblLoai = new JLabel("Hình thức:");
        lblLoai.setFont(UITheme.FONT_BOLD);
        lblLoai.setForeground(UITheme.HOMU_TEXT);
        cboLoaiDon = new JComboBox<>(new String[]{"Tất cả hình thức", "Tại quán (Thẻ rung)", "Mang đi (STT)"});
        cboLoaiDon.setFont(UITheme.FONT_REGULAR);
        cboLoaiDon.addActionListener(e -> apDungBoLoc());

        JLabel lblTT = new JLabel("Trạng thái đơn:");
        lblTT.setFont(UITheme.FONT_BOLD);
        lblTT.setForeground(UITheme.HOMU_TEXT);
        cboTrangThai = new JComboBox<>(new String[]{
                "Tất cả trạng thái", "Đang pha chế", "Chờ giao nước", "Hoàn tất", "Đã hủy"
        });
        cboTrangThai.setFont(UITheme.FONT_REGULAR);
        cboTrangThai.addActionListener(e -> apDungBoLoc());

        JLabel lblPT = new JLabel("Thanh toán:");
        lblPT.setFont(UITheme.FONT_BOLD);
        lblPT.setForeground(UITheme.HOMU_TEXT);
        cboPhuongThuc = new JComboBox<>(new String[]{"Tất cả", "Tiền mặt", "Chuyển khoản QR"});
        cboPhuongThuc.setFont(UITheme.FONT_REGULAR);
        cboPhuongThuc.addActionListener(e -> apDungBoLoc());

        JButton btnLamMoi = UITheme.createRoundedButton("Làm mới bộ lọc", UITheme.HOMU_LIGHT_PANEL, UITheme.HOMU_TEXT, 8);
        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
            cboLoaiDon.setSelectedIndex(0);
            cboTrangThai.setSelectedIndex(0);
            cboPhuongThuc.setSelectedIndex(0);
            apDungBoLoc();
        });

        filterRow1.add(lblTim);
        filterRow1.add(txtTimKiem);
        filterRow1.add(lblLoai);
        filterRow1.add(cboLoaiDon);
        filterRow1.add(lblTT);
        filterRow1.add(cboTrangThai);
        filterRow1.add(lblPT);
        filterRow1.add(cboPhuongThuc);
        filterRow1.add(btnLamMoi);

        JPanel statsRow = new JPanel(new BorderLayout());
        statsRow.setOpaque(false);
        statsRow.setBorder(new EmptyBorder(4, 4, 0, 4));

        lblTongSoHD = new JLabel("Tìm thấy: 0 hóa đơn");
        lblTongSoHD.setFont(UITheme.FONT_BOLD);
        lblTongSoHD.setForeground(UITheme.HOMU_CARAMEL);

        lblTongTienLoc = new JLabel("Tổng tiền: 0 đ");
        lblTongTienLoc.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTongTienLoc.setForeground(new Color(185, 28, 28));

        statsRow.add(lblTongSoHD, BorderLayout.WEST);
        statsRow.add(lblTongTienLoc, BorderLayout.EAST);

        filterCard.add(filterRow1, BorderLayout.CENTER);
        filterCard.add(statsRow, BorderLayout.SOUTH);

        // Bảng Master
        JPanel masterCard = new UITheme.RoundedPanel(14, Color.WHITE, UITheme.HOMU_BORDER, 1);
        masterCard.setLayout(new BorderLayout(0, 8));
        masterCard.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblMasterTitle = new JLabel("DANH SÁCH HÓA ĐƠN BÁN HÀNG (MASTER VIEW)");
        lblMasterTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblMasterTitle.setForeground(UITheme.HOMU_TEXT);

        String[] colsHD = {
                "STT", "Mã hóa đơn", "Thời gian lập", "Loại đơn", "Thẻ / STT",
                "Khách hàng", "SĐT", "Thu ngân", "Hình thức TT", "Giảm giá", "Tổng thanh toán", "Trạng thái"
        };
        modelHoaDon = new DefaultTableModel(colsHD, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblHoaDon = new JTable(modelHoaDon);
        tblHoaDon.setRowHeight(36);
        tblHoaDon.setFont(UITheme.FONT_REGULAR);
        tblHoaDon.setGridColor(UITheme.HOMU_LIGHT_PANEL);
        tblHoaDon.setShowVerticalLines(false);
        tblHoaDon.setSelectionBackground(new Color(244, 239, 230));
        tblHoaDon.setSelectionForeground(UITheme.HOMU_TEXT);
        tblHoaDon.getTableHeader().setFont(UITheme.FONT_BOLD);
        tblHoaDon.getTableHeader().setBackground(UITheme.HOMU_LIGHT_PANEL);
        tblHoaDon.getTableHeader().setForeground(UITheme.HOMU_TEXT);
        tblHoaDon.getTableHeader().setPreferredSize(new Dimension(0, 34));

        tblHoaDon.getColumnModel().getColumn(0).setPreferredWidth(38);
        tblHoaDon.getColumnModel().getColumn(1).setPreferredWidth(115);
        tblHoaDon.getColumnModel().getColumn(2).setPreferredWidth(130);
        tblHoaDon.getColumnModel().getColumn(3).setPreferredWidth(85);
        tblHoaDon.getColumnModel().getColumn(4).setPreferredWidth(75);
        tblHoaDon.getColumnModel().getColumn(5).setPreferredWidth(130);
        tblHoaDon.getColumnModel().getColumn(6).setPreferredWidth(95);
        tblHoaDon.getColumnModel().getColumn(7).setPreferredWidth(110);
        tblHoaDon.getColumnModel().getColumn(8).setPreferredWidth(95);
        tblHoaDon.getColumnModel().getColumn(9).setPreferredWidth(75);
        tblHoaDon.getColumnModel().getColumn(10).setPreferredWidth(110);
        tblHoaDon.getColumnModel().getColumn(11).setPreferredWidth(100);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        tblHoaDon.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        tblHoaDon.getColumnModel().getColumn(3).setCellRenderer(centerRender);
        tblHoaDon.getColumnModel().getColumn(4).setCellRenderer(centerRender);

        DefaultTableCellRenderer rightRender = new DefaultTableCellRenderer();
        rightRender.setHorizontalAlignment(SwingConstants.RIGHT);
        tblHoaDon.getColumnModel().getColumn(9).setCellRenderer(rightRender);

        tblHoaDon.getColumnModel().getColumn(10).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(new Color(185, 28, 28));
                setFont(UITheme.FONT_BOLD);
                setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        tblHoaDon.getColumnModel().getColumn(11).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String st = String.valueOf(value);
                setFont(UITheme.FONT_BOLD);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (st.contains("Pha chế")) {
                    c.setForeground(new Color(217, 119, 6));
                } else if (st.contains("Chờ giao")) {
                    c.setForeground(new Color(124, 58, 237));
                } else if (st.contains("Hoàn tất")) {
                    c.setForeground(UITheme.HOMU_GREEN);
                } else if (st.contains("Đã hủy")) {
                    c.setForeground(new Color(185, 28, 28));
                }
                return c;
            }
        });

        tblHoaDon.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienThiChiTietHoaDonDuocChon();
            }
        });

        JScrollPane scrollMaster = new JScrollPane(tblHoaDon);
        scrollMaster.setBorder(BorderFactory.createLineBorder(UITheme.HOMU_BORDER));
        scrollMaster.setPreferredSize(new Dimension(0, 260));

        masterCard.add(lblMasterTitle, BorderLayout.NORTH);
        masterCard.add(scrollMaster, BorderLayout.CENTER);

        // Bảng Detail
        JPanel detailCard = new UITheme.RoundedPanel(14, Color.WHITE, UITheme.HOMU_BORDER, 1);
        detailCard.setLayout(new BorderLayout(0, 8));
        detailCard.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel detailHeader = new JPanel(new BorderLayout());
        detailHeader.setOpaque(false);

        lblChiTietHeader = new JLabel("CHI TIẾT LY NƯỚC CỦA HÓA ĐƠN (DETAIL VIEW - Vui lòng chọn hóa đơn ở trên)");
        lblChiTietHeader.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblChiTietHeader.setForeground(UITheme.HOMU_TEXT);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        JButton btnInLaiBill = UITheme.createRoundedButton("🖨 In Lại Bill (Re-print)", UITheme.HOMU_CARAMEL, Color.WHITE, 8);
        JButton btnHuyDon = UITheme.createRoundedButton("✖ Hủy Hóa Đơn (BR03)", UITheme.COLOR_DANGER, Color.WHITE, 8);
        JButton btnXuatExcel = UITheme.createRoundedButton("📊 Xuất DS Ra Excel/CSV", UITheme.HOMU_GREEN, Color.WHITE, 8);

        btnInLaiBill.addActionListener(e -> inLaiHoaDonDangChon());
        btnHuyDon.addActionListener(e -> huyHoaDonDangChon());
        btnXuatExcel.addActionListener(e -> xuatDanhSachHoaDonExcel());

        actionBtns.add(btnInLaiBill);
        actionBtns.add(btnHuyDon);
        actionBtns.add(btnXuatExcel);

        detailHeader.add(lblChiTietHeader, BorderLayout.WEST);
        detailHeader.add(actionBtns, BorderLayout.EAST);

        String[] colsCT = {
                "STT", "Mã món", "Tên món đồ uống", "Size", "SL", "Đơn giá",
                "Độ ngọt (% đường)", "Mức đá / Nhiệt độ", "Topping thêm & Ghi chú", "Thành tiền"
        };
        modelChiTiet = new DefaultTableModel(colsCT, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setRowHeight(34);
        tblChiTiet.setFont(UITheme.FONT_REGULAR);
        tblChiTiet.setGridColor(UITheme.HOMU_LIGHT_PANEL);
        tblChiTiet.setShowVerticalLines(false);
        tblChiTiet.getTableHeader().setFont(UITheme.FONT_BOLD);
        tblChiTiet.getTableHeader().setBackground(UITheme.HOMU_LIGHT_PANEL);
        tblChiTiet.getTableHeader().setForeground(UITheme.HOMU_TEXT);
        tblChiTiet.getTableHeader().setPreferredSize(new Dimension(0, 32));

        tblChiTiet.getColumnModel().getColumn(0).setPreferredWidth(38);
        tblChiTiet.getColumnModel().getColumn(1).setPreferredWidth(75);
        tblChiTiet.getColumnModel().getColumn(2).setPreferredWidth(180);
        tblChiTiet.getColumnModel().getColumn(3).setPreferredWidth(55);
        tblChiTiet.getColumnModel().getColumn(4).setPreferredWidth(45);
        tblChiTiet.getColumnModel().getColumn(5).setPreferredWidth(85);
        tblChiTiet.getColumnModel().getColumn(6).setPreferredWidth(110);
        tblChiTiet.getColumnModel().getColumn(7).setPreferredWidth(110);
        tblChiTiet.getColumnModel().getColumn(8).setPreferredWidth(170);
        tblChiTiet.getColumnModel().getColumn(9).setPreferredWidth(95);

        tblChiTiet.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        tblChiTiet.getColumnModel().getColumn(3).setCellRenderer(centerRender);
        tblChiTiet.getColumnModel().getColumn(4).setCellRenderer(centerRender);
        tblChiTiet.getColumnModel().getColumn(5).setCellRenderer(rightRender);

        tblChiTiet.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(new Color(185, 28, 28));
                setFont(UITheme.FONT_BOLD);
                setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        JScrollPane scrollDetail = new JScrollPane(tblChiTiet);
        scrollDetail.setBorder(BorderFactory.createLineBorder(UITheme.HOMU_BORDER));

        detailCard.add(detailHeader, BorderLayout.NORTH);
        detailCard.add(scrollDetail, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, masterCard, detailCard);
        splitPane.setDividerLocation(310);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        add(filterCard, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void taiDanhSachHoaDon() {
        apDungBoLoc();
    }

    private void apDungBoLoc() {
        dsHienThi.clear();
        String keyword = txtTimKiem.getText().trim().toLowerCase();
        String loaiDonFilter = (String) cboLoaiDon.getSelectedItem();
        String trangThaiFilter = (String) cboTrangThai.getSelectedItem();
        String ptFilter = (String) cboPhuongThuc.getSelectedItem();

        double tongTienLoc = 0;

        for (HoaDon hd : DataStore.getDsHoaDon()) {
            if (!keyword.isEmpty()) {
                String ma = hd.getMaHD().toLowerCase();
                String sdt = (hd.getKhachHang() != null && hd.getKhachHang().getSoDienThoai() != null)
                        ? hd.getKhachHang().getSoDienThoai() : "";
                String tenKH = (hd.getKhachHang() != null && hd.getKhachHang().getHoTen() != null)
                        ? hd.getKhachHang().getHoTen().toLowerCase() : "";
                String soThe = String.valueOf(hd.getSoTheRung());
                String stt = String.valueOf(hd.getSoThuTuNgay());

                if (!ma.contains(keyword) && !sdt.contains(keyword)
                        && !tenKH.contains(keyword) && !soThe.equals(keyword) && !stt.equals(keyword)) {
                    continue;
                }
            }

            if (loaiDonFilter != null && !loaiDonFilter.startsWith("Tất cả")) {
                if (loaiDonFilter.contains("Tại quán") && !"TAI_QUAN".equals(hd.getLoaiDon())) continue;
                if (loaiDonFilter.contains("Mang đi") && !"MANG_DI".equals(hd.getLoaiDon())) continue;
            }

            if (trangThaiFilter != null && !trangThaiFilter.startsWith("Tất cả")) {
                if (trangThaiFilter.contains("Đang pha chế") && !"DANG_PHA_CHE".equals(hd.getTrangThaiDon())) continue;
                if (trangThaiFilter.contains("Chờ giao") && !"CHO_GIAO".equals(hd.getTrangThaiDon())) continue;
                if (trangThaiFilter.contains("Hoàn tất") && !"HOAN_TAT".equals(hd.getTrangThaiDon())) continue;
                if (trangThaiFilter.contains("Đã hủy") && !"DA_HUY".equals(hd.getTrangThaiDon())) continue;
            }

            if (ptFilter != null && !ptFilter.startsWith("Tất cả")) {
                if (ptFilter.contains("Tiền mặt") && !"TIEN_MAT".equals(hd.getPhuongThucThanhToan())) continue;
                if (ptFilter.contains("Chuyển khoản") && !"CHUYEN_KHOAN".equals(hd.getPhuongThucThanhToan())) continue;
            }

            dsHienThi.add(hd);
            if (!"DA_HUY".equals(hd.getTrangThaiDon())) {
                tongTienLoc += hd.tinhTongTien();
            }
        }

        modelHoaDon.setRowCount(0);
        int stt = 1;
        for (HoaDon hd : dsHienThi) {
            String loaiHienThi = "TAI_QUAN".equals(hd.getLoaiDon()) ? "Tại quán" : "Mang đi";
            String theOrStt = "TAI_QUAN".equals(hd.getLoaiDon())
                    ? String.format("Thẻ #%02d", hd.getSoTheRung())
                    : String.format("STT #%02d", hd.getSoThuTuNgay());
            String tenKH = hd.getKhachHang() != null ? hd.getKhachHang().getHoTen() : "(Khách vãng lai)";
            String sdtKH = hd.getKhachHang() != null ? hd.getKhachHang().getSoDienThoai() : "-";
            String tenNV = hd.getNhanVien() != null ? hd.getNhanVien().getHoTen() : "NV01";
            String pt = "TIEN_MAT".equals(hd.getPhuongThucThanhToan()) ? "Tiền mặt" : "VietQR";
            String tt = formatTrangThai(hd.getTrangThaiDon());

            modelHoaDon.addRow(new Object[]{
                    stt++,
                    hd.getMaHD(),
                    hd.getNgayLap().format(DATE_FMT),
                    loaiHienThi,
                    theOrStt,
                    tenKH,
                    sdtKH,
                    tenNV,
                    pt,
                    UITheme.formatMoney(hd.getGiamGia()),
                    UITheme.formatMoney(hd.tinhTongTien()),
                    tt
            });
        }

        lblTongSoHD.setText("Tìm thấy: " + dsHienThi.size() + " hóa đơn");
        lblTongTienLoc.setText("Tổng tiền (không gồm bill hủy): " + UITheme.formatMoneySpace(tongTienLoc));

        if (!dsHienThi.isEmpty()) {
            tblHoaDon.setRowSelectionInterval(0, 0);
            hienThiChiTietHoaDonDuocChon();
        } else {
            hoaDonDangChon = null;
            modelChiTiet.setRowCount(0);
            lblChiTietHeader.setText("CHI TIẾT LY NƯỚC (Không có dữ liệu phù hợp)");
        }
    }

    private String formatTrangThai(String ttCode) {
        if (ttCode == null) return "Chưa rõ";
        switch (ttCode) {
            case "DANG_PHA_CHE": return "Đang pha chế";
            case "CHO_GIAO": return "Chờ giao nước";
            case "HOAN_TAT": return "Hoàn tất";
            case "DA_HUY": return "Đã hủy";
            default: return ttCode;
        }
    }

    private void hienThiChiTietHoaDonDuocChon() {
        int row = tblHoaDon.getSelectedRow();
        if (row < 0 || row >= dsHienThi.size()) {
            hoaDonDangChon = null;
            modelChiTiet.setRowCount(0);
            return;
        }
        hoaDonDangChon = dsHienThi.get(row);
        lblChiTietHeader.setText("CHI TIẾT ĐƠN HÀNG [" + hoaDonDangChon.getMaHD() + "] - "
                + ("TAI_QUAN".equals(hoaDonDangChon.getLoaiDon()) ? "Thẻ rung #" + hoaDonDangChon.getSoTheRung() : "STT #" + hoaDonDangChon.getSoThuTuNgay())
                + " | Tổng: " + UITheme.formatMoney(hoaDonDangChon.tinhTongTien())
                + " (" + formatTrangThai(hoaDonDangChon.getTrangThaiDon()) + ")");

        modelChiTiet.setRowCount(0);
        int stt = 1;
        for (ChiTietHoaDon ct : hoaDonDangChon.getDsChiTiet()) {
            modelChiTiet.addRow(new Object[]{
                    stt++,
                    ct.getMon().getMaMon(),
                    ct.getMon().getTenMon(),
                    ct.getKichCo().getKyHieu(),
                    ct.getSoLuong(),
                    UITheme.formatMoney(ct.getDonGiaThucTe()),
                    ct.getMucDuong() + "%",
                    ChiTietHoaDon.formatMucDa(ct.getMucDa()),
                    ct.getGhiChuTopping() != null && !ct.getGhiChuTopping().isEmpty() ? ct.getGhiChuTopping() : "-",
                    UITheme.formatMoney(ct.tinhThanhTien())
            });
        }
    }

    private void inLaiHoaDonDangChon() {
        if (hoaDonDangChon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn trong danh sách để in lại!",
                    "Chưa chọn hóa đơn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder bill = new StringBuilder();
        bill.append("========================================\n");
        bill.append("               HOMU CAFE                \n");
        bill.append("           [BẢN IN LẠI BILL]            \n");
        bill.append("   Đ/c: 12 Nguyễn Văn Bảo, Gò Vấp, HCM  \n");
        bill.append("========================================\n");
        bill.append("Mã HĐ: ").append(hoaDonDangChon.getMaHD()).append("\n");
        bill.append("Ngày lập: ").append(hoaDonDangChon.getNgayLap().format(DATE_FMT)).append("\n");
        bill.append("Thu ngân: ").append(hoaDonDangChon.getNhanVien() != null ? hoaDonDangChon.getNhanVien().getHoTen() : "NV01").append("\n");
        if (hoaDonDangChon.getKhachHang() != null) {
            bill.append("Khách hàng: ").append(hoaDonDangChon.getKhachHang().getHoTen())
                .append(" (").append(hoaDonDangChon.getKhachHang().getSoDienThoai()).append(")\n");
        }
        if ("TAI_QUAN".equals(hoaDonDangChon.getLoaiDon())) {
            bill.append(">>> HÌNH THỨC: UỐNG TẠI QUÁN\n");
            bill.append(">>> THẺ RUNG SỐ: #").append(String.format("%02d", hoaDonDangChon.getSoTheRung())).append("\n");
        } else {
            bill.append(">>> HÌNH THỨC: MANG ĐI (TAKE-AWAY)\n");
            bill.append(">>> SỐ THỨ TỰ: #").append(String.format("%02d", hoaDonDangChon.getSoThuTuNgay())).append("\n");
        }
        bill.append("----------------------------------------\n");
        for (ChiTietHoaDon ct : hoaDonDangChon.getDsChiTiet()) {
            bill.append(String.format("%dx %s (Size %s)   %s\n",
                    ct.getSoLuong(), ct.getMon().getTenMon(), ct.getKichCo().getKyHieu(),
                    UITheme.formatMoney(ct.tinhThanhTien())));
            bill.append("   + ").append(ct.getMoTaTuyBien()).append("\n");
        }
        bill.append("----------------------------------------\n");
        bill.append("Tổng tiền hàng:      ").append(UITheme.formatMoney(hoaDonDangChon.tinhTongTienHang())).append("\n");
        bill.append("Chiết khấu điểm:    -").append(UITheme.formatMoney(hoaDonDangChon.getGiamGia())).append("\n");
        bill.append("TỔNG THANH TOÁN:     ").append(UITheme.formatMoney(hoaDonDangChon.tinhTongTien())).append("\n");
        bill.append("Phương thức:         ").append("TIEN_MAT".equals(hoaDonDangChon.getPhuongThucThanhToan()) ? "Tiền mặt" : "Chuyển khoản VietQR").append("\n");
        bill.append("Trạng thái đơn:      ").append(formatTrangThai(hoaDonDangChon.getTrangThaiDon())).append("\n");
        bill.append("========================================\n");
        bill.append("   Cảm ơn Quý khách & Hẹn gặp lại!     \n");

        JTextArea area = new JTextArea(bill.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        area.setBackground(new Color(254, 252, 232));
        area.setBorder(new EmptyBorder(10, 12, 10, 12));

        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "Bản In Lại Hóa Đơn Nhiệt (Re-print Bill)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void huyHoaDonDangChon() {
        if (hoaDonDangChon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn cần hủy!");
            return;
        }

        if ("DA_HUY".equals(hoaDonDangChon.getTrangThaiDon())) {
            JOptionPane.showMessageDialog(this, "Hóa đơn này đã ở trạng thái ĐÃ HỦY từ trước!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String passQL = JOptionPane.showInputDialog(this,
                "HỦY HÓA ĐƠN [" + hoaDonDangChon.getMaHD() + "] (Ràng buộc BR03)\n"
                        + "Tuyệt đối không xóa cứng hóa đơn khỏi CSDL!\n"
                        + "Vui lòng nhập mật khẩu Quản lý / Admin để xác nhận hủy (Mặc định: 123456):",
                "Xác nhận quyền Quản lý", JOptionPane.WARNING_MESSAGE);

        if (passQL == null) return;
        if (!"123456".equals(passQL.trim())) {
            JOptionPane.showMessageDialog(this, "Mật khẩu Quản lý không chính xác! Thao tác hủy bị từ chối.",
                    "Lỗi bảo mật", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String lyDo = JOptionPane.showInputDialog(this,
                "Nhập lý do hủy hóa đơn (VD: Khách đổi ý, Nhập sai món...):",
                "Lý do hủy đơn", JOptionPane.QUESTION_MESSAGE);
        if (lyDo == null || lyDo.trim().isEmpty()) {
            lyDo = "Khách yêu cầu hủy đơn";
        }

        hoaDonDangChon.capNhatTrangThai("DA_HUY");
        DataStore.notifyDataChanged();

        String thongBaoThe = "";
        if ("TAI_QUAN".equals(hoaDonDangChon.getLoaiDon()) && hoaDonDangChon.getSoTheRung() > 0) {
            thongBaoThe = "\n• Thẻ rung #" + String.format("%02d", hoaDonDangChon.getSoTheRung())
                    + " đã được giải phóng tự động về khay sạc.";
        }

        JOptionPane.showMessageDialog(this,
                "Đã hủy hóa đơn [" + hoaDonDangChon.getMaHD() + "] thành công!\n"
                        + "• Trạng thái mới: ĐÃ HỦY\n"
                        + "• Lý do ghi nhận: " + lyDo
                        + thongBaoThe,
                "Hủy hóa đơn thành công (BR03)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void xuatDanhSachHoaDonExcel() {
        if (dsHienThi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu hóa đơn để xuất!");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("DanhSach_HoaDon_HomuCafe.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(new FileWriter(f, StandardCharsets.UTF_8))) {
                pw.println('\ufeff' + "STT,MaHD,NgayLap,LoaiDon,SoTheOrSTT,KhachHang,SoDienThoai,NhanVien,PhuongThucTT,GiamGia,TongTien,TrangThai");
                int idx = 1;
                for (HoaDon hd : dsHienThi) {
                    pw.printf("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%.0f\",\"%.0f\",\"%s\"%n",
                            idx++,
                            hd.getMaHD(),
                            hd.getNgayLap().format(DATE_FMT),
                            "TAI_QUAN".equals(hd.getLoaiDon()) ? "Tai quan" : "Mang di",
                            "TAI_QUAN".equals(hd.getLoaiDon()) ? "The #" + hd.getSoTheRung() : "STT #" + hd.getSoThuTuNgay(),
                            hd.getKhachHang() != null ? hd.getKhachHang().getHoTen() : "Vang lai",
                            hd.getKhachHang() != null ? hd.getKhachHang().getSoDienThoai() : "",
                            hd.getNhanVien() != null ? hd.getNhanVien().getHoTen() : "",
                            hd.getPhuongThucThanhToan(),
                            hd.getGiamGia(),
                            hd.tinhTongTien(),
                            hd.getTrangThaiDon()
                    );
                }
                JOptionPane.showMessageDialog(this,
                        "Đã xuất " + dsHienThi.size() + " hóa đơn ra file thành công:\n" + f.getAbsolutePath(),
                        "Xuất Báo Cáo Thành Công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi ghi file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
