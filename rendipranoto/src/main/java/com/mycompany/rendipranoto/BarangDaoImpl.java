package com.mycompany.rendipranoto;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class BarangDaoImpl implements BarangDao {

    private final JdbcTemplate jdbc;

    // Mengubah satu baris hasil query menjadi objek Barang
    private final RowMapper<Barang> rowMapper = (rs, rowNum) -> new Barang(
            rs.getInt("id"),
            rs.getString("kode"),
            rs.getString("nama"),
            rs.getInt("harga"),
            rs.getInt("stok"));

    public BarangDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Barang> findAll() {
        return jdbc.query(
                "SELECT id, kode, nama, harga, stok FROM barang ORDER BY id",
                rowMapper);
    }

    @Override
    public Barang findById(int id) {
        List<Barang> hasil = jdbc.query(
                "SELECT id, kode, nama, harga, stok FROM barang WHERE id = ?",
                rowMapper, id);
        return hasil.isEmpty() ? null : hasil.get(0);
    }

    @Override
    public void save(Barang b) {
        jdbc.update(
                "INSERT INTO barang (kode, nama, harga, stok) VALUES (?, ?, ?, ?)",
                b.getKode(), b.getNama(), b.getHarga(), b.getStok());
    }

    @Override
    public void update(Barang b) {
        jdbc.update(
                "UPDATE barang SET kode = ?, nama = ?, harga = ?, stok = ? WHERE id = ?",
                b.getKode(), b.getNama(), b.getHarga(), b.getStok(), b.getId());
    }

    @Override
    public void delete(int id) {
        jdbc.update("DELETE FROM barang WHERE id = ?", id);
    }
}