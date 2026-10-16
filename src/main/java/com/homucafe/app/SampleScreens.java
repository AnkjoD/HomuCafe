package com.homucafe.app;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.homucafe.shared.ui.Theme;
import com.homucafe.shared.ui.DrinkIllustration;

/** Các bố cục mẫu. Chỉ có dữ liệu giả, chưa chứa Controller/Service nghiệp vụ. */
final class SampleScreens {
    private SampleScreens() { }
    static JComponent create(String name) {
        return switch (name) {
            case "Tổng quan" -> dashboard();
            case "Bán hàng" -> sales();
            case "Sơ đồ bàn" -> tables();
            case "Quầy pha chế" -> kitchen();
            case "Nhập hàng" -> receiving();
            case "Công thức món" -> recipes();
            case "Nhân viên & ca" -> staff();
            case "Ca của tôi" -> ownShift();
            case "Khách hàng" -> customers();
            case "Thu chi" -> finance();
            case "Báo cáo" -> reports();
            default -> throw new IllegalArgumentException(name);
        };
    }

    private static JPanel dashboard() {
        JPanel page = Theme.page("Một ngày ở Homu", "Chào buổi sáng, chủ quán.", "Tổng quan hoạt động • Số liệu minh họa để duyệt giao diện");
        JPanel body = new JPanel(new BorderLayout(0, 22)); body.setOpaque(false);
        body.add(Theme.cards(Theme.card("TIỀN THU HÔM NAY", "2.840.000 đ", "48 đơn đã thanh toán"),
                Theme.card("BÀN ĐANG PHỤC VỤ", "5 / 12", "7 bàn sẵn sàng đón khách"),
                Theme.card("NGUYÊN LIỆU SẮP HẾT", "2", "Cần bổ sung trước ca chiều")), BorderLayout.NORTH);
        JPanel lower = new JPanel(new GridLayout(1, 2, 22, 0)); lower.setOpaque(false);
        lower.add(new SalesChart());
        lower.add(Theme.tablePanel("Món được yêu thích", Theme.table(new String[]{"MÓN", "SỐ LY"},
                new Object[][]{{"Cà phê sữa",168},{"Trà đào",124},{"Latte",96},{"Bạc xỉu",82}})));
        body.add(lower, BorderLayout.CENTER); page.add(body); return page;
    }

    private static JPanel sales() {
        JPanel page = Theme.page("Bán hàng tại quầy", "Một đơn ngon lành.", "Chọn món, kiểm tra đơn và thanh toán • Chưa nối xử lý nút bấm");
        JPanel body = new JPanel(new BorderLayout(20, 0)); body.setOpaque(false);
        JPanel menu = new JPanel(new BorderLayout(0, 18)); menu.setOpaque(false);
        JTextField search = new JTextField(); search.setToolTipText("Tìm món — sẽ nối Controller sau");
        search.getAccessibleContext().setAccessibleName("Tìm món");
        menu.add(Theme.row(new JLabel("Tìm món"), search, new JComboBox<>(new String[]{"Tất cả", "Cà phê", "Trà", "Khác"})), BorderLayout.NORTH);
        search.setColumns(16);
        JPanel products = new JPanel(new GridLayout(2, 3, 12, 12)); products.setOpaque(false);
        String[][] dishes = {{"Cà phê sữa","32.000 đ"},{"Latte","45.000 đ"},{"Trà đào","38.000 đ"},{"Matcha latte","45.000 đ"},{"Cacao nóng","40.000 đ"},{"Bạc xỉu","35.000 đ"}};
        for (int index=0;index<dishes.length;index++) {
            String[] dish=dishes[index];
            JPanel card=Theme.surface(new BorderLayout(0,14));card.setBorder(new EmptyBorder(13,13,15,13));
            card.add(new DrinkIllustration(index),BorderLayout.CENTER);
            JLabel name=Theme.label(dish[0],15,Theme.INK);name.setFont(new Font("Segoe UI",Font.BOLD,15));
            JPanel details=new JPanel(new BorderLayout());details.setOpaque(false);
            JButton add=Theme.button("+",true);add.setToolTipText("Thêm món — sẽ nối Controller sau");add.getAccessibleContext().setAccessibleName("Thêm "+dish[0]);
            details.add(Theme.stack(6,name,Theme.label("Size M · "+dish[1],13,Theme.ORANGE)));
            details.add(add,BorderLayout.EAST);
            card.add(details,BorderLayout.SOUTH);products.add(card);
        }
        menu.add(products); body.add(menu);
        JPanel bill = Theme.surface(new BorderLayout(0, 18));
        bill.setBorder(new EmptyBorder(18, 18, 18, 18)); bill.setPreferredSize(new Dimension(310, 0));
        bill.add(Theme.stack(12, Theme.label("ĐƠN MẪU · HC0049", 12, Theme.MUTED), Theme.title("Bàn 03"),
                new JComboBox<>(new String[]{"Tại bàn", "Mang đi"})), BorderLayout.NORTH);
        JTable lines=Theme.table(new String[]{"MÓN", "SL", "TIỀN"},new Object[][]{{"Cà phê sữa M",2,"64.000"},{"Trà đào M",1,"38.000"}});
        lines.getColumnModel().getColumn(0).setPreferredWidth(140);
        lines.getColumnModel().getColumn(1).setPreferredWidth(35);
        lines.getColumnModel().getColumn(2).setPreferredWidth(80);
        bill.add(Theme.tablePanel("Chi tiết món",lines));
        JButton pay = Theme.button("Thanh toán", true); Theme.markUnwired(pay);
        JButton send = Theme.button("Gửi pha chế", false); Theme.markUnwired(send);
        pay.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));send.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));
        JPanel summary=Theme.stack(14,Theme.label("Tổng cộng     102.000 đ",20,Theme.INK),send,pay);
        summary.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,0,0,0,Theme.LINE),new EmptyBorder(18,0,0,0)));
        bill.add(summary,BorderLayout.SOUTH);
        body.add(bill, BorderLayout.EAST); page.add(body); return page;
    }

    private static JPanel tables() {
        JPanel page = Theme.page("Không gian phục vụ", "Sơ đồ bàn", "Tầng trệt • 5 bàn đang phục vụ / 7 bàn trống");
        JPanel grid = new JPanel(new GridLayout(3, 4, 16, 16)); grid.setOpaque(false);
        for (int i=1;i<=12;i++) {
            boolean busy = java.util.Set.of(1,3,5,8,10).contains(i);
            JPanel card = Theme.card("BÀN " + String.format("%02d",i), busy ? "Đang phục vụ" : "Trống", busy ? "2 khách · 18 phút" : "4 chỗ · Sẵn sàng");
            if(busy) card.setBackground(Theme.PALE);
            grid.add(card);
        }
        page.add(grid); return page;
    }

    private static JPanel kitchen() {
        JPanel page = Theme.page("Pha chế & phục vụ", "Từng món, đúng nhịp.", "Phiếu pha chế mẫu • Chưa cập nhật trạng thái thực tế");
        JPanel columns = new JPanel(new GridLayout(1,3,18,0)); columns.setOpaque(false);
        String[] stages={"CHỜ PHA CHẾ", "ĐANG THỰC HIỆN", "SẴN SÀNG GIAO"};
        String[] actions={"Bắt đầu pha chế", "Đã làm xong", "Đã bàn giao"};
        for(int i=0;i<3;i++) {
            JPanel lane = Theme.surface(new BorderLayout(0,18)); lane.setBackground(Theme.PALE); lane.setBorder(new EmptyBorder(18,18,18,18));
            lane.add(Theme.label(stages[i],12,Theme.GREEN),BorderLayout.NORTH);
            JButton action=Theme.button(actions[i],true); Theme.markUnwired(action);
            JPanel ticket=Theme.stack(18,Theme.title("HC00"+(49+i)),Theme.label("Bàn 0"+(3+i)+" · 10:24",12,Theme.MUTED),
                    Theme.label("2 × Cà phê sữa M",14,Theme.INK),Theme.label("1 × Trà đào M",14,Theme.INK),
                    Theme.label("Ít đường · Đá thường",12,Theme.MUTED),action);
            lane.add(ticket,BorderLayout.CENTER); columns.add(lane);
        }
        page.add(columns); return page;
    }

    private static JPanel list(String title,String subtitle,String action,String[] cols,Object[][] rows) {
        JPanel page=Theme.page("Quản lý quán",title,subtitle);
        JPanel body=new JPanel(new BorderLayout(0,18)); body.setOpaque(false);
        if(action!=null){JButton button=Theme.button(action,true);Theme.markUnwired(button);body.add(Theme.row(button),BorderLayout.NORTH);}
        body.add(Theme.tablePanel("Dữ liệu minh họa",Theme.table(cols,rows)),BorderLayout.CENTER);page.add(body);return page;
    }
    private static JPanel receiving(){return list("Mỗi lần nhập, rõ ràng.","Theo dõi nguyên liệu nhận thực tế và nhà cung cấp.","+ Tạo phiếu nhập",
            new String[]{"SỐ PHIẾU","NHÀ CUNG CẤP","NGUYÊN LIỆU","SỐ LƯỢNG","THÀNH TIỀN"},
            new Object[][]{{"PN0018","Nông trại Đồi Xanh","Cà phê Arabica","5 kg","1.400.000 đ"},{"PN0017","Sữa Việt","Sữa tươi","12 lít","384.000 đ"}});}
    private static JPanel recipes(){return list("Công thức của Homu.","Định lượng cho một ly • Mẫu quy đổi về kg và lít",null,
            new String[]{"MÓN / SIZE","NGUYÊN LIỆU","ĐỊNH LƯỢNG","ĐƠN VỊ"},
            new Object[][]{{"Cà phê sữa / M","Cà phê Arabica","0,018","kg"},{"Cà phê sữa / M","Sữa đặc","0,025","kg"},{"Latte / M","Cà phê Arabica","0,018","kg"},{"Latte / M","Sữa tươi","0,18","lít"},{"Trà đào / M","Trà lài","0,005","kg"},{"Trà đào / M","Syrup đào","0,03","lít"}});}
    private static JPanel staff(){return list("Cùng nhau vận hành quán.","Ca sáng 07:00–14:00 • Đối soát và phân công ở bước nghiệp vụ sau.","Đối soát ca",
            new String[]{"NHÂN VIÊN MẪU","VAI TRÒ","CA LÀM","TRẠNG THÁI"},
            new Object[][]{{"Minh Anh","Thu ngân","07:00–14:00","Đang làm việc"},{"Gia Huy","Pha chế","07:00–14:00","Đang làm việc"},{"Hoàng Nam","Quản lý","07:00–14:00","Đang làm việc"}});}
    private static JPanel ownShift(){return list("Ca của tôi", "Quầy 01 · Ca sáng · Chỉ xem ca đang được giao cho tài khoản mẫu", "Chốt ca",
            new String[]{"KHOẢN MỤC", "SỐ TIỀN MẪU"}, new Object[][]{{"Tiền đầu ca","500.000 đ"},{"Tiền mặt thu theo đơn","1.230.000 đ"},{"Tiền mặt đã hoàn","50.000 đ"},{"Tiền mặt dự kiến","1.680.000 đ"}});}
    private static JPanel customers(){return list("Khách quen của Homu.","Chỉ chủ quán có mục này • Thông tin dưới đây hoàn toàn hư cấu.",null,
            new String[]{"KHÁCH HÀNG MẪU","ĐIỆN THOẠI ĐÃ CHE","LẦN GHÉ","GHI CHÚ"},
            new Object[][]{{"Khách mẫu A","09•• ••• 101",12,"Ít đường"},{"Khách mẫu B","09•• ••• 202",8,"Hay mua mang đi"}});}
    private static JPanel finance(){return list("Thu đúng. Chi rõ.","Sổ tiền mẫu • Chênh lệch thu chi không đồng nghĩa lợi nhuận.","+ Ghi khoản chi",
            new String[]{"NGÀY MẪU","NỘI DUNG","PHÂN LOẠI","SỐ TIỀN"},
            new Object[][]{{"16/10/2026","Nhập cà phê Arabica","Nhập nguyên liệu","1.400.000 đ"},{"15/10/2026","Tiền điện tháng 9","Vận hành","1.600.000 đ"},{"14/10/2026","Vật tư vệ sinh","Vận hành","240.000 đ"}});}
    private static JPanel reports(){return list("Một góc nhìn rõ hơn.","Số liệu mẫu • Tiền thu ròng = tiền thu thành công − tiền hoàn trong kỳ.",null,
            new String[]{"CHỈ TIÊU","GIÁ TRỊ MẪU","CÁCH HIỂU"},
            new Object[][]{{"Tiền thu thành công","24.800.000 đ","Theo thời điểm xác nhận thu"},{"Tiền hoàn","200.000 đ","Theo thời điểm xác nhận hoàn"},{"Tiền thu ròng","24.600.000 đ","Trong cùng kỳ báo cáo"},{"Số ly đã bán","568","Không gồm món hủy"}});}

    /** Biểu đồ tự vẽ bằng Java2D; không cần ảnh hoặc thư viện bên ngoài. */
    private static final class SalesChart extends JPanel {
        SalesChart(){setOpaque(false);}
        @Override protected void paintComponent(Graphics graphics){
            super.paintComponent(graphics);
            Graphics2D g=(Graphics2D)graphics.create();
            try {
                Theme.smooth(g);g.setColor(Theme.WHITE);g.fillRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);g.setColor(Theme.LINE);g.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
                g.setColor(Theme.INK);g.setFont(new Font("Segoe UI",Font.BOLD,16));g.drawString("Nhịp bán hàng trong tuần",24,37);
                g.setColor(Theme.MUTED);g.setFont(new Font("Segoe UI",Font.PLAIN,11));g.drawString("Tiền thu theo ngày · Triệu đồng · Dữ liệu mẫu",24,61);
                int bottom=getHeight()-70,available=Math.max(30,getHeight()-180),width=Math.max(12,(getWidth()-105)/7);
                for(int n=0;n<=4;n++){int y=bottom-available*n/4;g.setColor(Theme.LINE);g.drawLine(44,y,getWidth()-25,y);g.setColor(Theme.MUTED);g.drawString(String.valueOf(n),22,y+4);}
                int[] values={54,71,61,84,95,69,81};String[] days={"T7","CN","T2","T3","T4","T5","T6"};
                for(int i=0;i<7;i++){int h=available*values[i]/100;int x=55+i*width;g.setColor(i==6?Theme.GREEN:new Color(210,225,204));g.fillRoundRect(x,bottom-h,Math.max(8,width-18),h,8,8);g.setColor(Theme.MUTED);g.drawString(days[i],x+5,bottom+25);}
            } finally {g.dispose();}
        }
    }
}
