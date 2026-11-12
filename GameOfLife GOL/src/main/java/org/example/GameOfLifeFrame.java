package org.example;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GameOfLifeFrame extends JFrame {
    private final GameOfLifePanel lifePanel;
    private final JLabel generationLabel;
    private final JToggleButton playPauseButton;
    private final JSlider speedSlider;

    // Neu: moderne Controls
    private final JLabel tpsLabel;
    private final JComboBox<String> themeCombo;
    private final JButton accentButton;

    private Color accent = new Color(76, 139, 245); // Default Akzent
    private boolean dark = true;

    public GameOfLifeFrame() {
        super("Conway's Game of Life");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        lifePanel = new GameOfLifePanel(60, 40, 16); // leicht größere Zellen
        lifePanel.applyTheme(dark, accent);

        generationLabel = new JLabel("Generation: 0");
        generationLabel.setFont(generationLabel.getFont().deriveFont(Font.BOLD));

        playPauseButton = new JToggleButton("Start");
        speedSlider = new JSlider(1, 30, 10);
        speedSlider.setToolTipText("Geschwindigkeit (Ticks/Sek)");
        speedSlider.setMajorTickSpacing(5);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        tpsLabel = new JLabel(speedSlider.getValue() + " t/s");

        // Theme & Accent
        themeCombo = new JComboBox<>(new String[]{"Dark", "Light"});
        themeCombo.setSelectedItem(dark ? "Dark" : "Light");
        accentButton = new JButton("Accent");
        accentButton.setFocusable(false);
        accentButton.setBackground(accent);
        accentButton.setForeground(chooseForeground(accent));
        accentButton.setOpaque(true);

        // Toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JButton stepButton = new JButton("Step");
        JButton clearButton = new JButton("Clear");
        JButton randomButton = new JButton("Random");

        toolbar.add(playPauseButton);
        toolbar.add(stepButton);
        toolbar.add(clearButton);
        toolbar.add(randomButton);
        toolbar.addSeparator(new Dimension(10, 0));
        toolbar.add(new JLabel("Speed:"));
        toolbar.add(speedSlider);
        toolbar.add(tpsLabel);
        toolbar.addSeparator(new Dimension(10, 0));
        toolbar.add(new JLabel("Theme:"));
        toolbar.add(themeCombo);
        toolbar.add(accentButton);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(generationLabel);

        add(toolbar, BorderLayout.NORTH);
        add(lifePanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);

        // Aktionen
        playPauseButton.addActionListener(e -> {
            if (playPauseButton.isSelected()) {
                playPauseButton.setText("Pause");
                lifePanel.start();
            } else {
                playPauseButton.setText("Start");
                lifePanel.pause();
            }
        });
        stepButton.addActionListener(e -> lifePanel.step());
        clearButton.addActionListener(e -> {
            lifePanel.clear();
            generationLabel.setText("Generation: " + lifePanel.getGeneration());
            if (playPauseButton.isSelected()) {
                playPauseButton.setSelected(false);
                playPauseButton.setText("Start");
                lifePanel.pause();
            }
        });
        randomButton.addActionListener(e -> {
            lifePanel.randomize();
            generationLabel.setText("Generation: " + lifePanel.getGeneration());
        });

        speedSlider.addChangeListener(e -> {
            int v = speedSlider.getValue();
            lifePanel.setTicksPerSecond(v);
            tpsLabel.setText(v + " t/s");
        });

        // Generation-Label bei Änderungen aktualisieren
        lifePanel.setOnGenerationChanged(gen -> generationLabel.setText("Generation: " + gen));

        // Tastenkürzel: Leertaste = Start/Pause
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "togglePlay");
        getRootPane().getActionMap().put("togglePlay", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playPauseButton.doClick();
            }
        });

        // Theme-Umschaltung
        themeCombo.addActionListener(e -> {
            dark = "Dark".equals(themeCombo.getSelectedItem());
            switchLaf(dark);
            // Nach LAF-Wechsel UI refreshen und Theme anwenden
            FlatLaf.updateUI();
            lifePanel.applyTheme(dark, accent);
            // Farben des Accent-Buttons ggf. anpassen, damit lesbar
            accentButton.setForeground(chooseForeground(accent));
            pack();
        });

        // Accent-Farbwahl
        accentButton.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Accent-Farbe wählen", accent);
            if (chosen != null) {
                accent = chosen;
                accentButton.setBackground(accent);
                accentButton.setForeground(chooseForeground(accent));
                lifePanel.applyTheme(dark, accent);
            }
        });
    }

    private void switchLaf(boolean dark) {
        try {
            if (dark) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
        } catch (Exception ignored) {
        }
    }

    private Color chooseForeground(Color bg) {
        // Kontrastreiche Schrift auf dem Accent-Button
        double luma = 0.2126 * bg.getRed() + 0.7152 * bg.getGreen() + 0.0722 * bg.getBlue();
        return luma > 150 ? Color.BLACK : Color.WHITE;
    }
}
