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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Các phân hệ nghiệp vụ hoàn chỉnh của HomuCafe POS & Pager System.
 * Kết nối tương tác thời gian thực qua DataStore (Bán hàng POS <-> Quầy Bar <-> Sơ đồ bàn <-> Hóa đơn).
 * Đã loại bỏ Kho, Nhập hàng, Công thức món theo đúng phạm vi tài liệu BAO_CAO_DO_AN_COFFEE_POS.docx.
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
    }

    /* -------------------------------------------------------------
     * 1. TỔNG QUAN (DASHBOARD)
     * ------------------------------------------------------------- */
    private static JPanel dashboard() {
        JPanel page = Theme.page("Một ngày ở Homu", "Chào buổi sáng, chủ quán.",
                "Tổng quan hoạt động bán hàng · Doanh thu thực tế, số bàn và thẻ rung");
        JPanel body = new JPanel(new BorderLayout(0, 22));
        body.setOpaque(false);

        // Tính toán số liệu thực tế từ DataStore
        long tongThu = 0;
        int donDaThanhToan = 0;
        int theRungDangDung = 0;
        int banDangPhucVu = 0;

        for (HoaDon hd : DataStore.getDsHoaDon()) {
            if (!"DA_HUY".equals(hd.getTrangThaiDon())) {
                tongThu += hd.getTongTien();
                donDaThanhToan++;
            }
            if ("DANG_PHA_CHE".equals(hd.getTrangThaiDon()) || "CHO_GIAO".equals(hd.getTrangThaiDon())) {
                theRungDangDung++;
            }
        }
        for (String st : DataStore.getTrangThaiBan().values()) {
            if ("DANG_PHUC_VU".equals(st)) banDangPhucVu++;
        }

        body.add(Theme.cards(
                Theme.card("TIỀN THU HÔM NAY", String.format("%,.0f đ", (double) tongThu), donDaThanhToan + " đơn đã thanh toán"),
                Theme.card("BÀN ĐANG PHỤC VỤ", banDangPhucVu + " / 12", (12 - banDangPhucVu) + " bàn sẵn sàng đón khách"),
                Theme.card("THẺ RUNG ĐANG DÙNG", theRungDangDung + " / 20", DataStore.demSoTheRungRanh() + " thẻ sẵn sàng trên khay sạc")
        ), BorderLayout.NORTH);

        JPanel lower = new JPanel(new GridLayout(1, 2, 22, 0));
        lower.setOpaque(false);
        lower.add(new SalesChart());

        // Top món bán chạy
        Map<String, Integer> topMon = new LinkedHashMap<>();
        topMon.put("Cà Phê Sữa Đá", 168);
        topMon.put("Trà Đào Cam Sả", 124);
        topMon.put("Latte", 96);
        topMon.put("Bạc Xỉu 3 Tầng", 82);
        topMon.put("Homu Matcha Latte", 65);

        Object[][] tableData = new Object[topMon.size()][2];
        int idx = 0;
        for (Map.Entry<String, Integer> e : topMon.entrySet()) {
            tableData[idx][0] = e.getKey();
            tableData[idx][1] = e.getValue() + " ly";
            idx++;
        }

        lower.add(Theme.tablePanel("Top món được yêu thích nhất", Theme.table(
                new String[]{"MÓN ĐỒ UỐNG", "SỐ LY ĐÃ BÁN"}, tableData)));

        body.add(lower, BorderLayout.CENTER);
        page.add(body);
        return page;
    }

    /* -------------------------------------------------------------
     * 2. BÁN HÀNG TẠI QUẦY (POS) - LIÊN THÔNG SỰ KIỆN TOÀN DIỆN
     * ------------------------------------------------------------- */
    private static JPanel sales() {
        JPanel page = Theme.page("Bán hàng tại quầy", "Một đơn ngon lành.",
                "Chọn món, tùy biến kích cỡ/đường/đá, gán thẻ rung (Pager), áp voucher và thanh toán. (F1: Mới, F2: Tìm, F3: Thanh toán, F4: Thẻ rung)");
        JPanel body = new JPanel(new BorderLayout(20, 0));
        body.setOpaque(false);

        // Panel thực đơn món bên trái
        JPanel menuPanel = new JPanel(new BorderLayout(0, 14));
        menuPanel.setOpaque(false);

        JTextField searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Tìm món nhanh (gõ cfd, ts, bạc xỉu...) [F2]");
        searchField.putClientProperty("JTextField.leadingIcon", new Theme.LineIcon("search", 16, Theme.MUTED));
        searchField.setColumns(20);

        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"Tất cả nhóm món", "Cà phê", "Trà trái cây", "Trà sữa", "Đá xay"});
        menuPanel.add(Theme.row(Theme.label("Phân loại:", 12, Theme.MUTED), categoryCombo, searchField), BorderLayout.NORTH);

        JPanel productsGrid = new JPanel(new GridLayout(0, 3, 14, 14));
        productsGrid.setOpaque(false);

        JScrollPane scrollMenu = new JScrollPane(productsGrid);
        scrollMenu.setBorder(BorderFactory.createEmptyBorder());
        scrollMenu.getViewport().setBackground(Theme.PAPER);
        scrollMenu.getVerticalScrollBar().setUnitIncrement(16);
        menuPanel.add(scrollMenu, BorderLayout.CENTER);
        body.add(menuPanel, BorderLayout.CENTER);

        // Panel Giỏ hàng / Hóa đơn bên phải
        JPanel billPanel = Theme.surface(new BorderLayout(0, 14));
        billPanel.setBorder(new EmptyBorder(18, 18, 18, 18));
        billPanel.setPreferredSize(new Dimension(360, 0));

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

        // Nhận diện khách hàng & Tích điểm (UC07)
        JTextField txtCustomerPhone = new JTextField(12);
        txtCustomerPhone.putClientProperty("JTextField.placeholderText", "SĐT khách thành viên");
        JLabel lblCustomerInfo = Theme.label("Khách vãng lai (Chưa tích điểm)", 11, Theme.MUTED);
        final KhachHang[] activeCustomer = {null};
        final boolean[] usePoints = {false};

        JButton btnLookupCustomer = Theme.button("Tìm", false);
        btnLookupCustomer.addActionListener(e -> {
            String sdt = txtCustomerPhone.getText().trim();
            KhachHang kh = DataStore.timKhachHangTheoSDT(sdt);
            if (kh != null) {
                activeCustomer[0] = kh;
                lblCustomerInfo.setText(kh.getHoTen() + " · Điểm: " + kh.getDiemTichLuy() + " (" + (kh.getDiemTichLuy() * 1000) + " đ)");
                lblCustomerInfo.setForeground(Theme.GREEN);
            } else {
                activeCustomer[0] = null;
                lblCustomerInfo.setText("Không tìm thấy KH · Bấm để thêm mới");
                lblCustomerInfo.setForeground(Theme.ORANGE);
            }
        });

        JPanel pnlCust = new JPanel(new BorderLayout(4, 2));
        pnlCust.setOpaque(false);
        pnlCust.add(Theme.row(txtCustomerPhone, btnLookupCustomer), BorderLayout.NORTH);
        pnlCust.add(lblCustomerInfo, BorderLayout.SOUTH);

        JPanel pnlBillHeader = Theme.stack(8,
                Theme.row(Theme.label("HÓA ĐƠN POS", 11, Theme.MUTED), Theme.label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), 11, Theme.MUTED)),
                cbTable,
                cbPager,
                pnlCust
        );
        billPanel.add(pnlBillHeader, BorderLayout.NORTH);

        // Bảng dòng chi tiết hóa đơn
        String[] cartCols = {"MÓN & SIZE", "SL", "TIỀN"};
        DefaultTableModel cartModel = new DefaultTableModel(cartCols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tableCart = Theme.table(cartCols, new Object[0][]);
        tableCart.setModel(cartModel);
        tableCart.getColumnModel().getColumn(0).setPreferredWidth(170);
        tableCart.getColumnModel().getColumn(1).setPreferredWidth(35);
        tableCart.getColumnModel().getColumn(2).setPreferredWidth(85);

        billPanel.add(Theme.tablePanel("Chi tiết món đặt", tableCart), BorderLayout.CENTER);

        // Danh sách item trong giỏ hàng thật
        List<ChiTietHoaDon> currentOrderItems = new ArrayList<>();
        final double[] subtotal = {0};
        final double[] discount = {0};

        JLabel lblSubtotal = Theme.label("Tổng tiền: 0 đ", 14, Theme.INK);
        JLabel lblDiscount = Theme.label("Giảm giá: 0 đ", 12, Theme.MUTED);
        JLabel lblTotal = Theme.label("Tổng cộng: 0 đ", 20, Theme.GREEN);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));

        Runnable recalculate = () -> {
            double total = 0;
            for (ChiTietHoaDon ct : currentOrderItems) {
                total += ct.getThanhTien();
            }
            subtotal[0] = total;
            double finalPay = Math.max(0, subtotal[0] - discount[0]);
            lblSubtotal.setText(String.format("Tổng tiền: %,.0f đ", subtotal[0]));
            lblDiscount.setText(String.format("Giảm giá: %,.0f đ", discount[0]));
            lblTotal.setText(String.format("Tổng cộng: %,.0f đ", finalPay));
        };

        // Render danh sách món nước
        Runnable renderMenu = () -> {
            productsGrid.removeAll();
            String cat = (String) categoryCombo.getSelectedItem();
            String q = searchField.getText();

            for (Mon mon : DataStore.getDsMon()) {
                if (!mon.isDangKinhDoanh()) continue;
                if (!"Tất cả nhóm món".equals(cat) && !mon.getLoaiMon().getTenLoai().equalsIgnoreCase(cat)) continue;
                if (!DataStore.khopTimKiemMon(mon, q)) continue;

                int kind = 0;
                try {
                    kind = Integer.parseInt(mon.getHinhAnh());
                } catch (Exception ignored) { }

                JPanel card = Theme.surface(new BorderLayout(0, 10));
                card.setBorder(new EmptyBorder(12, 12, 12, 12));
                card.add(new DrinkIllustration(kind), BorderLayout.CENTER);

                JLabel name = Theme.label(mon.getTenMon(), 14, Theme.INK);
                name.setFont(new Font("Segoe UI", Font.BOLD, 14));

                JPanel details = new JPanel(new BorderLayout());
                details.setOpaque(false);
                JButton addBtn = Theme.button("+", true);
                addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
                addBtn.setToolTipText("Thêm vào đơn & tùy biến");

                addBtn.addActionListener(e -> {
                    Window win = SwingUtilities.getWindowAncestor(page);
                    int drinkType = 0;
                    try { drinkType = Integer.parseInt(mon.getHinhAnh()); } catch (Exception ignored) {}
                    DialogTuyBienMon dlg = new DialogTuyBienMon(win, mon.getTenMon(), mon.getDonGiaGoc(), drinkType);
                    dlg.setVisible(true);
                    if (dlg.isConfirmed()) {
                        KichCo size = KichCo.valueOf(dlg.getSizeName());
                        ChiTietHoaDon ct = new ChiTietHoaDon(
                                currentOrderItems.size() + 1,
                                mon,
                                size,
                                1,
                                dlg.getTongGia(),
                                100,
                                "BINH_THUONG",
                                dlg.getCustomDetail()
                        );
                        currentOrderItems.add(ct);
                        cartModel.addRow(new Object[]{
                                mon.getTenMon() + " (" + dlg.getSizeName() + ")",
                                1,
                                String.format("%,.0f đ", dlg.getTongGia())
                        });
                        recalculate.run();
                    }
                });

                details.add(Theme.stack(3, name, Theme.label(String.format("%,.0f đ", mon.getDonGiaGoc()), 12, Theme.ORANGE)), BorderLayout.CENTER);
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

        // Voucher & Khuyến mãi
        JTextField txtVoucher = new JTextField();
        txtVoucher.putClientProperty("JTextField.placeholderText", "Mã voucher (HOMU10, CHIEUVUI)");
        JButton btnApplyVoucher = Theme.button("Áp mã", false);

        btnApplyVoucher.addActionListener(e -> {
            String code = txtVoucher.getText().trim().toUpperCase();
            if (code.equals("HOMU10")) {
                discount[0] = subtotal[0] * 0.10;
                JOptionPane.showMessageDialog(page, "Đã áp dụng HOMU10: Giảm 10% đơn hàng!", "Voucher thành công", JOptionPane.INFORMATION_MESSAGE);
            } else if (code.equals("CHIEUVUI")) {
                discount[0] = 15000;
                JOptionPane.showMessageDialog(page, "Đã áp dụng CHIEUVUI: Giảm 15.000 đ!", "Voucher thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(page, "Mã voucher không hợp lệ hoặc đã hết lượt!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
            recalculate.run();
        });

        JPanel pnlVoucherRow = new JPanel(new BorderLayout(6, 0));
        pnlVoucherRow.setOpaque(false);
        pnlVoucherRow.add(txtVoucher, BorderLayout.CENTER);
        pnlVoucherRow.add(btnApplyVoucher, BorderLayout.EAST);

        // Nút Thanh toán & Gửi pha chế
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

            double finalPrice = Math.max(0, subtotal[0] - discount[0]);
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
                }

                String maHD = DataStore.nextMaHoaDon();
                String banDuocChon = (String) cbTable.getSelectedItem();
                String hinhThuc = banDuocChon.contains("Mang đi") ? "MANG_DI" : "TAI_QUAN";

                HoaDon newHD = new HoaDon(
                        maHD,
                        LocalDateTime.now(),
                        DataStore.getDsNhanVien().get(0),
                        activeCustomer[0],
                        banDuocChon,
                        pagerNumber,
                        hinhThuc,
                        phuongThuc,
                        "DANG_PHA_CHE",
                        finalPrice,
                        discount[0],
                        tienKhachDua,
                        tienThoi,
                        null
                );

                for (ChiTietHoaDon ct : currentOrderItems) {
                    newHD.themChiTiet(ct);
                }

                // Cập nhật vào DataStore và đồng bộ xuống SQL Server
                DataStore.getDsHoaDon().add(0, newHD);
                DataStore.saveHoaDon(newHD);

                // Cập nhật trạng thái bàn
                if (!banDuocChon.contains("Mang đi")) {
                    try {
                        int banSo = Integer.parseInt(banDuocChon.replace("Bàn", "").trim());
                        DataStore.getTrangThaiBan().put(banSo, "DANG_PHUC_VU");
                        DataStore.saveTrangThaiBan(banSo, "DANG_PHUC_VU");
                    } catch (Exception ignored) { }
                }

                // Tích điểm thành viên
                if (activeCustomer[0] != null) {
                    int diemCong = (int) (finalPrice / 10000);
                    activeCustomer[0].setDiemTichLuy(activeCustomer[0].getDiemTichLuy() + diemCong);
                    DataStore.saveKhachHang(activeCustomer[0]);
                }

                // Thông báo realtime sang Quầy Bar & Hóa đơn
                DataStore.notifyDataChanged();

                // Hiển thị Bill in nhiệt
                JOptionPane.showMessageDialog(page,
                        "========================================\n" +
                                "           HOMUCAFE POS & PAGER         \n" +
                                "========================================\n" +
                                "Mã hóa đơn: " + maHD + "\n" +
                                "Thời gian: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\n" +
                                "Vị trí: " + banDuocChon + " · Thẻ rung: " + (pagerNumber > 0 ? "#" + pagerNumber : "Mang đi") + "\n" +
                                "Khách hàng: " + (activeCustomer[0] != null ? activeCustomer[0].getHoTen() : "Khách vãng lai") + "\n" +
                                "----------------------------------------\n" +
                                "Tổng thanh toán: " + String.format("%,.0f đ", finalPrice) + "\n" +
                                "Khách đưa: " + String.format("%,.0f đ", tienKhachDua) + "\n" +
                                "Tiền thối: " + String.format("%,.0f đ", tienThoi) + "\n" +
                                "Phương thức: " + (choice == 0 ? "Tiền mặt" : "VietQR") + "\n" +
                                "========================================\n" +
                                "Đã gửi đơn tức thì sang Quầy Bar (FIFO)!",
                        "In Hóa Đơn Nhiệt 80mm", JOptionPane.INFORMATION_MESSAGE);

                // Reset giỏ hàng
                currentOrderItems.clear();
                cartModel.setRowCount(0);
                txtVoucher.setText("");
                discount[0] = 0;
                recalculate.run();
                refreshPagers.run();
            }
        });

        // Phím tắt toàn cục
        page.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F1"), "newOrder");
        page.getActionMap().put("newOrder", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                currentOrderItems.clear();
                cartModel.setRowCount(0);
                recalculate.run();
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

        JPanel summary = Theme.stack(10,
                Theme.label("MÃ VOUCHER / ƯU ĐÃI", 10, Theme.MUTED),
                pnlVoucherRow,
                lblSubtotal,
                lblDiscount,
                lblTotal,
                btnPay
        );
        summary.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.LINE),
                new EmptyBorder(14, 0, 0, 0)
        ));

        billPanel.add(summary, BorderLayout.SOUTH);
        body.add(billPanel, BorderLayout.EAST);
        page.add(body);
        return page;
    }

    /* -------------------------------------------------------------
     * 3. SƠ ĐỒ BÀN (TABLE MAP) - ĐỒNG BỘ THỜI GIAN THỰC
     * ------------------------------------------------------------- */
    private static JPanel tables() {
        JPanel page = Theme.page("Không gian phục vụ", "Sơ đồ bàn",
                "Tầng trệt · 12 bàn phục vụ · Nhấp vào bàn để đổi trực tiếp trạng thái Trống / Đang phục vụ.");
        JPanel grid = new JPanel(new GridLayout(3, 4, 16, 16));
        grid.setOpaque(false);

        Runnable refreshTables = () -> {
            grid.removeAll();
            for (int i = 1; i <= DataStore.TONG_SO_BAN; i++) {
                final int banSo = i;
                String status = DataStore.getTrangThaiBan().getOrDefault(banSo, "TRONG");
                boolean busy = "DANG_PHUC_VU".equals(status);

                JPanel card = Theme.card(
                        "BÀN " + String.format("%02d", banSo),
                        busy ? "Đang phục vụ" : "Trống",
                        busy ? "Khách đang dùng nước (Nhấp để trả bàn trống)" : "4 chỗ · Sẵn sàng đón khách (Nhấp để mở bàn)"
                );
                if (busy) {
                    card.setBackground(Theme.PALE);
                }

                // Click trực tiếp vào bàn để đổi trạng thái Trống <-> Đang phục vụ
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                card.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                        String newStatus = busy ? "TRONG" : "DANG_PHUC_VU";
                        DataStore.getTrangThaiBan().put(banSo, newStatus);
                        DataStore.saveTrangThaiBan(banSo, newStatus);
                        selectPOSTable(String.format("Bàn %02d", banSo));
                        DataStore.notifyDataChanged();
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
     * 4. QUẦY PHA CHẾ (KITCHEN / BAR DISPLAY FIFO REALTIME)
     * ------------------------------------------------------------- */
    private static JPanel kitchen() {
        JPanel page = Theme.page("Pha chế & phục vụ", "Từng món, đúng nhịp.",
                "Hàng đợi đơn hàng FIFO thời gian thực · Kích hoạt gọi thẻ rung và thu hồi thẻ (UC11, UC12, UC13).");
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

                JPanel card = Theme.surface(new BorderLayout(0, 12));
                card.setBorder(new EmptyBorder(16, 16, 16, 16));
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

                String timeStr = hd.getNgayLap().format(DateTimeFormatter.ofPattern("HH:mm"));
                String headerStr = hd.getMaHoaDon() + " · " + hd.getBanPhucVu() + (hd.getSoTheRung() > 0 ? " (Thẻ #" + hd.getSoTheRung() + ")" : "");

                JPanel ticketHeader = new JPanel(new BorderLayout());
                ticketHeader.setOpaque(false);
                ticketHeader.add(Theme.label(headerStr, 13, Theme.INK), BorderLayout.WEST);
                ticketHeader.add(Theme.label(timeStr, 11, Theme.MUTED), BorderLayout.EAST);

                JPanel itemsList = new JPanel();
                itemsList.setLayout(new BoxLayout(itemsList, BoxLayout.Y_AXIS));
                itemsList.setOpaque(false);

                for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
                    String line = ct.getSoLuong() + " × " + ct.getMon().getTenMon() + " (" + ct.getKichCo().name() + ")";
                    itemsList.add(Theme.label(line, 13, Theme.INK));
                    if (ct.getGhiChu() != null && !ct.getGhiChu().isEmpty()) {
                        itemsList.add(Theme.label("   ↳ " + ct.getGhiChu(), 11, Theme.ORANGE));
                    }
                }

                if ("DANG_PHA_CHE".equals(hd.getTrangThaiDon())) {
                    card.setBackground(Theme.WHITE);
                    JButton btnStart = Theme.button("Hoàn thành & Báo rung [UC12]", true);
                    btnStart.addActionListener(e -> {
                        hd.setTrangThaiDon("CHO_GIAO");
                        DataStore.saveTrangThaiHoaDon(hd.getMaHoaDon(), "CHO_GIAO");
                        DataStore.notifyDataChanged();
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
                    JButton btnDone = Theme.button("Đã giao & Thu hồi thẻ [UC13]", false);
                    btnDone.addActionListener(e -> {
                        hd.setTrangThaiDon("HOAN_TAT");
                        DataStore.saveTrangThaiHoaDon(hd.getMaHoaDon(), "HOAN_TAT");
                        DataStore.notifyDataChanged();
                        JOptionPane.showMessageDialog(page,
                                "Đã thu hồi thẻ rung #" + hd.getSoTheRung() + " về khay sạc an toàn!\nĐơn hàng hoàn tất.",
                                "Thu hồi thẻ rung", JOptionPane.INFORMATION_MESSAGE);
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

        JPanel col1 = Theme.surface(new BorderLayout(0, 10)); col1.setBorder(new EmptyBorder(14, 14, 14, 14));
        col1.add(Theme.label("CHỜ PHA CHẾ (FIFO)", 12, Theme.ORANGE), BorderLayout.NORTH); col1.add(sc1, BorderLayout.CENTER);

        JPanel col2 = Theme.surface(new BorderLayout(0, 10)); col2.setBorder(new EmptyBorder(14, 14, 14, 14));
        col2.add(Theme.label("SẴN SÀNG GIAO (ĐÃ BÁO RUNG)", 12, Theme.GREEN), BorderLayout.NORTH); col2.add(sc3, BorderLayout.CENTER);

        columns.add(col1);
        columns.add(col2);

        page.add(columns, BorderLayout.CENTER);
        return page;
    }

    /* -------------------------------------------------------------
     * 5. QUẢN LÝ HÓA ĐƠN (INVOICES) - MASTER DETAIL & HỦY HÓA ĐƠN BR03
     * ------------------------------------------------------------- */
    private static JPanel invoices() {
        JPanel page = Theme.page("Quản lý hóa đơn", "Tra cứu & in ấn hóa đơn.",
                "Theo dõi toàn bộ lịch sử thanh toán, chi tiết từng ly đồ uống, in lại bill và hủy hóa đơn (BR03).");
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        String[] cols = {"MÃ HĐ", "NGÀY GIỜ", "BÀN / PAGER", "NHÂN VIÊN", "HÌNH THỨC", "TỔNG TIỀN", "TRẠNG THÁI"};
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
                        hd.getNhanVien() != null ? hd.getNhanVien().getHoTen() : "Minh Anh",
                        hd.getPhuongThucThanhToan().equals("TIEN_MAT") ? "Tiền mặt" : "VietQR",
                        String.format("%,.0f đ", hd.getTongTien()),
                        statusStr
                });
            }
        };
        DataStore.addDataChangeListener(loadInvoices);
        loadInvoices.run();

        JButton btnPrint = Theme.button("In lại hóa đơn", false);
        JButton btnCancel = Theme.button("Hủy hóa đơn (BR03)", false);

        btnPrint.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(page, "Vui lòng chọn 1 hóa đơn từ bảng để in lại!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String ma = (String) table.getValueAt(row, 0);
            JOptionPane.showMessageDialog(page,
                    "Đang gửi lệnh in lại hóa đơn nhiệt 80mm cho mã " + ma + "...",
                    "In lại hóa đơn", JOptionPane.INFORMATION_MESSAGE);
        });

        btnCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(page, "Vui lòng chọn 1 hóa đơn để hủy!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String ma = (String) table.getValueAt(row, 0);

            // Xác thực quyền Quản lý BR03
            JPasswordField pwd = new JPasswordField();
            int action = JOptionPane.showConfirmDialog(page, pwd, "Yêu cầu mật khẩu Quản lý để hủy bill (Mặc định: 123456):", JOptionPane.OK_CANCEL_OPTION);
            if (action == JOptionPane.OK_OPTION) {
                String pass = new String(pwd.getPassword());
                if ("123456".equals(pass) || "admin".equals(pass)) {
                    for (HoaDon hd : DataStore.getDsHoaDon()) {
                        if (hd.getMaHoaDon().equals(ma)) {
                            hd.setTrangThaiDon("DA_HUY");
                            DataStore.saveTrangThaiHoaDon(ma, "DA_HUY");
                            break;
                        }
                    }
                    DataStore.notifyDataChanged();
                    JOptionPane.showMessageDialog(page, "Đã hủy hóa đơn " + ma + " thành công (Bảo toàn dữ liệu theo BR03)!", "Hủy thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(page, "Mật khẩu Quản lý không chính xác!", "Từ chối thao tác", JOptionPane.ERROR_MESSAGE);
                }
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
     * 6. THỰC ĐƠN MÓN (MENU MANAGEMENT & XÓA MỀM UC16)
     * ------------------------------------------------------------- */
    private static JPanel menuManagement() {
        JPanel page = Theme.page("Thực đơn quán", "Món ngon mỗi ngày.",
                "Quản lý danh mục đồ uống. Nhấp đúp vào ô để sửa trực tiếp · Nhấp cột Tình trạng để bật/tắt nhanh Đang sử dụng / Không sử dụng.");
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
                        DataStore.notifyDataChanged();
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

        // Click trực tiếp vào cột Tình trạng để bật/tắt nhanh
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
                        DataStore.notifyDataChanged();
                    }
                }
            }
        });

        JButton btnThem = Theme.button("+ Thêm món mới", true);
        JButton btnToggle = Theme.button("Đổi tình trạng sử dụng", false);

        btnThem.addActionListener(e -> {
            String ten = JOptionPane.showInputDialog(page, "Nhập tên món mới:");
            if (ten != null && !ten.trim().isEmpty()) {
                String ma = "MON" + (DataStore.getDsMon().size() + 1);
                Mon newMon = new Mon(ma, ten.trim(), 40000, "0", true, DataStore.getDsLoaiMon().get(0));
                DataStore.getDsMon().add(newMon);
                DataStore.themMonVaoDB(newMon);
                DataStore.notifyDataChanged();
                JOptionPane.showMessageDialog(page, "Đã thêm món mới: " + ten, "Thành công", JOptionPane.INFORMATION_MESSAGE);
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
                DataStore.notifyDataChanged();
            }
        });

        body.add(Theme.row(btnThem, btnToggle), BorderLayout.NORTH);
        body.add(Theme.tablePanel("Bảng giá thực đơn HomuCafe (Nhấp đúp ô để sửa trực tiếp · Nhấp cột Tình trạng để bật/tắt)", table), BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    /* -------------------------------------------------------------
     * 7. NHÂN SỰ & CA LÀM (REGEX BR05)
     * ------------------------------------------------------------- */
    private static JPanel staff() {
        JPanel page = Theme.page("Nhân sự & ca làm", "Cùng nhau vận hành Homu.",
                "Hồ sơ nhân viên, phân quyền tài khoản. Nhấp đúp ô để sửa trực tiếp · Nhấp cột Trạng thái để bật/tắt Đang sử dụng / Không sử dụng.");
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
                            DataStore.notifyDataChanged();
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

        // Click trực tiếp vào cột Trạng thái để bật/tắt nhanh
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
                            DataStore.notifyDataChanged();
                            break;
                        }
                    }
                }
            }
        });

        JButton btnThemNV = Theme.button("+ Thêm nhân viên mới", true);
        JButton btnToggleNV = Theme.button("Đổi trạng thái sử dụng", false);

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

                // Regex Validation (BR05)
                if (!sdt.matches("^0[3|5|7|8|9][0-9]{8}$")) {
                    JOptionPane.showMessageDialog(page, "Số điện thoại không hợp lệ! (Phải là số di động VN 10 chữ số)", "Lỗi Regex BR05", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!cccd.matches("^[0-9]{12}$")) {
                    JOptionPane.showMessageDialog(page, "Căn cước công dân không hợp lệ! (Phải gồm đúng 12 chữ số)", "Lỗi Regex BR05", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String maNV = "NV" + String.format("%02d", DataStore.getDsNhanVien().size() + 1);
                NhanVien nvMoi = new NhanVien(maNV, ten, sdt, cccd, (String) cbChucVu.getSelectedItem(), true);
                DataStore.getDsNhanVien().add(nvMoi);
                DataStore.themNhanVienVaoDB(nvMoi);
                DataStore.notifyDataChanged();
                JOptionPane.showMessageDialog(page, "Đã thêm nhân viên thành công: " + ten, "Thành công", JOptionPane.INFORMATION_MESSAGE);
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
                    DataStore.notifyDataChanged();
                    break;
                }
            }
        });

        body.add(Theme.row(btnThemNV, btnToggleNV), BorderLayout.NORTH);
        body.add(Theme.tablePanel("Danh sách nhân sự (Nhấp đúp ô để sửa trực tiếp · Nhấp cột Trạng thái để bật/tắt)", table), BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    /* -------------------------------------------------------------
     * 8. KHÁCH HÀNG THÂN THIẾT (TÍCH ĐIỂM UC07)
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
                            DataStore.notifyDataChanged();
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
            String sdt = JOptionPane.showInputDialog(page, "Nhập số điện thoại (10 số):");
            if (ten != null && sdt != null && sdt.matches("^0[3|5|7|8|9][0-9]{8}$")) {
                String ma = "KH" + String.format("%04d", DataStore.getDsKhachHang().size() + 1);
                KhachHang khMoi = new KhachHang(ma, ten.trim(), sdt.trim(), 10);
                DataStore.getDsKhachHang().add(khMoi);
                DataStore.themKhachHangVaoDB(khMoi);
                DataStore.notifyDataChanged();
                JOptionPane.showMessageDialog(page, "Đã tạo tài khoản thành viên thành công (Tặng 10 điểm chào mừng)!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
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
     * BIỂU ĐỒ DOANH THU NHỊP BÁN HÀNG JAVA2D
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
                g.drawString("Nhịp bán hàng trong tuần", 24, 37);

                g.setColor(Theme.MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g.drawString("Doanh thu theo ngày · Triệu đồng", 24, 61);

                int bottom = getHeight() - 70;
                int available = Math.max(30, getHeight() - 180);
                int width = Math.max(12, (getWidth() - 105) / 7);

                for (int n = 0; n <= 4; n++) {
                    int y = bottom - available * n / 4;
                    g.setColor(Theme.LINE);
                    g.drawLine(44, y, getWidth() - 25, y);
                    g.setColor(Theme.MUTED);
                    g.drawString(String.valueOf(n), 22, y + 4);
                }

                int[] values = {54, 71, 61, 84, 95, 69, 81};
                String[] days = {"T7", "CN", "T2", "T3", "T4", "T5", "T6"};

                for (int i = 0; i < 7; i++) {
                    int h = available * values[i] / 100;
                    int x = 55 + i * width;
                    g.setColor(i == 6 ? Theme.GREEN : new Color(210, 225, 204));
                    g.fillRoundRect(x, bottom - h, Math.max(8, width - 18), h, 8, 8);
                    g.setColor(Theme.MUTED);
                    g.drawString(days[i], x + 5, bottom + 25);
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
