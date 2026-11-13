/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.rendipranoto;

import java.awt.*;
import javax.swing.*;

public class DashboardAdmin extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    public DashboardAdmin() {
        setTitle("Admin Warung");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== Sidebar menu =====
        JPanel sidebar = new JPanel(new GridLayout(0, 1, 0, 8));
        sidebar.setBackground(new Color(40, 44, 52));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel judul = new JLabel("WARUNG ADMIN", SwingConstants.CENTER);
        judul.setForeground(Color.WHITE);
        judul.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sidebar.add(judul);

        sidebar.add(buatMenu("Dashboard", "dashboard"));
        sidebar.add(buatMenu("Data Barang", "barang"));
        sidebar.add(buatMenu("Transaksi", "transaksi"));
        sidebar.add(buatMenu("Laporan", "laporan"));

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> logout());
        sidebar.add(btnLogout);

        // ===== Konten: nanti ganti placeholder dengan panel asli =====
        contentPanel.add(buatPlaceholder("Selamat datang, Admin"), "dashboard");
        contentPanel.add(new PanelBarang(), "barang");
        contentPanel.add(new PanelTransaksi(), "transaksi");
        contentPanel.add(new PanelLaporan(), "laporan");

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JButton buatMenu(String teks, String namaCard) {
        JButton b = new JButton(teks);
        b.setFocusPainted(false);
        b.addActionListener(e -> cardLayout.show(contentPanel, namaCard));
        return b;
    }

    private JPanel buatPlaceholder(String teks) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel(teks, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    private void logout() {
        int pilih = JOptionPane.showConfirmDialog(this, "Yakin ingin logout?",
                "Logout", JOptionPane.YES_NO_OPTION);
        if (pilih == JOptionPane.YES_OPTION) {
            dispose();
            JFrame f = new JFrame("Login");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(new LoginFrom());
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        }
    }
}