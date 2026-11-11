package entity;

import java.util.Objects;

/**
 * Thực thể Loại món đồ uống (LOAI_MON)
 */
public class LoaiMon {
    private String maLoai;
    private String tenLoai;

    public LoaiMon() {
    }

    public LoaiMon(String maLoai, String tenLoai) {
        this.maLoai = maLoai;
        this.tenLoai = tenLoai;
    }

    public String getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(String maLoai) {
        this.maLoai = maLoai;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    @Override
    public String toString() {
        return tenLoai;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoaiMon)) return false;
        LoaiMon loaiMon = (LoaiMon) o;
        return Objects.equals(maLoai, loaiMon.maLoai);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maLoai);
    }
}

