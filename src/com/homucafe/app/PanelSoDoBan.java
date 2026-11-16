package com.homucafe.app;

import com.homucafe.entity.Ban;
import com.homucafe.entity.HoaDon;
import com.homucafe.entity.PhieuDatBan;
import com.homucafe.shared.ui.Theme;
import com.homucafe.util.DataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình 3: Sơ đồ 12 bàn phục vụ (Table Map).
 * 3 trạng thái màu rõ ràng: Xanh (Trống), Đỏ cam (Đang phục vụ), Vàng (Đã đặt trước).
 * Thao tác nhanh: Mở bán hàng POS, Đặt bàn trước, Check-in nhận bàn, Chuyển/Gộp bàn, Dọn bàn.
 */
public class PanelSoDoBan extends JPanel {

    public PanelSoDoBan() {
        setLayout(new BorderLayout());
        setOpaque(false);
        initUI();
    }

    private void initUI() {
        JPanel page = Theme.page("Không gian phục vụ", "Sơ đồ bàn",
                "12 bàn phục vụ · Xanh lá: Trống · Cam đỏ: Đang phục vụ · Vàng: Đã đặt trước · Nhấp vào bàn để thao tác");
        JPanel grid = new JPanel(new GridLayout(3, 4, 16, 16));
        grid.setOpaque(false);

        Runnable refreshTables = () -> {
            grid.removeAll();
            for (int i = 1; i <= DataStore.TONG_SO_BAN; i++) {
                final int banSo = i;
                Ban ban = DataStore.timBanTheoSo(banSo);
                String status = ban != null ? ban.getTrangThai() : DataStore.getTrangThaiBan().getOrDefault(banSo, "TRONG");

                JPanel card = new JPanel(new BorderLayout(0, 8));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Theme.LINE, 1, true),
                        new EmptyBorder(14, 16, 14, 16)
                ));
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                JLabel lblName = Theme.label(String.format("BÀN %02d", banSo), 16, Theme.INK);
                lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));

                int soCho = ban != null ? ban.getSoChoNgoi() : (banSo % 2 == 0 ? 2 : 4);
                JLabel lblCap = Theme.label(soCho + " chỗ ngồi", 11, Theme.MUTED);

                JPanel top = new JPanel(new BorderLayout());
                top.setOpaque(false);
                top.add(lblName, BorderLayout.WEST);
                top.add(lblCap, BorderLayout.EAST);
                card.add(top, BorderLayout.NORTH);

                JPanel center = new JPanel();
                center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
                center.setOpaque(false);

                if ("TRONG".equalsIgnoreCase(status)) {
                    card.setBackground(new Color(240, 253, 244)); // Xanh lá nhạt
                    JLabel lblSt = Theme.label("● TRỐNG", 13, new Color(22, 101, 52));
                    lblSt.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    JLabel lblDesc = Theme.label("Sẵn sàng đón khách\n(Nhấp để mở đơn / đặt trước)", 11, Theme.MUTED);
                    center.add(lblSt);
                    center.add(Box.createVerticalStrut(4));
                    center.add(lblDesc);
                } else if ("DANG_PHUC_VU".equalsIgnoreCase(status)) {
                    card.setBackground(new Color(255, 247, 237)); // Cam nhạt
                    JLabel lblSt = Theme.label("● ĐANG PHỤC VỤ", 13, new Color(194, 65, 12));
                    lblSt.setFont(new Font("Segoe UI", Font.BOLD, 13));

                    HoaDon hdNgoi = DataStore.timHoaDonDangNgoi(banSo);
                    String infoStr = hdNgoi != null
                            ? hdNgoi.getMaHoaDon() + " · " + String.format("%,.0f đ", hdNgoi.getTongTien())
                            : "Đang có khách ngồi";
                    JLabel lblDesc = Theme.label(infoStr, 11, Theme.INK);
                    center.add(lblSt);
                    center.add(Box.createVerticalStrut(4));
                    center.add(lblDesc);
                } else if ("DA_DAT".equalsIgnoreCase(status)) {
                    card.setBackground(new Color(254, 252, 232)); // Vàng nhạt
                    JLabel lblSt = Theme.label("● ĐÃ ĐẶT TRƯỚC", 13, new Color(180, 83, 9));
                    lblSt.setFont(new Font("Segoe UI", Font.BOLD, 13));

                    PhieuDatBan pdb = DataStore.timPhieuDatBanTheoSoBan(banSo);
                    String khStr = pdb != null
                            ? pdb.getKhachHang().getHoTen() + " (" + pdb.getNgayDen().format(DateTimeFormatter.ofPattern("HH:mm")) + ")"
                            : "Đã có khách hẹn trước";
                    JLabel lblDesc = Theme.label(khStr, 11, Theme.INK);
                    center.add(lblSt);
                    center.add(Box.createVerticalStrut(4));
                    center.add(lblDesc);
                }

                card.add(center, BorderLayout.CENTER);

                // Sự kiện click
                card.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                        Window win = SwingUtilities.getWindowAncestor(page);
                        if ("TRONG".equalsIgnoreCase(status)) {
                            String[] opts = {"Mở bán hàng POS", "Đặt bàn trước", "Đóng"};
                            int choice = JOptionPane.showOptionDialog(page,
                                    "Bàn " + String.format("%02d", banSo) + " đang TRỐNG. Bạn muốn thực hiện thao tác nào?",
                                    "Quản lý Bàn " + String.format("%02d", banSo),
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
                            if (choice == 0) {
                                PanelBanHangPOS.selectPOSTable(String.format("Bàn %02d", banSo));
                            } else if (choice == 1) {
                                DialogDatBan dlg = new DialogDatBan(win, banSo);
                                dlg.setVisible(true);
                            }
                        } else if ("DA_DAT".equalsIgnoreCase(status)) {
                            PhieuDatBan pdb = DataStore.timPhieuDatBanTheoSoBan(banSo);
                            String info = pdb != null
                                    ? "Khách đặt: " + pdb.getKhachHang().getHoTen() + " · SĐT: " + pdb.getKhachHang().getSoDienThoai() +
                                      "\nGiờ hẹn: " + pdb.getNgayDen().format(DateTimeFormatter.ofPattern("HH:mm dd/MM")) +
                                      "\nSố người: " + pdb.getSoLuongKhach() + " · Tiền cọc: " + String.format("%,.0f đ", pdb.getTienCoc())
                                    : "Bàn đã được đặt trước.";

                            String[] opts = {"Khách nhận bàn (Check-in)", "Hủy đặt bàn", "Đóng"};
                            int choice = JOptionPane.showOptionDialog(page,
                                    info + "\n\nBạn muốn thực hiện thao tác nào?",
                                    "Phiếu đặt Bàn " + String.format("%02d", banSo),
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opts, opts[0]);
                            if (choice == 0) {
                                if (pdb != null) {
                                    DataStore.capNhatTrangThaiPhieuDatBan(pdb.getMaPhieu(), "DA_NHAN");
                                }
                                DataStore.setTrangThaiBan(banSo, "DANG_PHUC_VU");
                                PanelBanHangPOS.selectPOSTable(String.format("Bàn %02d", banSo));
                                JOptionPane.showMessageDialog(page, "Đã check-in cho khách thành công! Đang chuyển sang màn hình Bán hàng.", "Check-in thành công", JOptionPane.INFORMATION_MESSAGE);
                            } else if (choice == 1) {
                                int confirm = JOptionPane.showConfirmDialog(page, "Bạn có chắc chắn muốn hủy phiếu đặt bàn này?", "Xác nhận hủy", JOptionPane.YES_NO_OPTION);
                                if (confirm == JOptionPane.YES_OPTION) {
                                    if (pdb != null) {
                                        DataStore.capNhatTrangThaiPhieuDatBan(pdb.getMaPhieu(), "DA_HUY");
                                    }
                                    DataStore.setTrangThaiBan(banSo, "TRONG");
                                }
                            }
                        } else if ("DANG_PHUC_VU".equalsIgnoreCase(status)) {
                            String[] opts = {"Mở bán hàng POS", "Chuyển / Gộp bàn", "Dọn bàn / Trả bàn trống", "Đóng"};
                            int choice = JOptionPane.showOptionDialog(page,
                                    "Bàn " + String.format("%02d", banSo) + " đang phục vụ khách. Bạn muốn thực hiện thao tác nào?",
                                    "Quản lý Bàn " + String.format("%02d", banSo),
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
                            if (choice == 0) {
                                PanelBanHangPOS.selectPOSTable(String.format("Bàn %02d", banSo));
                            } else if (choice == 1) {
                                List<String> targetChoices = new ArrayList<>();
                                for (int dest = 1; dest <= DataStore.TONG_SO_BAN; dest++) {
                                    if (dest == banSo) continue;
                                    Ban dbBan = DataStore.timBanTheoSo(dest);
                                    String destSt = dbBan != null ? dbBan.getTrangThai() : DataStore.getTrangThaiBan().getOrDefault(dest, "TRONG");
                                    if ("DA_DAT".equalsIgnoreCase(destSt)) continue;
                                    String hint = "TRONG".equalsIgnoreCase(destSt) ? "Trống (Chuyển bàn)" : "Đang phục vụ (Gộp bàn)";
                                    targetChoices.add(String.format("Bàn %02d - %s", dest, hint));
                                }
                                if (targetChoices.isEmpty()) {
                                    JOptionPane.showMessageDialog(page, "Không còn bàn phù hợp để chuyển hoặc gộp!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                                } else {
                                    String picked = (String) JOptionPane.showInputDialog(page,
                                            "Chọn bàn đích để chuyển / gộp từ Bàn " + String.format("%02d", banSo) + ":",
                                            "Chuyển / Gộp bàn",
                                            JOptionPane.QUESTION_MESSAGE, null,
                                            targetChoices.toArray(new String[0]), targetChoices.get(0));
                                    if (picked != null) {
                                        int destBan = Integer.parseInt(picked.split(" - ")[0].replaceAll("[^0-9]", ""));
                                        int movedOrders = DataStore.chuyenHoacGopBan(banSo, destBan);
                                        JOptionPane.showMessageDialog(page,
                                                String.format("Đã chuyển Bàn %02d sang Bàn %02d thành công! (Đã cập nhật %d đơn liên quan)", banSo, destBan, movedOrders),
                                                "Chuyển / Gộp bàn thành công", JOptionPane.INFORMATION_MESSAGE);
                                    }
                                }
                            } else if (choice == 2) {
                                DataStore.setTrangThaiBan(banSo, "TRONG");
                                JOptionPane.showMessageDialog(page, "Đã dọn bàn và đổi Bàn " + String.format("%02d", banSo) + " về trạng thái TRỐNG!", "Trả bàn thành công", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });

                grid.add(card);
            }
            grid.revalidate();
            grid.repaint();
        };

        DataStore.addDataChangeListener(refreshTables);
        refreshTables.run();

        page.add(grid, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);
    }
}
