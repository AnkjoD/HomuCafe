package com.homucafe.app;

import com.homucafe.entity.TaiKhoan;
import com.homucafe.shared.ui.Theme;
import com.homucafe.util.DataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Màn hình Đăng nhập chuẩn hóa thương hiệu HomuCafe (FlatLaf).
 * Xác thực tài khoản người dùng qua TaiKhoan_DAO / DataStore.
 */
public class DialogDangNhap extends JDialog {
    private final JTextField txtUser = new JTextField("admin");
    private final JPasswordField txtPass = new JPasswordField("123456");
    private boolean dangNhapThanhCong = false;

    public DialogDangNhap(Frame parent) {
        super(parent, "Đăng nhập · HomuCafe Coffee POS", true);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.WHITE);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(Theme.WHITE);
        root.setBorder(new EmptyBorder(26, 32, 26, 32));

        // Header logo & title
        JPanel pnlHeader = new JPanel(new BorderLayout(12, 0));
        pnlHeader.setOpaque(false);

        JLabel mark = new JLabel(new Theme.LineIcon("brand", 26, Color.WHITE));
        mark.setOpaque(true);
        mark.setBackground(Theme.GREEN);
        mark.setHorizontalAlignment(SwingConstants.CENTER);
        mark.setPreferredSize(new Dimension(48, 50));

        pnlHeader.add(mark, BorderLayout.WEST);
        pnlHeader.add(Theme.stack(3,
                Theme.title("homu."),
                Theme.label("ĐĂNG NHẬP HỆ THỐNG POS & PAGER", 10, Theme.MUTED)
        ), BorderLayout.CENTER);
        root.add(pnlHeader, BorderLayout.NORTH);

        // Body form
        JPanel pnlForm = new JPanel(new GridLayout(0, 1, 0, 10));
        pnlForm.setOpaque(false);

        txtUser.putClientProperty("JTextField.placeholderText", "Tên đăng nhập (admin, thungan01, bar01)");
        txtUser.putClientProperty("FlatLaf.style", "arc: 12; margin: 9,12,9,12");

        txtPass.putClientProperty("JTextField.placeholderText", "Mật khẩu (123456)");
        txtPass.putClientProperty("FlatLaf.style", "arc: 12; margin: 9,12,9,12");

        pnlForm.add(field("Tên đăng nhập", txtUser));
        pnlForm.add(field("Mật khẩu", txtPass));

        JLabel lblHint = Theme.label("Tài khoản demo: admin / thungan01 / bar01 (Mật khẩu: 123456)", 10, Theme.MUTED);
        pnlForm.add(lblHint);

        root.add(pnlForm, BorderLayout.CENTER);

        // Footer buttons
        JButton btnThoat = Theme.button("Thoát", false);
        btnThoat.addActionListener(e -> System.exit(0));

        JButton btnLogin = Theme.button("Đăng nhập →", true);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.addActionListener(e -> xuLyDangNhap());

        KeyAdapter enterKey = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    xuLyDangNhap();
                }
            }
        };
        txtUser.addKeyListener(enterKey);
        txtPass.addKeyListener(enterKey);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(Theme.row(btnThoat, btnLogin), BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setMinimumSize(new Dimension(420, 360));
        setLocationRelativeTo(null);
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

    private void xuLyDangNhap() {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TaiKhoan tk = DataStore.dangNhap(u, p);
        if (tk != null) {
            dangNhapThanhCong = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập hoặc mật khẩu không chính xác!", "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
            txtPass.setText("");
            txtPass.requestFocusInWindow();
        }
    }

    public boolean isDangNhapThanhCong() {
        return dangNhapThanhCong;
    }
}
