package com.homucafe.report;

import com.homucafe.shared.ui.Theme;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Locale;
import javax.swing.JPanel;

/** Chỉ vẽ dữ liệu được truyền vào; không truy vấn database hoặc tính nghiệp vụ. */
final class ReportChart extends JPanel {
    enum Kind { LINE, COLUMNS, BARS, DONUT, COMPARE }
    private static final Color[] COLORS = {Theme.GREEN, new Color(133, 164, 125), Theme.ORANGE,
            new Color(178, 158, 110), new Color(110, 149, 158)};
    private final Kind kind;
    private final String[] labels;
    private final double[] values;
    private final double[] comparison;
    private final String unit;

    ReportChart(Kind kind, String[] labels, double[] values, String unit) {
        this(kind, labels, values, null, unit);
    }

    ReportChart(Kind kind, String[] labels, double[] values, double[] comparison, String unit) {
        if (labels.length != values.length || (comparison != null && comparison.length != values.length)) {
            throw new IllegalArgumentException("Số nhãn và số giá trị phải bằng nhau.");
        }
        this.kind = kind;
        // Sao chép mảng để thay đổi từ bên ngoài không tự làm đổi dữ liệu đang vẽ.
        this.labels = labels.clone();
        this.values = values.clone();
        this.comparison = comparison == null ? null : comparison.clone();
        this.unit = unit;
        setOpaque(false);
        setPreferredSize(new Dimension(360, 250));
        getAccessibleContext().setAccessibleName("Biểu đồ " + unit);
        StringBuilder description = new StringBuilder("<html>");
        for (int i = 0; i < labels.length; i++) {
            description.append(labels[i]).append(": ").append(number(values[i])).append(" ").append(unit);
            if (comparison != null) description.append(" / ").append(number(comparison[i]));
            description.append("<br>");
        }
        setToolTipText(description.append("</html>").toString());
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        // Graphics2D là bút vẽ; dùng bản sao rồi dispose để không ảnh hưởng panel khác.
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            Theme.smooth(g);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            switch (kind) {
                case DONUT -> donut(g);
                case BARS -> bars(g);
                default -> plot(g);
            }
        } finally { g.dispose(); }
    }

    private static String number(double value) {
        return String.format(Locale.forLanguageTag("vi-VN"), value == Math.rint(value) ? "%.0f" : "%.2f", value);
    }

    private void centered(Graphics2D g, String text, int x, int y) {
        g.drawString(text, x - g.getFontMetrics().stringWidth(text) / 2, y);
    }

    private void plot(Graphics2D g) {
        int left = 34, right = getWidth() - 24, top = 30, bottom = getHeight() - 36;
        if (right <= left || bottom <= top || values.length == 0) return;
        double maximum = 0;
        for (double value : values) maximum = Math.max(maximum, value);
        if (comparison != null) for (double value : comparison) maximum = Math.max(maximum, value);
        // Làm tròn trục Y lên và chừa chỗ phía trên để ghi giá trị.
        double scale = maximum <= 5 ? 1 : maximum <= 25 ? 5 : maximum <= 100 ? 20 : 50;
        double ceiling = Math.max(scale, Math.ceil(maximum * 1.16 / scale) * scale);
        int height = bottom - top;
        for (int tick = 0; tick <= 4; tick++) {
            int y = bottom - height * tick / 4;
            g.setColor(Theme.LINE); g.drawLine(left, y, right, y);
            g.setColor(Theme.MUTED); g.drawString(number(ceiling * tick / 4), 0, y + 4);
        }
        double step = (right - left) / (double) values.length;
        int[] xs = new int[values.length], ys = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            xs[i] = (int) (left + step * (i + .5));
            ys[i] = bottom - (int) (values[i] / ceiling * height);
        }
        if (kind == Kind.LINE) {
            Path2D line = new Path2D.Double();
            for (int i = 0; i < xs.length; i++) {
                if (i == 0) line.moveTo(xs[i], ys[i]); else line.lineTo(xs[i], ys[i]);
            }
            Path2D area = (Path2D) line.clone();
            area.lineTo(xs[xs.length - 1], bottom); area.lineTo(xs[0], bottom); area.closePath();
            g.setColor(Theme.PALE); g.fill(area);
            g.setColor(Theme.GREEN); g.setStroke(new BasicStroke(2.5f)); g.draw(line);
        }
        for (int i = 0; i < xs.length; i++) {
            g.setColor(Theme.GREEN);
            if (kind == Kind.LINE) {
                g.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
                centered(g, number(values[i]), xs[i], ys[i] - 10);
            } else if (kind == Kind.COMPARE && comparison != null) {
                int width = Math.max(5, (int) step / 4);
                int secondY = bottom - (int) (comparison[i] / ceiling * height);
                g.fillRoundRect(xs[i] - width - 2, ys[i], width, bottom - ys[i], 5, 5);
                centered(g, number(values[i]), xs[i] - width / 2 - 2, ys[i] - 9);
                g.setColor(Theme.ORANGE);
                g.fillRoundRect(xs[i] + 2, secondY, width, bottom - secondY, 5, 5);
                centered(g, number(comparison[i]), xs[i] + width / 2 + 2, secondY - 9);
            } else {
                int width = Math.max(8, (int) (step * .55));
                g.fillRoundRect(xs[i] - width / 2, ys[i], width, bottom - ys[i], 6, 6);
                centered(g, number(values[i]), xs[i], ys[i] - 10);
            }
            g.setColor(Theme.MUTED); centered(g, labels[i], xs[i], bottom + 23);
        }
    }

    private void bars(Graphics2D g) {
        double maximum = 1;
        for (double value : values) maximum = Math.max(maximum, value);
        int step = (getHeight() - 8) / Math.max(1, values.length);
        int available = Math.max(1, getWidth() - 3);
        for (int i = 0; i < values.length; i++) {
            int y = 16 + step * i;
            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.setColor(Theme.INK); g.drawString(labels[i], 0, y);
            String value = number(values[i]) + " " + unit;
            g.drawString(value, getWidth() - g.getFontMetrics().stringWidth(value), y);
            g.setColor(Theme.PALE); g.fillRoundRect(0, y + 9, available, 10, 10, 10);
            g.setColor(COLORS[i % COLORS.length]);
            g.fillRoundRect(0, y + 9, (int) (available * values[i] / maximum), 10, 10, 10);
        }
    }

    private void donut(Graphics2D g) {
        double total = 0;
        for (double value : values) total += value;
        if (total <= 0) { g.drawString("Chưa có dữ liệu", 12, 30); return; }
        int diameter = Math.min(148, Math.min(getWidth() - 40, getHeight() - 24 - labels.length * 23));
        if (diameter < 30) return;
        int x = (getWidth() - diameter) / 2, start = 90;
        for (int i = 0; i < values.length; i++) {
            int angle = i == values.length - 1 ? 90 - start - 360 : -(int) Math.round(values[i] / total * 360);
            g.setColor(COLORS[i % COLORS.length]); g.fillArc(x, 0, diameter, diameter, start, angle); start += angle;
        }
        int inset = diameter / 4;
        g.setColor(Theme.WHITE); g.fillOval(x + inset, inset, diameter - 2 * inset, diameter - 2 * inset);
        g.setColor(Theme.INK); g.setFont(new Font("Segoe UI", Font.BOLD, 15));
        centered(g, number(total), getWidth() / 2, diameter / 2);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 10)); centered(g, unit, getWidth() / 2, diameter / 2 + 18);
        for (int i = 0; i < labels.length; i++) {
            int y = diameter + 24 + i * 23;
            g.setColor(COLORS[i % COLORS.length]); g.fillRoundRect(2, y - 9, 9, 9, 3, 3);
            g.setColor(Theme.INK); g.drawString(labels[i], 19, y);
            String text = number(values[i]) + " · " + Math.round(values[i] / total * 100) + "%";
            g.drawString(text, getWidth() - g.getFontMetrics().stringWidth(text), y);
        }
    }
}
