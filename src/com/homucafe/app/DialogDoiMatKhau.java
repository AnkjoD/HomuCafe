package com.homucafe.app;

import com.homucafe.entity.TaiKhoan;
import com.homucafe.shared.ui.Theme;
import com.homucafe.util.DataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Hộp thoại Đổi mật khẩu tài khoản người dùng.
 */
public class DialogDoiMatKhau extends JDialog {

    public DialogDoiMatKhau(Frame parent) {
        super(parent, "Đổi mật khẩu · HomuCafe", true);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.WHITE);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Theme.WHITE);
        root.setBorder(new EmptyBorder(22, 28, 22, 28));

        TaiKhoan current = DataStore.getTaiKhoanDangNhap();
        String username = current != null ? current.getTenDangNhap() : "admin";

        root.add(Theme.stack(3,
                Theme.title("Đổi mật khẩu tài khoản"),
                Theme.label("Tài khoản đang đăng nhập: " + username, 12, Theme.MUTED)
        ), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 10));
        form.setOpaque(false);

        JPasswordField txtPassCu = new JPasswordField();
        JPasswordField txtPassMoi = new JPasswordField();
        JPasswordField txtPassXacNhan = new JPasswordField();

        txtPassCu.putClientProperty("FlatLaf.style", "arc: 10");
        txtPassMoi.putClientProperty("FlatLaf.style", "arc: 10");
        txtPassXacNhan.putClientProperty("FlatLaf.style", "arc: 10");

        form.add(field("Mật khẩu hiện tại", txtPassCu));
        form.add(field("Mật khẩu mới", txtPassMoi));
        form.add(field("Xác nhận mật khẩu mới", txtPassXacNhan));

        root.add(form, BorderLayout.CENTER);

        JButton btnHuy = Theme.button("Hủy bỏ", false);
        btnHuy.addActionListener(e -> dispose());

        JButton btnLuu = Theme.button("Lưu mật khẩu mới", true);
        btnLuu.addActionListener(e -> {
            String cu = new String(txtPassCu.getPassword()).trim();
            String moi = new String(txtPassMoi.getPassword()).trim();
            String xn = new String(txtPassXacNhan.getPassword()).trim();

            if (cu.isEmpty() || moi.isEmpty() || xn.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ các trường!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!moi.equals(xn)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp với mật khẩu mới!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (moi.length() < 6) {
                JOptionPane.showMessageDialog(this, "Mật khẩu mới phải có ít nhất 6 ký tự!", "Lỗi bảo mật", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = DataStore.doiMatKhau(username, cu, moi);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã đổi mật khẩu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Mật khẩu hiện tại không chính xác!", "Lỗi xác thực", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(btnHuy);
        footer.add(btnLuu);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(getParent());
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
}
