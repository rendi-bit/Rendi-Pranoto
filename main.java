/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.rendiprnanoto;

/**
 *
 * @author LENOVO
 */
public class main {
    
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.JFrame frame = new javax.swing.JFrame("Login");
            
            frame.setDefaultCloseOperation(
                javax.swing.JFrame.EXIT_ON_CLOSE
            );
            
            frame.setContentPane(new LoginFrom());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}