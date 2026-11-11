package com.homucafe.inventory;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.util.regex.Pattern;

public class InventoryController {
    private final InventoryView view;
    private final TableRowSorter<DefaultTableModel> sorter;
    public InventoryController(InventoryView view) {
        this.view = view;
        JTable table = view.getIngredientTable();
        DefaultTableModel model  = (DefaultTableModel) table.getModel();    
        this.sorter = new TableRowSorter<>(model);

        table.setRowSorter(sorter);
        view.getSearchField().addActionListener(e -> search());
    }

    public void search(){
        String keyword = view.getSearchField().getText().trim();  
        if(keyword.isEmpty()){
            sorter.setRowFilter(null);
        }
        else{
            String expression  = "?(iu)" + Pattern.quote(keyword);
            sorter.setRowFilter(RowFilter.regexFilter(expression, 0, 1));
        }
        int visibleRowCount = view.getIngredientTable().getRowCount();

        view.showResultCount(visibleRowCount);
    }

}
