package com.mycompany.rendipranoto;

import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PanelLaporan extends JPanel {

    private static final String URL = "jdbc:mysql://localhost:3306/admin_rpl2";
    private static final String USER = "root";
    private static final String PASS = "";

    private final JTextField txtDari = new JTextField(10);
    private final JTextField txtSampai = new JTextField(10);

    private final JLabel lblPendapatan = new JLabel("Total Pendapatan: Rp 0");
    private final JLabel lblJumlah = new JLabel("Jumlah Transaksi: 0");

    private final DefaultTableModel modelPenjualan = buatModel(
            "ID", "Tanggal", "Total", "Bayar", "Kembali");
    private final DefaultTableModel modelTerlaris = buatModel(
            "Kode", "Nama", "Terjual", "Omzet");

    public PanelLaporan() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ===== Filter tanggal =====
        txtDari.setText(LocalDate.now().toString());
        txtSampai.setText(LocalDate.now().toString());

        JButton btnTampil = new JButton("Tampilkan");
        JButton btnHariIni = new JButton("Hari Ini");
        JButton btnBulanIni = new JButton("Bulan Ini");

        btnTampil.addActionListener(e -> tampilkan());
        btnHariIni.addActionListener(e -> {
            txtDari.setText(LocalDate.now().toString());
            txtSampai.setText(LocalDate.now().toString());
            tampilkan();
        });
        btnBulanIni.addActionListener(e -> {
            txtDari.setText(LocalDate.now().withDayOfMonth(1).toString());
            txtSampai.setText(LocalDate.now().toString());
            tampilkan();
        });

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        filter.add(new JLabel("Dari (yyyy-MM-dd)"));
        filter.add(txtDari);
        filter.add(new JLabel("Sampai"));
        filter.add(txtSampai);
        filter.add(btnTampil);
        filter.add(btnHariIni);
        filter.add(btnBulanIni);

        // ===== Ringkasan =====
        lblPendapatan.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblJumlah.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JPanel ringkasan = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 5));
        ringkasan.add(lblPendapatan);
        ringkasan.add(lblJumlah);

        JPanel atas = new JPanel(new GridLayout(2, 1));
        atas.add(filter);
        atas.add(ringkasan);

        // ===== Dua tabel =====
        JScrollPane spPenjualan = new JScrollPane(buatTabel(modelPenjualan));
        spPenjualan.setBorder(BorderFactory.createTitledBorder("Riwayat Penjualan"));

        JScrollPane spTerlaris = new JScrollPane(buatTabel(modelTerlaris));
        spTerlaris.setBorder(BorderFactory.createTitledBorder("Barang Terlaris (10 teratas)"));

        JPanel tengah = new JPanel(new GridLayout(1, 2, 10, 0));
        tengah.add(spPenjualan);
        tengah.add(spTerlaris);

        add(atas, BorderLayout.NORTH);
        add(tengah, BorderLayout.CENTER);

        // Muat ulang setiap kali menu Laporan dibuka
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                tampilkan();
            }
        });

        tampilkan();
    }

    private static DefaultTableModel buatModel(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable buatTabel(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(24);
        return t;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private void tampilkan() {
        LocalDate dari;
        LocalDate sampai;
        try {
            dari = LocalDate.parse(txtDari.getText().trim());
            sampai = LocalDate.parse(txtSampai.getText().trim());
        } catch (DateTimeParseException e) {
            pesan("Format tanggal harus yyyy-MM-dd, contoh: 2026-11-13");
            return;
        }
        if (dari.isAfter(sampai)) {
            pesan("Tanggal 'Dari' tidak boleh setelah tanggal 'Sampai'");
            return;
        }

        Date tglDari = Date.valueOf(dari);
        Date tglSampai = Date.valueOf(sampai);
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        modelPenjualan.setRowCount(0);
        modelTerlaris.setRowCount(0);
        long totalPendapatan = 0;
        int jumlahTransaksi = 0;

        String sqlPenjualan = "SELECT id, tanggal, total, bayar, kembali FROM penjualan "
                + "WHERE DATE(tanggal) BETWEEN ? AND ? ORDER BY tanggal DESC";

        String sqlTerlaris = "SELECT b.kode, b.nama, SUM(d.jumlah) AS terjual, SUM(d.subtotal) AS omzet "
                + "FROM detail_penjualan d "
                + "JOIN penjualan p ON p.id = d.penjualan_id "
                + "JOIN barang b ON b.id = d.barang_id "
                + "WHERE DATE(p.tanggal) BETWEEN ? AND ? "
                + "GROUP BY b.id, b.kode, b.nama ORDER BY terjual DESC LIMIT 10";

        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(sqlPenjualan)) {
                ps.setDate(1, tglDari);
                ps.setDate(2, tglSampai);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int total = rs.getInt("total");
                        totalPendapatan += total;
                        jumlahTransaksi++;
                        modelPenjualan.addRow(new Object[]{
                            rs.getInt("id"),
                            fmt.format(rs.getTimestamp("tanggal")),
                            rupiah(total),
                            rupiah(rs.getInt("bayar")),
                            rupiah(rs.getInt("kembali"))
                        });
                    }
                }
            }

            try (PreparedStatement ps = c.prepareStatement(sqlTerlaris)) {
                ps.setDate(1, tglDari);
                ps.setDate(2, tglSampai);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        modelTerlaris.addRow(new Object[]{
                            rs.getString("kode"),
                            rs.getString("nama"),
                            rs.getInt("terjual"),
                            rupiah(rs.getLong("omzet"))
                        });
                    }
                }
            }
        } catch (SQLException ex) {
            pesan("Gagal memuat laporan: " + ex.getMessage());
            return;
        }

        lblPendapatan.setText("Total Pendapatan: " + rupiah(totalPendapatan));
        lblJumlah.setText("Jumlah Transaksi: " + jumlahTransaksi);
    }

    private String rupiah(long angka) {
        return "Rp " + String.format("%,d", angka);
    }

    private void pesan(String teks) {
        JOptionPane.showMessageDialog(this, teks);
    }
}