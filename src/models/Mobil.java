package models;

import java.math.BigDecimal;

public class Mobil {
    private int id;
    private String nama;
    private String tipe;
    private int tahun;
    private BigDecimal hargaSewa;

    // Constructor
    public Mobil(int id, String nama, String tipe, int tahun, BigDecimal hargaSewa) {
        this.id = id;
        this.nama = nama;
        this.tipe = tipe;
        this.tahun = tahun;
        this.hargaSewa = hargaSewa;
    }

    // Constructor tanpa ID (untuk data baru)
    public Mobil(String nama, String tipe, int tahun, BigDecimal hargaSewa) {
        this.nama = nama;
        this.tipe = tipe;
        this.tahun = tahun;
        this.hargaSewa = hargaSewa;
    }

    // Getter dan Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }

    public int getTahun() {
        return tahun;
    }

    public void setTahun(int tahun) {
        this.tahun = tahun;
    }

    public BigDecimal getHargaSewa() {
        return hargaSewa;
    }

    public void setHargaSewa(BigDecimal hargaSewa) {
        this.hargaSewa = hargaSewa;
    }

    @Override
    public String toString() {
        return nama + " - " + tipe + " (" + tahun + ")";
    }
}
