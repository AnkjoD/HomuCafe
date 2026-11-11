import com.formdev.flatlaf.FlatLightLaf;
import com.homucafe.app.WorkspaceView;
import com.homucafe.app.PreviewRole;
import com.homucafe.shared.ui.Theme;
import gui.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ScreenExporter {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        Theme.install();

        File outDir = new File("HomuCafe_Screenshots");
        outDir.mkdirs();

        int w = 1380;
        int h = 880;

        // 1. WorkspaceView screens
        WorkspaceView ws = new WorkspaceView();
        ws.setSize(w, h);
        layoutComponent(ws);

        String[] screens = {
            "Tổng quan", "Bán hàng", "Sơ đồ bàn", "Quầy pha chế", 
            "Quản lý hóa đơn", "Thực đơn món", "Voucher & ưu đãi", 
            "Nhân viên & ca", "Khách hàng", "Báo cáo"
        };

        // Method to switch screen in WorkspaceView:
        // Let's check showScreen method in WorkspaceView
        Method showScreenMethod = null;
        try {
            showScreenMethod = WorkspaceView.class.getDeclaredMethod("showScreen", String.class);
            showScreenMethod.setAccessible(true);
        } catch (Exception e) {
            // maybe via cards layout or navigation
        }

        for (String scr : screens) {
            try {
                if (showScreenMethod != null) {
                    showScreenMethod.invoke(ws, scr);
                } else {
                    // Try accessing cards layout
                    Field cardsField = WorkspaceView.class.getDeclaredField("cards");
                    cardsField.setAccessible(true);
                    CardLayout cl = (CardLayout) cardsField.get(ws);
                    Field contentField = WorkspaceView.class.getDeclaredField("content");
                    contentField.setAccessible(true);
                    JPanel content = (JPanel) contentField.get(ws);
                    cl.show(content, scr);
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

        // 2. GUI Frm screens
        try {
            FrmDangNhap login = new FrmDangNhap();
            login.setSize(920, 640);
            layoutComponent(login.getContentPane());
            BufferedImage imgLogin = new BufferedImage(920, 640, BufferedImage.TYPE_INT_RGB);
            Graphics2D gLogin = imgLogin.createGraphics();
            login.getContentPane().paint(gLogin);
            gLogin.dispose();
            ImageIO.write(imgLogin, "png", new File(outDir, "gui_FrmDangNhap.png"));
            System.out.println("Exported gui_FrmDangNhap.png");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FrmBanHang banHang = new FrmBanHang();
            banHang.setSize(w, h);
            layoutComponent(banHang);
            BufferedImage imgBH = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D gBH = imgBH.createGraphics();
            banHang.paint(gBH);
            gBH.dispose();
            ImageIO.write(imgBH, "png", new File(outDir, "gui_FrmBanHang.png"));
            System.out.println("Exported gui_FrmBanHang.png");
        } catch (Exception e) {
            System.out.println("BanHang error: " + e.getMessage());
        }

        try {
            FrmManHinhBar bar = new FrmManHinhBar();
            bar.setSize(w, h);
            layoutComponent(bar);
            BufferedImage imgBar = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D gBar = imgBar.createGraphics();
            bar.paint(gBar);
            gBar.dispose();
            ImageIO.write(imgBar, "png", new File(outDir, "gui_FrmManHinhBar.png"));
            System.out.println("Exported gui_FrmManHinhBar.png");
        } catch (Exception e) {
            System.out.println("Bar error: " + e.getMessage());
        }

        try {
            FrmQuanLyMon qlMon = new FrmQuanLyMon();
            qlMon.setSize(w, h);
            layoutComponent(qlMon);
            BufferedImage imgMon = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D gMon = imgMon.createGraphics();
            qlMon.paint(gMon);
            gMon.dispose();
            ImageIO.write(imgMon, "png", new File(outDir, "gui_FrmQuanLyMon.png"));
            System.out.println("Exported gui_FrmQuanLyMon.png");
        } catch (Exception e) {
            System.out.println("Mon error: " + e.getMessage());
        }

        try {
            FrmThongKe thongKe = new FrmThongKe();
            thongKe.setSize(w, h);
            layoutComponent(thongKe);
            BufferedImage imgTK = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D gTK = imgTK.createGraphics();
            thongKe.paint(gTK);
            gTK.dispose();
            ImageIO.write(imgTK, "png", new File(outDir, "gui_FrmThongKe.png"));
            System.out.println("Exported gui_FrmThongKe.png");
        } catch (Exception e) {
            System.out.println("ThongKe error: " + e.getMessage());
        }

        System.out.println("All screens exported successfully!");
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
