package util;

import entity.*;

import java.awt.*;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Kho dữ liệu trung tâm In-Memory DataStore (Thương hiệu HomuCafe).
 * Hỗ trợ đồng bộ dữ liệu thời gian thực giữa quầy POS, Bar Monitor, Quản lý Hóa đơn,
 * Thực đơn, Nhân sự và Báo cáo đối soát ca.
 */
public class DataStore {
    public static final int TONG_SO_THE_RUNG = 20; // Khay sạc gồm 20 thẻ rung (01..20)

    private static final List<LoaiMon> dsLoaiMon = new ArrayList<>();
    private static final List<Mon> dsMon = new ArrayList<>();
    private static final List<NhanVien> dsNhanVien = new ArrayList<>();
    private static final List<TaiKhoan> dsTaiKhoan = new ArrayList<>();
    private static final List<KhachHang> dsKhachHang = new ArrayList<>();
    private static final List<HoaDon> dsHoaDon = new ArrayList<>();
    private static final List<Runnable> listeners = new ArrayList<>();

    private static TaiKhoan taiKhoanDangNhap;
    private static int sttCounter = 14;

    static {
        initSampleData();
    }

    private static void initSampleData() {
        // 1. Danh mục Loại món
        LoaiMon lmCaPhe = new LoaiMon("LM01", "Cà phê");
        LoaiMon lmTraSua = new LoaiMon("LM02", "Trà sữa");
        LoaiMon lmTraTraiCay = new LoaiMon("LM03", "Trà trái cây");
        LoaiMon lmDaXay = new LoaiMon("LM04", "Đá xay");
        LoaiMon lmBanhNgot = new LoaiMon("LM05", "Bánh ngọt");

        dsLoaiMon.addAll(Arrays.asList(lmCaPhe, lmTraSua, lmTraTraiCay, lmDaXay, lmBanhNgot));

        // 2. Danh mục Món đồ uống HomuCafe
        dsMon.add(new Mon("CF001", "Cà Phê Đen Đá", 29000, "images/cf_den_da.jpg", true, lmCaPhe));
        dsMon.add(new Mon("CF002", "Cà Phê Sữa Đá", 35000, "images/cf_sua_da.jpg", true, lmCaPhe));
        dsMon.add(new Mon("CF003", "Bạc Xỉu 3 Tầng", 39000, "images/bac_xiu.jpg", true, lmCaPhe));
        dsMon.add(new Mon("TEA01", "Trà Đào Cam Sả", 45000, "images/tra_dao_cam_sa.jpg", true, lmTraTraiCay));
        dsMon.add(new Mon("TEA02", "Trà Sen Vàng Kem", 49000, "images/tra_sen_vang.jpg", true, lmTraTraiCay));
        dsMon.add(new Mon("MILK01", "Trà Sữa Trân Châu", 42000, "images/ts_tran_chau.jpg", true, lmTraSua));
        dsMon.add(new Mon("ICE01", "Homu Matcha Đá Xay", 55000, "images/matcha_da_xay.jpg", true, lmDaXay));
        dsMon.add(new Mon("CF004", "Caramel Macchiato", 52000, "images/caramel_macchiato.jpg", true, lmCaPhe));
        dsMon.add(new Mon("TEA03", "Trà Vải Hoa Hồng", 46000, "images/tra_vai_hoa_hong.jpg", true, lmTraTraiCay));
        dsMon.add(new Mon("MILK02", "Trà Sữa Ô Long Nướng", 45000, "images/ts_olong.jpg", true, lmTraSua));
        dsMon.add(new Mon("ICE02", "Cookie & Cream Đá Xay", 58000, "images/cookie_da_xay.jpg", true, lmDaXay));
        dsMon.add(new Mon("CAKE01", "Bánh Tiramisu Homu", 39000, "images/tiramisu.jpg", true, lmBanhNgot));
        dsMon.add(new Mon("CAKE02", "Croissant Trứng Muối", 35000, "images/croissant.jpg", true, lmBanhNgot));

        // 3. Nhân viên & Tài khoản
        NhanVien nv1 = new NhanVien("NV01", "Trần Văn An", "0901234567", "079204001234", "Thu ngân", true);
        NhanVien nv2 = new NhanVien("NV02", "Lê Thị Hoa", "0912345678", "079204005678", "Pha chế", true);
        NhanVien nv3 = new NhanVien("NV03", "Phạm Minh Vũ", "0987654321", "079204009012", "Thu ngân", true);
        NhanVien nv4 = new NhanVien("NV04", "Nguyễn Bảo Trọng", "0938112233", "079204003456", "Quản lý", true);
        NhanVien nv5 = new NhanVien("NV05", "Võ Hiếu Tài", "0977889900", "079204007890", "Quản lý", true);
        dsNhanVien.addAll(Arrays.asList(nv1, nv2, nv3, nv4, nv5));

        TaiKhoan tkAdmin = new TaiKhoan("admin", "123456", "ADMIN", nv4);
        TaiKhoan tkThuNgan = new TaiKhoan("thungan01", "123456", "STAFF", nv1);
        TaiKhoan tkBar = new TaiKhoan("bar01", "123456", "STAFF", nv2);
        TaiKhoan tkQuanLy = new TaiKhoan("quanly01", "123456", "ADMIN", nv5);
        dsTaiKhoan.addAll(Arrays.asList(tkAdmin, tkThuNgan, tkBar, tkQuanLy));

        taiKhoanDangNhap = tkAdmin;

        // 4. Khách hàng thành viên
        KhachHang kh1 = new KhachHang("KH0001", "Nguyễn Hoàng Nam", "0987654321", 35);
        KhachHang kh2 = new KhachHang("KH0002", "Trần Ngọc Ánh", "0909123456", 50);
        KhachHang kh3 = new KhachHang("KH0003", "Lê Minh Khôi", "0918889999", 15);
        dsKhachHang.addAll(Arrays.asList(kh1, kh2, kh3));

        // 5. Đơn hàng mẫu ban đầu trên Bar Monitor & Quản lý hóa đơn
        Mon traDao = timMonTheoMa("TEA01");
        Mon cfSua = timMonTheoMa("CF002");
        Mon matcha = timMonTheoMa("ICE01");
        Mon bacXiu = timMonTheoMa("CF003");
        Mon traSen = timMonTheoMa("TEA02");
        Mon cfDen = timMonTheoMa("CF001");
        Mon tsTranChau = timMonTheoMa("MILK01");
        Mon caramel = timMonTheoMa("CF004");
        Mon tiramisu = timMonTheoMa("CAKE01");
        Mon traVai = timMonTheoMa("TEA03");

        // Đơn 1: THẺ RUNG #08 - ĐANG PHA CHẾ (Chờ 3 phút)
        HoaDon hd1 = new HoaDon("HD261028_0010", 10, LocalDateTime.now().minusMinutes(3),
                8, "TAI_QUAN", "TIEN_MAT", 0, "DANG_PHA_CHE", nv1, kh1);
        hd1.themChiTiet(new ChiTietHoaDon(1, traDao, KichCo.L, 2, 57000, 70, "IT_DA", "+Đào miếng"));
        hd1.themChiTiet(new ChiTietHoaDon(2, cfSua, KichCo.M, 1, 41000, 100, "BINH_THUONG", ""));
        hd1.themChiTiet(new ChiTietHoaDon(3, matcha, KichCo.L, 1, 67000, 100, "BINH_THUONG", "+Kem cheese"));
        dsHoaDon.add(hd1);

        // Đơn 2: SỐ THỨ TỰ #14 - SẮP TRỄ (Chờ 7 phút)
        HoaDon hd2 = new HoaDon("HD261028_0011", 14, LocalDateTime.now().minusMinutes(7),
                0, "MANG_DI", "CHUYEN_KHOAN", 0, "DANG_PHA_CHE", nv1, null);
        hd2.themChiTiet(new ChiTietHoaDon(4, bacXiu, KichCo.M, 1, 45000, 30, "BINH_THUONG", "Nhiều đá"));
        hd2.themChiTiet(new ChiTietHoaDon(5, traSen, KichCo.S, 1, 49000, 100, "BINH_THUONG", "+Sen mềm"));
        dsHoaDon.add(hd2);

        // Đơn 3: THẺ RUNG #03 - ƯU TIÊN GẤP (Chờ 12 phút)
        HoaDon hd3 = new HoaDon("HD261028_0012", 12, LocalDateTime.now().minusMinutes(12),
                3, "TAI_QUAN", "TIEN_MAT", 0, "DANG_PHA_CHE", nv1, null);
        hd3.themChiTiet(new ChiTietHoaDon(6, cfDen, KichCo.S, 3, 29000, 0, "BINH_THUONG", "Đá riêng"));
        hd3.themChiTiet(new ChiTietHoaDon(7, tsTranChau, KichCo.M, 2, 48000, 50, "BINH_THUONG", "+Trân châu trắng"));
        dsHoaDon.add(hd3);

        // Đơn 4: THẺ RUNG #07 - ĐANG RUNG CHUÔNG (CHO_GIAO)
        HoaDon hd4 = new HoaDon("HD261028_0013", 11, LocalDateTime.now().minusMinutes(8),
                7, "TAI_QUAN", "CHUYEN_KHOAN", 0, "CHO_GIAO", nv1, kh2);
        hd4.themChiTiet(new ChiTietHoaDon(8, caramel, KichCo.L, 1, 64000, 100, "NONG", ""));
        hd4.themChiTiet(new ChiTietHoaDon(9, tiramisu, KichCo.S, 1, 39000, 100, "BINH_THUONG", ""));
        dsHoaDon.add(hd4);

        // Đơn 5: SỐ THỨ TỰ #13 - CHỜ KHÁCH LẤY (CHO_GIAO)
        HoaDon hd5 = new HoaDon("HD261028_0009", 13, LocalDateTime.now().minusMinutes(9),
                0, "MANG_DI", "CHUYEN_KHOAN", 0, "CHO_GIAO", nv1, null);
        hd5.themChiTiet(new ChiTietHoaDon(10, traVai, KichCo.M, 2, 52000, 70, "BINH_THUONG", ""));
        dsHoaDon.add(hd5);
    }

    public static void addDataChangeListener(Runnable r) {
        listeners.add(r);
    }

    public static void notifyDataChanged() {
        for (Runnable r : listeners) {
            r.run();
        }
    }

    public static List<LoaiMon> getDsLoaiMon() {
        return dsLoaiMon;
    }

    public static List<Mon> getDsMon() {
        return dsMon;
    }

    public static List<NhanVien> getDsNhanVien() {
        return dsNhanVien;
    }

    public static List<TaiKhoan> getDsTaiKhoan() {
        return dsTaiKhoan;
    }

    public static List<KhachHang> getDsKhachHang() {
        return dsKhachHang;
    }

    public static List<HoaDon> getDsHoaDon() {
        return dsHoaDon;
    }

    public static TaiKhoan getTaiKhoanDangNhap() {
        return taiKhoanDangNhap;
    }

    public static void setTaiKhoanDangNhap(TaiKhoan tk) {
        taiKhoanDangNhap = tk;
    }

    public static Mon timMonTheoMa(String maMon) {
        for (Mon m : dsMon) {
            if (m.getMaMon().equalsIgnoreCase(maMon)) {
                return m;
            }
        }
        return null;
    }

    public static KhachHang timKhachHangTheoSDT(String sdt) {
        if (sdt == null) return null;
        for (KhachHang kh : dsKhachHang) {
            if (kh.getSoDienThoai().equals(sdt.trim())) {
                return kh;
            }
        }
        return null;
    }

    public static boolean isTheRungDangBan(int soThe) {
        for (HoaDon hd : dsHoaDon) {
            if ("TAI_QUAN".equals(hd.getLoaiDon())
                    && hd.getSoTheRung() == soThe
                    && ("DANG_PHA_CHE".equals(hd.getTrangThaiDon()) || "CHO_GIAO".equals(hd.getTrangThaiDon()))) {
                return true;
            }
        }
        return false;
    }

    public static int demSoTheRungRanh() {
        int ban = 0;
        for (int i = 1; i <= TONG_SO_THE_RUNG; i++) {
            if (isTheRungDangBan(i)) ban++;
        }
        return Math.max(0, TONG_SO_THE_RUNG - ban);
    }

    public static int goiYTheRungTiepTheo() {
        for (int i = 1; i <= 99; i++) {
            if (!isTheRungDangBan(i)) {
                return i;
            }
        }
        return 1;
    }

    public static int nextSoThuTuNgay() {
        return ++sttCounter;
    }

    public static String nextMaHoaDon() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        return String.format("HD%s_%04d", datePart, sttCounter + 1);
    }

    public static boolean khopTimKiemMon(Mon mon, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return true;
        String q = boDauTiengViet(keyword.trim().toLowerCase());
        String ten = boDauTiengViet(mon.getTenMon().toLowerCase());
        String ma = mon.getMaMon().toLowerCase();

        if (ten.contains(q) || ma.contains(q)) return true;

        String[] words = ten.split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) initials.append(w.charAt(0));
        }
        String initStr = initials.toString();
        String altInit = initStr.replace("cp", "cf");
        return initStr.contains(q) || altInit.contains(q);
    }

    public static String boDauTiengViet(String str) {
        if (str == null) return "";
        String temp = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    /**
     * Bảng màu thẻ món ấm cúng chuẩn quán cà phê HomuCafe
     */
    public static Color getCardColorForMon(Mon mon) {
        String ma = mon.getMaMon().toUpperCase();
        if (ma.equals("CF001")) return new Color(90, 56, 37);    // Nâu cafe đen đậm
        if (ma.equals("CF002")) return new Color(122, 75, 42);   // Nâu cafe sữa
        if (ma.equals("CF003")) return new Color(140, 83, 43);   // Nâu caramel bạc xỉu
        if (ma.equals("CF004")) return new Color(111, 67, 42);   // Caramel Macchiato
        if (ma.equals("TEA01")) return new Color(180, 83, 9);    // Cam đào tự nhiên
        if (ma.equals("TEA02")) return new Color(161, 98, 7);    // Vàng sen ấm
        if (ma.equals("TEA03")) return new Color(159, 45, 72);   // Đỏ hồng vải
        if (ma.startsWith("MILK")) return new Color(109, 64, 42); // Nâu trà sữa
        if (ma.startsWith("ICE")) return new Color(83, 107, 79); // Xanh Matcha HomuCafe
        return new Color(130, 99, 67);                           // Nâu gỗ ấm
    }
}
