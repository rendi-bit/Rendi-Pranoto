package com.mycompany.rendipranoto;

import java.util.List;

public interface BarangDao {

    List<Barang> findAll();

    Barang findById(int id);

    void save(Barang barang);

    void update(Barang barang);

    void delete(int id);
}