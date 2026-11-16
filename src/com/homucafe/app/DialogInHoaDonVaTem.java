package com.homucafe.app;

import com.homucafe.connectdb.ConnectDB;
import com.homucafe.entity.ChiTietHoaDon;
import com.homucafe.entity.HoaDon;
import com.homucafe.shared.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Hộp thoại hiển thị và in Hóa đơn nhiệt (80mm) cùng Tem dán ly (Sticker) cho HomuCafe.
 */
public class DialogInHoaDonVaTem extends JDialog {
    private final HoaDon hoaDon;

    public DialogInHoaDonVaTem(Window owner, HoaDon hoaDon) {
        super(owner, "In hóa đơn & Tem dán ly · " + hoaDon.getMaHoaDon(), ModalityType.APPLICATION_MODAL);
        this.hoaDon = hoaDon;
        initUI(owner);
    }

    private void initUI(Window owner) {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.WHITE);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Hóa đơn 80mm
        JPanel pnlBill = new JPanel(new BorderLayout());
        pnlBill.setBackground(Theme.WHITE);
        pnlBill.setBorder(new EmptyBorder(16, 20, 16, 20));

        JTextArea txtBill = new JTextArea(generateBillContent(hoaDon));
        txtBill.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtBill.setEditable(false);
        txtBill.setBackground(new Color(254, 252, 232)); // Màu giấy nhiệt ấm
        txtBill.setBorder(new EmptyBorder(12, 16, 12, 16));
        pnlBill.add(new JScrollPane(txtBill), BorderLayout.CENTER);

        JButton btnInHoaDon = Theme.button("In Hóa Đơn (80mm)", true);
        btnInHoaDon.addActionListener(e -> inHoaDonNhiet(txtBill));
        JPanel pnlBillBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBillBottom.setOpaque(false);
        pnlBillBottom.add(btnInHoaDon);
        pnlBill.add(pnlBillBottom, BorderLayout.SOUTH);

        tabbedPane.addTab("Hóa Đơn Nhiệt (80mm)", pnlBill);

        // Tab 2: Tem dán ly (Stickers)
        JPanel pnlTem = new JPanel(new BorderLayout());
        pnlTem.setBackground(Theme.WHITE);
        pnlTem.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel pnlTemList = new JPanel();
        pnlTemList.setLayout(new BoxLayout(pnlTemList, BoxLayout.Y_AXIS));
        pnlTemList.setBackground(Theme.WHITE);

        int totalCups = hoaDon.getTongSoLy();
        int currentCup = 1;
        for (ChiTietHoaDon ct : hoaDon.getDsChiTiet()) {
            for (int k = 1; k <= ct.getSoLuong(); k++) {
                pnlTemList.add(createCupSticker(ct, currentCup++, totalCups));
                pnlTemList.add(Box.createVerticalStrut(12));
            }
        }

        JScrollPane scrollTem = new JScrollPane(pnlTemList);
        scrollTem.setBorder(BorderFactory.createEmptyBorder());
        pnlTem.add(scrollTem, BorderLayout.CENTER);

        JButton btnInTem = Theme.button("In Toàn Bộ Tem Dán Ly", true);
        btnInTem.addActionListener(e -> inToanBoTemDanLy(totalCups));
        JPanel pnlTemBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlTemBottom.setOpaque(false);
        pnlTemBottom.add(btnInTem);
        pnlTem.add(pnlTemBottom, BorderLayout.SOUTH);

        tabbedPane.addTab("Tem Dán Ly (Sticker " + totalCups + " ly)", pnlTem);

        add(tabbedPane, BorderLayout.CENTER);

        // Nút Đóng
        JButton btnDong = Theme.button("Đóng", false);
        btnDong.addActionListener(e -> dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Theme.PAPER);
        footer.add(btnDong);
        add(footer, BorderLayout.SOUTH);

        setSize(480, 600);
        setLocationRelativeTo(owner);
    }

    private JPanel createCupSticker(ChiTietHoaDon ct, int cupIndex, int totalCups) {
        JPanel sticker = new JPanel(new BorderLayout(8, 6));
        sticker.setBackground(new Color(255, 255, 255));
        sticker.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 200, 185), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        sticker.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Header tem: Số ly / Tổng ly & Thẻ rung / Bàn
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel lblCup = Theme.label(String.format("TEM [%d/%d]", cupIndex, totalCups), 12, Theme.MUTED);
        lblCup.setFont(new Font("Segoe UI", Font.BOLD, 12));

        String posStr = hoaDon.getSoTheRung() > 0 ? "Thẻ #" + hoaDon.getSoTheRung() : hoaDon.getBanPhucVu();
        JLabel lblPos = Theme.label(posStr, 13, Theme.GREEN);
        lblPos.setFont(new Font("Segoe UI", Font.BOLD, 13));

        top.add(lblCup, BorderLayout.WEST);
        top.add(lblPos, BorderLayout.EAST);

        // Tên món & Size
        JLabel lblTenMon = Theme.label(ct.getMon().getTenMon() + " (Size " + ct.getKichCo().name() + ")", 14, Theme.INK);
        lblTenMon.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Tùy biến đường đá topping
        String custom = ct.getGhiChu();
        if (custom == null || custom.isEmpty()) custom = "Chuẩn công thức";
        JLabel lblCustom = Theme.label("• " + custom, 12, Theme.ORANGE);

        // Footer tem: Mã đơn & giờ
        String footerStr = hoaDon.getMaHoaDon() + " · " + hoaDon.getNgayLap().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        JLabel lblFooter = Theme.label(footerStr, 10, Theme.MUTED);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.add(lblTenMon);
        center.add(Box.createVerticalStrut(3));
        center.add(lblCustom);

        sticker.add(top, BorderLayout.NORTH);
        sticker.add(center, BorderLayout.CENTER);
        sticker.add(lblFooter, BorderLayout.SOUTH);
        return sticker;
    }

    private static String centerText(String text, int width) {
        if (text == null) return "";
        if (text.length() >= width) return text.substring(0, width);
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text;
    }

    private String generateBillContent(HoaDon hd) {
        StringBuilder bill = new StringBuilder();
        bill.append("========================================\n");
        bill.append(centerText(ConnectDB.getShopName().toUpperCase(), 40)).append("\n");
        bill.append(centerText("COFFEE POS & PAGER SYSTEM", 40)).append("\n");
        bill.append(centerText(ConnectDB.getShopAddress(), 40)).append("\n");
        bill.append(centerText("Hotline: " + ConnectDB.getShopPhone() + " · Wifi: homucafe", 40)).append("\n");
        bill.append("========================================\n");
        bill.append("Mã HĐ:       ").append(hd.getMaHoaDon()).append("\n");
        bill.append("Thời gian:   ").append(hd.getNgayLap().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        bill.append("Thu ngân:    ").append(hd.getNhanVien() != null ? hd.getNhanVien().getHoTen() : "Thu ngân").append("\n");
        bill.append("Khách hàng:  ").append(hd.getKhachHang() != null ? hd.getKhachHang().getHoTen() : "Khách vãng lai").append("\n");
        if ("TAI_QUAN".equals(hd.getLoaiDon())) {
            bill.append(">>> PHỤC VỤ: ").append(hd.getBanPhucVu().toUpperCase()).append("\n");
            bill.append(">>> THẺ RUNG SỐ: #").append(String.format("%02d", hd.getSoTheRung())).append("\n");
        } else {
            bill.append(">>> HÌNH THỨC: MANG ĐI (TAKE-AWAY)\n");
            bill.append(">>> SỐ THỨ TỰ: #").append(String.format("%02d", hd.getSoThuTuNgay() > 0 ? hd.getSoThuTuNgay() : 1)).append("\n");
        }
        bill.append("----------------------------------------\n");
        bill.append(String.format("%-22s %3s %12s\n", "TÊN MÓN", "SL", "THÀNH TIỀN"));
        bill.append("----------------------------------------\n");

        for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
            String ten = ct.getMon().getTenMon() + " (" + ct.getKichCo().name() + ")";
            if (ten.length() > 22) ten = ten.substring(0, 20) + "..";
            bill.append(String.format("%-22s %3d %,12.0f đ\n", ten, ct.getSoLuong(), ct.getThanhTien()));
            if (ct.getGhiChu() != null && !ct.getGhiChu().isEmpty()) {
                bill.append("  ↳ ").append(ct.getGhiChu()).append("\n");
            }
        }

        bill.append("----------------------------------------\n");
        bill.append(String.format("Tổng tiền hàng:             %,12.0f đ\n", hd.tinhTongTienHang()));
        if (hd.getGiamGia() > 0) {
            bill.append(String.format("Giảm giá / Voucher:        -%,12.0f đ\n", hd.getGiamGia()));
        }
        bill.append("----------------------------------------\n");
        bill.append(String.format("TỔNG THANH TOÁN:            %,12.0f đ\n", hd.getTongTien()));
        bill.append(String.format("Tiền khách đưa:             %,12.0f đ\n", hd.getTienKhachDua()));
        bill.append(String.format("Tiền thối lại:              %,12.0f đ\n", hd.getTienThoi()));
        String pt = hd.getPhuongThucThanhToan();
        if (pt == null || pt.contains("TIEN_MAT")) pt = "Tiền mặt (Đã thu 100%)";
        else pt = "Chuyển khoản VietQR (Đã nhận)";
        bill.append("Phương thức:                ").append(pt).append("\n");
        bill.append("========================================\n");
        bill.append("     ĐƠN HÀNG ĐÃ GỬI SANG QUẦY BAR      \n");
        bill.append("  Quý khách vui lòng giữ thẻ rung, khi  \n");
        bill.append("  thẻ rung & phát sáng mời nhận nước!   \n");
        bill.append("       Xin cảm ơn & Hẹn gặp lại!        \n");
        bill.append("========================================\n");
        return bill.toString();
    }

    private void inHoaDonNhiet(JTextArea textArea) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("HomuCafe_Bill_" + hoaDon.getMaHoaDon());

        java.awt.print.PageFormat pf = job.defaultPage();
        java.awt.print.Paper paper = new java.awt.print.Paper();
        // Khổ giấy in nhiệt 80mm x 297mm, chừa lề nhỏ 4pt tránh Windows cắt chữ
        double wPt = 80.0 * 72.0 / 25.4;
        double hPt = 297.0 * 72.0 / 25.4;
        paper.setSize(wPt, hPt);
        paper.setImageableArea(4, 4, wPt - 8, hPt - 8);
        pf.setPaper(paper);
        pf.setOrientation(java.awt.print.PageFormat.PORTRAIT);

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 8));
            int y = 14;
            for (String line : textArea.getText().split("\n")) {
                g2d.drawString(line, 4, y);
                y += 11;
            }
            return Printable.PAGE_EXISTS;
        }, pf);

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this, "Đã gửi lệnh in hóa đơn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi in: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void inToanBoTemDanLy(int totalCups) {
        if (totalCups <= 0) {
            JOptionPane.showMessageDialog(this, "Không có tem nào để in!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<List<String>> stickers = new ArrayList<>();
        String posStr = hoaDon.getSoTheRung() > 0 ? "The #" + hoaDon.getSoTheRung() : hoaDon.getBanPhucVu();
        String timeStr = hoaDon.getNgayLap().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        int cupIndex = 1;

        for (ChiTietHoaDon ct : hoaDon.getDsChiTiet()) {
            for (int k = 1; k <= ct.getSoLuong(); k++) {
                List<String> lines = new ArrayList<>();
                lines.add(String.format("TEM [%d/%d]  |  %s", cupIndex++, totalCups, posStr));
                lines.add("--------------------------------");
                lines.add(ct.getMon().getTenMon() + " (" + ct.getKichCo().name() + ")");
                String custom = ct.getGhiChu();
                if (custom == null || custom.isEmpty()) custom = "Chuan cong thuc";
                // Chia dòng ghi chú tránh tràn khổ tem nhãn nhỏ (50x30mm)
                while (custom.length() > 30) {
                    int cut = custom.lastIndexOf(' ', 30);
                    if (cut <= 0) cut = 30;
                    lines.add("* " + custom.substring(0, cut).trim());
                    custom = custom.substring(cut).trim();
                }
                if (!custom.isEmpty()) {
                    lines.add("* " + custom);
                }
                lines.add("--------------------------------");
                lines.add(hoaDon.getMaHoaDon() + " - " + timeStr);
                stickers.add(lines);
            }
        }

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("HomuCafe_TemLy_" + hoaDon.getMaHoaDon());

        java.awt.print.PageFormat pf = job.defaultPage();
        java.awt.print.Paper paper = new java.awt.print.Paper();
        // Khổ tem nhiệt tiêu chuẩn 50mm x 30mm (1mm = 72 / 25.4 pt ~ 2.8346 pt)
        double wPt = 50.0 * 72.0 / 25.4;   // ~141.7 pt
        double hPt = 30.0 * 72.0 / 25.4;   // ~85.0 pt
        paper.setSize(wPt, hPt);
        paper.setImageableArea(4, 4, wPt - 8, hPt - 8);
        pf.setPaper(paper);
        pf.setOrientation(java.awt.print.PageFormat.PORTRAIT);

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex >= stickers.size()) return Printable.NO_SUCH_PAGE;
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            List<String> lines = stickers.get(pageIndex);
            int y = 14;
            for (int i = 0; i < lines.size(); i++) {
                g2d.setFont(new Font("Monospaced", (i == 0 || i == 2) ? Font.BOLD : Font.PLAIN, i == 2 ? 10 : 8));
                g2d.drawString(lines.get(i), 6, y);
                y += (i == 2 ? 13 : 11);
            }
            return Printable.PAGE_EXISTS;
        }, pf);

        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this,
                        "Đã gửi lệnh in " + stickers.size() + " tem dán ly thành công!",
                        "In Tem Thành Công", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi in tem: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
