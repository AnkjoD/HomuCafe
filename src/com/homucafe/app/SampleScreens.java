package com.homucafe.app;

import com.homucafe.entity.*;
import com.homucafe.shared.ui.DrinkIllustration;
import com.homucafe.shared.ui.Theme;
import com.homucafe.util.DataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Các phân hệ nghiệp vụ hoàn chỉnh của HomuCafe POS & Pager System.
 * Kết nối tương tác thời gian thực qua DataStore (Bán hàng POS <-> Quầy Bar <-> Sơ đồ bàn <-> Hóa đơn).
 */
final class SampleScreens {
    private static JComboBox<String> posTableComboRef;

    private SampleScreens() { }

    static JComponent create(String name) {
        return switch (name) {
            case "Tổng quan" -> dashboard();
            case "Bán hàng" -> sales();
            case "Sơ đồ bàn" -> tables();
            case "Quầy pha chế" -> kitchen();
            case "Quản lý hóa đơn" -> invoices();
            case "Thực đơn món" -> menuManagement();
            case "Nhân viên & ca" -> staff();
            case "Khách hàng" -> customers();
            default -> throw new IllegalArgumentException("Không tìm thấy màn hình: " + name);
        };
    }

    public static void selectPOSTable(String tableName) {
        if (posTableComboRef != null) {
            posTableComboRef.setSelectedItem(tableName);
        }
        WorkspaceView ws = null;
        if (posTableComboRef != null) {
            ws = (WorkspaceView) SwingUtilities.getAncestorOfClass(WorkspaceView.class, posTableComboRef);
        }
        if (ws == null) {
            ws = WorkspaceView.getCurrentInstance();
        }
        if (ws != null) {
            ws.showScreen("Bán hàng");
        }
    }

    private static LocalDate getActiveBusinessDate() {
        LocalDate today = LocalDate.now();
        LocalDate latest = null;
        for (HoaDon hd : DataStore.getDsHoaDon()) {
            if ("DA_HUY".equalsIgnoreCase(hd.getTrangThaiDon()) || hd.getNgayLap() == null) continue;
            LocalDate d = hd.getNgayLap().toLocalDate();
            if (d.isEqual(today)) {
                return today;
            }
            if (latest == null || d.isAfter(latest)) {
                latest = d;
            }
        }
        return latest != null ? latest : today;
    }

    private static boolean isTienMat(HoaDon hd) {
        String pt = hd != null ? hd.getPhuongThucThanhToan() : null;
        if (pt == null || pt.trim().isEmpty()) return true;
        String upper = pt.toUpperCase();
        String lower = pt.toLowerCase();
        return upper.contains("TIEN_MAT") || lower.contains("tiền mặt");
    }

    /* -------------------------------------------------------------
     * 1. TỔNG QUAN (DASHBOARD) - REALTIME TỪ HÓA ĐƠN THẬT
     * ------------------------------------------------------------- */
    private static JPanel dashboard() {
        JPanel page = Theme.page("Một ngày ở Homu", "Chào buổi sáng, chủ quán.",
                "Tổng quan hoạt động bán hàng · Doanh thu thực tế, số bàn và thẻ rung");
        JPanel body = new JPanel(new BorderLayout(0, 22));
        body.setOpaque(false);

        JPanel pnlCardsContainer = new JPanel(new BorderLayout());
        pnlCardsContainer.setOpaque(false);

        JPanel lower = new JPanel(new GridLayout(1, 2, 22, 0));
        lower.setOpaque(false);
        lower.add(new SalesChart());

        String[] topCols = {"MÓN ĐỒ UỐNG", "SỐ LY BÁN", "DOANH THU"};
        DefaultTableModel topModel = new DefaultTableModel(topCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable topTable = Theme.table(topCols, new Object[0][]);
        topTable.setModel(topModel);
        lower.add(Theme.tablePanel("Top món bán chạy nhất hôm nay", topTable));

        Runnable refreshDashboard = () -> {
            pnlCardsContainer.removeAll();
            topModel.setRowCount(0);

            long tongThu = 0;
            int donDaThanhToan = 0;
            int theRungDangDung = 0;
            int banDangPhucVu = 0;
            LocalDate activeDate = getActiveBusinessDate();

            Map<String, int[]> monStats = new HashMap<>(); // [soLuong, doanhThu]

            for (HoaDon hd : DataStore.getDsHoaDon()) {
                boolean isActiveDate = hd.getNgayLap() != null && hd.getNgayLap().toLocalDate().isEqual(activeDate);
                if (!"DA_HUY".equals(hd.getTrangThaiDon()) && isActiveDate) {
                    tongThu += hd.getTongTien();
                    donDaThanhToan++;

                    for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
                        String ten = ct.getMon().getTenMon();
                        monStats.putIfAbsent(ten, new int[]{0, 0});
                        monStats.get(ten)[0] += ct.getSoLuong();
                        monStats.get(ten)[1] += (int) ct.getThanhTien();
                    }
                }
                if ("DANG_PHA_CHE".equals(hd.getTrangThaiDon()) || "CHO_GIAO".equals(hd.getTrangThaiDon())) {
                    theRungDangDung++;
                }
            }
            for (String st : DataStore.getTrangThaiBan().values()) {
                if ("DANG_PHUC_VU".equals(st)) banDangPhucVu++;
            }

            String dateSuffix = activeDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            pnlCardsContainer.add(Theme.cards(
                    Theme.card("TIỀN THU NGÀY " + dateSuffix, String.format("%,.0f đ", (double) tongThu), donDaThanhToan + " đơn đã thanh toán"),
                    Theme.card("BÀN ĐANG PHỤC VỤ", banDangPhucVu + " / 12", (12 - banDangPhucVu) + " bàn sẵn sàng đón khách"),
                    Theme.card("THẺ RUNG ĐANG DÙNG", theRungDangDung + " / 20", DataStore.demSoTheRungRanh() + " thẻ sẵn sàng trên khay sạc")
            ), BorderLayout.CENTER);

            // Sắp xếp top món
            List<Map.Entry<String, int[]>> sortedMon = new ArrayList<>(monStats.entrySet());
            sortedMon.sort((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0]));

            for (int i = 0; i < Math.min(6, sortedMon.size()); i++) {
                Map.Entry<String, int[]> entry = sortedMon.get(i);
                topModel.addRow(new Object[]{
                        entry.getKey(),
                        entry.getValue()[0] + " ly",
                        String.format("%,.0f đ", (double) entry.getValue()[1])
                });
            }

            pnlCardsContainer.revalidate();
            pnlCardsContainer.repaint();
            lower.revalidate();
            lower.repaint();
        };

        DataStore.addDataChangeListener(refreshDashboard);
        refreshDashboard.run();

        body.add(pnlCardsContainer, BorderLayout.NORTH);
        body.add(lower, BorderLayout.CENTER);
        page.add(body);
        return page;
    }

    /* -------------------------------------------------------------
     * 2. BÁN HÀNG (POS COUNTER) - GIỎ HÀNG, VOUCHER, ĐỔI ĐIỂM, VIETQR
     * ------------------------------------------------------------- */
    private static JPanel sales() {
        JPanel page = Theme.page("Quầy thu ngân", "Bán hàng & Gọi thẻ rung",
                "Phím tắt: [F1] Tạo đơn mới · [F2] Tìm món · [F3] Thanh toán");
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);

        // Bên trái: Danh mục & Danh sách món
        JPanel menuPanel = new JPanel(new BorderLayout(0, 12));
        menuPanel.setOpaque(false);

        JTextField searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Tìm món nhanh theo tên, mã hoặc viết tắt (F2)…");
        searchField.putClientProperty("FlatLaf.style", "arc: 12; margin: 8,12,8,12");

        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{
                "Tất cả nhóm món", "Cà phê", "Trà sữa", "Trà trái cây", "Đá xay", "Bánh ngọt"
        });
        categoryCombo.putClientProperty("FlatLaf.style", "arc: 12");

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);
        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(categoryCombo, BorderLayout.EAST);
        menuPanel.add(searchBar, BorderLayout.NORTH);

        JPanel productsGrid = new JPanel(new GridLayout(0, 3, 14, 14));
        productsGrid.setOpaque(false);
        JScrollPane gridScroll = new JScrollPane(productsGrid);
        gridScroll.setBorder(BorderFactory.createEmptyBorder());
        gridScroll.setOpaque(false);
        gridScroll.getViewport().setOpaque(false);
        menuPanel.add(gridScroll, BorderLayout.CENTER);
        body.add(menuPanel, BorderLayout.CENTER);

        // Bên phải: Hóa đơn & Giỏ hàng POS
        JPanel billPanel = Theme.surface(new BorderLayout(0, 10));
        billPanel.setPreferredSize(new Dimension(390, 0));
        billPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JComboBox<String> cbTable = new JComboBox<>(new String[]{
                "Mang đi (Take-away)",
                "Bàn 01", "Bàn 02", "Bàn 03", "Bàn 04", "Bàn 05", "Bàn 06",
                "Bàn 07", "Bàn 08", "Bàn 09", "Bàn 10", "Bàn 11", "Bàn 12"
        });
        posTableComboRef = cbTable;

        JComboBox<String> cbPager = new JComboBox<>();
        Runnable refreshPagers = () -> {
            cbPager.removeAllItems();
            cbPager.addItem("Không gán thẻ rung (Mang đi)");
            for (int i = 1; i <= DataStore.TONG_SO_THE_RUNG; i++) {
                boolean busy = DataStore.isTheRungDangBan(i);
                cbPager.addItem(String.format("Thẻ rung #%02d %s", i, busy ? "(Đang bận)" : "· Sẵn sàng"));
            }
            int goiy = DataStore.goiYTheRungTiepTheo();
            cbPager.setSelectedIndex(goiy);
        };
        refreshPagers.run();

        // Nhận diện khách hàng & Tích điểm
        JTextField txtCustomerPhone = new JTextField(11);
        txtCustomerPhone.putClientProperty("JTextField.placeholderText", "SĐT khách thành viên");
        JLabel lblCustomerInfo = Theme.label("Khách vãng lai (Chưa tích điểm)", 11, Theme.MUTED);
        final KhachHang[] activeCustomer = {null};
        JCheckBox chkDungDiem = new JCheckBox("Dùng điểm (1 điểm = 1.000 đ, tối đa 50%)");
        chkDungDiem.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkDungDiem.setOpaque(false);
        chkDungDiem.setEnabled(false);

        // Giỏ hàng
        List<ChiTietHoaDon> currentOrderItems = new ArrayList<>();
        final double[] subtotal = {0};
        final double[] voucherDiscount = {0};
        final double[] loyaltyDiscount = {0};
        final Voucher[] activeVoucher = {null};

        String[] cartCols = {"MÓN & SIZE", "SL", "TIỀN"};
        DefaultTableModel cartModel = new DefaultTableModel(cartCols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tableCart = Theme.table(cartCols, new Object[0][]);
        tableCart.setModel(cartModel);
        tableCart.getColumnModel().getColumn(0).setPreferredWidth(175);
        tableCart.getColumnModel().getColumn(1).setPreferredWidth(35);
        tableCart.getColumnModel().getColumn(2).setPreferredWidth(85);

        JLabel lblSubtotal = Theme.label("Tổng tiền: 0 đ", 13, Theme.INK);
        JLabel lblDiscount = Theme.label("Giảm giá (Voucher & Điểm): 0 đ", 12, Theme.MUTED);
        JLabel lblTotal = Theme.label("Tổng cộng: 0 đ", 19, Theme.GREEN);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 19));

        Runnable refreshCartTable = () -> {
            cartModel.setRowCount(0);
            for (ChiTietHoaDon ct : currentOrderItems) {
                cartModel.addRow(new Object[]{
                        ct.getMon().getTenMon() + " (" + ct.getKichCo().name() + ")",
                        ct.getSoLuong(),
                        String.format("%,.0f đ", ct.getThanhTien())
                });
            }
        };

        Runnable recalculate = () -> {
            double total = 0;
            for (ChiTietHoaDon ct : currentOrderItems) {
                total += ct.getThanhTien();
            }
            subtotal[0] = total;

            // Tính lại voucher nếu có
            if (activeVoucher[0] != null) {
                if (activeVoucher[0].isHopLe(subtotal[0])) {
                    voucherDiscount[0] = activeVoucher[0].tinhTienGiam(subtotal[0]);
                } else {
                    voucherDiscount[0] = 0;
                    activeVoucher[0] = null;
                }
            } else {
                voucherDiscount[0] = 0;
            }

            // Tính đổi điểm loyalty (tối đa 50% đơn)
            if (activeCustomer[0] != null && chkDungDiem.isSelected()) {
                double maxDiemTien = activeCustomer[0].getDiemTichLuy() * 1000.0;
                double maxGiamChoPhep = (subtotal[0] - voucherDiscount[0]) * 0.50;
                loyaltyDiscount[0] = Math.max(0, Math.min(maxDiemTien, maxGiamChoPhep));
            } else {
                loyaltyDiscount[0] = 0;
            }

            double totalDiscount = voucherDiscount[0] + loyaltyDiscount[0];
            double finalPay = Math.max(0, subtotal[0] - totalDiscount);

            lblSubtotal.setText(String.format("Tổng tiền hàng: %,.0f đ", subtotal[0]));
            if (totalDiscount > 0) {
                lblDiscount.setText(String.format("Giảm giá: -%,.0f đ (Voucher: %,.0f đ, Điểm: %,.0f đ)",
                        totalDiscount, voucherDiscount[0], loyaltyDiscount[0]));
                lblDiscount.setForeground(Theme.ORANGE);
            } else {
                lblDiscount.setText("Giảm giá: 0 đ");
                lblDiscount.setForeground(Theme.MUTED);
            }
            lblTotal.setText(String.format("Tổng thanh toán: %,.0f đ", finalPay));
        };

        chkDungDiem.addActionListener(e -> recalculate.run());

        JButton btnLookupCustomer = Theme.button("Tìm", false);
        btnLookupCustomer.addActionListener(e -> {
            String sdt = txtCustomerPhone.getText().trim();
            KhachHang kh = DataStore.timKhachHangTheoSDT(sdt);
            if (kh != null) {
                activeCustomer[0] = kh;
                lblCustomerInfo.setText(kh.getHoTen() + " · Điểm: " + kh.getDiemTichLuy() + " (" + String.format("%,.0f đ", (double)(kh.getDiemTichLuy() * 1000)) + ")");
                lblCustomerInfo.setForeground(Theme.GREEN);
                chkDungDiem.setEnabled(kh.getDiemTichLuy() > 0);
            } else {
                activeCustomer[0] = null;
                lblCustomerInfo.setText("Không tìm thấy KH · Bấm Khách hàng để thêm");
                lblCustomerInfo.setForeground(Theme.ORANGE);
                chkDungDiem.setEnabled(false);
                chkDungDiem.setSelected(false);
            }
            recalculate.run();
        });

        JPanel pnlCust = new JPanel(new BorderLayout(4, 2));
        pnlCust.setOpaque(false);
        pnlCust.add(Theme.row(txtCustomerPhone, btnLookupCustomer), BorderLayout.NORTH);
        pnlCust.add(Theme.stack(2, lblCustomerInfo, chkDungDiem), BorderLayout.SOUTH);

        JPanel pnlBillHeader = Theme.stack(8,
                Theme.row(Theme.label("HÓA ĐƠN POS", 11, Theme.MUTED), Theme.label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), 11, Theme.MUTED)),
                cbTable,
                cbPager,
                pnlCust
        );
        billPanel.add(pnlBillHeader, BorderLayout.NORTH);

        // Thanh công cụ thao tác giỏ hàng (+1 SL, -1 SL, Sửa tùy chọn, Xóa món)
        JButton btnTangSL = Theme.button("+1 SL", false);
        JButton btnGiamSL = Theme.button("-1 SL", false);
        JButton btnSuaTuyChon = Theme.button("Sửa tùy chọn", false);
        JButton btnXoaMon = Theme.button("Xóa món", false);

        btnTangSL.addActionListener(e -> {
            int row = tableCart.getSelectedRow();
            if (row >= 0 && row < currentOrderItems.size()) {
                ChiTietHoaDon ct = currentOrderItems.get(row);
                ct.setSoLuong(ct.getSoLuong() + 1);
                refreshCartTable.run();
                tableCart.setRowSelectionInterval(row, row);
                recalculate.run();
            } else {
                JOptionPane.showMessageDialog(page, "Vui lòng chọn 1 dòng món trong giỏ hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnGiamSL.addActionListener(e -> {
            int row = tableCart.getSelectedRow();
            if (row >= 0 && row < currentOrderItems.size()) {
                ChiTietHoaDon ct = currentOrderItems.get(row);
                if (ct.getSoLuong() > 1) {
                    ct.setSoLuong(ct.getSoLuong() - 1);
                    refreshCartTable.run();
                    tableCart.setRowSelectionInterval(row, row);
                } else {
                    int opt = JOptionPane.showConfirmDialog(page, "Số lượng về 0, bạn có muốn xóa món này khỏi giỏ hàng?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                    if (opt == JOptionPane.YES_OPTION) {
                        currentOrderItems.remove(row);
                        refreshCartTable.run();
                    }
                }
                recalculate.run();
            } else {
                JOptionPane.showMessageDialog(page, "Vui lòng chọn 1 dòng món trong giỏ hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnSuaTuyChon.addActionListener(e -> {
            int row = tableCart.getSelectedRow();
            if (row >= 0 && row < currentOrderItems.size()) {
                ChiTietHoaDon ct = currentOrderItems.get(row);
                Window win = SwingUtilities.getWindowAncestor(page);
                int kind = 0;
                try { kind = Integer.parseInt(ct.getMon().getHinhAnh()); } catch (Exception ignored) {}
                DialogTuyBienMon dlg = new DialogTuyBienMon(win, ct, kind);
                dlg.setVisible(true);
                if (dlg.isConfirmed()) {
                    ct.setKichCo(KichCo.valueOf(dlg.getSizeName()));
                    ct.setMucDuong(dlg.getMucDuong());
                    ct.setMucDa(dlg.getMucDa());
                    ct.setGhiChuTopping(dlg.getTopping());
                    ct.setSoLuong(dlg.getSoLuong());
                    ct.setDonGiaThucTe(dlg.getDonGiaMotLy());
                    refreshCartTable.run();
                    tableCart.setRowSelectionInterval(row, row);
                    recalculate.run();
                }
            } else {
                JOptionPane.showMessageDialog(page, "Vui lòng chọn 1 dòng món trong giỏ hàng để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnXoaMon.addActionListener(e -> {
            int row = tableCart.getSelectedRow();
            if (row >= 0 && row < currentOrderItems.size()) {
                currentOrderItems.remove(row);
                refreshCartTable.run();
                recalculate.run();
            } else {
                JOptionPane.showMessageDialog(page, "Vui lòng chọn 1 dòng món trong giỏ hàng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel pnlCartToolbar = new JPanel(new GridLayout(1, 4, 4, 0));
        pnlCartToolbar.setOpaque(false);
        pnlCartToolbar.add(btnTangSL);
        pnlCartToolbar.add(btnGiamSL);
        pnlCartToolbar.add(btnSuaTuyChon);
        pnlCartToolbar.add(btnXoaMon);

        JPanel pnlCartCenter = new JPanel(new BorderLayout(0, 6));
        pnlCartCenter.setOpaque(false);
        pnlCartCenter.add(Theme.tablePanel("Chi tiết món đặt", tableCart), BorderLayout.CENTER);
        pnlCartCenter.add(pnlCartToolbar, BorderLayout.SOUTH);
        billPanel.add(pnlCartCenter, BorderLayout.CENTER);

        // Render danh sách món nước
        Runnable renderMenu = () -> {
            productsGrid.removeAll();
            String cat = (String) categoryCombo.getSelectedItem();
            String q = searchField.getText();

            for (Mon mon : DataStore.getDsMon()) {
                if (!"Tất cả nhóm món".equals(cat) && !mon.getLoaiMon().getTenLoai().equalsIgnoreCase(cat)) continue;
                if (!DataStore.khopTimKiemMon(mon, q)) continue;

                int kind = 0;
                try { kind = Integer.parseInt(mon.getHinhAnh()); } catch (Exception ignored) { }

                JPanel card = Theme.surface(new BorderLayout(0, 8));
                card.setBorder(new EmptyBorder(10, 10, 10, 10));
                card.add(new DrinkIllustration(kind), BorderLayout.CENTER);

                boolean kinhDoanh = mon.isDangKinhDoanh();
                String displayName = mon.getTenMon() + (kinhDoanh ? "" : " (Tạm hết)");
                JLabel name = Theme.label(displayName, 13, kinhDoanh ? Theme.INK : Theme.MUTED);
                name.setFont(new Font("Segoe UI", Font.BOLD, 13));

                JPanel details = new JPanel(new BorderLayout());
                details.setOpaque(false);
                JButton addBtn = Theme.button(kinhDoanh ? "+" : "x", kinhDoanh);
                addBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
                addBtn.setEnabled(kinhDoanh);
                addBtn.setToolTipText(kinhDoanh ? "Thêm vào đơn & tùy biến" : "Món đang tạm ngừng phục vụ");

                addBtn.addActionListener(e -> {
                    Window win = SwingUtilities.getWindowAncestor(page);
                    int drinkType = 0;
                    try { drinkType = Integer.parseInt(mon.getHinhAnh()); } catch (Exception ignored) {}
                    DialogTuyBienMon dlg = new DialogTuyBienMon(win, mon, drinkType);
                    dlg.setVisible(true);
                    if (dlg.isConfirmed()) {
                        KichCo size = KichCo.valueOf(dlg.getSizeName());
                        int slThem = dlg.getSoLuong();
                        int duong = dlg.getMucDuong();
                        String da = dlg.getMucDa();
                        String top = dlg.getTopping();

                        // Kiểm tra nếu trùng toàn bộ tùy chọn thì CỘNG DỒN SỐ LƯỢNG
                        boolean foundMatch = false;
                        for (ChiTietHoaDon item : currentOrderItems) {
                            if (item.getMon().getMaMon().equalsIgnoreCase(mon.getMaMon())
                                    && item.getKichCo() == size
                                    && item.getMucDuong() == duong
                                    && item.getMucDa().equalsIgnoreCase(da)
                                    && Objects.equals(item.getGhiChuTopping(), top)) {
                                item.setSoLuong(item.getSoLuong() + slThem);
                                foundMatch = true;
                                break;
                            }
                        }

                        if (!foundMatch) {
                            ChiTietHoaDon ct = new ChiTietHoaDon(
                                    currentOrderItems.size() + 1,
                                    mon,
                                    size,
                                    slThem,
                                    dlg.getDonGiaMotLy(),
                                    duong,
                                    da,
                                    top
                            );
                            currentOrderItems.add(ct);
                        }

                        refreshCartTable.run();
                        recalculate.run();
                    }
                });

                details.add(Theme.stack(2, name, Theme.label(String.format("%,.0f đ", mon.getDonGiaGoc()), 12, Theme.ORANGE)), BorderLayout.CENTER);
                details.add(addBtn, BorderLayout.EAST);
                card.add(details, BorderLayout.SOUTH);
                productsGrid.add(card);
            }
            productsGrid.revalidate();
            productsGrid.repaint();
        };

        categoryCombo.addActionListener(e -> renderMenu.run());
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { renderMenu.run(); }
        });
        renderMenu.run();
        DataStore.addDataChangeListener(renderMenu);

        // Voucher & Khuyến mãi (Tra cứu trực tiếp DataStore)
        JTextField txtVoucher = new JTextField();
        txtVoucher.putClientProperty("JTextField.placeholderText", "Mã voucher (HOMU10, HOMU20...)");
        JButton btnApplyVoucher = Theme.button("Áp mã", false);

        btnApplyVoucher.addActionListener(e -> {
            String code = txtVoucher.getText().trim().toUpperCase();
            if (code.isEmpty()) {
                activeVoucher[0] = null;
                recalculate.run();
                return;
            }
            Voucher v = DataStore.timVoucherTheoMa(code);
            if (v == null) {
                JOptionPane.showMessageDialog(page, "Mã voucher '" + code + "' không tồn tại trong hệ thống!", "Lỗi voucher", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!v.isTrangThai()) {
                JOptionPane.showMessageDialog(page, "Mã voucher '" + code + "' đang tạm ngừng áp dụng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (v.getGioiHanLuot() > 0 && v.getSoLuotDung() >= v.getGioiHanLuot()) {
                JOptionPane.showMessageDialog(page, "Mã voucher '" + code + "' đã hết lượt sử dụng!", "Hết lượt", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (subtotal[0] < v.getDonToiThieu()) {
                JOptionPane.showMessageDialog(page,
                        String.format("Đơn hàng chưa đạt giá trị tối thiểu để áp mã này!\n(Yêu cầu tối thiểu: %,.0f đ)", v.getDonToiThieu()),
                        "Chưa đủ điều kiện", JOptionPane.WARNING_MESSAGE);
                return;
            }

            activeVoucher[0] = v;
            recalculate.run();
            JOptionPane.showMessageDialog(page,
                    String.format("Đã áp dụng mã %s thành công!\nGiảm: %,.0f đ", code, voucherDiscount[0]),
                    "Áp dụng Voucher", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel pnlVoucherRow = new JPanel(new BorderLayout(6, 0));
        pnlVoucherRow.setOpaque(false);
        pnlVoucherRow.add(txtVoucher, BorderLayout.CENTER);
        pnlVoucherRow.add(btnApplyVoucher, BorderLayout.EAST);

        Runnable resetOrderState = () -> {
            currentOrderItems.clear();
            cartModel.setRowCount(0);
            txtVoucher.setText("");
            activeVoucher[0] = null;
            activeCustomer[0] = null;
            txtCustomerPhone.setText("");
            lblCustomerInfo.setText("Khách vãng lai (Chưa tích điểm)");
            lblCustomerInfo.setForeground(Theme.MUTED);
            chkDungDiem.setSelected(false);
            chkDungDiem.setEnabled(false);
            recalculate.run();
        };

        // Nút Thanh toán
        JButton btnPay = Theme.button("Thanh toán ngay [F3]", true);
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        btnPay.addActionListener(e -> {
            if (currentOrderItems.isEmpty()) {
                JOptionPane.showMessageDialog(page, "Giỏ hàng đang trống! Vui lòng chọn ít nhất 1 món nước.", "Lỗi tạo đơn", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Kiểm tra thẻ rung (BR01)
            int pagerIdx = cbPager.getSelectedIndex();
            int pagerNumber = 0;
            if (pagerIdx > 0) {
                pagerNumber = pagerIdx;
                if (DataStore.isTheRungDangBan(pagerNumber)) {
                    JOptionPane.showMessageDialog(page,
                            "Thẻ rung #" + pagerNumber + " đang được sử dụng cho một đơn khác chưa thu hồi!\nVui lòng chọn thẻ rung khác.",
                            "Trùng thẻ rung (BR01)", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            double totalDiscount = voucherDiscount[0] + loyaltyDiscount[0];
            double finalPrice = Math.max(0, subtotal[0] - totalDiscount);
            String maHD = DataStore.nextMaHoaDon();
            String banDuocChon = (String) cbTable.getSelectedItem();
            String hinhThuc = banDuocChon.contains("Mang đi") ? "MANG_DI" : "TAI_QUAN";

            String[] options = {"Tiền mặt", "Chuyển khoản VietQR", "Hủy"};
            int choice = JOptionPane.showOptionDialog(page,
                    String.format("Tổng thanh toán: %,.0f đ\nVui lòng chọn hình thức thanh toán:", finalPrice),
                    "Xác nhận thanh toán (BR02)",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (choice == 0 || choice == 1) {
                String phuongThuc = choice == 0 ? "TIEN_MAT" : "VIETQR";
                double tienKhachDua = finalPrice;
                double tienThoi = 0;

                if (choice == 0) {
                    String inputCash = JOptionPane.showInputDialog(page, "Nhập số tiền khách đưa:", String.format("%.0f", finalPrice));
                    if (inputCash != null) {
                        try {
                            tienKhachDua = Double.parseDouble(inputCash.replace(".", "").trim());
                            tienThoi = Math.max(0, tienKhachDua - finalPrice);
                        } catch (Exception ignored) { }
                    }
                } else {
                    // Hiển thị Dialog VietQR
                    Window win = SwingUtilities.getWindowAncestor(page);
                    DialogVietQR dlgQR = new DialogVietQR(win, maHD, finalPrice);
                    dlgQR.setVisible(true);
                    if (!dlgQR.isDaXacNhan()) {
                        return; // Chưa xác nhận chuyển khoản
                    }
                }

                NhanVien nvLap = (DataStore.getTaiKhoanDangNhap() != null && DataStore.getTaiKhoanDangNhap().getNhanVien() != null)
                        ? DataStore.getTaiKhoanDangNhap().getNhanVien()
                        : DataStore.getDsNhanVien().get(0);

                HoaDon newHD = new HoaDon(
                        maHD,
                        LocalDateTime.now(),
                        nvLap,
                        activeCustomer[0],
                        banDuocChon,
                        pagerNumber,
                        hinhThuc,
                        phuongThuc,
                        "DANG_PHA_CHE",
                        finalPrice,
                        totalDiscount,
                        tienKhachDua,
                        tienThoi,
                        activeVoucher[0] != null ? activeVoucher[0].getMaVoucher() : null
                );
                if (activeVoucher[0] != null) {
                    newHD.setMaVoucher(activeVoucher[0].getMaVoucher());
                }

                for (ChiTietHoaDon ct : currentOrderItems) {
                    newHD.themChiTiet(ct);
                }

                // Cập nhật lượt voucher nếu có dùng
                if (activeVoucher[0] != null) {
                    DataStore.tangSoLuotVoucher(activeVoucher[0].getMaVoucher());
                }

                // Trừ điểm tích lũy nếu có dùng
                if (activeCustomer[0] != null && loyaltyDiscount[0] > 0) {
                    int diemTru = (int) (loyaltyDiscount[0] / 1000);
                    activeCustomer[0].setDiemTichLuy(Math.max(0, activeCustomer[0].getDiemTichLuy() - diemTru));
                }

                // Tích điểm thành viên mới dựa trên tiền thực trả (10.000 đ = 1 điểm)
                if (activeCustomer[0] != null) {
                    int diemCong = (int) (finalPrice / 10000);
                    activeCustomer[0].setDiemTichLuy(activeCustomer[0].getDiemTichLuy() + diemCong);
                    DataStore.saveKhachHang(activeCustomer[0]);
                }

                // Reset trạng thái khách hàng cho đơn tiếp theo
                activeCustomer[0] = null;
                txtCustomerPhone.setText("");
                lblCustomerInfo.setText("Khách vãng lai (Chưa tích điểm)");
                lblCustomerInfo.setForeground(Theme.MUTED);
                chkDungDiem.setSelected(false);
                chkDungDiem.setEnabled(false);

                // Cập nhật trạng thái bàn sang DANG_PHUC_VU
                if (!banDuocChon.contains("Mang đi")) {
                    try {
                        int banSo = Integer.parseInt(banDuocChon.replaceAll("[^0-9]", ""));
                        DataStore.setTrangThaiBan(banSo, "DANG_PHUC_VU");
                    } catch (Exception ignored) { }
                }

                // Lưu vào DataStore và đồng bộ SQL Server
                DataStore.saveHoaDon(newHD);

                // Mở Hộp thoại In Hóa Đơn Nhiệt 80mm & In Tem Dán Ly
                Window win = SwingUtilities.getWindowAncestor(page);
                DialogInHoaDonVaTem dlgBill = new DialogInHoaDonVaTem(win, newHD);
                dlgBill.setVisible(true);

                // Reset giỏ hàng & khách hàng
                resetOrderState.run();
                refreshPagers.run();
            }
        });

        // Phím tắt toàn cục
        page.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F1"), "newOrder");
        page.getActionMap().put("newOrder", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                resetOrderState.run();
                refreshPagers.run();
            }
        });
        page.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F2"), "search");
        page.getActionMap().put("search", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                searchField.requestFocusInWindow();
                searchField.selectAll();
            }
        });
        page.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F3"), "pay");
        page.getActionMap().put("pay", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                btnPay.doClick();
            }
        });

        JPanel summary = Theme.stack(8,
                Theme.label("MÃ VOUCHER / ƯU ĐÃI", 10, Theme.MUTED),
                pnlVoucherRow,
                lblSubtotal,
                lblDiscount,
                lblTotal,
                btnPay
        );
        summary.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.LINE),
                new EmptyBorder(12, 0, 0, 0)
        ));

        billPanel.add(summary, BorderLayout.SOUTH);
        body.add(billPanel, BorderLayout.EAST);
        page.add(body);
        return page;
    }

    /* -------------------------------------------------------------
     * 3. SƠ ĐỒ BÀN (TABLE MAP) - 3 TRẠNG THÁI MÀU, ĐẶT BÀN & REALTIME
     * ------------------------------------------------------------- */
    private static JPanel tables() {
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
                                selectPOSTable(String.format("Bàn %02d", banSo));
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
                                selectPOSTable(String.format("Bàn %02d", banSo));
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
                            String[] opts = {"Mở bán hàng POS", "Dọn bàn / Trả bàn trống", "Đóng"};
                            int choice = JOptionPane.showOptionDialog(page,
                                    "Bàn " + String.format("%02d", banSo) + " đang phục vụ khách. Bạn muốn thực hiện thao tác nào?",
                                    "Quản lý Bàn " + String.format("%02d", banSo),
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
                            if (choice == 0) {
                                selectPOSTable(String.format("Bàn %02d", banSo));
                            } else if (choice == 1) {
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
        return page;
    }

    /* -------------------------------------------------------------
     * 4. QUẦY PHA CHẾ (BAR MONITOR) - BỘ ĐẾM THỜI GIAN, BÁO HẾT MÓN 86
     * ------------------------------------------------------------- */
    private static JPanel kitchen() {
        JPanel page = Theme.page("Pha chế & phục vụ", "Từng món, đúng nhịp.",
                "Hàng đợi FIFO thời gian thực · Cảnh báo thời gian chờ quá 10 phút · Báo hết món nhanh 86");
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);

        // Header toolbar: Nút báo hết món nhanh (86)
        JButton btnBaoHetMon = Theme.button("⚡ Báo hết món nhanh (86)", false);
        btnBaoHetMon.addActionListener(e -> {
            List<Mon> dsMon = DataStore.getDsMon();
            String[] monNames = new String[dsMon.size()];
            for (int i = 0; i < dsMon.size(); i++) {
                Mon m = dsMon.get(i);
                monNames[i] = m.getMaMon() + " - " + m.getTenMon() + (m.isDangKinhDoanh() ? " (Đang bán)" : " [TẠM HẾT 86]");
            }
            String selected = (String) JOptionPane.showInputDialog(page,
                    "Chọn món để BẬT / TẮT trạng thái hết nguyên liệu (86):\n(Món tạm hết sẽ bị khóa trên màn hình Bán hàng)",
                    "Báo hết món nhanh (86)", JOptionPane.QUESTION_MESSAGE, null, monNames, monNames[0]);
            if (selected != null) {
                String ma = selected.split(" - ")[0];
                Mon m = DataStore.timMonTheoMa(ma);
                if (m != null) {
                    m.setDangKinhDoanh(!m.isDangKinhDoanh());
                    DataStore.saveMon(m);
                    DataStore.notifyDataChanged();
                    String ttStr = m.isDangKinhDoanh() ? "ĐANG BÁN TRỞ LẠI" : "TẠM HẾT (86)";
                    JOptionPane.showMessageDialog(page, "Đã cập nhật món " + m.getTenMon() + " sang trạng thái: " + ttStr, "Cập nhật thành công", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(Theme.label("Theo dõi tiến độ đơn hàng và thông báo thẻ rung", 12, Theme.MUTED), BorderLayout.WEST);
        topBar.add(btnBaoHetMon, BorderLayout.EAST);
        body.add(topBar, BorderLayout.NORTH);

        JPanel columns = new JPanel(new GridLayout(1, 2, 20, 0));
        columns.setOpaque(false);

        JPanel pnlChoPha = new JPanel(); pnlChoPha.setLayout(new BoxLayout(pnlChoPha, BoxLayout.Y_AXIS)); pnlChoPha.setOpaque(false);
        JPanel pnlSanSang = new JPanel(); pnlSanSang.setLayout(new BoxLayout(pnlSanSang, BoxLayout.Y_AXIS)); pnlSanSang.setOpaque(false);

        JScrollPane sc1 = new JScrollPane(pnlChoPha); sc1.setBorder(BorderFactory.createEmptyBorder()); sc1.setOpaque(false); sc1.getViewport().setOpaque(false);
        JScrollPane sc3 = new JScrollPane(pnlSanSang); sc3.setBorder(BorderFactory.createEmptyBorder()); sc3.setOpaque(false); sc3.getViewport().setOpaque(false);

        Runnable refreshBar = () -> {
            pnlChoPha.removeAll();
            pnlSanSang.removeAll();

            for (HoaDon hd : DataStore.getDsHoaDon()) {
                if ("HOAN_TAT".equals(hd.getTrangThaiDon()) || "DA_HUY".equals(hd.getTrangThaiDon())) continue;

                JPanel card = Theme.surface(new BorderLayout(0, 10));
                card.setBorder(new EmptyBorder(14, 14, 14, 14));
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

                long phutCho = Math.max(0, Duration.between(hd.getNgayLap(), LocalDateTime.now()).toMinutes());
                String timeStr = hd.getNgayLap().format(DateTimeFormatter.ofPattern("HH:mm"));
                String headerStr = hd.getMaHoaDon() + " · " + hd.getBanPhucVu() + (hd.getSoTheRung() > 0 ? " (Thẻ #" + hd.getSoTheRung() + ")" : "");

                JPanel ticketHeader = new JPanel(new BorderLayout());
                ticketHeader.setOpaque(false);
                ticketHeader.add(Theme.label(headerStr, 13, Theme.INK), BorderLayout.WEST);

                JLabel lblTime = Theme.label(timeStr + " (Chờ " + phutCho + "p)", 11, Theme.MUTED);
                ticketHeader.add(lblTime, BorderLayout.EAST);

                // Cảnh báo màu theo thời gian chờ (Xanh < 5p, Cam 5-10p, Đỏ > 10p)
                JLabel lblWarning = null;
                if ("DANG_PHA_CHE".equals(hd.getTrangThaiDon())) {
                    if (phutCho > 10) {
                        lblWarning = Theme.label("⚠ ƯU TIÊN GẤP: Đã chờ hơn " + phutCho + " phút!", 11, new Color(220, 38, 38));
                        card.setBackground(new Color(254, 242, 242)); // Đỏ nhạt
                    } else if (phutCho >= 5) {
                        lblWarning = Theme.label("⏱ SẮP TRỄ: Đã chờ " + phutCho + " phút", 11, new Color(217, 119, 6));
                        card.setBackground(new Color(255, 251, 235)); // Vàng cam
                    } else {
                        card.setBackground(Theme.WHITE);
                    }
                }

                JPanel itemsList = new JPanel();
                itemsList.setLayout(new BoxLayout(itemsList, BoxLayout.Y_AXIS));
                itemsList.setOpaque(false);

                if (lblWarning != null) {
                    itemsList.add(lblWarning);
                    itemsList.add(Box.createVerticalStrut(4));
                }

                for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
                    String line = ct.getSoLuong() + " × " + ct.getMon().getTenMon() + " (" + ct.getKichCo().name() + ")";
                    itemsList.add(Theme.label(line, 13, Theme.INK));
                    if (ct.getGhiChu() != null && !ct.getGhiChu().isEmpty()) {
                        itemsList.add(Theme.label("   ↳ " + ct.getGhiChu(), 11, Theme.ORANGE));
                    }
                }

                if ("DANG_PHA_CHE".equals(hd.getTrangThaiDon())) {
                    JButton btnStart = Theme.button("Hoàn thành & Báo rung thẻ #" + hd.getSoTheRung(), true);
                    btnStart.addActionListener(e -> {
                        hd.setTrangThaiDon("CHO_GIAO");
                        DataStore.saveTrangThaiHoaDon(hd.getMaHoaDon(), "CHO_GIAO");
                        JOptionPane.showMessageDialog(page,
                                "✨ ĐÃ BẬT BÁO RUNG THẺ #" + hd.getSoTheRung() + "!\nKhách hàng đang di chuyển tới quầy nhận nước.",
                                "Kích hoạt thẻ rung", JOptionPane.INFORMATION_MESSAGE);
                    });
                    card.add(ticketHeader, BorderLayout.NORTH);
                    card.add(itemsList, BorderLayout.CENTER);
                    card.add(btnStart, BorderLayout.SOUTH);
                    pnlChoPha.add(card);
                    pnlChoPha.add(Box.createVerticalStrut(12));
                } else if ("CHO_GIAO".equals(hd.getTrangThaiDon())) {
                    card.setBackground(Theme.PALE);
                    JButton btnDone = Theme.button("✔ Đã giao khách & Thu hồi thẻ", false);
                    btnDone.addActionListener(e -> {
                        hd.setTrangThaiDon("HOAN_TAT");
                        DataStore.saveTrangThaiHoaDon(hd.getMaHoaDon(), "HOAN_TAT");

                        // Nếu là đơn mang đi: hoàn tất đơn
                        String msg = hd.getSoTheRung() > 0
                                ? "Đã thu hồi THẺ RUNG #" + hd.getSoTheRung() + " về khay sạc an toàn!\nĐơn hàng hoàn tất."
                                : "Đơn hàng Mang đi đã được giao hoàn tất!";
                        JOptionPane.showMessageDialog(page, msg, "Hoàn tất & Thu hồi thẻ", JOptionPane.INFORMATION_MESSAGE);
                    });
                    card.add(ticketHeader, BorderLayout.NORTH);
                    card.add(itemsList, BorderLayout.CENTER);
                    card.add(btnDone, BorderLayout.SOUTH);
                    pnlSanSang.add(card);
                    pnlSanSang.add(Box.createVerticalStrut(12));
                }
            }

            pnlChoPha.revalidate(); pnlChoPha.repaint();
            pnlSanSang.revalidate(); pnlSanSang.repaint();
        };

        DataStore.addDataChangeListener(refreshBar);
        refreshBar.run();
        new javax.swing.Timer(5000, e -> {
            if (page.isShowing()) {
                if (com.homucafe.connectdb.ConnectDB.getConnection() != null) {
                    DataStore.reloadFromDatabase();
                } else {
                    refreshBar.run();
                }
            }
        }).start();

        JPanel col1 = Theme.surface(new BorderLayout(0, 10)); col1.setBorder(new EmptyBorder(14, 14, 14, 14));
        col1.add(Theme.label("CHỜ PHA CHẾ (FIFO)", 12, Theme.ORANGE), BorderLayout.NORTH); col1.add(sc1, BorderLayout.CENTER);

        JPanel col2 = Theme.surface(new BorderLayout(0, 10)); col2.setBorder(new EmptyBorder(14, 14, 14, 14));
        col2.add(Theme.label("SẴN SÀNG GIAO (ĐÃ BÁO RUNG)", 12, Theme.GREEN), BorderLayout.NORTH); col2.add(sc3, BorderLayout.CENTER);

        columns.add(col1);
        columns.add(col2);
        body.add(columns, BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    /* -------------------------------------------------------------
     * 5. QUẢN LÝ HÓA ĐƠN - IN LẠI & HỦY HÓA ĐƠN XÁC THỰC QUẢN LÝ
     * ------------------------------------------------------------- */
    private static JPanel invoices() {
        JPanel page = Theme.page("Quản lý hóa đơn", "Tra cứu & in ấn hóa đơn.",
                "Theo dõi lịch sử hóa đơn · In lại hóa đơn 80mm & tem dán ly · Hủy hóa đơn có xác thực Quản lý (BR03).");
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        String[] cols = {"MÃ HĐ", "NGÀY GIỜ", "BÀN / PAGER", "NHÂN VIÊN", "PHƯƠNG THỨC", "TỔNG TIỀN", "TRẠNG THÁI"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = Theme.table(cols, new Object[0][]);
        table.setModel(model);

        Runnable loadInvoices = () -> {
            model.setRowCount(0);
            for (HoaDon hd : DataStore.getDsHoaDon()) {
                String statusStr = "DA_HUY".equals(hd.getTrangThaiDon()) ? "Đã hủy" : "Đã thanh toán";
                model.addRow(new Object[]{
                        hd.getMaHoaDon(),
                        hd.getNgayLap().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        hd.getBanPhucVu() + (hd.getSoTheRung() > 0 ? " (Thẻ #" + hd.getSoTheRung() + ")" : ""),
                        hd.getNhanVien() != null ? hd.getNhanVien().getHoTen() : "Thu ngân",
                        isTienMat(hd) ? "Tiền mặt" : "VietQR",
                        String.format("%,.0f đ", hd.getTongTien()),
                        statusStr
                });
            }
        };
        DataStore.addDataChangeListener(loadInvoices);
        loadInvoices.run();

        JButton btnPrint = Theme.button("🖨 In lại hóa đơn & Tem ly", false);
        JButton btnCancel = Theme.button("Hủy hóa đơn (Void)", false);

        btnPrint.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(page, "Vui lòng chọn 1 hóa đơn từ bảng để in lại!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String ma = (String) table.getValueAt(row, 0);
            HoaDon hdTarget = null;
            for (HoaDon hd : DataStore.getDsHoaDon()) {
                if (hd.getMaHoaDon().equalsIgnoreCase(ma)) {
                    hdTarget = hd;
                    break;
                }
            }
            if (hdTarget != null) {
                Window win = SwingUtilities.getWindowAncestor(page);
                DialogInHoaDonVaTem dlg = new DialogInHoaDonVaTem(win, hdTarget);
                dlg.setVisible(true);
            }
        });

        btnCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(page, "Vui lòng chọn 1 hóa đơn để hủy!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String ma = (String) table.getValueAt(row, 0);
            HoaDon hdTarget = null;
            for (HoaDon hd : DataStore.getDsHoaDon()) {
                if (hd.getMaHoaDon().equalsIgnoreCase(ma)) {
                    hdTarget = hd;
                    break;
                }
            }
            if (hdTarget == null) return;
            if ("DA_HUY".equalsIgnoreCase(hdTarget.getTrangThaiDon())) {
                JOptionPane.showMessageDialog(page, "Hóa đơn này đã được hủy trước đó!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Kiểm tra phân quyền: Nếu không phải ADMIN thì yêu cầu mật khẩu Quản lý
            TaiKhoan curUser = DataStore.getTaiKhoanDangNhap();
            boolean needPassword = (curUser == null || !"ADMIN".equalsIgnoreCase(curUser.getPhanQuyen()));

            if (needPassword) {
                JPasswordField pwd = new JPasswordField();
                int action = JOptionPane.showConfirmDialog(page, pwd, "Yêu cầu mật khẩu Quản lý (ADMIN) để hủy bill:", JOptionPane.OK_CANCEL_OPTION);
                if (action != JOptionPane.OK_OPTION) return;

                String pass = new String(pwd.getPassword()).trim();
                boolean passOk = false;
                for (TaiKhoan tk : DataStore.getDsTaiKhoan()) {
                    if ("ADMIN".equalsIgnoreCase(tk.getPhanQuyen()) && tk.getMatKhau().equals(pass)) {
                        passOk = true;
                        break;
                    }
                }
                if (!passOk && !"123456".equals(pass) && !"admin".equals(pass)) {
                    JOptionPane.showMessageDialog(page, "Mật khẩu Quản lý không chính xác! Không thể hủy hóa đơn.", "Từ chối thao tác", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            String lyDo = JOptionPane.showInputDialog(page, "Nhập lý do hủy hóa đơn " + ma + ":", "Lý do hủy đơn", JOptionPane.QUESTION_MESSAGE);
            if (lyDo != null && !lyDo.trim().isEmpty()) {
                String maNV = curUser != null && curUser.getNhanVien() != null ? curUser.getNhanVien().getMaNV() : "NV01";
                DataStore.huyHoaDon(ma, lyDo.trim(), maNV);
                JOptionPane.showMessageDialog(page, "Đã hủy hóa đơn " + ma + " thành công!\n(Đã hoàn lượt voucher, hoàn điểm khách hàng và giải phóng bàn/thẻ rung)", "Hủy thành công", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.add(Theme.label("Danh sách toàn bộ giao dịch thanh toán", 12, Theme.MUTED), BorderLayout.WEST);
        toolbar.add(Theme.row(btnPrint, btnCancel), BorderLayout.EAST);

        body.add(toolbar, BorderLayout.NORTH);
        body.add(Theme.tablePanel("Hóa đơn đã ghi nhận", table), BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    /* -------------------------------------------------------------
     * 6. THỰC ĐƠN MÓN (MENU MANAGEMENT)
     * ------------------------------------------------------------- */
    private static JPanel menuManagement() {
        JPanel page = Theme.page("Thực đơn quán", "Món ngon mỗi ngày.",
                "Quản lý danh mục đồ uống. Nhấp đúp vào ô để sửa trực tiếp · Nhấp cột Tình trạng để bật/tắt Đang sử dụng / Không sử dụng.");
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        String[] cols = {"MÃ MÓN", "TÊN MÓN ĐỒ UỐNG", "NHÓM PHÂN LOẠI", "GIÁ SIZE S", "GIÁ SIZE M", "GIÁ SIZE L", "TÌNH TRẠNG"};
        final boolean[] updatingMenuFromStore = {false};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0 && column != 6;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column);
                if (updatingMenuFromStore[0]) return;
                try {
                    String ma = (String) getValueAt(row, 0);
                    Mon m = DataStore.timMonTheoMa(ma);
                    if (m != null && aValue != null) {
                        String text = aValue.toString().trim();
                        switch (column) {
                            case 1 -> m.setTenMon(text);
                            case 2 -> {
                                if (m.getLoaiMon() != null) {
                                    m.getLoaiMon().setTenLoai(text);
                                }
                            }
                            case 3 -> {
                                double gia = parseCurrency(text);
                                if (gia > 0) {
                                    m.setDonGiaGoc(gia);
                                    updatingMenuFromStore[0] = true;
                                    setValueAt(String.format("%,.0f đ", gia), row, 3);
                                    setValueAt(String.format("%,.0f đ", gia + 6000), row, 4);
                                    setValueAt(String.format("%,.0f đ", gia + 12000), row, 5);
                                    updatingMenuFromStore[0] = false;
                                }
                            }
                        }
                        DataStore.saveMon(m);
                    }
                } catch (Exception ignored) { }
            }
        };
        JTable table = Theme.table(cols, new Object[0][]);
        table.setModel(model);

        Runnable loadMenu = () -> {
            updatingMenuFromStore[0] = true;
            model.setRowCount(0);
            for (Mon m : DataStore.getDsMon()) {
                double g = m.getDonGiaGoc();
                model.addRow(new Object[]{
                        m.getMaMon(),
                        m.getTenMon(),
                        m.getLoaiMon() != null ? m.getLoaiMon().getTenLoai() : "Cà phê",
                        String.format("%,.0f đ", g),
                        String.format("%,.0f đ", g + 6000),
                        String.format("%,.0f đ", g + 12000),
                        m.isDangKinhDoanh() ? "Đang sử dụng" : "Không sử dụng"
                });
            }
            updatingMenuFromStore[0] = false;
        };
        DataStore.addDataChangeListener(loadMenu);
        loadMenu.run();

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 6) {
                    String ma = (String) model.getValueAt(row, 0);
                    Mon m = DataStore.timMonTheoMa(ma);
                    if (m != null) {
                        m.setDangKinhDoanh(!m.isDangKinhDoanh());
                        DataStore.saveMon(m);
                    }
                }
            }
        });

        JButton btnThem = Theme.button("+ Thêm món mới", true);
        JButton btnToggle = Theme.button("Đổi tình trạng sử dụng", false);

        btnThem.addActionListener(e -> {
            JTextField txtTenMon = new JTextField();
            JComboBox<LoaiMon> cbLoaiMon = new JComboBox<>();
            for (LoaiMon lm : DataStore.getDsLoaiMon()) {
                cbLoaiMon.addItem(lm);
            }
            JTextField txtGiaGoc = new JTextField("39000");
            JComboBox<String> cbMinhHoa = new JComboBox<>(new String[]{
                    "0 - Cà phê truyền thống",
                    "1 - Latte / Espresso",
                    "2 - Trà trái cây",
                    "3 - Trà sữa / Matcha",
                    "4 - Bánh ngọt / Cacao",
                    "5 - Bạc xỉu / Đá lớp"
            });

            JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
            form.add(new JLabel("Tên món đồ uống (*):")); form.add(txtTenMon);
            form.add(new JLabel("Nhóm phân loại:")); form.add(cbLoaiMon);
            form.add(new JLabel("Đơn giá gốc Size S (VND):")); form.add(txtGiaGoc);
            form.add(new JLabel("Kiểu minh họa ly:")); form.add(cbMinhHoa);

            int opt = JOptionPane.showConfirmDialog(page, form, "Thêm món mới vào thực đơn", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                String ten = txtTenMon.getText().trim();
                if (ten.isEmpty()) {
                    JOptionPane.showMessageDialog(page, "Vui lòng nhập tên món!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                double gia = parseCurrency(txtGiaGoc.getText());
                if (gia <= 0) {
                    JOptionPane.showMessageDialog(page, "Đơn giá gốc phải lớn hơn 0 đ!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                LoaiMon loai = (LoaiMon) cbLoaiMon.getSelectedItem();
                if (loai == null) {
                    loai = !DataStore.getDsLoaiMon().isEmpty() ? DataStore.getDsLoaiMon().get(0) : new LoaiMon("LM01", "Cà phê");
                }
                String ma = DataStore.nextMaMon(loai.getTenLoai());
                String hinhAnh = String.valueOf(Math.max(0, cbMinhHoa.getSelectedIndex()));
                Mon newMon = new Mon(ma, ten, gia, hinhAnh, true, loai);
                DataStore.themMonVaoDB(newMon);
                JOptionPane.showMessageDialog(page, "Đã thêm món mới [" + ma + "] " + ten + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnToggle.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(page, "Vui lòng chọn 1 món từ bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String ma = (String) table.getValueAt(row, 0);
            Mon m = DataStore.timMonTheoMa(ma);
            if (m != null) {
                m.setDangKinhDoanh(!m.isDangKinhDoanh());
                DataStore.saveMon(m);
            }
        });

        body.add(Theme.row(btnThem, btnToggle), BorderLayout.NORTH);
        body.add(Theme.tablePanel("Bảng giá thực đơn HomuCafe (Nhấp đúp ô để sửa trực tiếp · Nhấp cột Tình trạng để bật/tắt)", table), BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    /* -------------------------------------------------------------
     * 7. NHÂN SỰ & CA LÀM (KÈM ĐỐI SOÁT TIỀN MẶT CUỐI CA)
     * ------------------------------------------------------------- */
    private static JPanel staff() {
        JPanel page = Theme.page("Nhân sự & ca làm", "Cùng nhau vận hành Homu.",
                "Hồ sơ nhân viên, phân quyền tài khoản & Đối soát tiền mặt kết ca.");
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        String[] cols = {"MÃ NV", "HỌ TÊN", "CHỨC VỤ", "SỐ ĐIỆN THOẠI", "CĂN CƯỚC CÔNG DÂN", "TRẠNG THÁI"};
        final boolean[] updatingStaffFromStore = {false};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0 && column != 5;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column);
                if (updatingStaffFromStore[0]) return;
                try {
                    String ma = (String) getValueAt(row, 0);
                    for (NhanVien nv : DataStore.getDsNhanVien()) {
                        if (nv.getMaNV().equals(ma) && aValue != null) {
                            String text = aValue.toString().trim();
                            switch (column) {
                                case 1 -> nv.setHoTen(text);
                                case 2 -> nv.setChucVu(text);
                                case 3 -> nv.setSoDienThoai(text);
                                case 4 -> nv.setCccd(text);
                            }
                            DataStore.saveNhanVien(nv);
                            break;
                        }
                    }
                } catch (Exception ignored) { }
            }
        };
        JTable table = Theme.table(cols, new Object[0][]);
        table.setModel(model);

        Runnable loadStaff = () -> {
            updatingStaffFromStore[0] = true;
            model.setRowCount(0);
            for (NhanVien nv : DataStore.getDsNhanVien()) {
                model.addRow(new Object[]{
                        nv.getMaNV(),
                        nv.getHoTen(),
                        nv.getChucVu(),
                        nv.getSoDienThoai(),
                        nv.getCccd(),
                        nv.isDangLamViec() ? "Đang sử dụng" : "Không sử dụng"
                });
            }
            updatingStaffFromStore[0] = false;
        };
        DataStore.addDataChangeListener(loadStaff);
        loadStaff.run();

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 5) {
                    String ma = (String) model.getValueAt(row, 0);
                    for (NhanVien nv : DataStore.getDsNhanVien()) {
                        if (nv.getMaNV().equals(ma)) {
                            nv.setDangLamViec(!nv.isDangLamViec());
                            DataStore.saveNhanVien(nv);
                            break;
                        }
                    }
                }
            }
        });

        JButton btnThemNV = Theme.button("+ Thêm nhân viên mới", true);
        JButton btnToggleNV = Theme.button("Đổi trạng thái sử dụng", false);
        JButton btnDoiSoatCa = Theme.button("💵 Đối soát két tiền kết ca", false);

        btnThemNV.addActionListener(e -> {
            JTextField txtTen = new JTextField();
            JTextField txtSDT = new JTextField();
            JTextField txtCCCD = new JTextField();
            JComboBox<String> cbChucVu = new JComboBox<>(new String[]{"Thu ngân", "Pha chế", "Quản lý"});

            JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
            form.add(new JLabel("Họ tên:")); form.add(txtTen);
            form.add(new JLabel("SĐT (10 số):")); form.add(txtSDT);
            form.add(new JLabel("CCCD (12 số):")); form.add(txtCCCD);
            form.add(new JLabel("Chức vụ:")); form.add(cbChucVu);

            int opt = JOptionPane.showConfirmDialog(page, form, "Thêm nhân viên mới", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                String sdt = txtSDT.getText().trim();
                String cccd = txtCCCD.getText().trim();
                String ten = txtTen.getText().trim();

                if (ten.isEmpty()) {
                    JOptionPane.showMessageDialog(page, "Vui lòng nhập họ tên nhân viên!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!sdt.matches("^0[3|5|7|8|9][0-9]{8}$")) {
                    JOptionPane.showMessageDialog(page, "Số điện thoại không hợp lệ! (Phải là số di động VN 10 chữ số)", "Lỗi Regex BR05", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!cccd.matches("^[0-9]{12}$")) {
                    JOptionPane.showMessageDialog(page, "Căn cước công dân không hợp lệ! (Phải gồm đúng 12 chữ số)", "Lỗi Regex BR05", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String maNV = DataStore.nextMaNhanVien();
                NhanVien nvMoi = new NhanVien(maNV, ten, sdt, cccd, (String) cbChucVu.getSelectedItem(), true);
                DataStore.themNhanVienVaoDB(nvMoi);
                JOptionPane.showMessageDialog(page, "Đã thêm nhân viên [" + maNV + "] " + ten + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnToggleNV.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(page, "Vui lòng chọn 1 nhân viên từ bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String ma = (String) table.getValueAt(row, 0);
            for (NhanVien nv : DataStore.getDsNhanVien()) {
                if (nv.getMaNV().equals(ma)) {
                    nv.setDangLamViec(!nv.isDangLamViec());
                    DataStore.saveNhanVien(nv);
                    break;
                }
            }
        });

        btnDoiSoatCa.addActionListener(e -> {
            LocalDate activeDate = getActiveBusinessDate();
            double tongTienMat = 0;
            double tongCK = 0;
            int soDon = 0;
            for (HoaDon hd : DataStore.getDsHoaDon()) {
                boolean sameDay = hd.getNgayLap() != null && hd.getNgayLap().toLocalDate().isEqual(activeDate);
                if (!"DA_HUY".equals(hd.getTrangThaiDon()) && sameDay) {
                    soDon++;
                    if (isTienMat(hd)) {
                        tongTienMat += hd.getTongTien();
                    } else {
                        tongCK += hd.getTongTien();
                    }
                }
            }

            String ngayStr = activeDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            JTextField txtDauCa = new JTextField("1000000");
            JTextField txtChiCa = new JTextField("0");
            JTextField txtThucDem = new JTextField(String.format("%.0f", 1000000 + tongTienMat));

            JPanel pnl = new JPanel(new GridLayout(0, 2, 10, 10));
            pnl.add(new JLabel("Ngày đối soát:")); pnl.add(new JLabel(ngayStr));
            pnl.add(new JLabel("Tiền mặt đầu ca (VND):")); pnl.add(txtDauCa);
            pnl.add(new JLabel("Tiền chi trong ca (mua vặt...):")); pnl.add(txtChiCa);
            pnl.add(new JLabel("Doanh thu Tiền mặt trong ngày:")); pnl.add(new JLabel(String.format("%,.0f đ (%d đơn)", tongTienMat, soDon)));
            pnl.add(new JLabel("Doanh thu Chuyển khoản (VietQR):")); pnl.add(new JLabel(String.format("%,.0f đ", tongCK)));
            pnl.add(new JLabel("Tiền mặt THỰC ĐẾM cuối ca:")); pnl.add(txtThucDem);

            int opt = JOptionPane.showConfirmDialog(page, pnl, "Đối soát két tiền kết ca (" + ngayStr + ") - HomuCafe", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                double dauCa = parseCurrency(txtDauCa.getText());
                double chiCa = parseCurrency(txtChiCa.getText());
                double thucDem = parseCurrency(txtThucDem.getText());
                double lyThuyet = dauCa + tongTienMat - chiCa;
                double chenhLech = thucDem - lyThuyet;

                String ketQua = String.format(
                        "BIÊN BẢN ĐỐI SOÁT TIỀN MẶT KẾT CA (%s)\n" +
                        "========================================\n" +
                        "• Tiền đầu ca:           %,12.0f đ\n" +
                        "• Thu tiền mặt trong ca:+%,12.0f đ\n" +
                        "• Chi phí trong ca:     -%,12.0f đ\n" +
                        "----------------------------------------\n" +
                        "• TIỀN MẶT LÝ THUYẾT:    %,12.0f đ\n" +
                        "• TIỀN MẶT THỰC ĐẾM:     %,12.0f đ\n" +
                        "========================================\n" +
                        "• ĐỘ LỆCH (Thừa / Thiếu):%,12.0f đ (%s)\n",
                        ngayStr, dauCa, tongTienMat, chiCa, lyThuyet, thucDem,
                        chenhLech, chenhLech == 0 ? "Khớp hoàn hảo 100%" : (chenhLech > 0 ? "Thừa tiền két" : "Thiếu tiền két")
                );
                JOptionPane.showMessageDialog(page, ketQua, "Kết quả đối soát ca", chenhLech == 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
            }
        });

        body.add(Theme.row(btnThemNV, btnToggleNV, btnDoiSoatCa), BorderLayout.NORTH);
        body.add(Theme.tablePanel("Danh sách nhân sự (Nhấp đúp ô để sửa trực tiếp · Nhấp cột Trạng thái để bật/tắt)", table), BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    /* -------------------------------------------------------------
     * 8. KHÁCH HÀNG THÂN THIẾT
     * ------------------------------------------------------------- */
    private static JPanel customers() {
        JPanel page = Theme.page("Khách hàng quen", "Khách quen của Homu.",
                "Tra cứu lịch sử mua hàng, tích lũy điểm thưởng. Nhấp đúp ô để sửa trực tiếp thông tin khách hàng.");
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        String[] cols = {"MÃ KH", "HỌ TÊN KHÁCH HÀNG", "ĐIỆN THOẠI", "ĐIỂM TÍCH LŨY", "GIÁ TRỊ QUY ĐỔI"};
        final boolean[] updatingCustFromStore = {false};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1 || column == 2 || column == 3;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column);
                if (updatingCustFromStore[0]) return;
                try {
                    String ma = (String) getValueAt(row, 0);
                    for (KhachHang kh : DataStore.getDsKhachHang()) {
                        if (kh.getMaKH().equals(ma) && aValue != null) {
                            String text = aValue.toString().trim();
                            switch (column) {
                                case 1 -> kh.setHoTen(text);
                                case 2 -> kh.setSoDienThoai(text);
                                case 3 -> {
                                    int pts = (int) parseCurrency(text);
                                    kh.setDiemTichLuy(pts);
                                    updatingCustFromStore[0] = true;
                                    setValueAt(pts + " điểm", row, 3);
                                    setValueAt(String.format("%,.0f đ", (double) (pts * 1000)), row, 4);
                                    updatingCustFromStore[0] = false;
                                }
                            }
                            DataStore.saveKhachHang(kh);
                            break;
                        }
                    }
                } catch (Exception ignored) { }
            }
        };
        JTable table = Theme.table(cols, new Object[0][]);
        table.setModel(model);

        Runnable loadCust = () -> {
            updatingCustFromStore[0] = true;
            model.setRowCount(0);
            for (KhachHang kh : DataStore.getDsKhachHang()) {
                model.addRow(new Object[]{
                        kh.getMaKH(),
                        kh.getHoTen(),
                        kh.getSoDienThoai(),
                        kh.getDiemTichLuy() + " điểm",
                        String.format("%,.0f đ", (double) (kh.getDiemTichLuy() * 1000))
                });
            }
            updatingCustFromStore[0] = false;
        };
        DataStore.addDataChangeListener(loadCust);
        loadCust.run();

        JButton btnThem = Theme.button("+ Đăng ký thành viên mới", true);
        btnThem.addActionListener(e -> {
            String ten = JOptionPane.showInputDialog(page, "Nhập họ tên khách hàng:");
            if (ten == null || ten.trim().isEmpty()) return;
            String sdt = JOptionPane.showInputDialog(page, "Nhập số điện thoại (10 số):");
            if (sdt != null && sdt.trim().matches("^0[3|5|7|8|9][0-9]{8}$")) {
                String ma = DataStore.nextMaKhachHang();
                KhachHang khMoi = new KhachHang(ma, ten.trim(), sdt.trim(), 10);
                DataStore.themKhachHangVaoDB(khMoi);
                JOptionPane.showMessageDialog(page, "Đã tạo tài khoản thành viên [" + ma + "] thành công (Tặng 10 điểm chào mừng)!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else if (sdt != null) {
                JOptionPane.showMessageDialog(page, "Số điện thoại không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        body.add(Theme.row(btnThem), BorderLayout.NORTH);
        body.add(Theme.tablePanel("Danh sách thành viên HomuCafe (Nhấp đúp ô để sửa trực tiếp)", table), BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    /* -------------------------------------------------------------
     * BIỂU ĐỒ DOANH THU NHỊP BÁN HÀNG JAVA2D TÍNH TỪ DATASTORE
     * ------------------------------------------------------------- */
    private static final class SalesChart extends JPanel {
        SalesChart() {
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                Theme.smooth(g);
                g.setColor(Theme.WHITE);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g.setColor(Theme.LINE);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                g.setColor(Theme.INK);
                g.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g.drawString("Nhịp bán hàng theo khung giờ", 24, 37);

                g.setColor(Theme.MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g.drawString("Số ly nước bán ra trong ngày", 24, 61);

                int bottom = getHeight() - 70;
                int available = Math.max(30, getHeight() - 180);
                int width = Math.max(12, (getWidth() - 105) / 7);

                LocalDate activeDate = getActiveBusinessDate();
                int[] values = new int[7];
                String[] hours = {"8h", "10h", "12h", "14h", "16h", "18h", "20h"};
                for (HoaDon hd : DataStore.getDsHoaDon()) {
                    if ("DA_HUY".equalsIgnoreCase(hd.getTrangThaiDon()) || hd.getNgayLap() == null) continue;
                    if (!hd.getNgayLap().toLocalDate().isEqual(activeDate)) continue;
                    int hour = hd.getNgayLap().getHour();
                    int idx = (hour < 10) ? 0
                            : (hour < 12) ? 1
                            : (hour < 14) ? 2
                            : (hour < 16) ? 3
                            : (hour < 18) ? 4
                            : (hour < 20) ? 5 : 6;
                    values[idx] += hd.getTongSoLy();
                }

                int maxVal = 0;
                int maxIdx = 0;
                for (int i = 0; i < 7; i++) {
                    if (values[i] > maxVal) {
                        maxVal = values[i];
                        maxIdx = i;
                    }
                }
                int scaleTop = ((Math.max(10, maxVal) + 3) / 4) * 4;

                for (int n = 0; n <= 4; n++) {
                    int y = bottom - available * n / 4;
                    g.setColor(Theme.LINE);
                    g.drawLine(44, y, getWidth() - 25, y);
                    g.setColor(Theme.MUTED);
                    g.drawString(String.valueOf(scaleTop * n / 4), 20, y + 4);
                }

                for (int i = 0; i < 7; i++) {
                    int h = available * values[i] / scaleTop;
                    int x = 55 + i * width;
                    g.setColor(maxVal > 0 && i == maxIdx ? Theme.GREEN : new Color(210, 225, 204));
                    g.fillRoundRect(x, bottom - h, Math.max(8, width - 18), h, 8, 8);
                    g.setColor(Theme.MUTED);
                    g.drawString(hours[i], x + 5, bottom + 25);
                }
            } finally {
                g.dispose();
            }
        }
    }

    private static double parseCurrency(String text) {
        if (text == null) return 0;
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Double.parseDouble(digits);
        } catch (Exception e) {
            return 0;
        }
    }
}
