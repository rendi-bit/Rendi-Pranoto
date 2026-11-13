package com.mycompany.rendipranoto;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;

public class PanelBarang extends JPanel {

    // Semua akses database lewat DAO, tidak ada SQL lagi di panel
    private final BarangDao dao = AppConfig.getContext().getBean(BarangDao.class);

    private final JTextField txtKode = new JTextField();
    private final JTextField txtNama = new JTextField();
    private final JTextField txtHarga = new JTextField();
    private final JTextField txtStok = new JTextField();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Kode", "Nama", "Harga", "Stok"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabel = new JTable(model);

    private int idTerpilih = -1;

    public PanelBarang() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ===== Form input =====
        JPanel form = new JPanel(new GridLayout(2, 4, 10, 5));
        form.add(new JLabel("Kode"));
        form.add(new JLabel("Nama"));
        form.add(new JLabel("Harga"));
        form.add(new JLabel("Stok"));
        form.add(txtKode);
        form.add(txtNama);
        form.add(txtHarga);
        form.add(txtStok);

        // ===== Tombol =====
        JButton btnTambah = new JButton("Tambah");
        JButton btnUbah = new JButton("Ubah");
        JButton btnHapus = new JButton("Hapus");
        JButton btnBersih = new JButton("Bersihkan");

        btnTambah.addActionListener(e -> tambah());
        btnUbah.addActionListener(e -> ubah());
        btnHapus.addActionListener(e -> hapus());
        btnBersih.addActionListener(e -> bersihkan());

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        tombol.add(btnTambah);
        tombol.add(btnUbah);
        tombol.add(btnHapus);
        tombol.add(btnBersih);

        JPanel atas = new JPanel(new BorderLayout());
        atas.add(form, BorderLayout.CENTER);
        atas.add(tombol, BorderLayout.SOUTH);

        // ===== Tabel =====
        tabel.setRowHeight(24);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.getColumnModel().getColumn(0).setMaxWidth(50);
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                isiFormDariTabel();
            }
        });

        add(atas, BorderLayout.NORTH);
        add(new JScrollPane(tabel), BorderLayout.CENTER);

        // Muat ulang data setiap kali menu Data Barang dibuka
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadData();
            }
        });

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        try {
            List<Barang> daftar = dao.findAll();
            for (Barang b : daftar) {
                model.addRow(new Object[]{
                    b.getId(), b.getKode(), b.getNama(), b.getHarga(), b.getStok()
                });
            }
        } catch (DataAccessException ex) {
            pesan("Gagal memuat data: " + ex.getMessage());
        }
    }

    private void isiFormDariTabel() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            return;
        }
        idTerpilih = (int) model.getValueAt(baris, 0);
        txtKode.setText(model.getValueAt(baris, 1).toString());
        txtNama.setText(model.getValueAt(baris, 2).toString());
        txtHarga.setText(model.getValueAt(baris, 3).toString());
        txtStok.setText(model.getValueAt(baris, 4).toString());
    }

    // Membaca isi form menjadi objek Barang, null kalau isian tidak valid
    private Barang bacaForm() {
        String kode = txtKode.getText().trim();
        String nama = txtNama.getText().trim();
        String harga = txtHarga.getText().trim();
        String stok = txtStok.getText().trim();

        if (kode.isEmpty() || nama.isEmpty() || harga.isEmpty() || stok.isEmpty()) {
            pesan("Semua kolom wajib diisi");
            return null;
        }
        try {
            return new Barang(idTerpilih, kode, nama,
                    Integer.parseInt(harga), Integer.parseInt(stok));
        } catch (NumberFormatException e) {
            pesan("Harga dan stok harus berupa angka");
            return null;
        }
    }

    private void tambah() {
        Barang b = bacaForm();
        if (b == null) {
            return;
        }
        try {
            dao.save(b);
            pesan("Barang berhasil ditambahkan");
            bersihkan();
            loadData();
        } catch (DuplicateKeyException ex) {
            pesan("Kode barang sudah dipakai");
        } catch (DataAccessException ex) {
            pesan("Gagal menambah: " + ex.getMessage());
        }
    }

    private void ubah() {
        if (idTerpilih < 0) {
            pesan("Pilih barang di tabel terlebih dahulu");
            return;
        }
        Barang b = bacaForm();
        if (b == null) {
            return;
        }
        try {
            dao.update(b);
            pesan("Barang berhasil diubah");
            bersihkan();
            loadData();
        } catch (DuplicateKeyException ex) {
            pesan("Kode barang sudah dipakai");
        } catch (DataAccessException ex) {
            pesan("Gagal mengubah: " + ex.getMessage());
        }
    }

    private void hapus() {
        if (idTerpilih < 0) {
            pesan("Pilih barang di tabel terlebih dahulu");
            return;
        }
        int pilih = JOptionPane.showConfirmDialog(this, "Yakin hapus barang ini?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (pilih != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.delete(idTerpilih);
            bersihkan();
            loadData();
        } catch (DataAccessException ex) {
            pesan("Gagal menghapus (barang mungkin sudah pernah terjual): " + ex.getMessage());
        }
    }

    private void bersihkan() {
        idTerpilih = -1;
        txtKode.setText("");
        txtNama.setText("");
        txtHarga.setText("");
        txtStok.setText("");
        tabel.clearSelection();
        txtKode.requestFocus();
    }

    private void pesan(String teks) {
        JOptionPane.showMessageDialog(this, teks);
    }
}