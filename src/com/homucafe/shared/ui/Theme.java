package com.homucafe.shared.ui;

import com.formdev.flatlaf.FlatLightLaf;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Màu sắc, font và các thành phần giao diện dùng chung theo phong cách HomuCafe (FlatLaf & Java2D).
 */
public final class Theme {
    public static final Color PAPER = new Color(246, 245, 239);
    public static final Color WHITE = new Color(255, 254, 250);
    public static final Color INK = new Color(36, 61, 53);
    public static final Color GREEN = new Color(40, 87, 71);
    public static final Color MUTED = new Color(115, 127, 116);
    public static final Color LINE = new Color(224, 229, 218);
    public static final Color PALE = new Color(233, 239, 226);
    public static final Color ORANGE = new Color(177, 92, 54);

    private Theme() { }

    public static void install() {
        try {
            FlatLightLaf.setup();
        } catch (Throwable t) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
        }

        Font font = new Font("Segoe UI", Font.PLAIN, 13);
        UIManager.put("defaultFont", font);
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("Component.focusColor", GREEN);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Button.margin", new Insets(10, 16, 10, 16));
        UIManager.put("ComboBox.padding", new Insets(9, 12, 9, 12));
        UIManager.put("ScrollBar.width", 9);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("TableHeader.background", new Color(244, 246, 240));
        UIManager.put("Panel.background", WHITE);
        UIManager.put("Label.foreground", INK);
        UIManager.put("TextField.background", WHITE);
        UIManager.put("TextField.foreground", INK);
        UIManager.put("Table.selectionBackground", PALE);
        UIManager.put("Table.selectionForeground", INK);
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
    }

    public static JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    public static JLabel title(String text) {
        JLabel label = label(text, 32, INK);
        label.setFont(new Font("Cambria", Font.PLAIN, 34));
        return label;
    }

    public static JPanel stack(int gap, Component... components) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JComponent jc) {
                jc.setAlignmentX(Component.LEFT_ALIGNMENT);
            }
            if (i > 0) panel.add(Box.createVerticalStrut(gap));
            panel.add(components[i]);
        }
        return panel;
    }

    public static JPanel row(Component... components) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        for (Component component : components) panel.add(component);
        return panel;
    }

    public static JButton button(String text, boolean primary) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    smooth(g2);
                    if (primary) {
                        g2.setColor(getModel().isPressed() ? GREEN.darker() : getModel().isRollover() ? new Color(50, 105, 85) : GREEN);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                        setForeground(Color.WHITE);
                    } else {
                        g2.setColor(getModel().isRollover() ? PALE : WHITE);
                        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                        g2.setColor(LINE);
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                        setForeground(INK);
                    }
                    super.paintComponent(g2);
                } finally {
                    g2.dispose();
                }
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setMargin(new Insets(10, 16, 10, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JPanel card(String name, String value, String note) {
        JPanel card = surface(new BorderLayout(10, 12));
        card.setBorder(new EmptyBorder(20, 22, 20, 22));
        JLabel number = label(value, 29, INK);
        number.setFont(new Font("Segoe UI", Font.BOLD, value.length() > 13 ? 20 : 29));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(label(name, 11, MUTED));
        header.add(new JLabel(new LineIcon(name.contains("TIỀN") ? "Thu chi" : name.contains("BÀN") ? "Sơ đồ bàn" : name.contains("THẺ") ? "Thẻ rung" : "Bán hàng", 20, MUTED)), BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);
        card.add(number, BorderLayout.CENTER);
        card.add(label(note, 11, MUTED), BorderLayout.SOUTH);
        card.setPreferredSize(new Dimension(240, 140));
        return card;
    }

    public static JPanel cards(JPanel... cards) {
        JPanel panel = new JPanel(new GridLayout(1, cards.length, 15, 0));
        panel.setOpaque(false);
        for (JPanel card : cards) panel.add(card);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        return panel;
    }

    public static JPanel page(String section, String title, String subtitle) {
        JPanel page = new JPanel(new BorderLayout(0, 24));
        page.setBackground(PAPER);
        page.setBorder(new EmptyBorder(28, 30, 24, 30));
        page.add(stack(9, label(section.toUpperCase(), 10, ORANGE), title(title),
                label(subtitle, 12, MUTED)), BorderLayout.NORTH);
        return page;
    }

    public static JTable table(String[] columns, Object[][] data) {
        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(54);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setGridColor(LINE);
        table.setBackground(WHITE);
        table.setForeground(INK);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10));
        table.getTableHeader().setBackground(new Color(244, 246, 240));
        table.getTableHeader().setForeground(INK);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setBorder(new EmptyBorder(0, 16, 0, 12));
                setBackground(selected ? PALE : row % 2 == 0 ? WHITE : new Color(250, 251, 247));
                setForeground(col == 0 ? MUTED : INK);
                setFont(new Font("Segoe UI", col == 1 ? Font.BOLD : Font.PLAIN, 12));
                return this;
            }

            @Override protected void paintComponent(Graphics graphics) {
                String text = getText();
                java.util.Set<String> statusSet = java.util.Set.of(
                        "Đang sử dụng", "Không sử dụng",
                        "Đang làm việc", "Đã nghỉ",
                        "Đang hoạt động", "Đã tắt", "Hết hạn", "Sắp bắt đầu",
                        "Đang phục vụ", "Trống",
                        "Đã thanh toán", "Chờ pha", "Đang pha", "Sẵn sàng", "Hoàn thành",
                        "Sắp hết", "Đủ sử dụng", "Đang bán", "Ngừng bán", "Tạm hết"
                );
                if (statusSet.contains(text)) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    try {
                        smooth(g);
                        g.setColor(getBackground());
                        g.fillRect(0, 0, getWidth(), getHeight());
                        boolean warn = text.equals("Chờ pha") || text.equals("Sắp hết")
                                || text.equals("Không sử dụng") || text.equals("Đã nghỉ")
                                || text.equals("Đã tắt") || text.equals("Hết hạn")
                                || text.equals("Ngừng bán") || text.equals("Tạm hết");
                        boolean ready = text.equals("Sẵn sàng") || text.equals("Đã thanh toán")
                                || text.equals("Đang làm việc") || text.equals("Đang hoạt động")
                                || text.equals("Đang sử dụng") || text.equals("Đang bán");
                        g.setColor(warn ? new Color(250, 237, 216) : ready ? PALE : new Color(225, 235, 245));
                        g.fillRoundRect(10, 13, Math.min(getWidth() - 14, g.getFontMetrics().stringWidth(text) + 24), 28, 12, 12);
                        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
                        g.setColor(warn ? ORANGE : ready ? GREEN : new Color(60, 100, 160));
                        g.drawString(text, 22, 31);
                    } finally {
                        g.dispose();
                    }
                } else {
                    super.paintComponent(graphics);
                }
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
        return table;
    }

    public static JPanel tablePanel(String title, JTable table) {
        JPanel panel = surface(new BorderLayout());
        panel.setBorder(new EmptyBorder(3, 3, 4, 3));
        JLabel heading = label(title, 15, INK);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 15));
        heading.setBorder(new EmptyBorder(19, 18, 19, 18));
        panel.add(heading, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setColumnHeaderView(table.getTableHeader());
        scroll.getViewport().setBackground(WHITE);
        scroll.setOpaque(false);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    public static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public static JPanel surface(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    smooth(g);
                    g.setColor(getBackground());
                    g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    g.setColor(LINE);
                    g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                } finally {
                    g.dispose();
                }
            }
        };
        panel.setOpaque(false);
        panel.setBackground(WHITE);
        return panel;
    }

    /**
     * Nút menu điều hướng phẳng, bo góc hiện đại, tự vẽ nền xanh khi active và hiệu ứng hover mượt mà.
     */
    public static class NavButton extends JButton {
        private boolean active = false;

        public NavButton(String text) {
            super(text);
            setIcon(new LineIcon(text, 19, null));
            setIconTextGap(12);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setHorizontalAlignment(SwingConstants.LEFT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(10, 16, 10, 16));
        }

        public void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        public boolean isActive() {
            return active;
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                smooth(g2);
                if (active) {
                    g2.setColor(GREEN);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    setForeground(Color.WHITE);
                } else if (getModel().isRollover()) {
                    g2.setColor(PALE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    setForeground(INK);
                } else {
                    setForeground(INK);
                }
                super.paintComponent(g2);
            } finally {
                g2.dispose();
            }
        }
    }

    public static NavButton navigationButton(String name) {
        return new NavButton(name);
    }

    /**
     * Icon vector vẽ tay bằng Java2D, tự chuyển màu theo màu chữ của nút.
     */
    public static final class LineIcon implements Icon {
        private final String name;
        private final int size;
        private final Color color;

        public LineIcon(String name, int size, Color color) {
            this.name = name;
            this.size = size;
            this.color = color;
        }

        public int getIconWidth() { return size; }
        public int getIconHeight() { return size; }

        public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                smooth(g);
                g.translate(x, y);
                g.scale(size / 24.0, size / 24.0);
                g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(color == null ? c.getForeground() : color);

                if (name.equals("search")) {
                    g.drawOval(3, 3, 12, 12);
                    g.drawLine(14, 14, 21, 21);
                } else if (name.contains("bàn") || name.contains("Bàn")) {
                    g.drawRoundRect(3, 6, 18, 6, 2, 2);
                    g.drawLine(6, 12, 6, 21);
                    g.drawLine(18, 12, 18, 21);
                    g.drawLine(6, 17, 18, 17);
                } else if (name.contains("pha") || name.contains("Pha") || name.equals("brand")) {
                    g.drawRoundRect(4, 7, 13, 13, 4, 4);
                    g.drawArc(14, 9, 8, 7, -90, 180);
                    g.drawLine(7, 2, 7, 4);
                    g.drawLine(12, 2, 12, 4);
                } else if (name.contains("Nhân") || name.contains("Khách")) {
                    g.drawOval(8, 3, 8, 8);
                    g.drawArc(4, 13, 16, 14, 0, 180);
                    g.drawLine(4, 20, 20, 20);
                } else if (name.contains("Thu chi") || name.contains("Ca")) {
                    g.drawRoundRect(3, 5, 18, 15, 3, 3);
                    g.drawRoundRect(14, 10, 8, 6, 2, 2);
                    g.fillOval(16, 12, 2, 2);
                } else if (name.contains("Báo")) {
                    g.drawLine(4, 3, 4, 21);
                    g.drawLine(4, 21, 22, 21);
                    g.drawLine(9, 17, 9, 12);
                    g.drawLine(14, 17, 14, 6);
                    g.drawLine(19, 17, 19, 9);
                } else if (name.contains("Voucher")) {
                    g.drawRoundRect(3, 5, 18, 14, 3, 3);
                    g.drawLine(9, 15, 15, 9);
                    g.drawOval(7, 8, 3, 3);
                    g.drawOval(14, 13, 3, 3);
                } else if (name.contains("Hóa đơn") || name.contains("đơn")) {
                    g.drawRoundRect(4, 2, 16, 20, 2, 2);
                    g.drawLine(8, 7, 16, 7);
                    g.drawLine(8, 11, 16, 11);
                    g.drawLine(8, 15, 13, 15);
                } else if (name.contains("Thực đơn") || name.contains("Món") || name.contains("Bán")) {
                    g.drawRoundRect(5, 3, 14, 18, 2, 2);
                    g.drawLine(9, 8, 15, 8);
                    g.drawLine(9, 12, 15, 12);
                    g.drawLine(9, 16, 13, 16);
                } else {
                    g.drawRoundRect(3, 3, 7, 7, 2, 2);
                    g.drawRoundRect(14, 3, 7, 7, 2, 2);
                    g.drawRoundRect(3, 14, 7, 7, 2, 2);
                    g.drawRoundRect(14, 14, 7, 7, 2, 2);
                }
            } finally {
                g.dispose();
            }
        }
    }
}
