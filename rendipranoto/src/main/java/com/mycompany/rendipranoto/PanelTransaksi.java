package com.mycompany.rendipranoto;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PanelTransaksi extends JPanel {

    private static final String URL = "jdbc:mysql://localhost:3306/admin_rpl2";
    private static final String USER = "root";
    private static final String PASS = "";

    // Data satu barang untuk ditampilkan di combo box
    private static class Item {
        int id;
        String kode;
        String nama;
        int harga;
        int stok;

        @Override
        public String toString() {
            return kode + " - " + nama + " (stok " + stok + ")";
        }
    }

    private final JComboBox<Item> cmbBarang = new JComboBox<>();
    private final JSpinner spnJumlah = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
    private final JTextField txtBayar = new JTextField(10);
    private final JLabel lblTotal = new JLabel("Total: Rp 0");
    private final JLabel lblKembali = new JLabel("Kembalian: Rp 0");

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Kode", "Nama", "Harga", "Jumlah", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabel = new JTable(model);

    public PanelTransaksi() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ===== Bagian atas: pilih barang =====
        JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton btnTambah = new JButton("Tambah ke Keranjang");
        btnTambah.addActionListener(e -> tambahKeKeranjang());
        cmbBarang.setPreferredSize(new Dimension(320, 28));
        spnJumlah.setPreferredSize(new Dimension(70, 28));
        atas.add(new JLabel("Barang"));
        atas.add(cmbBarang);
        atas.add(new JLabel("Jumlah"));
        atas.add(spnJumlah);
        atas.add(btnTambah);

        // ===== Tengah: keranjang =====
        tabel.setRowHeight(24);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.getColumnModel().getColumn(0).setMaxWidth(50);

        // ===== Bawah: total, bayar, tombol =====
        txtBayar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                hitungKembalian();
            }
        });

        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblKembali.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton btnHapusItem = new JButton("Hapus Item");
        JButton btnSimpan = new JButton("Simpan Transaksi");
        JButton btnBatal = new JButton("Batal");
        btnHapusItem.addActionListener(e -> hapusItem());
        btnSimpan.addActionListener(e -> simpan());
        btnBatal.addActionListener(e -> reset());

        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        info.add(lblTotal);
        info.add(new JLabel("Bayar (Rp)"));
        info.add(txtBayar);
        info.add(lblKembali);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        tombol.add(btnHapusItem);
        tombol.add(btnSimpan);
        tombol.add(btnBatal);

        JPanel bawah = new JPanel(new GridLayout(2, 1));
        bawah.add(info);
        bawah.add(tombol);

        add(atas, BorderLayout.NORTH);
        add(new JScrollPane(tabel), BorderLayout.CENTER);
        add(bawah, BorderLayout.SOUTH);

        loadBarang();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // Isi combo box dengan barang yang stoknya masih ada
    private void loadBarang() {
        cmbBarang.removeAllItems();
        String sql = "SELECT id, kode, nama, harga, stok FROM barang WHERE stok > 0 ORDER BY nama";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Item it = new Item();
                it.id = rs.getInt("id");
                it.kode = rs.getString("kode");
                it.nama = rs.getString("nama");
                it.harga = rs.getInt("harga");
                it.stok = rs.getInt("stok");
                cmbBarang.addItem(it);
            }
        } catch (SQLException ex) {
            pesan("Gagal memuat barang: " + ex.getMessage());
        }
    }

    private void tambahKeKeranjang() {
        Item it = (Item) cmbBarang.getSelectedItem();
        if (it == null) {
            pesan("Tidak ada barang yang dipilih");
            return;
        }
        int jumlah = (int) spnJumlah.getValue();

        // Kalau barang sudah ada di keranjang, jumlahnya ditambah
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((int) model.getValueAt(i, 0) == it.id) {
                int jumlahBaru = (int) model.getValueAt(i, 4) + jumlah;
                if (jumlahBaru > it.stok) {
                    pesan("Stok tidak cukup. Sisa stok: " + it.stok);
                    return;
                }
                model.setValueAt(jumlahBaru, i, 4);
                model.setValueAt(jumlahBaru * it.harga, i, 5);
                hitungTotal();
                return;
            }
        }

        if (jumlah > it.stok) {
            pesan("Stok tidak cukup. Sisa stok: " + it.stok);
            return;
        }
        model.addRow(new Object[]{it.id, it.kode, it.nama, it.harga, jumlah, it.harga * jumlah});
        spnJumlah.setValue(1);
        hitungTotal();
    }

    private void hapusItem() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            pesan("Pilih item di keranjang terlebih dahulu");
            return;
        }
        model.removeRow(baris);
        hitungTotal();
    }

    private int getTotal() {
        int total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            total += (int) model.getValueAt(i, 5);
        }
        return total;
    }

    private void hitungTotal() {
        lblTotal.setText("Total: Rp " + String.format("%,d", getTotal()));
        hitungKembalian();
    }

    private void hitungKembalian() {
        try {
            int bayar = Integer.parseInt(txtBayar.getText().trim());
            int kembali = bayar - getTotal();
            lblKembali.setText("Kembalian: Rp " + String.format("%,d", Math.max(kembali, 0)));
        } catch (NumberFormatException e) {
            lblKembali.setText("Kembalian: Rp 0");
        }
    }

    private void simpan() {
        if (model.getRowCount() == 0) {
            pesan("Keranjang masih kosong");
            return;
        }
        int total = getTotal();
        int bayar;
        try {
            bayar = Integer.parseInt(txtBayar.getText().trim());
        } catch (NumberFormatException e) {
            pesan("Isi kolom bayar dengan angka");
            return;
        }
        if (bayar < total) {
            pesan("Uang bayar kurang dari total");
            return;
        }

        try (Connection c = getConnection()) {
            c.setAutoCommit(false); // semua langkah berhasil, atau semuanya dibatalkan
            try {
                int penjualanId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO penjualan (total, bayar, kembali) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, total);
                    ps.setInt(2, bayar);
                    ps.setInt(3, bayar - total);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        penjualanId = keys.getInt(1);
                    }
                }

                for (int i = 0; i < model.getRowCount(); i++) {
                    int barangId = (int) model.getValueAt(i, 0);
                    String nama = model.getValueAt(i, 2).toString();
                    int harga = (int) model.getValueAt(i, 3);
                    int jumlah = (int) model.getValueAt(i, 4);
                    int subtotal = (int) model.getValueAt(i, 5);

                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO detail_penjualan (penjualan_id, barang_id, harga, jumlah, subtotal) "
                            + "VALUES (?, ?, ?, ?, ?)")) {
                        ps.setInt(1, penjualanId);
                        ps.setInt(2, barangId);
                        ps.setInt(3, harga);
                        ps.setInt(4, jumlah);
                        ps.setInt(5, subtotal);
                        ps.executeUpdate();
                    }

                    // Kurangi stok, hanya berhasil kalau stok masih cukup
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE barang SET stok = stok - ? WHERE id = ? AND stok >= ?")) {
                        ps.setInt(1, jumlah);
                        ps.setInt(2, barangId);
                        ps.setInt(3, jumlah);
                        if (ps.executeUpdate() == 0) {
                            throw new SQLException("Stok tidak cukup untuk " + nama);
                        }
                    }
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }

            pesan("Transaksi berhasil!\nTotal: Rp " + String.format("%,d", total)
                    + "\nKembalian: Rp " + String.format("%,d", bayar - total));
            reset();
        } catch (SQLException ex) {
            pesan("Transaksi gagal: " + ex.getMessage());
        }
    }

    private void reset() {
        model.setRowCount(0);
        txtBayar.setText("");
        spnJumlah.setValue(1);
        hitungTotal();
        loadBarang();
    }

    private void pesan(String teks) {
        JOptionPane.showMessageDialog(this, teks);
    }
}