package com.homucafe.app;

import com.homucafe.connectdb.ConnectDB;
import com.homucafe.shared.ui.Theme;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Hộp thoại hiển thị mã QR thanh toán VietQR động cho HomuCafe.
 * Hỗ trợ tải ảnh QR chuẩn EMVCo từ VietQR API (nếu có mạng) và tự động fallback về Java2D khi offline.
 */
public class DialogVietQR extends JDialog {
    private final String maHD;
    private final double soTien;
    private boolean daXacNhan = false;
    private volatile BufferedImage vietQrImage;

    public DialogVietQR(Window owner, String maHD, double soTien) {
        super(owner, "Thanh toán VietQR · HomuCafe", ModalityType.APPLICATION_MODAL);
        this.maHD = maHD;
        this.soTien = soTien;
        initUI(owner);
    }

    private void initUI(Window owner) {
        String bankBin = ConnectDB.getBankBin();
        String bankAccount = ConnectDB.getBankAccount();
        String bankName = ConnectDB.getBankName();
        String bankOwner = ConnectDB.getBankOwner();

        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.WHITE);

        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBackground(Theme.WHITE);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel lblTitle = Theme.label("QUÉT MÃ VIETQR THANH TOÁN", 17, Theme.INK);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        // QR Canvas: Hiển thị ảnh VietQR thật nếu tải được, fallback vẽ bằng Java2D khi offline
        JPanel qrCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.smooth(g2);
                int size = 220;
                int startX = (getWidth() - size) / 2;
                int startY = 10;

                // Nền QR bo góc
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(startX - 10, startY - 10, size + 20, size + 20, 16, 16);
                g2.setColor(Theme.LINE);
                g2.drawRoundRect(startX - 10, startY - 10, size + 20, size + 20, 16, 16);

                BufferedImage img = vietQrImage;
                if (img != null) {
                    int iw = img.getWidth();
                    int ih = img.getHeight();
                    if (iw > 0 && ih > 0) {
                        double scale = Math.min((double) size / iw, (double) size / ih);
                        int drawW = Math.max(1, (int) Math.round(iw * scale));
                        int drawH = Math.max(1, (int) Math.round(ih * scale));
                        int drawX = startX + (size - drawW) / 2;
                        int drawY = startY + (size - drawH) / 2;
                        g2.drawImage(img, drawX, drawY, drawW, drawH, null);
                    }
                } else {
                    g2.setColor(Theme.INK);
                    int cell = 10;
                    for (int r = 0; r < 22; r++) {
                        for (int c = 0; c < 22; c++) {
                            boolean corner = (r < 7 && c < 7) || (r < 7 && c >= 15) || (r >= 15 && c < 7);
                            if (corner) {
                                if (r == 0 || r == 6 || c == 0 || c == 6 || c == 15 || c == 21 || r == 15 || r == 21
                                        || (r >= 2 && r <= 4 && c >= 2 && c <= 4)
                                        || (r >= 2 && r <= 4 && c >= 17 && c <= 19)
                                        || (r >= 17 && r <= 19 && c >= 2 && c <= 4)) {
                                    g2.fillRect(startX + c * cell, startY + r * cell, cell, cell);
                                }
                            } else if ((r * 31 + c * 19 + (int) soTien + maHD.hashCode()) % 3 == 0) {
                                g2.fillRect(startX + c * cell, startY + r * cell, cell, cell);
                            }
                        }
                    }
                }
                g2.dispose();
            }
        };
        qrCanvas.setOpaque(false);
        qrCanvas.setPreferredSize(new Dimension(260, 250));

        // Tải ảnh QR chuẩn EMVCo Napas247 từ VietQR API qua SwingWorker (bất đồng bộ, timeout 3s)
        long tienNguyen = Math.max(0L, (long) Math.round(soTien));
        String urlStr = "https://img.vietqr.io/image/"
                + URLEncoder.encode(bankBin, StandardCharsets.UTF_8)
                + "-" + URLEncoder.encode(bankAccount, StandardCharsets.UTF_8)
                + "-compact2.png?amount=" + tienNguyen
                + "&addInfo=" + URLEncoder.encode(maHD, StandardCharsets.UTF_8)
                + "&accountName=" + URLEncoder.encode(bankOwner, StandardCharsets.UTF_8);

        SwingWorker<BufferedImage, Void> qrWorker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                URLConnection conn = URI.create(urlStr).toURL().openConnection();
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setRequestProperty("User-Agent", "HomuCafe-POS/1.0");
                try (InputStream is = conn.getInputStream()) {
                    return ImageIO.read(is);
                }
            }

            @Override
            protected void done() {
                try {
                    BufferedImage img = get();
                    if (img != null) {
                        vietQrImage = img;
                        if (isDisplayable()) {
                            qrCanvas.repaint();
                        }
                    }
                } catch (Exception ignored) {
                    // Fallback Java2D sẵn có khi mất mạng hoặc timeout
                }
            }
        };
        qrWorker.execute();

        JPanel pnlInfo = new JPanel();
        pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.Y_AXIS));
        pnlInfo.setOpaque(false);

        JLabel lblBank = Theme.label("Ngân hàng: " + bankName, 12, Theme.MUTED);
        JLabel lblAcc = Theme.label("Chủ TK: " + bankOwner + " (" + bankAccount + ")", 12, Theme.MUTED);
        JLabel lblAmount = Theme.label("Số tiền: " + String.format("%,.0f đ", soTien), 18, Theme.GREEN);
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel lblContent = Theme.label("Nội dung: " + maHD, 13, Theme.INK);
        lblContent.setFont(new Font("Segoe UI", Font.BOLD, 13));

        lblBank.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAcc.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAmount.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblContent.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlInfo.add(lblBank);
        pnlInfo.add(Box.createVerticalStrut(3));
        pnlInfo.add(lblAcc);
        pnlInfo.add(Box.createVerticalStrut(6));
        pnlInfo.add(lblAmount);
        pnlInfo.add(Box.createVerticalStrut(3));
        pnlInfo.add(lblContent);

        JButton btnHuy = Theme.button("Hủy bỏ", false);
        btnHuy.addActionListener(e -> dispose());

        JButton btnXacNhan = Theme.button("✓ ĐÃ NHẬN ĐỦ TIỀN CHUYỂN KHOẢN", true);
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnXacNhan.addActionListener(e -> {
            daXacNhan = true;
            dispose();
        });

        JPanel south = new JPanel(new BorderLayout(0, 12));
        south.setOpaque(false);
        south.add(pnlInfo, BorderLayout.NORTH);
        south.add(Theme.row(btnHuy, btnXacNhan), BorderLayout.SOUTH);

        p.add(lblTitle, BorderLayout.NORTH);
        p.add(qrCanvas, BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);

        setContentPane(p);
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isDaXacNhan() {
        return daXacNhan;
    }
}
