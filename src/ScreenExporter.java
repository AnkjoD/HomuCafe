import com.homucafe.app.WorkspaceView;
import com.homucafe.shared.ui.Theme;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;

/**
 * Công cụ chụp ảnh màn hình tự động toàn bộ giao diện HomuCafe.
 */
public class ScreenExporter {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        Theme.install();

        File outDir = new File("HomuCafe_Screenshots");
        outDir.mkdirs();

        int w = 1380;
        int h = 880;

        WorkspaceView ws = new WorkspaceView();
        ws.setSize(w, h);
        layoutComponent(ws);

        String[] screens = {
            "Tổng quan", "Bán hàng", "Sơ đồ bàn", "Quầy pha chế", 
            "Quản lý hóa đơn", "Thực đơn món", "Voucher & ưu đãi", 
            "Nhân viên & ca", "Khách hàng", "Báo cáo"
        };

        Method showScreenMethod = null;
        try {
            showScreenMethod = WorkspaceView.class.getDeclaredMethod("showScreen", String.class);
            showScreenMethod.setAccessible(true);
        } catch (Exception ignored) { }

        for (String scr : screens) {
            try {
                if (showScreenMethod != null) {
                    showScreenMethod.invoke(ws, scr);
                }
                layoutComponent(ws);
                BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = img.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ws.paint(g2);
                g2.dispose();
                String safeName = scr.replace(" ", "_").replace("&", "va");
                ImageIO.write(img, "png", new File(outDir, "homu_" + safeName + ".png"));
                System.out.println("Exported screen: " + scr);
            } catch (Exception ex) {
                System.out.println("Failed to export " + scr + ": " + ex.getMessage());
            }
        }
        System.out.println("Hoàn tất xuất ảnh màn hình!");
    }

    private static void layoutComponent(Component c) {
        c.doLayout();
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                layoutComponent(child);
            }
        }
    }
}
