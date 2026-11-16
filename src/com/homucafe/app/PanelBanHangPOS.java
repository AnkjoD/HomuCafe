package com.homucafe.app;

import com.homucafe.entity.*;
import com.homucafe.shared.ui.DrinkIllustration;
import com.homucafe.shared.ui.Theme;
import com.homucafe.util.DataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Màn hình 2: Bán hàng tại quầy (POS Counter).
 * Tùy biến món (Size/Đường/Đá/Topping), Giỏ hàng thao tác nhanh (+1, -1, Sửa, Tách bill, Xóa),
 * Tạm giữ đơn hàng chờ [F4], Khôi phục đơn giữ, Ưu đãi hạng VIP tự động,
 * Tra cứu Voucher, Tích lũy/Đổi điểm khách hàng, Thanh toán Tiền mặt (Quick Cash) / VietQR, và In bill 80mm/Tem ly.
 */
public class PanelBanHangPOS extends JPanel {
    private static JComboBox<String> posTableComboRef;

    public PanelBanHangPOS() {
        setLayout(new BorderLayout());
        setOpaque(false);
        initUI();
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

    private void initUI() {
        JPanel page = Theme.page("Quầy thu ngân", "Bán hàng & Gọi thẻ rung",
                "Phím tắt: [F1] Tạo đơn mới · [F2] Tìm món · [F3] Thanh toán · [F4] Giữ đơn");
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
        billPanel.setPreferredSize(new Dimension(410, 0));
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

        // Nhận diện khách hàng & Tích điểm & Hạng VIP
        JTextField txtCustomerPhone = new JTextField(11);
        txtCustomerPhone.putClientProperty("JTextField.placeholderText", "SĐT khách thành viên");
        JLabel lblCustomerInfo = Theme.label("Khách vãng lai (Chưa tích điểm)", 11, Theme.MUTED);
        final KhachHang[] activeCustomer = {null};
        JCheckBox chkDungDiem = new JCheckBox("Dùng điểm (1 điểm = 1.000 đ, tối đa 50%)");
        chkDungDiem.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkDungDiem.setOpaque(false);
        chkDungDiem.setEnabled(false);

        // Nút Giữ đơn & Danh sách đơn giữ
        JButton btnGiuDon = Theme.button("Giữ đơn [F4]", false);
        JButton btnDanhSachDonGiu = Theme.button("Đơn giữ (" + DataStore.getDsDonTamGiu().size() + ")", false);
        Runnable refreshHoldOrdersBadge = () -> btnDanhSachDonGiu.setText("Đơn giữ (" + DataStore.getDsDonTamGiu().size() + ")");
        DataStore.addDataChangeListener(refreshHoldOrdersBadge);

        // Giỏ hàng
        List<ChiTietHoaDon> currentOrderItems = new ArrayList<>();
        final double[] subtotal = {0};
        final double[] vipDiscount = {0};
        final double[] voucherDiscount = {0};
        final double[] loyaltyDiscount = {0};
        final Voucher[] activeVoucher = {null};

        String[] cartCols = {"MÓN & SIZE", "SL", "TIỀN"};
        DefaultTableModel cartModel = new DefaultTableModel(cartCols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tableCart = Theme.table(cartCols, new Object[0][]);
        tableCart.setModel(cartModel);
        tableCart.getColumnModel().getColumn(0).setPreferredWidth(185);
        tableCart.getColumnModel().getColumn(1).setPreferredWidth(35);
        tableCart.getColumnModel().getColumn(2).setPreferredWidth(90);

        JLabel lblSubtotal = Theme.label("Tổng tiền: 0 đ", 13, Theme.INK);
        JLabel lblDiscount = Theme.label("Giảm giá (VIP + Voucher + Điểm): 0 đ", 12, Theme.MUTED);
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

            // (c) Tự động tính ưu đãi hạng VIP
            if (activeCustomer[0] != null) {
                double vipPercent = activeCustomer[0].getPhanTramGiamVIP();
                vipDiscount[0] = (subtotal[0] * vipPercent) / 100.0;
            } else {
                vipDiscount[0] = 0;
            }

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

            // Tính đổi điểm loyalty (tối đa 50% đơn sau khi trừ VIP và voucher)
            if (activeCustomer[0] != null && chkDungDiem.isSelected()) {
                double maxDiemTien = activeCustomer[0].getDiemTichLuy() * 1000.0;
                double remaining = Math.max(0, subtotal[0] - vipDiscount[0] - voucherDiscount[0]);
                double maxGiamChoPhep = remaining * 0.50;
                loyaltyDiscount[0] = Math.max(0, Math.min(maxDiemTien, maxGiamChoPhep));
            } else {
                loyaltyDiscount[0] = 0;
            }

            double totalDiscount = vipDiscount[0] + voucherDiscount[0] + loyaltyDiscount[0];
            double finalPay = Math.max(0, subtotal[0] - totalDiscount);

            lblSubtotal.setText(String.format("Tổng tiền hàng: %,.0f đ", subtotal[0]));
            if (totalDiscount > 0) {
                lblDiscount.setText(String.format("Giảm giá: -%,.0f đ (VIP: %,.0f đ, Voucher: %,.0f đ, Điểm: %,.0f đ)",
                        totalDiscount, vipDiscount[0], voucherDiscount[0], loyaltyDiscount[0]));
                lblDiscount.setForeground(Theme.ORANGE);
            } else {
                lblDiscount.setText("Giảm giá (VIP + Voucher + Điểm): 0 đ");
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
                lblCustomerInfo.setText(kh.getHoTen() + " · " + kh.getHangThanhVien() + " · Điểm: " + kh.getDiemTichLuy() + " (" + String.format("%,.0f đ", (double)(kh.getDiemTichLuy() * 1000)) + ")");
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

        JPanel pnlHoldBar = new JPanel(new GridLayout(1, 2, 6, 0));
        pnlHoldBar.setOpaque(false);
        pnlHoldBar.add(btnGiuDon);
        pnlHoldBar.add(btnDanhSachDonGiu);

        JPanel pnlBillHeader = Theme.stack(6,
                Theme.row(Theme.label("HÓA ĐƠN POS", 11, Theme.MUTED), Theme.label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), 11, Theme.MUTED)),
                cbTable,
                cbPager,
                pnlCust,
                pnlHoldBar
        );
        billPanel.add(pnlBillHeader, BorderLayout.NORTH);

        // Thanh công cụ thao tác giỏ hàng (+1 SL, -1 SL, Sửa tùy chọn, Tách bill, Xóa món)
        JButton btnTangSL = Theme.button("+1", false);
        JButton btnGiamSL = Theme.button("-1", false);
        JButton btnSuaTuyChon = Theme.button("Sửa", false);
        JButton btnTachBill = Theme.button("Tách bill", false);
        JButton btnXoaMon = Theme.button("Xóa", false);

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

        Runnable resetOrderState = () -> {
            currentOrderItems.clear();
            cartModel.setRowCount(0);
            activeVoucher[0] = null;
            activeCustomer[0] = null;
            txtCustomerPhone.setText("");
            lblCustomerInfo.setText("Khách vãng lai (Chưa tích điểm)");
            lblCustomerInfo.setForeground(Theme.MUTED);
            chkDungDiem.setSelected(false);
            chkDungDiem.setEnabled(false);
            recalculate.run();
        };

        // (a) Xử lý Giữ đơn [F4]
        btnGiuDon.addActionListener(e -> {
            if (currentOrderItems.isEmpty()) {
                JOptionPane.showMessageDialog(page, "Giỏ hàng đang trống! Vui lòng chọn món trước khi tạm giữ.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String defaultName = (activeCustomer[0] != null) ? activeCustomer[0].getHoTen() : (String) cbTable.getSelectedItem();
            String note = JOptionPane.showInputDialog(page, "Nhập tên khách hoặc ghi chú cho đơn tạm giữ:", defaultName);
            if (note == null) return;
            note = note.trim().isEmpty() ? defaultName : note.trim();

            String maDonTam = "HOLD-" + (System.currentTimeMillis() % 100000);
            DonTamGiu donTam = new DonTamGiu(
                    maDonTam,
                    note,
                    LocalDateTime.now(),
                    (String) cbTable.getSelectedItem(),
                    activeCustomer[0],
                    new ArrayList<>(currentOrderItems)
            );
            DataStore.themDonTamGiu(donTam);
            resetOrderState.run();
            refreshHoldOrdersBadge.run();
            JOptionPane.showMessageDialog(page, "Đã tạm giữ đơn hàng '" + note + "'. Quầy đã sẵn sàng phục vụ khách tiếp theo!", "Tạm giữ thành công [F4]", JOptionPane.INFORMATION_MESSAGE);
        });

        // (a) Xử lý Mở danh sách đơn giữ tạm
        btnDanhSachDonGiu.addActionListener(e -> {
            List<DonTamGiu> dsGiu = DataStore.getDsDonTamGiu();
            if (dsGiu.isEmpty()) {
                JOptionPane.showMessageDialog(page, "Hiện không có đơn hàng nào đang tạm giữ.", "Đơn giữ tạm", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Window win = SwingUtilities.getWindowAncestor(page);
            JDialog dlg = new JDialog(win, "Danh sách đơn hàng tạm giữ (" + dsGiu.size() + ")", Dialog.ModalityType.APPLICATION_MODAL);
            JPanel root = new JPanel(new BorderLayout(0, 12));
            root.setBackground(Theme.WHITE);
            root.setBorder(new EmptyBorder(16, 18, 16, 18));

            String[] cols = {"MÃ ĐƠN", "GHI CHÚ / KHÁCH", "BÀN", "SỐ MÓN", "TỔNG TIỀN", "THỜI GIAN GIỮ"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
            for (DonTamGiu d : dsGiu) {
                model.addRow(new Object[]{
                        d.getMaDonTam(),
                        d.getTenGoiNho(),
                        d.getBanChon(),
                        d.getTongSoLuongMon() + " ly",
                        String.format("%,.0f đ", d.getTongTienTamTinh()),
                        d.getThoiGianGiu().format(dtf)
                });
            }

            JTable tableGiu = Theme.table(cols, new Object[0][]);
            tableGiu.setModel(model);
            if (tableGiu.getRowCount() > 0) tableGiu.setRowSelectionInterval(0, 0);

            root.add(Theme.tablePanel("Chọn 1 đơn tạm giữ để khôi phục hoặc xóa", tableGiu), BorderLayout.CENTER);

            JButton btnRestore = Theme.button("✓ Khôi phục vào giỏ hàng POS", true);
            JButton btnDelete = Theme.button("Xóa đơn giữ", false);
            JButton btnClose = Theme.button("Đóng", false);

            btnRestore.addActionListener(ev -> {
                int row = tableGiu.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dlg, "Vui lòng chọn 1 đơn hàng tạm giữ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!currentOrderItems.isEmpty()) {
                    int confirm = JOptionPane.showConfirmDialog(dlg,
                            "Giỏ hàng hiện tại đang có món. Bạn có chắc muốn ghi đè bằng đơn tạm giữ này?",
                            "Xác nhận khôi phục", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return;
                }

                DonTamGiu d = DataStore.layVaXoaDonTamGiu(row);
                if (d != null) {
                    currentOrderItems.clear();
                    currentOrderItems.addAll(d.getDsMon());
                    if (d.getBanChon() != null) {
                        cbTable.setSelectedItem(d.getBanChon());
                    }
                    if (d.getKhachHang() != null) {
                        activeCustomer[0] = d.getKhachHang();
                        txtCustomerPhone.setText(activeCustomer[0].getSoDienThoai());
                        lblCustomerInfo.setText(activeCustomer[0].getHoTen() + " · " + activeCustomer[0].getHangThanhVien() + " · Điểm: " + activeCustomer[0].getDiemTichLuy());
                        lblCustomerInfo.setForeground(Theme.GREEN);
                        chkDungDiem.setEnabled(activeCustomer[0].getDiemTichLuy() > 0);
                    } else {
                        activeCustomer[0] = null;
                        txtCustomerPhone.setText("");
                        lblCustomerInfo.setText("Khách vãng lai (Chưa tích điểm)");
                        lblCustomerInfo.setForeground(Theme.MUTED);
                        chkDungDiem.setEnabled(false);
                    }
                    refreshCartTable.run();
                    recalculate.run();
                    refreshHoldOrdersBadge.run();
                    dlg.dispose();
                }
            });

            btnDelete.addActionListener(ev -> {
                int row = tableGiu.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dlg, "Vui lòng chọn 1 đơn hàng tạm giữ để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(dlg, "Bạn có chắc chắn muốn xóa đơn tạm giữ này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    DataStore.xoaDonTamGiu(row);
                    model.removeRow(row);
                    refreshHoldOrdersBadge.run();
                    if (model.getRowCount() == 0) dlg.dispose();
                }
            });

            btnClose.addActionListener(ev -> dlg.dispose());

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            footer.setOpaque(false);
            footer.add(btnDelete);
            footer.add(btnClose);
            footer.add(btnRestore);
            root.add(footer, BorderLayout.SOUTH);

            dlg.setContentPane(root);
            dlg.setSize(720, 380);
            dlg.setLocationRelativeTo(win);
            dlg.setVisible(true);
        });

        // (b) Xử lý Tách Bill (Split Bill)
        btnTachBill.addActionListener(e -> {
            int totalQty = currentOrderItems.stream().mapToInt(ChiTietHoaDon::getSoLuong).sum();
            if (totalQty < 2) {
                JOptionPane.showMessageDialog(page,
                        "Giỏ hàng cần có từ 2 ly trở lên để thực hiện tách bill!",
                        "Không thể tách bill", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Window win = SwingUtilities.getWindowAncestor(page);
            JDialog dlg = new JDialog(win, "Tách Bill Thanh Toán · HomuCafe", Dialog.ModalityType.APPLICATION_MODAL);
            JPanel root = new JPanel(new BorderLayout(0, 14));
            root.setBackground(Theme.WHITE);
            root.setBorder(new EmptyBorder(16, 20, 16, 20));

            JLabel lblTitle = Theme.label("Chọn số lượng món muốn tách ra thanh toán trước (Bill 1):", 13, Theme.INK);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
            root.add(lblTitle, BorderLayout.NORTH);

            JPanel pnlItems = new JPanel(new GridLayout(0, 1, 0, 8));
            pnlItems.setOpaque(false);

            List<JSpinner> spinners = new ArrayList<>();
            for (ChiTietHoaDon ct : currentOrderItems) {
                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setOpaque(false);
                JLabel lblItem = Theme.label(ct.getMon().getTenMon() + " (" + ct.getKichCo().name() + ") - Tổng: " + ct.getSoLuong() + " ly", 12, Theme.INK);
                JSpinner sp = new JSpinner(new SpinnerNumberModel(0, 0, ct.getSoLuong(), 1));
                sp.setPreferredSize(new Dimension(70, 30));
                spinners.add(sp);
                row.add(lblItem, BorderLayout.CENTER);
                row.add(sp, BorderLayout.EAST);
                pnlItems.add(row);
            }

            JScrollPane scroll = new JScrollPane(pnlItems);
            scroll.setBorder(BorderFactory.createTitledBorder("Chi tiết món trong giỏ"));
            root.add(scroll, BorderLayout.CENTER);

            JButton btnConfirmSplit = Theme.button("✓ Xác nhận tách & Thanh toán Bill 1", true);
            JButton btnCancelSplit = Theme.button("Hủy bỏ", false);
            btnCancelSplit.addActionListener(ev -> dlg.dispose());

            btnConfirmSplit.addActionListener(ev -> {
                int totalSplit = 0;
                for (JSpinner sp : spinners) {
                    totalSplit += (Integer) sp.getValue();
                }
                if (totalSplit <= 0) {
                    JOptionPane.showMessageDialog(dlg, "Vui lòng chọn ít nhất 1 ly để tách sang Bill 1!", "Chưa chọn món tách", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (totalSplit >= totalQty) {
                    JOptionPane.showMessageDialog(dlg, "Bạn đang chọn toàn bộ món! Hãy chọn ít hơn tổng số lượng để tách thành 2 bill.", "Lỗi tách bill", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                List<ChiTietHoaDon> itemsBill1 = new ArrayList<>();
                List<ChiTietHoaDon> itemsBill2 = new ArrayList<>();

                for (int i = 0; i < currentOrderItems.size(); i++) {
                    ChiTietHoaDon orig = currentOrderItems.get(i);
                    int splitQty = (Integer) spinners.get(i).getValue();
                    int remainQty = orig.getSoLuong() - splitQty;

                    if (splitQty > 0) {
                        ChiTietHoaDon ct1 = new ChiTietHoaDon(itemsBill1.size() + 1, orig.getMon(), orig.getKichCo(), splitQty, orig.getDonGiaThucTe(), orig.getMucDuong(), orig.getMucDa(), orig.getGhiChuTopping());
                        itemsBill1.add(ct1);
                    }
                    if (remainQty > 0) {
                        ChiTietHoaDon ct2 = new ChiTietHoaDon(itemsBill2.size() + 1, orig.getMon(), orig.getKichCo(), remainQty, orig.getDonGiaThucTe(), orig.getMucDuong(), orig.getMucDa(), orig.getGhiChuTopping());
                        itemsBill2.add(ct2);
                    }
                }

                // Tự động lưu Bill 2 vào DonTamGiu
                String noteBill2 = "Bill 2 - " + ((activeCustomer[0] != null) ? activeCustomer[0].getHoTen() : (String) cbTable.getSelectedItem());
                DonTamGiu donBill2 = new DonTamGiu(
                        "SPLIT-" + (System.currentTimeMillis() % 100000),
                        noteBill2,
                        LocalDateTime.now(),
                        (String) cbTable.getSelectedItem(),
                        activeCustomer[0],
                        itemsBill2
                );
                DataStore.themDonTamGiu(donBill2);

                // Giỏ hiện tại nhận các món của Bill 1
                currentOrderItems.clear();
                currentOrderItems.addAll(itemsBill1);
                refreshCartTable.run();
                recalculate.run();
                refreshHoldOrdersBadge.run();
                dlg.dispose();

                JOptionPane.showMessageDialog(page,
                        "Đã tách bill thành công!\n• Giỏ hàng hiện tại là Bill 1 (" + totalSplit + " ly) sẵn sàng bấm Thanh toán ngay.\n• Các món còn lại (" + (totalQty - totalSplit) + " ly) đã được chuyển sang Đơn giữ tạm '" + noteBill2 + "' để thu tiếp sau.",
                        "Tách Bill thành công", JOptionPane.INFORMATION_MESSAGE);
            });

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            footer.setOpaque(false);
            footer.add(btnCancelSplit);
            footer.add(btnConfirmSplit);
            root.add(footer, BorderLayout.SOUTH);

            dlg.setContentPane(root);
            dlg.setSize(520, 380);
            dlg.setLocationRelativeTo(win);
            dlg.setVisible(true);
        });

        JPanel pnlCartToolbar = new JPanel(new GridLayout(1, 5, 4, 0));
        pnlCartToolbar.setOpaque(false);
        pnlCartToolbar.add(btnTangSL);
        pnlCartToolbar.add(btnGiamSL);
        pnlCartToolbar.add(btnSuaTuyChon);
        pnlCartToolbar.add(btnTachBill);
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

            double totalDiscount = vipDiscount[0] + voucherDiscount[0] + loyaltyDiscount[0];
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
                    Window win = SwingUtilities.getWindowAncestor(page);
                    double[] cashResult = showQuickCashDialog(win, finalPrice);
                    if (cashResult == null) {
                        return; // Người dùng hủy thu tiền
                    }
                    tienKhachDua = cashResult[0];
                    tienThoi = cashResult[1];
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
                refreshHoldOrdersBadge.run();
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
        page.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F4"), "holdOrder");
        page.getActionMap().put("holdOrder", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                btnGiuDon.doClick();
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
        add(page, BorderLayout.CENTER);
    }

    public static double[] showQuickCashDialog(Window owner, double finalPrice) {
        JDialog dialog = new JDialog(owner, "Thanh toán Tiền mặt · HomuCafe", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Theme.WHITE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel lblMustPay = Theme.label("Tổng tiền phải thu: " + String.format("%,.0f đ", finalPrice), 18, Theme.GREEN);
        lblMustPay.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextField txtCash = new JTextField(String.format("%.0f", finalPrice));
        txtCash.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtCash.putClientProperty("FlatLaf.style", "arc: 10; margin: 8,10,8,10");

        JLabel lblChange = Theme.label("Tiền thối lại cho khách: 0 đ", 15, Theme.INK);
        lblChange.setFont(new Font("Segoe UI", Font.BOLD, 15));

        Runnable updateChange = () -> {
            double cash = DataStore.parseCurrency(txtCash.getText());
            if (cash < finalPrice) {
                lblChange.setText(String.format("Khách đưa còn thiếu: %,.0f đ", finalPrice - cash));
                lblChange.setForeground(new Color(220, 38, 38));
            } else {
                lblChange.setText(String.format("Tiền thối lại cho khách: %,.0f đ", cash - finalPrice));
                lblChange.setForeground(Theme.GREEN);
            }
        };

        txtCash.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateChange.run(); }
            @Override public void removeUpdate(DocumentEvent e) { updateChange.run(); }
            @Override public void changedUpdate(DocumentEvent e) { updateChange.run(); }
        });

        JPanel pnlQuick = new JPanel(new GridLayout(1, 5, 6, 0));
        pnlQuick.setOpaque(false);
        long[] presets = {(long) Math.ceil(finalPrice), 50000L, 100000L, 200000L, 500000L};
        String[] presetLabels = {"Đúng tiền", "50.000", "100.000", "200.000", "500.000"};
        for (int i = 0; i < presets.length; i++) {
            final long val = presets[i];
            JButton b = Theme.button(presetLabels[i], false);
            b.addActionListener(e -> {
                txtCash.setText(String.valueOf(val));
                txtCash.requestFocusInWindow();
            });
            pnlQuick.add(b);
        }

        JPanel center = Theme.stack(10,
                lblMustPay,
                Theme.label("Số tiền khách đưa (VND):", 12, Theme.INK),
                txtCash,
                pnlQuick,
                lblChange
        );
        root.add(center, BorderLayout.CENTER);

        final boolean[] confirmed = {false};
        JButton btnCancel = Theme.button("Hủy bỏ", false);
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnConfirm = Theme.button("✓ Xác nhận thu tiền", true);
        ActionListener confirmAction = e -> {
            double cash = DataStore.parseCurrency(txtCash.getText());
            if (cash < finalPrice) {
                JOptionPane.showMessageDialog(dialog,
                        String.format("Số tiền khách đưa (%,.0f đ) nhỏ hơn tổng hóa đơn (%,.0f đ)!", cash, finalPrice),
                        "Chưa đủ tiền thanh toán", JOptionPane.WARNING_MESSAGE);
                return;
            }
            confirmed[0] = true;
            dialog.dispose();
        };
        btnConfirm.addActionListener(confirmAction);
        txtCash.addActionListener(confirmAction);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(btnCancel);
        footer.add(btnConfirm);
        root.add(footer, BorderLayout.SOUTH);

        updateChange.run();
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(470, 290));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);

        if (!confirmed[0]) return null;
        double cash = DataStore.parseCurrency(txtCash.getText());
        return new double[]{cash, Math.max(0, cash - finalPrice)};
    }
}
