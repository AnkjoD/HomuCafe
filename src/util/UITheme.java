package util;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Bảng màu & Thiết kế giao diện phong cách HOMU CAFE:
 * Tone màu ấm cúng, sang trọng lấy cảm hứng từ cà phê Nhật Bản:
 * - Nền kem ấm: #FAF6EF (Cream Warm)
 * - Nâu cafe rang đậm (Header/Brand): #43352C
 * - Nâu cafe sâu (Nav Bar): #322720
 * - Xanh Matcha rêu (Primary Button/Accent): #536B4F, hover #42593F
 * - Nâu Caramel / Cafe sữa (Secondary/Eyebrow): #826343
 * - Viền mềm: #E6DEC8, thẻ card: #FFFFFF
 * - Text chính: #43352C, Text phụ: #74685C
 */
public class UITheme {
    // Bảng màu HomuCafe Aesthetic
    public static final Color HOMU_BG_CREAM = new Color(250, 246, 239);    // #FAF6EF
    public static final Color HOMU_HEADER = new Color(67, 53, 44);         // #43352C
    public static final Color HOMU_NAV = new Color(50, 39, 32);            // #322720
    public static final Color HOMU_GREEN = new Color(83, 107, 79);         // #536B4F (Matcha)
    public static final Color HOMU_GREEN_HOVER = new Color(66, 89, 63);   // #42593F
    public static final Color HOMU_CARAMEL = new Color(130, 99, 67);       // #826343
    public static final Color HOMU_BORDER = new Color(230, 222, 200);      // #E6DEC8
    public static final Color HOMU_CARD_BG = Color.WHITE;
    public static final Color HOMU_LIGHT_PANEL = new Color(244, 239, 230); // #F4EFE6
    public static final Color HOMU_TEXT = new Color(67, 53, 44);           // #43352C
    public static final Color HOMU_MUTED = new Color(116, 104, 92);        // #74685C

    // Màu chức năng
    public static final Color COLOR_DANGER = new Color(194, 65, 12);       // Cam đất / Đỏ gạch
    public static final Color COLOR_WARNING = new Color(217, 119, 6);      // Cam hổ phách
    public static final Color COLOR_PURPLE = new Color(124, 58, 237);      // Tím gọi rung
    public static final Color COLOR_INFO = new Color(59, 130, 246);        // Xanh dương nhẹ

    // Màn hình Bar Monitor (Dark Theme cafe đêm)
    public static final Color BAR_BG = new Color(28, 25, 23);              // Stone 900
    public static final Color BAR_CARD = new Color(41, 37, 36);            // Stone 800

    // Typography
    public static final Font FONT_BRAND = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 17);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);

    private static final DecimalFormat MONEY_FMT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        MONEY_FMT = new DecimalFormat("#,###", symbols);
    }

    public static String formatMoney(double amount) {
        return MONEY_FMT.format(Math.round(amount)) + "đ";
    }

    public static String formatMoneySpace(double amount) {
        return MONEY_FMT.format(Math.round(amount)) + " đ";
    }

    /**
     * Nút bấm bo góc phong cách HomuCafe (Anti-aliasing, viền mượt, hiệu ứng hover/press)
     */
    public static JButton createRoundedButton(String text, Color bg, Color fg, int radius) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getBackground();
                if (!isEnabled()) {
                    fill = new Color(200, 195, 185);
                } else if (getModel().isPressed()) {
                    fill = fill.darker();
                } else if (getModel().isRollover()) {
                    fill = brighten(fill, 0.12f);
                }
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }

    public static Color brighten(Color c, float factor) {
        int r = Math.min(255, (int) (c.getRed() + (255 - c.getRed()) * factor));
        int g = Math.min(255, (int) (c.getGreen() + (255 - c.getGreen()) * factor));
        int b = Math.min(255, (int) (c.getBlue() + (255 - c.getBlue()) * factor));
        return new Color(r, g, b);
    }

    /**
     * Panel bo góc chuẩn mực với nền và viền ấm
     */
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private Color bgColor;
        private Color borderColor;
        private int borderThickness;

        public RoundedPanel(int radius, Color bgColor, Color borderColor, int borderThickness) {
            this.radius = radius;
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            this.borderThickness = borderThickness;
            setOpaque(false);
        }

        public void setBgColor(Color bgColor) {
            this.bgColor = bgColor;
            repaint();
        }

        public void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (bgColor != null) {
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            if (borderColor != null && borderThickness > 0) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(borderThickness));
                int offset = borderThickness / 2;
                g2.drawRoundRect(offset, offset, getWidth() - borderThickness - 1, getHeight() - borderThickness - 1, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Viền bo góc cho JTextField và components
     */
    public static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        private final int thickness;
        private final Insets insets;

        public RoundedBorder(Color color, int radius, int thickness, Insets insets) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
            this.insets = insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + thickness / 2, y + thickness / 2,
                    width - thickness - 1, height - thickness - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return insets;
        }

        @Override
        public Insets getBorderInsets(Component c, Insets i) {
            i.left = insets.left;
            i.top = insets.top;
            i.right = insets.right;
            i.bottom = insets.bottom;
            return i;
        }
    }

    public static JTextField createStyledTextField(String text) {
        JTextField tf = new JTextField(text);
        tf.setFont(FONT_REGULAR);
        tf.setForeground(HOMU_TEXT);
        tf.setBackground(Color.WHITE);
        tf.setBorder(new RoundedBorder(HOMU_BORDER, 8, 1, new Insets(6, 12, 6, 12)));
        return tf;
    }
}
