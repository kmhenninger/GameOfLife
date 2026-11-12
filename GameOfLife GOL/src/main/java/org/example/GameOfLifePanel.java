package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import java.util.function.IntConsumer;

public class GameOfLifePanel extends JPanel {
    private final int cols;
    private final int rows;
    private final int cellSize;
    private boolean[][] grid;
    private boolean[][] buffer;

    // Altersverfolgung für sanfte Farb-/Alpha-Übergänge
    private int[][] age;
    private int[][] ageBuffer;

    private int generation = 0;
    private final Random random = new Random();
    private Timer timer;
    private int ticksPerSecond = 10;
    private IntConsumer onGenerationChanged;

    private boolean drawing;
    private boolean drawState; // true -> set alive, false -> set dead

    // Theme/Palette
    private boolean darkTheme = false;
    private Color accent = new Color(76, 139, 245);
    private Color bgTop = new Color(245, 247, 250);
    private Color bgBottom = Color.WHITE;
    private Color gridColor = new Color(0, 0, 0, 25);

    public GameOfLifePanel(int cols, int rows, int cellSize) {
        this.cols = cols;
        this.rows = rows;
        this.cellSize = cellSize;
        this.grid = new boolean[rows][cols];
        this.buffer = new boolean[rows][cols];
        this.age = new int[rows][cols];
        this.ageBuffer = new int[rows][cols];

        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int c = e.getX() / cellSize;
                int r = e.getY() / cellSize;
                if (inBounds(r, c)) {
                    drawState = !grid[r][c];
                    drawing = true;
                    grid[r][c] = drawState;
                    age[r][c] = drawState ? Math.max(1, age[r][c] + 1) : 0;
                    repaint();
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                drawing = false;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!drawing) return;
                int c = e.getX() / cellSize;
                int r = e.getY() / cellSize;
                if (inBounds(r, c)) {
                    if (grid[r][c] != drawState) {
                        grid[r][c] = drawState;
                        age[r][c] = drawState ? Math.max(1, age[r][c] + 1) : 0;
                        repaint();
                    }
                }
            }
        });

        // Start mit hellem Theme
        applyTheme(false, accent);
    }

    public void applyTheme(boolean dark, Color accentColor) {
        this.darkTheme = dark;
        this.accent = accentColor != null ? accentColor : this.accent;

        if (darkTheme) {
            bgTop = new Color(20, 22, 25);
            bgBottom = new Color(30, 33, 38);
            gridColor = new Color(255, 255, 255, 36);
        } else {
            bgTop = new Color(245, 247, 250);
            bgBottom = Color.WHITE;
            gridColor = new Color(0, 0, 0, 30);
        }
        repaint();
    }

    public void start() {
        ensureTimer();
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    public void pause() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }

    public void step() {
        computeNextGeneration();
        generation++;
        if (onGenerationChanged != null) onGenerationChanged.accept(generation);
        repaint();
    }

    public void clear() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = false;
                age[r][c] = 0;
            }
        }
        generation = 0;
        if (onGenerationChanged != null) onGenerationChanged.accept(generation);
        repaint();
    }

    public void randomize() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                boolean alive = random.nextBoolean() && random.nextBoolean(); // ~25% lebend
                grid[r][c] = alive;
                age[r][c] = alive ? 1 : 0;
            }
        }
        generation = 0;
        if (onGenerationChanged != null) onGenerationChanged.accept(generation);
        repaint();
    }

    public void setTicksPerSecond(int tps) {
        ticksPerSecond = Math.max(1, Math.min(60, tps));
        if (timer != null) {
            timer.setDelay(computeDelayMillis(ticksPerSecond));
        }
    }

    public int getGeneration() {
        return generation;
    }

    public void setOnGenerationChanged(IntConsumer listener) {
        this.onGenerationChanged = listener;
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    private void computeNextGeneration() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int n = neighbors(r, c);
                boolean alive = grid[r][c];
                boolean next = alive ? (n == 2 || n == 3) : (n == 3);
                buffer[r][c] = next;
                if (next) {
                    ageBuffer[r][c] = alive ? age[r][c] + 1 : 1;
                } else {
                    ageBuffer[r][c] = 0;
                }
            }
        }
        boolean[][] tmp = grid;
        grid = buffer;
        buffer = tmp;

        int[][] tmpA = age;
        age = ageBuffer;
        ageBuffer = tmpA;
    }

    private int neighbors(int r, int c) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int rr = r + dr;
                int cc = c + dc;
                if (rr >= 0 && rr < rows && cc >= 0 && cc < cols && grid[rr][cc]) {
                    count++;
                }
            }
        }
        return count;
    }

    private void ensureTimer() {
        if (timer == null) {
            timer = new Timer(computeDelayMillis(ticksPerSecond), e -> step());
        } else {
            timer.setDelay(computeDelayMillis(ticksPerSecond));
        }
    }

    private int computeDelayMillis(int tps) {
        return Math.max(16, 1000 / tps);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Hintergrund mit sanftem Verlauf
            Paint gp = new GradientPaint(0, 0, bgTop, 0, getHeight(), bgBottom);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Grid
            g2.setColor(gridColor);
            for (int x = 0; x <= cols * cellSize; x += cellSize) {
                g2.drawLine(x, 0, x, rows * cellSize);
            }
            for (int y = 0; y <= rows * cellSize; y += cellSize) {
                g2.drawLine(0, y, cols * cellSize, y);
            }

            // Zellen: abgerundet, mit Altersfarbverlauf
            int pad = Math.max(1, cellSize / 8);
            int arc = Math.max(8, cellSize / 2);
            for (int r = 0; r < rows; r++) {
                int y = r * cellSize + pad;
                for (int c = 0; c < cols; c++) {
                    if (!grid[r][c]) continue;

                    int x = c * cellSize + pad;
                    int w = cellSize - 2 * pad;
                    int h = cellSize - 2 * pad;

                    // Alters-basiertes Blending und Alpha
                    int a = Math.min(age[r][c], 12);
                    float t = a / 12f;
                    Color bright = changeBrightness(accent, darkTheme ? 1.2f : 1.1f);
                    Color darkC = changeBrightness(accent, darkTheme ? 0.7f : 0.85f);
                    Color fill = mix(bright, darkC, t);
                    int alpha = clamp((int) (90 + t * 165), 60, 255);
                    Color fillA = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), alpha);

                    if (darkTheme) {
                        // leichter Schatten
                        g2.setColor(new Color(0, 0, 0, 80));
                        g2.fillRoundRect(x + 1, y + 1, w, h, arc, arc);
                    }

                    g2.setColor(fillA);
                    g2.fillRoundRect(x, y, w, h, arc, arc);

                    // feiner Rand
                    Color border = changeBrightness(fill, darkTheme ? 0.75f : 0.9f);
                    g2.setColor(new Color(border.getRed(), border.getGreen(), border.getBlue(), clamp(alpha + 10, 60, 255)));
                    g2.drawRoundRect(x, y, w, h, arc, arc);
                }
            }
        } finally {
            g2.dispose();
        }
    }

    // Hilfsfunktionen

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private Color mix(Color c1, Color c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (c1.getRed() * (1 - t) + c2.getRed() * t);
        int g = (int) (c1.getGreen() * (1 - t) + c2.getGreen() * t);
        int b = (int) (c1.getBlue() * (1 - t) + c2.getBlue() * t);
        return new Color(r, g, b);
    }

    private Color changeBrightness(Color c, float factor) {
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        float b = Math.max(0f, Math.min(1f, hsb[2] * factor));
        return Color.getHSBColor(hsb[0], hsb[1], b);
    }
}
