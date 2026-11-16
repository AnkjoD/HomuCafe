package com.homucafe.util;

import com.homucafe.entity.*;

import java.awt.*;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Kho dữ liệu trung tâm In-Memory DataStore (Thương hiệu HomuCafe).
 * Quản lý đồng bộ dữ liệu thời gian thực giữa quầy POS, Bar Monitor, Quản lý Hóa đơn,
 * Thực đơn, Nhân sự, Sơ đồ bàn, Voucher, Đặt bàn trước và Báo cáo đối soát ca.
 */
public class DataStore {
    public static final int TONG_SO_THE_RUNG = 20; // Khay sạc gồm 20 thẻ rung (01..20)
    public static final int TONG_SO_BAN = 12;      // Sơ đồ gồm 12 bàn

    private static final List<LoaiMon> dsLoaiMon = new ArrayList<>();
    private static final List<Mon> dsMon = new ArrayList<>();
    private static final List<NhanVien> dsNhanVien = new ArrayList<>();
    private static final List<TaiKhoan> dsTaiKhoan = new ArrayList<>();
    private static final List<KhachHang> dsKhachHang = new ArrayList<>();
    private static final List<HoaDon> dsHoaDon = new ArrayList<>();
    private static final List<Voucher> dsVoucher = new ArrayList<>();
    private static final List<Ban> dsBan = new ArrayList<>();
    private static final List<PhieuDatBan> dsPhieuDatBan = new ArrayList<>();
    private static final List<DonTamGiu> dsDonTamGiu = new ArrayList<>();
    private static final List<PhieuChiCa> dsPhieuChiCa = new ArrayList<>();
    private static final Map<Integer, String> trangThaiBan = new LinkedHashMap<>();
    private static final List<Runnable> listeners = new ArrayList<>();

    private static TaiKhoan taiKhoanDangNhap;
    private static int sttCounter = 0;

    static {
        initSampleData();
        loadDataFromDatabaseIfAvailable();
        syncCountersFromLoadedData();
    }

    private static void loadDataFromDatabaseIfAvailable() {
        try {
            java.sql.Connection con = com.homucafe.connectdb.ConnectDB.getConnection();
            if (con == null) {
                com.homucafe.connectdb.ConnectDB.getInstance().connect();
                con = com.homucafe.connectdb.ConnectDB.getConnection();
            }
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

                com.homucafe.dao.TaiKhoan_DAO tkDAO = new com.homucafe.dao.TaiKhoan_DAO();
                List<TaiKhoan> dbTK = tkDAO.getAllTaiKhoan();
                if (!dbTK.isEmpty()) {
                    dsTaiKhoan.clear();
                    dsTaiKhoan.addAll(dbTK);
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
                    for (Map.Entry<Integer, String> entry : dbBan.entrySet()) {
                        for (Ban b : dsBan) {
                            if (b.getSoBan() == entry.getKey()) {
                                b.setTrangThai(entry.getValue());
                            }
                        }
                    }
                }

                com.homucafe.dao.Voucher_DAO vDAO = new com.homucafe.dao.Voucher_DAO();
                List<Voucher> dbV = vDAO.getAllVoucher();
                if (!dbV.isEmpty()) {
                    dsVoucher.clear();
                    dsVoucher.addAll(dbV);
                }

                com.homucafe.dao.PhieuDatBan_DAO pdbDAO = new com.homucafe.dao.PhieuDatBan_DAO();
                List<PhieuDatBan> dbPDB = pdbDAO.getAllPhieuDatBan();
                dsPhieuDatBan.clear();
                dsPhieuDatBan.addAll(dbPDB);

                com.homucafe.dao.HoaDon_DAO hdDAO = new com.homucafe.dao.HoaDon_DAO();
                List<HoaDon> dbHD = hdDAO.getAllHoaDon();
                dsHoaDon.clear();
                dsHoaDon.addAll(dbHD);

                System.out.println("☕ Đã đồng bộ dữ liệu SQL Server vào DataStore thành công!");
            }
        } catch (Throwable t) {
            System.err.println("Hoạt động ở chế độ In-Memory DataStore dự phòng: " + t.getMessage());
        }
    }

    private static void syncCountersFromLoadedData() {
        String prefix = "HD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "_";
        int maxToday = 0;
        for (HoaDon hd : dsHoaDon) {
            if (hd.getMaHoaDon() != null && hd.getMaHoaDon().startsWith(prefix)) {
                try {
                    int n = Integer.parseInt(hd.getMaHoaDon().substring(prefix.length()));
                    if (n > maxToday) maxToday = n;
                } catch (NumberFormatException ignored) { }
            }
        }
        sttCounter = maxToday;
    }

    /**
     * Đồng bộ lại toàn bộ dữ liệu từ CSDL SQL Server (Hỗ trợ nhiều máy chạy chung mạng LAN).
     */
    public static void reloadFromDatabase() {
        loadDataFromDatabaseIfAvailable();
        syncCountersFromLoadedData();
        notifyDataChanged();
    }

    private static void initSampleData() {
        // 1. Khởi tạo 12 bàn
        dsBan.clear();
        trangThaiBan.clear();
        for (int i = 1; i <= TONG_SO_BAN; i++) {
            int soCho = (i == 4 || i == 12) ? 6 : (i % 2 == 0 ? 2 : 4);
            String ma = String.format("B%02d", i);
            String ten = String.format("Bàn %02d", i);
            String tt = "TRONG";
            if (i == 1 || i == 3 || i == 5 || i == 8 || i == 10) tt = "DANG_PHUC_VU";
            else if (i == 4 || i == 12) tt = "DA_DAT";

            dsBan.add(new Ban(ma, ten, soCho, tt));
            trangThaiBan.put(i, tt);
        }

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
        dsMon.add(new Mon("CAKE01", "Bánh Tiramisu Homu", 39000, "4", true, lmBanhNgot));
        dsMon.add(new Mon("CAKE02", "Croissant Trứng Muối", 35000, "4", true, lmBanhNgot));

        // 4. Nhân viên & Tài khoản
        NhanVien nv1 = new NhanVien("NV01", "Trần Văn An", "0901234567", "079204001234", "Thu ngân", true);
        NhanVien nv2 = new NhanVien("NV02", "Lê Thị Hoa", "0912345678", "079204005678", "Pha chế", true);
        NhanVien nv3 = new NhanVien("NV03", "Phạm Minh Vũ", "0987654321", "079204009012", "Thu ngân", true);
        NhanVien nv4 = new NhanVien("NV04", "Nguyễn Bảo Trọng", "0938112233", "079204003456", "Quản lý", true);
        dsNhanVien.addAll(Arrays.asList(nv1, nv2, nv3, nv4));

        TaiKhoan tkAdmin = new TaiKhoan("admin", "123456", "ADMIN", nv4);
        TaiKhoan tkThuNgan = new TaiKhoan("thungan01", "123456", "STAFF", nv1);
        TaiKhoan tkBar = new TaiKhoan("bar01", "123456", "STAFF", nv2);
        dsTaiKhoan.addAll(Arrays.asList(tkAdmin, tkThuNgan, tkBar));

        // 5. Khách hàng thành viên
        KhachHang kh1 = new KhachHang("KH0001", "Nguyễn Hoàng Nam", "0987654321", 35);
        KhachHang kh2 = new KhachHang("KH0002", "Trần Ngọc Ánh", "0909123456", 50);
        KhachHang kh3 = new KhachHang("KH0003", "Lê Minh Khôi", "0918889999", 15);
        dsKhachHang.addAll(Arrays.asList(kh1, kh2, kh3));

        // 6. Danh mục Voucher
        LocalDate today = LocalDate.now();
        dsVoucher.add(new Voucher("HOMU10", "PHAN_TRAM", 10, 30000, 50000, 5, 100, today.minusDays(5), today.plusMonths(2), true));
        dsVoucher.add(new Voucher("HOMU20", "TIEN_MAT", 20000, 20000, 100000, 2, 50, today.minusDays(5), today.plusMonths(2), true));
        dsVoucher.add(new Voucher("HOMUWELCOME", "PHAN_TRAM", 15, 50000, 0, 12, 200, today.minusDays(5), today.plusMonths(2), true));
        dsVoucher.add(new Voucher("CHIEUVUI", "TIEN_MAT", 15000, 15000, 80000, 30, 200, today.minusDays(5), today.plusMonths(2), true));

        // 7. Phiếu đặt bàn mẫu
        Ban ban04 = timBanTheoSo(4);
        Ban ban12 = timBanTheoSo(12);

        PhieuDatBan pdb1 = new PhieuDatBan("PDB001", LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusHours(1), 6, 100000, "Họp mặt sinh nhật, cần chỗ yên tĩnh", "DA_DAT", kh1, nv1);
        if (ban04 != null) pdb1.themChiTiet(new ChiTietPhieuDatBan("PDB001", ban04, "Bàn 6 chỗ"));
        dsPhieuDatBan.add(pdb1);

        PhieuDatBan pdb2 = new PhieuDatBan("PDB002", LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(2), 6, 100000, "Đặt bàn tiệc gia đình", "DA_DAT", kh2, nv1);
        if (ban12 != null) pdb2.themChiTiet(new ChiTietPhieuDatBan("PDB002", ban12, "Bàn 6 chỗ view cửa sổ"));
        dsPhieuDatBan.add(pdb2);

        // 8. Đơn hàng mẫu ban đầu trên Bar Monitor & Quản lý hóa đơn
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
                "Mang đi", 2, "MANG_DI", "VIETQR", "DANG_PHA_CHE", 90000, 10000, 90000, 0, "HOMU10");
        hd2.setMaVoucher("HOMU10");
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

    public static java.time.LocalDate getActiveBusinessDate() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate latest = null;
        for (HoaDon hd : dsHoaDon) {
            if ("DA_HUY".equalsIgnoreCase(hd.getTrangThaiDon()) || hd.getNgayLap() == null) continue;
            java.time.LocalDate d = hd.getNgayLap().toLocalDate();
            if (d.isEqual(today)) {
                return today;
            }
            if (latest == null || d.isAfter(latest)) {
                latest = d;
            }
        }
        return latest != null ? latest : today;
    }

    public static boolean isTienMat(HoaDon hd) {
        String pt = hd != null ? hd.getPhuongThucThanhToan() : null;
        if (pt == null || pt.trim().isEmpty()) return true;
        String upper = pt.toUpperCase();
        String lower = pt.toLowerCase();
        return upper.contains("TIEN_MAT") || lower.contains("tiền mặt");
    }

    public static double parseCurrency(String text) {
        if (text == null) return 0;
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Double.parseDouble(digits);
        } catch (Exception e) {
            return 0;
        }
    }

    public static List<LoaiMon> getDsLoaiMon() { return dsLoaiMon; }
    public static List<Mon> getDsMon() { return dsMon; }
    public static List<NhanVien> getDsNhanVien() { return dsNhanVien; }
    public static List<TaiKhoan> getDsTaiKhoan() { return dsTaiKhoan; }
    public static List<KhachHang> getDsKhachHang() { return dsKhachHang; }
    public static List<HoaDon> getDsHoaDon() { return dsHoaDon; }
    public static List<Voucher> getDsVoucher() { return dsVoucher; }
    public static List<Ban> getDsBan() { return dsBan; }
    public static List<PhieuDatBan> getDsPhieuDatBan() { return dsPhieuDatBan; }
    public static Map<Integer, String> getTrangThaiBan() { return trangThaiBan; }

    public static TaiKhoan getTaiKhoanDangNhap() { return taiKhoanDangNhap; }
    public static void setTaiKhoanDangNhap(TaiKhoan tk) {
        taiKhoanDangNhap = tk;
        notifyDataChanged();
    }

    public static TaiKhoan dangNhap(String tenDangNhap, String matKhau) {
        if (tenDangNhap == null || matKhau == null) return null;
        for (TaiKhoan tk : dsTaiKhoan) {
            if (tk.getTenDangNhap().equalsIgnoreCase(tenDangNhap.trim()) && tk.getMatKhau().equals(matKhau.trim())) {
                setTaiKhoanDangNhap(tk);
                return tk;
            }
        }
        return null;
    }

    public static boolean doiMatKhau(String tenDangNhap, String matKhauCu, String matKhauMoi) {
        for (TaiKhoan tk : dsTaiKhoan) {
            if (tk.getTenDangNhap().equalsIgnoreCase(tenDangNhap) && tk.getMatKhau().equals(matKhauCu)) {
                tk.setMatKhau(matKhauMoi);
                new Thread(() -> {
                    try {
                        new com.homucafe.dao.TaiKhoan_DAO().doiMatKhau(tenDangNhap, matKhauMoi);
                    } catch (Throwable t) {
                        logSqlError("doiMatKhau", t);
                    }
                }).start();
                return true;
            }
        }
        return false;
    }

    public static TaiKhoan timTaiKhoanTheoMaNV(String maNV) {
        if (maNV == null) return null;
        for (TaiKhoan tk : dsTaiKhoan) {
            if (tk.getNhanVien() != null && maNV.equalsIgnoreCase(tk.getNhanVien().getMaNV())) {
                return tk;
            }
        }
        return null;
    }

    public static TaiKhoan timTaiKhoanTheoTen(String tenDangNhap) {
        if (tenDangNhap == null) return null;
        for (TaiKhoan tk : dsTaiKhoan) {
            if (tenDangNhap.trim().equalsIgnoreCase(tk.getTenDangNhap())) {
                return tk;
            }
        }
        return null;
    }

    public static void saveTaiKhoan(TaiKhoan tk) {
        if (tk == null || tk.getNhanVien() == null) return;
        TaiKhoan existing = timTaiKhoanTheoMaNV(tk.getNhanVien().getMaNV());
        if (existing != null) {
            existing.setTenDangNhap(tk.getTenDangNhap());
            existing.setMatKhau(tk.getMatKhau());
            existing.setPhanQuyen(tk.getPhanQuyen());
        } else {
            dsTaiKhoan.add(tk);
        }
        notifyDataChanged();
        new Thread(() -> {
            try {
                new com.homucafe.dao.TaiKhoan_DAO().luuHoacCapNhatTaiKhoan(tk);
            } catch (Throwable t) {
                logSqlError("saveTaiKhoan", t);
            }
        }).start();
    }

    public static Ban timBanTheoSo(int soBan) {
        for (Ban b : dsBan) {
            if (b.getSoBan() == soBan) return b;
        }
        return null;
    }

    public static PhieuDatBan timPhieuDatBanTheoSoBan(int soBan) {
        String maBan = String.format("B%02d", soBan);
        for (PhieuDatBan p : dsPhieuDatBan) {
            if ("DA_DAT".equalsIgnoreCase(p.getTrangThai())) {
                for (ChiTietPhieuDatBan ct : p.getDsChiTiet()) {
                    if (ct.getBan() != null && maBan.equalsIgnoreCase(ct.getBan().getMaBan())) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    public static HoaDon timHoaDonDangNgoi(int soBan) {
        String banStr = String.format("Bàn %02d", soBan);
        for (HoaDon hd : dsHoaDon) {
            if (banStr.equalsIgnoreCase(hd.getBanPhucVu()) || (hd.getSoBan() == soBan)) {
                if ("DANG_PHA_CHE".equals(hd.getTrangThaiDon()) || "CHO_GIAO".equals(hd.getTrangThaiDon())) {
                    return hd;
                }
            }
        }
        return null;
    }

    public static Mon timMonTheoMa(String maMon) {
        for (Mon m : dsMon) {
            if (m.getMaMon().equalsIgnoreCase(maMon)) return m;
        }
        return null;
    }

    public static Voucher timVoucherTheoMa(String maVoucher) {
        if (maVoucher == null) return null;
        for (Voucher v : dsVoucher) {
            if (v.getMaVoucher().equalsIgnoreCase(maVoucher.trim())) return v;
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

    public static synchronized int nextSoThuTuNgay() {
        return ++sttCounter;
    }

    public static synchronized String nextMaHoaDon() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String prefix = "HD" + datePart + "_";
        int maxToday = 0;
        Set<String> existing = new HashSet<>();
        for (HoaDon hd : dsHoaDon) {
            if (hd.getMaHoaDon() != null) {
                String ma = hd.getMaHoaDon().trim();
                existing.add(ma.toUpperCase());
                if (ma.startsWith(prefix)) {
                    try {
                        int n = Integer.parseInt(ma.substring(prefix.length()));
                        if (n > maxToday) maxToday = n;
                    } catch (NumberFormatException ignored) { }
                }
            }
        }
        int candidate = Math.max(sttCounter, maxToday) + 1;
        while (existing.contains((prefix + String.format("%04d", candidate)).toUpperCase())) {
            candidate++;
        }
        sttCounter = candidate;
        return prefix + String.format("%04d", candidate);
    }

    public static synchronized String nextMaKhachHang() {
        List<String> ids = new ArrayList<>();
        for (KhachHang kh : dsKhachHang) ids.add(kh.getMaKH());
        return nextIdWithPrefix(ids, "KH", 4);
    }

    public static synchronized String nextMaNhanVien() {
        List<String> ids = new ArrayList<>();
        for (NhanVien nv : dsNhanVien) ids.add(nv.getMaNV());
        return nextIdWithPrefix(ids, "NV", 2);
    }

    public static synchronized String nextMaPhieuDatBan() {
        List<String> ids = new ArrayList<>();
        for (PhieuDatBan pdb : dsPhieuDatBan) ids.add(pdb.getMaPhieu());
        return nextIdWithPrefix(ids, "PDB", 3);
    }

    public static synchronized String nextMaMon(String tenLoai) {
        String key = boDauTiengViet(tenLoai != null ? tenLoai : "").toLowerCase();
        String prefix = "SP";
        int digits = 3;
        if (key.contains("ca phe")) {
            prefix = "CF";
            digits = 3;
        } else if (key.contains("tra sua")) {
            prefix = "MILK";
            digits = 2;
        } else if (key.contains("tra")) {
            prefix = "TEA";
            digits = 2;
        } else if (key.contains("da xay")) {
            prefix = "ICE";
            digits = 2;
        } else if (key.contains("banh")) {
            prefix = "CAKE";
            digits = 2;
        }
        List<String> ids = new ArrayList<>();
        for (Mon m : dsMon) ids.add(m.getMaMon());
        return nextIdWithPrefix(ids, prefix, digits);
    }

    private static String nextIdWithPrefix(Collection<String> existingIds, String prefix, int digits) {
        int max = 0;
        Set<String> used = new HashSet<>();
        String upperPrefix = prefix.toUpperCase();
        for (String raw : existingIds) {
            if (raw == null) continue;
            String id = raw.trim().toUpperCase();
            used.add(id);
            if (id.startsWith(upperPrefix)) {
                String suffix = id.substring(upperPrefix.length()).replaceAll("[^0-9]", "");
                if (!suffix.isEmpty()) {
                    try {
                        int num = Integer.parseInt(suffix);
                        if (num > max) max = num;
                    } catch (NumberFormatException ignored) { }
                }
            }
        }
        int next = max + 1;
        String fmt = "%s%0" + digits + "d";
        String candidate = String.format(fmt, prefix, next);
        while (used.contains(candidate.toUpperCase())) {
            next++;
            candidate = String.format(fmt, prefix, next);
        }
        return candidate;
    }

    private static void logSqlError(String action, Throwable t) {
        System.err.println("[SQL Sync Warning] " + action + ": " + t.getMessage());
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
    // CÁC HÀM NGHIỆP VỤ & ĐỒNG BỘ CSDL
    // ==========================================
    public static void saveHoaDon(HoaDon hd) {
        if (!dsHoaDon.contains(hd)) {
            dsHoaDon.add(0, hd);
        }
        notifyDataChanged();
        new Thread(() -> {
            try {
                new com.homucafe.dao.HoaDon_DAO().themHoaDon(hd);
            } catch (Throwable t) {
                logSqlError("saveHoaDon", t);
            }
        }).start();
    }

    public static void saveTrangThaiHoaDon(String maHD, String trangThai) {
        for (HoaDon hd : dsHoaDon) {
            if (hd.getMaHoaDon().equalsIgnoreCase(maHD)) {
                hd.setTrangThaiDon(trangThai);
                break;
            }
        }
        notifyDataChanged();
        new Thread(() -> {
            try {
                new com.homucafe.dao.HoaDon_DAO().capNhatTrangThaiDon(maHD, trangThai);
            } catch (Throwable t) {
                logSqlError("saveTrangThaiHoaDon", t);
            }
        }).start();
    }

    public static boolean huyHoaDon(String maHD, String lyDoHuy, String maNVXacNhan) {
        HoaDon target = null;
        for (HoaDon hd : dsHoaDon) {
            if (hd.getMaHoaDon().equalsIgnoreCase(maHD)) {
                target = hd;
                break;
            }
        }
        if (target == null) return false;

        target.setTrangThaiDon("DA_HUY");
        target.setGhiChu(lyDoHuy);

        // Hoàn lại lượt voucher nếu có dùng
        if (target.getMaVoucher() != null) {
            giamSoLuotVoucher(target.getMaVoucher());
        }

        // Hoàn lại điểm tích lũy khách hàng nếu có
        if (target.getKhachHang() != null && target.getGiamGia() > 0) {
            int diemHoan = (int) (target.getGiamGia() / 1000);
            target.getKhachHang().setDiemTichLuy(target.getKhachHang().getDiemTichLuy() + diemHoan);
            saveKhachHang(target.getKhachHang());
        }

        // Giải phóng bàn nếu có
        if (target.getSoBan() > 0) {
            setTrangThaiBan(target.getSoBan(), "TRONG");
        }

        notifyDataChanged();
        new Thread(() -> {
            try {
                new com.homucafe.dao.HoaDon_DAO().huyHoaDon(maHD, lyDoHuy, maNVXacNhan);
            } catch (Throwable t) {
                logSqlError("huyHoaDon", t);
            }
        }).start();
        return true;
    }

    public static void saveMon(Mon m) {
        notifyDataChanged();
        new Thread(() -> {
            try {
                new com.homucafe.dao.Mon_DAO().capNhatMon(m);
            } catch (Throwable t) {
                logSqlError("saveMon", t);
            }
        }).start();
    }

    public static void themMonVaoDB(Mon m) {
        if (!dsMon.contains(m)) dsMon.add(m);
        notifyDataChanged();
        new Thread(() -> {
            try {
                new com.homucafe.dao.Mon_DAO().themMon(m);
            } catch (Throwable t) {
                logSqlError("themMonVaoDB", t);
            }
        }).start();
    }

    public static void saveNhanVien(NhanVien nv) {
        notifyDataChanged();
        new Thread(() -> {
            try {
                new com.homucafe.dao.NhanVien_DAO().capNhatNhanVien(nv);
            } catch (Throwable t) {
                logSqlError("saveNhanVien", t);
            }
        }).start();
    }

    public static void themNhanVienVaoDB(NhanVien nv) {
        if (!dsNhanVien.contains(nv)) dsNhanVien.add(nv);
        notifyDataChanged();
        new Thread(() -> {
            try {
                new com.homucafe.dao.NhanVien_DAO().themNhanVien(nv);
            } catch (Throwable t) {
                logSqlError("themNhanVienVaoDB", t);
            }
        }).start();
    }

    public static void saveKhachHang(KhachHang kh) {
        notifyDataChanged();
        new Thread(() -> {
            try {
                new com.homucafe.dao.KhachHang_DAO().capNhatKhachHang(kh);
            } catch (Throwable t) {
                logSqlError("saveKhachHang", t);
            }
        }).start();
    }

    public static void themKhachHangVaoDB(KhachHang kh) {
        if (!dsKhachHang.contains(kh)) dsKhachHang.add(kh);
        notifyDataChanged();
        new Thread(() -> {
            try {
                new com.homucafe.dao.KhachHang_DAO().themKhachHang(kh);
            } catch (Throwable t) {
                logSqlError("themKhachHangVaoDB", t);
            }
        }).start();
    }

    public static void setTrangThaiBan(int soBan, String trangThai) {
        trangThaiBan.put(soBan, trangThai);
        Ban b = timBanTheoSo(soBan);
        if (b != null) b.setTrangThai(trangThai);
        notifyDataChanged();
        saveTrangThaiBan(soBan, trangThai);
    }

    public static void saveTrangThaiBan(int soBan, String trangThai) {
        new Thread(() -> {
            try {
                new com.homucafe.dao.Ban_DAO().capNhatTrangThaiBan(soBan, trangThai);
            } catch (Throwable t) {
                logSqlError("saveTrangThaiBan", t);
            }
        }).start();
    }

    public static int chuyenHoacGopBan(int tuBanSo, int sangBanSo) {
        if (tuBanSo <= 0 || sangBanSo <= 0 || tuBanSo == sangBanSo) return 0;
        String tuBanStr = String.format("Bàn %02d", tuBanSo);
        String sangBanStr = String.format("Bàn %02d", sangBanSo);
        String maBanMoi = String.format("B%02d", sangBanSo);

        List<HoaDon> donCanChuyen = new ArrayList<>();
        HoaDon ganNhat = null;
        for (HoaDon hd : dsHoaDon) {
            if ("DA_HUY".equalsIgnoreCase(hd.getTrangThaiDon())) continue;
            boolean khopBan = tuBanStr.equalsIgnoreCase(hd.getBanPhucVu()) || hd.getSoBan() == tuBanSo;
            if (!khopBan) continue;
            if (ganNhat == null) ganNhat = hd;
            if ("DANG_PHA_CHE".equalsIgnoreCase(hd.getTrangThaiDon()) || "CHO_GIAO".equalsIgnoreCase(hd.getTrangThaiDon())) {
                donCanChuyen.add(hd);
            }
        }
        if (donCanChuyen.isEmpty() && ganNhat != null) {
            donCanChuyen.add(ganNhat);
        }

        for (HoaDon hd : donCanChuyen) {
            hd.setSoBan(sangBanSo);
            hd.setBanPhucVu(sangBanStr);
            final String maHD = hd.getMaHoaDon();
            new Thread(() -> {
                try {
                    new com.homucafe.dao.HoaDon_DAO().capNhatBanHoaDon(maHD, maBanMoi);
                } catch (Throwable t) {
                    logSqlError("chuyenHoacGopBan", t);
                }
            }).start();
        }

        setTrangThaiBan(tuBanSo, "TRONG");
        setTrangThaiBan(sangBanSo, "DANG_PHUC_VU");
        return donCanChuyen.size();
    }

    public static void saveVoucher(Voucher v) {
        if (!dsVoucher.contains(v)) {
            dsVoucher.add(v);
        }
        notifyDataChanged();
        new Thread(() -> {
            try {
                new com.homucafe.dao.Voucher_DAO().themVoucher(v);
            } catch (Throwable t) {
                logSqlError("saveVoucher", t);
            }
        }).start();
    }

    public static void capNhatTrangThaiVoucher(String maVoucher, boolean trangThai) {
        Voucher v = timVoucherTheoMa(maVoucher);
        if (v != null) {
            v.setTrangThai(trangThai);
            notifyDataChanged();
        }
        new Thread(() -> {
            try {
                new com.homucafe.dao.Voucher_DAO().capNhatTrangThai(maVoucher, trangThai);
            } catch (Throwable t) {
                logSqlError("capNhatTrangThaiVoucher", t);
            }
        }).start();
    }

    public static void tangSoLuotVoucher(String maVoucher) {
        Voucher v = timVoucherTheoMa(maVoucher);
        if (v != null) {
            v.setSoLuotDung(v.getSoLuotDung() + 1);
            notifyDataChanged();
        }
        new Thread(() -> {
            try {
                new com.homucafe.dao.Voucher_DAO().tangLuotDung(maVoucher);
            } catch (Throwable t) {
                logSqlError("tangSoLuotVoucher", t);
            }
        }).start();
    }

    public static void giamSoLuotVoucher(String maVoucher) {
        Voucher v = timVoucherTheoMa(maVoucher);
        if (v != null) {
            v.setSoLuotDung(Math.max(0, v.getSoLuotDung() - 1));
            notifyDataChanged();
        }
        new Thread(() -> {
            try {
                new com.homucafe.dao.Voucher_DAO().giamSoLuot(maVoucher);
            } catch (Throwable t) {
                logSqlError("giamSoLuotVoucher", t);
            }
        }).start();
    }

    public static void savePhieuDatBan(PhieuDatBan pdb) {
        if (!dsPhieuDatBan.contains(pdb)) {
            dsPhieuDatBan.add(0, pdb);
        }
        // Đổi trạng thái bàn tương ứng sang DA_DAT
        for (ChiTietPhieuDatBan ct : pdb.getDsChiTiet()) {
            if (ct.getBan() != null) {
                setTrangThaiBan(ct.getBan().getSoBan(), "DA_DAT");
            }
        }
        notifyDataChanged();
        new Thread(() -> {
            try {
                new com.homucafe.dao.PhieuDatBan_DAO().themPhieuDatBan(pdb);
            } catch (Throwable t) {
                logSqlError("savePhieuDatBan", t);
            }
        }).start();
    }

    public static void capNhatTrangThaiPhieuDatBan(String maPhieu, String trangThai) {
        for (PhieuDatBan p : dsPhieuDatBan) {
            if (p.getMaPhieu().equalsIgnoreCase(maPhieu)) {
                p.setTrangThai(trangThai);
                break;
            }
        }
        notifyDataChanged();
        new Thread(() -> {
            try {
                new com.homucafe.dao.PhieuDatBan_DAO().capNhatTrangThai(maPhieu, trangThai);
            } catch (Throwable t) {
                logSqlError("capNhatTrangThaiPhieuDatBan", t);
            }
        }).start();
    }

    // ==================== QUẢN LÝ ĐƠN TẠM GIỮ (HOLD ORDER / SPLIT BILL) ====================
    public static List<DonTamGiu> getDsDonTamGiu() {
        return Collections.unmodifiableList(dsDonTamGiu);
    }

    public static void themDonTamGiu(DonTamGiu d) {
        if (d != null) {
            dsDonTamGiu.add(0, d);
            notifyDataChanged();
        }
    }

    public static DonTamGiu layVaXoaDonTamGiu(int index) {
        if (index >= 0 && index < dsDonTamGiu.size()) {
            DonTamGiu d = dsDonTamGiu.remove(index);
            notifyDataChanged();
            return d;
        }
        return null;
    }

    public static boolean xoaDonTamGiu(int index) {
        if (index >= 0 && index < dsDonTamGiu.size()) {
            dsDonTamGiu.remove(index);
            notifyDataChanged();
            return true;
        }
        return false;
    }

    // ==================== SỔ PHIẾU CHI VẶT TRONG CA (PETTY CASH) ====================
    public static List<PhieuChiCa> getDsPhieuChiCa() {
        return Collections.unmodifiableList(dsPhieuChiCa);
    }

    public static PhieuChiCa themPhieuChiCa(double soTien, String lyDo, String nguoiChi) {
        String ma = "PC" + String.format("%04d", dsPhieuChiCa.size() + 1);
        PhieuChiCa pc = new PhieuChiCa(ma, LocalDateTime.now(), soTien, lyDo, nguoiChi);
        dsPhieuChiCa.add(0, pc);
        notifyDataChanged();
        return pc;
    }

    public static double tinhTongChiTheoNgay(LocalDate date) {
        if (date == null) date = LocalDate.now();
        final LocalDate target = date;
        return dsPhieuChiCa.stream()
                .filter(pc -> pc.getThoiGian() != null && pc.getThoiGian().toLocalDate().isEqual(target))
                .mapToDouble(PhieuChiCa::getSoTien)
                .sum();
    }
}
