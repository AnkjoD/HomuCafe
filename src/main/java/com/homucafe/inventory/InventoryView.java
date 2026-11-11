package com.homucafe.inventory;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.homucafe.shared.ui.Theme;

/** View chỉ dựng và hiển thị giao diện. Controller sẽ nối sự kiện sau. */
public class InventoryView extends JPanel {
    // Đặt thành thuộc tính để Controller lấy qua getter, không truy cập biến cục bộ.
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> stockFilter = new JComboBox<>(new String[]{"Tất cả tình trạng", "Sắp hết", "Đủ sử dụng"});
    private final JButton addButton = Theme.button("+ Thêm nguyên liệu", false);
    private final JButton receiveButton = Theme.button("Nhập kho", true);
    private final JButton countButton = Theme.button("Kiểm kê", false);
    private final JTable ingredientTable = Theme.table(
            new String[]{"MÃ", "NGUYÊN LIỆU", "NHÓM", "ĐƠN VỊ", "TỒN HIỆN TẠI", "TỐI THIỂU", "TÌNH TRẠNG"}, new Object[0][]);
    private final JLabel resultLabel = Theme.label("Dữ liệu mẫu • Chưa kết nối database", 11, Theme.MUTED);

    public InventoryView() {
        setLayout(new BorderLayout());
        JPanel page = Theme.page("Quản lý nguyên liệu", "Kho nguyên liệu", "Biết quán còn gì, cần nhập gì và đã sử dụng bao nhiêu.");
        JPanel body = new JPanel(new BorderLayout(0, 22));
        body.setOpaque(false);
        JPanel summaryCards=Theme.cards(
                Theme.card("LOẠI NGUYÊN LIỆU", "7", "Dữ liệu giả lập để duyệt giao diện"),
                Theme.card("DƯỚI MỨC TỐI THIỂU", "2", "Cà phê Arabica và sữa tươi"),
                Theme.card("KHO ĐANG XEM", "Kho chính", "Đơn vị chuẩn: kg / lít"));
        summaryCards.setPreferredSize(new Dimension(700,120));
        body.add(summaryCards,BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new BorderLayout(0, 16));
        listPanel.setOpaque(false);
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        searchField.setToolTipText("Nhập mã hoặc tên nguyên liệu. Chức năng tìm kiếm sẽ học ở Controller.");
        searchField.getAccessibleContext().setAccessibleName("Tìm nguyên liệu");
        searchField.putClientProperty("JTextField.placeholderText", "Nhập mã hoặc tên nguyên liệu…");
        searchField.putClientProperty("JTextField.leadingIcon", new Theme.LineIcon("search",16,Theme.MUTED));
        searchField.putClientProperty("FlatLaf.style", "arc: 12; margin: 9,12,9,12");
        toolbar.add(searchField, BorderLayout.CENTER);
        toolbar.add(Theme.row(stockFilter,addButton,receiveButton,countButton),BorderLayout.EAST);
        stockFilter.getAccessibleContext().setAccessibleName("Lọc tình trạng tồn kho");

        listPanel.add(toolbar, BorderLayout.NORTH);
        listPanel.add(Theme.tablePanel("Danh sách nguyên liệu", ingredientTable), BorderLayout.CENTER);
        listPanel.add(resultLabel, BorderLayout.SOUTH);
        body.add(listPanel, BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);

        for (JButton button : List.of(addButton, receiveButton, countButton)) Theme.markUnwired(button);
        // Tạm nạp dữ liệu mẫu ngay trong View để xem bố cục. Khi học Controller,
        // mình sẽ chuyển nguồn dữ liệu ra ngoài và gọi showRows() từ Controller.
        showRows(new Object[][]{
                {"NL001", "Cà phê Arabica", "Cà phê", "kg", new BigDecimal("2.4"), 3, "Sắp hết"},
                {"NL002", "Sữa tươi không đường", "Sữa", "lít", 3, 5, "Sắp hết"},
                {"NL003", "Sữa đặc", "Sữa", "kg", new BigDecimal("8.5"), 3, "Đủ sử dụng"},
                {"NL004", "Trà lài", "Trà", "kg", new BigDecimal("4.2"), 1, "Đủ sử dụng"},
                {"NL005", "Syrup đào", "Syrup", "lít", 6, 2, "Đủ sử dụng"},
                {"NL006", "Bột cacao", "Bột", "kg", new BigDecimal("2.8"), 1, "Đủ sử dụng"},
                {"NL007", "Đường cát", "Khác", "kg", 12, 5, "Đủ sử dụng"}
        });
        ingredientTable.getColumnModel().getColumn(1).setPreferredWidth(220);
    }

    /** Chỉ cập nhật nội dung bảng; gọi trên Swing EDT. Không truy vấn dữ liệu ở đây. */
    public void showRows(Object[][] rows) {
        DefaultTableModel model = (DefaultTableModel) ingredientTable.getModel();
        model.setRowCount(0); // Xóa những dòng đang hiển thị trước khi nạp lại.
        for (Object[] row : rows) model.addRow(row);
    }

    // Các điểm nối để Controller sử dụng trong buổi học tiếp theo.
    public JTextField getSearchField() { return searchField; }
    public JComboBox<String> getStockFilter() { return stockFilter; }
    public JButton getAddButton() { return addButton; }
    public JButton getReceiveButton() { return receiveButton; }
    public JButton getCountButton() { return countButton; }
    public JTable getIngredientTable() { return ingredientTable; }
    public void showResultCount(int count) { resultLabel.setText(count + " nguyên liệu • Dữ liệu mẫu, chưa kết nối database"); }
}
