package com.algoblock.gl.renderer;

public class DisplayTestPattern {
    private static final double CHECKERBOARD_SECONDS = 1.5;
    private static final double FULL_RED_SECONDS = 1.5;
    private static final double FULL_GREEN_SECONDS = 1.5;
    private static final double ROLLING_SECONDS = 2.0;
    private static final double TOTAL_SECONDS = CHECKERBOARD_SECONDS + FULL_RED_SECONDS + FULL_GREEN_SECONDS + ROLLING_SECONDS;

    private static final int BG_DARK_A = 0x111111;
    private static final int BG_DARK_B = 0x222222;
    private static final int BG_RED = 0xFF0000;
    private static final int BG_GREEN = 0x00FF00;
    private static final int BG_IDLE = 0x101820;

    public void renderTo(TerminalBuffer buffer, double timeSeconds) {
        int cols = buffer.cols();
        int rows = buffer.rows();
        double t = normalize(timeSeconds);
        if (t < CHECKERBOARD_SECONDS) {
            renderCheckerboard(buffer, cols, rows);
            return;
        }
        t -= CHECKERBOARD_SECONDS;
        if (t < FULL_RED_SECONDS) {
            fill(buffer, cols, rows, BG_RED);
            return;
        }
        t -= FULL_RED_SECONDS;
        if (t < FULL_GREEN_SECONDS) {
            fill(buffer, cols, rows, BG_GREEN);
            return;
        }
        t -= FULL_GREEN_SECONDS;
        renderRolling(buffer, cols, rows, t);
    }

    private static double normalize(double t) {
        if (t <= 0d) {
            return 0d;
        }
        return t % TOTAL_SECONDS;
    }

    private static void renderCheckerboard(TerminalBuffer buffer, int cols, int rows) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int bg = ((row + col) & 1) == 0 ? BG_DARK_A : BG_DARK_B;
                buffer.set(col, row, ' ', 0xFFFFFF, bg);
            }
        }
    }

    private static void fill(TerminalBuffer buffer, int cols, int rows, int bg) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                buffer.set(col, row, ' ', 0xFFFFFF, bg);
            }
        }
    }

    private static void renderRolling(TerminalBuffer buffer, int cols, int rows, double localTime) {
        int totalCells = Math.max(1, cols * rows);
        int active = (int) ((localTime / ROLLING_SECONDS) * totalCells) % totalCells;
        int trailing = (active - 1 + totalCells) % totalCells;
        for (int i = 0; i < totalCells; i++) {
            int col = i % cols;
            int row = i / cols;
            int bg = BG_IDLE;
            if (i == active) {
                bg = BG_RED;
            } else if (i == trailing) {
                bg = BG_GREEN;
            }
            buffer.set(col, row, ' ', 0xFFFFFF, bg);
        }
    }
}
