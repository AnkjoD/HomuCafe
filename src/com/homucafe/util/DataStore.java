package com.homucafe.util;

import com.homucafe.entity.*;

import java.awt.*;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Kho dữ liệu trung tâm In-Memory DataStore (Thương hiệu HomuCafe).
 * Quản lý đồng bộ dữ liệu thời gian thực giữa quầy POS, Bar Monitor, Quản lý Hóa đơn,
 * Thực đơn, Nhân sự, Sơ đồ bàn, Voucher và Báo cáo đối soát ca.
 */
public class DataStore {
    public static final int TONG_SO_THE_RUNG = 20; // Khay sạc gồm 20 thẻ rung (01..20)
    public static final int TONG_SO_BAN = 12;      // Sơ đồ gồm 12 bàn tầng trệt

    private static final List<LoaiMon> dsLoaiMon = new ArrayList<>();
    private static final List<Mon> dsMon = new ArrayList<>();
    private static final List<NhanVien> dsNhanVien = new ArrayList<>();
    private static final List<TaiKhoan> dsTaiKhoan = new ArrayList<>();
    private static final List<KhachHang> dsKhachHang = new ArrayList<>();
    private static final List<HoaDon> dsHoaDon = new ArrayList<>();
    private static final Map<Integer, String> trangThaiBan = new LinkedHashMap<>();
    private static final List<Runnable> listeners = new ArrayList<>();

    private static TaiKhoan taiKhoanDangNhap;
    private static int sttCounter = 14;

    static {
        initSampleData();
        loadDataFromDatabaseIfAvailable();
    }

    private static void loadDataFromDatabaseIfAvailable() {
        try {
            com.homucafe.connectdb.ConnectDB.getInstance().connect();
            java.sql.Connection con = com.homucafe.connectdb.ConnectDB.getConnection();
            if (con != null) {
                com.homucafe.dao.Mon_DAO monDAO = new com.homucafe.dao.Mon_DAO();
                List<Mon> dbMon = monDAO.getAllMon();
                if (!dbMon.isEmpty()) {
                    dsMon.clear();
                    dsMon.addAll(dbMon);
                }

                com.homucafe.dao.NhanVien_DAO nvDAO = new com.homucafe.dao.NhanVien_DAO();
                List<NhanVien> dbNV = nvDAO.getAllNhanVien();
                if (!dbNV.isEmpty()) {
                    dsNhanVien.clear();
                    dsNhanVien.addAll(dbNV);
                }

                com.homucafe.dao.KhachHang_DAO khDAO = new com.homucafe.dao.KhachHang_DAO();
                List<KhachHang> dbKH = khDAO.getAllKhachHang();
                if (!dbKH.isEmpty()) {
                    dsKhachHang.clear();
                    dsKhachHang.addAll(dbKH);
                }

                com.homucafe.dao.Ban_DAO banDAO = new com.homucafe.dao.Ban_DAO();
                Map<Integer, String> dbBan = banDAO.getAllTrangThaiBan();
                if (!dbBan.isEmpty()) {
                    trangThaiBan.putAll(dbBan);
                }

                com.homucafe.dao.HoaDon_DAO hdDAO = new com.homucafe.dao.HoaDon_DAO();
                List<HoaDon> dbHD = hdDAO.getAllHoaDon();
                if (!dbHD.isEmpty()) {
                    dsHoaDon.clear();
                    dsHoaDon.addAll(dbHD);
                }
                System.out.println("☕ Đã đồng bộ dữ liệu SQL Server vào DataStore thành công!");
            }
        } catch (Throwable t) {
            System.err.println("Hoạt động ở chế độ In-Memory DataStore dự phòng: " + t.getMessage());
        }
    }

    private static void initSampleData() {
        // 1. Khởi tạo 12 bàn
        for (int i = 1; i <= TONG_SO_BAN; i++) {
            trangThaiBan.put(i, "TRONG");
        }
        trangThaiBan.put(1, "DANG_PHUC_VU");
        trangThaiBan.put(3, "DANG_PHUC_VU");
        trangThaiBan.put(5, "DANG_PHUC_VU");
        trangThaiBan.put(8, "DANG_PHUC_VU");
        trangThaiBan.put(10, "DANG_PHUC_VU");

        // 2. Danh mục Loại món
        LoaiMon lmCaPhe = new LoaiMon("LM01", "Cà phê");
        LoaiMon lmTraSua = new LoaiMon("LM02", "Trà sữa");
        LoaiMon lmTraTraiCay = new LoaiMon("LM03", "Trà trái cây");
        LoaiMon lmDaXay = new LoaiMon("LM04", "Đá xay");
        LoaiMon lmBanhNgot = new LoaiMon("LM05", "Bánh ngọt");

        dsLoaiMon.addAll(Arrays.asList(lmCaPhe, lmTraSua, lmTraTraiCay, lmDaXay, lmBanhNgot));

        // 3. Danh mục Món đồ uống HomuCafe
        dsMon.add(new Mon("CF001", "Cà Phê Đen Đá", 29000, "0", true, lmCaPhe));
        dsMon.add(new Mon("CF002", "Cà Phê Sữa Đá", 35000, "0", true, lmCaPhe));
        dsMon.add(new Mon("CF003", "Bạc Xỉu 3 Tầng", 39000, "5", true, lmCaPhe));
        dsMon.add(new Mon("CF004", "Latte", 45000, "1", true, lmCaPhe));
        dsMon.add(new Mon("TEA01", "Trà Đào Cam Sả", 45000, "2", true, lmTraTraiCay));
        dsMon.add(new Mon("TEA02", "Trà Sen Vàng Kem", 49000, "2", true, lmTraTraiCay));
        dsMon.add(new Mon("TEA03", "Trà Vải Hoa Hồng", 46000, "2", true, lmTraTraiCay));
        dsMon.add(new Mon("MILK01", "Trà Sữa Trân Châu", 42000, "3", true, lmTraSua));
        dsMon.add(new Mon("MILK02", "Trà Sữa Ô Long Nướng", 45000, "3", true, lmTraSua));
        dsMon.add(new Mon("ICE01", "Homu Matcha Latte", 45000, "3", true, lmDaXay));
        dsMon.add(new Mon("ICE02", "Cacao Nóng / Đá", 40000, "4", true, lmDaXay));

        // 4. Nhân viên & Tài khoản
        NhanVien nv1 = new NhanVien("NV01", "Minh Anh", "0901234567", "079204001234", "Thu ngân", true);
        NhanVien nv2 = new NhanVien("NV02", "Gia Huy", "0912345678", "079204005678", "Pha chế", true);
        NhanVien nv3 = new NhanVien("NV03", "Hoàng Nam", "0987654321", "079204009012", "Quản lý", true);
        dsNhanVien.addAll(Arrays.asList(nv1, nv2, nv3));

        TaiKhoan tkAdmin = new TaiKhoan("admin", "123456", "ADMIN", nv3);
        TaiKhoan tkThuNgan = new TaiKhoan("thungan01", "123456", "STAFF", nv1);
        TaiKhoan tkBar = new TaiKhoan("bar01", "123456", "STAFF", nv2);
        dsTaiKhoan.addAll(Arrays.asList(tkAdmin, tkThuNgan, tkBar));

        taiKhoanDangNhap = tkAdmin;

        // 5. Khách hàng thành viên
        KhachHang kh1 = new KhachHang("KH0001", "Nguyễn Thu Hà", "0908123456", 35);
        KhachHang kh2 = new KhachHang("KH0002", "Trần Minh Tuấn", "0918654321", 50);
        KhachHang kh3 = new KhachHang("KH0003", "Lê Bảo Ngọc", "0977888999", 15);
        dsKhachHang.addAll(Arrays.asList(kh1, kh2, kh3));

        // 6. Đơn hàng mẫu ban đầu trên Bar Monitor & Quản lý hóa đơn
        Mon cfSua = timMonTheoMa("CF002");
        Mon traDao = timMonTheoMa("TEA01");
        Mon latte = timMonTheoMa("CF004");
        Mon bacXiu = timMonTheoMa("CF003");

        // Đơn 1: Đang chờ pha chế (Thẻ rung 05 - Bàn 03)
        HoaDon hd1 = new HoaDon("HD261021_0001", LocalDateTime.now().minusMinutes(3), nv1, kh1,
                "Bàn 03", 5, "TAI_QUAN", "TIEN_MAT", "DANG_PHA_CHE", 102000, 0, 150000, 48000, null);
        hd1.themChiTiet(new ChiTietHoaDon(1, cfSua, KichCo.M, 2, 41000, 100, "BINH_THUONG", "Trân châu"));
        hd1.themChiTiet(new ChiTietHoaDon(2, traDao, KichCo.M, 1, 51000, 70, "IT_DA", "Thạch đào"));
        dsHoaDon.add(hd1);

        // Đơn 2: Đang thực hiện pha chế (Thẻ rung 02 - Mang đi)
        HoaDon hd2 = new HoaDon("HD261021_0002", LocalDateTime.now().minusMinutes(7), nv1, kh2,
                "Mang đi", 2, "MANG_DI", "VIETQR", "DANG_PHA_CHE", 90000, 10000, 90000, 0, null);
        hd2.themChiTiet(new ChiTietHoaDon(3, latte, KichCo.L, 1, 57000, 50, "KHONG_DA", ""));
        hd2.themChiTiet(new ChiTietHoaDon(4, bacXiu, KichCo.S, 1, 39000, 100, "BINH_THUONG", ""));
        dsHoaDon.add(hd2);

        // Đơn 3: Sẵn sàng giao / Đã báo rung (Thẻ rung 08 - Bàn 01)
        HoaDon hd3 = new HoaDon("HD261021_0003", LocalDateTime.now().minusMinutes(12), nv1, null,
                "Bàn 01", 8, "TAI_QUAN", "TIEN_MAT", "CHO_GIAO", 45000, 0, 50000, 5000, null);
        hd3.themChiTiet(new ChiTietHoaDon(5, traDao, KichCo.S, 1, 45000, 100, "BINH_THUONG", ""));
        dsHoaDon.add(hd3);

        // Đơn 4: Đã hoàn tất & Đã bàn giao
        HoaDon hd4 = new HoaDon("HD261021_0004", LocalDateTime.now().minusMinutes(25), nv1, kh3,
                "Bàn 08", 11, "TAI_QUAN", "VIETQR", "HOAN_TAT", 135000, 0, 135000, 0, null);
        hd4.themChiTiet(new ChiTietHoaDon(6, cfSua, KichCo.L, 2, 47000, 70, "IT_DA", ""));
        hd4.themChiTiet(new ChiTietHoaDon(7, latte, KichCo.M, 1, 51000, 100, "BINH_THUONG", "Kem cheese"));
        dsHoaDon.add(hd4);
    }

    public static void addDataChangeListener(Runnable r) {
        listeners.add(r);
    }

    public static void notifyDataChanged() {
        for (Runnable r : listeners) {
            try {
                r.run();
            } catch (Exception ignored) { }
        }
    }

    public static List<LoaiMon> getDsLoaiMon() { return dsLoaiMon; }
    public static List<Mon> getDsMon() { return dsMon; }
    public static List<NhanVien> getDsNhanVien() { return dsNhanVien; }
    public static List<TaiKhoan> getDsTaiKhoan() { return dsTaiKhoan; }
    public static List<KhachHang> getDsKhachHang() { return dsKhachHang; }
    public static List<HoaDon> getDsHoaDon() { return dsHoaDon; }
    public static Map<Integer, String> getTrangThaiBan() { return trangThaiBan; }

    public static TaiKhoan getTaiKhoanDangNhap() { return taiKhoanDangNhap; }
    public static void setTaiKhoanDangNhap(TaiKhoan tk) { taiKhoanDangNhap = tk; }

    public static Mon timMonTheoMa(String maMon) {
        for (Mon m : dsMon) {
            if (m.getMaMon().equalsIgnoreCase(maMon)) return m;
        }
        return null;
    }

    public static KhachHang timKhachHangTheoSDT(String sdt) {
        if (sdt == null) return null;
        for (KhachHang kh : dsKhachHang) {
            if (kh.getSoDienThoai().equals(sdt.trim())) return kh;
        }
        return null;
    }

    public static boolean isTheRungDangBan(int soThe) {
        for (HoaDon hd : dsHoaDon) {
            if (hd.getSoTheRung() == soThe
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
        for (int i = 1; i <= TONG_SO_THE_RUNG; i++) {
            if (!isTheRungDangBan(i)) return i;
        }
        return 1;
    }

    public static int nextSoThuTuNgay() {
        return ++sttCounter;
    }

    public static String nextMaHoaDon() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        return String.format("HD%s_%04d", datePart, ++sttCounter);
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

    // ==========================================
    // CÁC HÀM ĐỒNG BỘ DỮ LIỆU XUỐNG SQL SERVER
    // ==========================================
    public static void saveHoaDon(HoaDon hd) {
        new Thread(() -> {
            try {
                new com.homucafe.dao.HoaDon_DAO().themHoaDon(hd);
            } catch (Throwable ignored) { }
        }).start();
    }

    public static void saveTrangThaiHoaDon(String maHD, String trangThai) {
        new Thread(() -> {
            try {
                new com.homucafe.dao.HoaDon_DAO().capNhatTrangThaiDon(maHD, trangThai);
            } catch (Throwable ignored) { }
        }).start();
    }

    public static void saveMon(Mon m) {
        new Thread(() -> {
            try {
                new com.homucafe.dao.Mon_DAO().capNhatMon(m);
            } catch (Throwable ignored) { }
        }).start();
    }

    public static void themMonVaoDB(Mon m) {
        new Thread(() -> {
            try {
                new com.homucafe.dao.Mon_DAO().themMon(m);
            } catch (Throwable ignored) { }
        }).start();
    }

    public static void saveNhanVien(NhanVien nv) {
        new Thread(() -> {
            try {
                new com.homucafe.dao.NhanVien_DAO().capNhatNhanVien(nv);
            } catch (Throwable ignored) { }
        }).start();
    }

    public static void themNhanVienVaoDB(NhanVien nv) {
        new Thread(() -> {
            try {
                new com.homucafe.dao.NhanVien_DAO().themNhanVien(nv);
            } catch (Throwable ignored) { }
        }).start();
    }

    public static void saveKhachHang(KhachHang kh) {
        new Thread(() -> {
            try {
                new com.homucafe.dao.KhachHang_DAO().capNhatKhachHang(kh);
            } catch (Throwable ignored) { }
        }).start();
    }

    public static void themKhachHangVaoDB(KhachHang kh) {
        new Thread(() -> {
            try {
                new com.homucafe.dao.KhachHang_DAO().themKhachHang(kh);
            } catch (Throwable ignored) { }
        }).start();
    }

    public static void saveTrangThaiBan(int soBan, String trangThai) {
        new Thread(() -> {
            try {
                new com.homucafe.dao.Ban_DAO().capNhatTrangThaiBan(soBan, trangThai);
            } catch (Throwable ignored) { }
        }).start();
    }
}

