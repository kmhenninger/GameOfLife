package org.example;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("flatlaf.useWindowDecorations", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                FlatLaf.updateUI();
            } catch (Exception ignored) {
            }
            GameOfLifeFrame frame = new GameOfLifeFrame();
            frame.setVisible(true);
        });
    }
}