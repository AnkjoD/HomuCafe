package com.homucafe.app;

import com.homucafe.entity.Ban;
import com.homucafe.entity.ChiTietPhieuDatBan;
import com.homucafe.entity.KhachHang;
import com.homucafe.entity.PhieuDatBan;
import com.homucafe.shared.ui.Theme;
import com.homucafe.util.DataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Hộp thoại Đặt bàn trước (PHIEU_DAT_BAN) cho HomuCafe.
 */
public class DialogDatBan extends JDialog {
    private final int soBan;
    private boolean daXacNhan = false;

    private JTextField txtTenKhach;
    private JTextField txtSDT;
    private JSpinner spnSoNguoi;
    private JTextField txtGioDen;
    private JTextField txtTienCoc;
    private JTextField txtGhiChu;

    public DialogDatBan(Window owner, int soBan) {
        super(owner, "Đặt bàn trước · Bàn " + String.format("%02d", soBan), ModalityType.APPLICATION_MODAL);
        this.soBan = soBan;
        initUI(owner);
    }

    private void initUI(Window owner) {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.WHITE);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(Theme.WHITE);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = Theme.title("Đặt chỗ trước cho Bàn " + String.format("%02d", soBan));
        JLabel subtitle = Theme.label("Tạo phiếu đặt bàn và giữ chỗ theo giờ hẹn", 12, Theme.MUTED);
        content.add(Theme.stack(4, title, subtitle), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 2, 14, 12));
        form.setOpaque(false);

        txtTenKhach = input("Ví dụ: Nguyễn Văn A");
        txtSDT = input("Số điện thoại khách (10 số)");
        spnSoNguoi = new JSpinner(new SpinnerNumberModel(4, 1, 20, 1));
        spnSoNguoi.setFont(new Font("Segoe UI", Font.BOLD, 14));

        LocalDateTime gioGoiY = LocalDateTime.now().plusHours(1);
        txtGioDen = input(gioGoiY.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        txtGioDen.setText(gioGoiY.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        txtTienCoc = input("100000");
        txtTienCoc.setText("100000");

        txtGhiChu = input("Yêu cầu riêng (sinh nhật, góc yên tĩnh...)");

        form.add(field("Tên khách hàng (*)", txtTenKhach));
        form.add(field("Số điện thoại (*)", txtSDT));
        form.add(field("Số lượng khách", spnSoNguoi));
        form.add(field("Giờ hẹn nhận bàn (dd/MM/yyyy HH:mm)", txtGioDen));
        form.add(field("Tiền đặt cọc (VND)", txtTienCoc));
        form.add(field("Ghi chú đặt bàn", txtGhiChu));

        content.add(form, BorderLayout.CENTER);

        JButton btnHuy = Theme.button("Hủy bỏ", false);
        btnHuy.addActionListener(e -> dispose());

        JButton btnLuu = Theme.button("✓ Lưu phiếu đặt bàn", true);
        btnLuu.addActionListener(e -> {
            String ten = txtTenKhach.getText().trim();
            String sdt = txtSDT.getText().trim();
            if (ten.isEmpty() || sdt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập họ tên và số điện thoại khách hàng!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int soKhach = (int) spnSoNguoi.getValue();
            double tienCoc = 0;
            try {
                tienCoc = Double.parseDouble(txtTienCoc.getText().replaceAll("[^0-9]", ""));
            } catch (Exception ignored) { }

            LocalDateTime gioDen = LocalDateTime.now().plusHours(1);
            try {
                gioDen = LocalDateTime.parse(txtGioDen.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            } catch (Exception ignored) { }

            // Tìm hoặc tạo khách hàng
            KhachHang kh = DataStore.timKhachHangTheoSDT(sdt);
            if (kh == null) {
                String maKH = "KH" + String.format("%04d", DataStore.getDsKhachHang().size() + 1);
                kh = new KhachHang(maKH, ten, sdt, 0);
                DataStore.themKhachHangVaoDB(kh);
            }

            String maPhieu = "PDB" + String.format("%03d", DataStore.getDsPhieuDatBan().size() + 1);
            com.homucafe.entity.NhanVien nvLap = (DataStore.getTaiKhoanDangNhap() != null && DataStore.getTaiKhoanDangNhap().getNhanVien() != null)
                    ? DataStore.getTaiKhoanDangNhap().getNhanVien()
                    : DataStore.getDsNhanVien().get(0);
            PhieuDatBan pdb = new PhieuDatBan(maPhieu, LocalDateTime.now(), gioDen, soKhach, tienCoc, txtGhiChu.getText().trim(), "DA_DAT", kh, nvLap);

            Ban ban = DataStore.timBanTheoSo(soBan);
            if (ban != null) {
                pdb.themChiTiet(new ChiTietPhieuDatBan(maPhieu, ban, "Bàn " + String.format("%02d", soBan)));
            }

            DataStore.savePhieuDatBan(pdb);
            daXacNhan = true;
            JOptionPane.showMessageDialog(this, "Đã tạo phiếu đặt bàn " + maPhieu + " và giữ chỗ Bàn " + String.format("%02d", soBan) + " thành công!", "Đặt bàn thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(Theme.label("Trạng thái bàn sẽ chuyển sang ĐÃ ĐẶT (Vàng).", 11, Theme.MUTED), BorderLayout.WEST);
        footer.add(Theme.row(btnHuy, btnLuu), BorderLayout.EAST);
        content.add(footer, BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setLocationRelativeTo(owner);
    }

    private static JPanel field(String label, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = Theme.label(label, 12, Theme.INK);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(lbl, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    private static JTextField input(String placeholder) {
        JTextField field = new JTextField();
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.putClientProperty("FlatLaf.style", "arc: 10");
        return field;
    }

    public boolean isDaXacNhan() {
        return daXacNhan;
    }
}
