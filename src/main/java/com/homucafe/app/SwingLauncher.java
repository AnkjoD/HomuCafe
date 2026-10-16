package com.homucafe.app;
import java.awt.Dimension;
import com.homucafe.shared.ui.Theme;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class SwingLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Toàn bộ việc tạo/cập nhật Swing chạy trên Event Dispatch Thread.
            Theme.install();
            JFrame frame = new JFrame("HomuCafe · Quản lý quán cà phê · Bản giao diện");

            frame.setSize(1380, 880);
            frame.setMinimumSize(new Dimension(1180, 760));
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // WorkspaceView chứa thanh menu và các màn hình chức năng.
            frame.setContentPane(new WorkspaceView());
            frame.setVisible(true);
        });
    }
}
