package com.algoblock.gl.renderer;

public class DisplayTestPattern {
    private static final double CHECKERBOARD_VARIANT_SECONDS = 1.0;
    private static final int CHECKERBOARD_VARIANTS = 4;
    private static final double CHECKERBOARD_SECONDS = CHECKERBOARD_VARIANT_SECONDS * CHECKERBOARD_VARIANTS;
    private static final double FULL_RED_SECONDS = 1.5;
    private static final double FULL_GREEN_SECONDS = 1.5;
    private static final double ROLLING_SECONDS = 8.0;
    private static final double TOTAL_SECONDS = CHECKERBOARD_SECONDS + FULL_RED_SECONDS + FULL_GREEN_SECONDS
            + ROLLING_SECONDS;

    private static final int BG_DARK_A = 0x101418;
    private static final int BG_DARK_B = 0xD9DEE3;
    private static final int BG_RED = 0xFF0000;
    private static final int BG_GREEN = 0x00FF00;
    private static final int BG_IDLE = 0x1C2833;
    private static final int FG_BRIGHT = 0xF8FCFF;
    private static final int FG_DARK = 0x0E1116;

    public RenderFrame renderTo(TerminalBuffer buffer, double timeSeconds) {
        int cols = buffer.cols();
        int rows = buffer.rows();
        double t = normalize(timeSeconds);
        if (t < CHECKERBOARD_SECONDS) {
            int variant = Math.min(CHECKERBOARD_VARIANTS - 1, (int) (t / CHECKERBOARD_VARIANT_SECONDS));
            renderCheckerboard(buffer, cols, rows, variant);
            return null;
        }
        t -= CHECKERBOARD_SECONDS;
        if (t < FULL_RED_SECONDS) {
            fill(buffer, cols, rows, BG_RED);
            return null;
        }
        t -= FULL_RED_SECONDS;
        if (t < FULL_GREEN_SECONDS) {
            fill(buffer, cols, rows, BG_GREEN);
            return null;
        }
        t -= FULL_GREEN_SECONDS;

        // Cursor jump test
        fill(buffer, cols, rows, BG_IDLE);
        int cursorCol = 0;
        int cursorRow = 0;
        int phase = (int) (t * 2.0) % 4; // jump twice a second
        if (phase == 0) {
            cursorCol = 1;
            cursorRow = 1; // Top-Left
        } else if (phase == 1) {
            cursorCol = cols - 2;
            cursorRow = 1; // Top-Right
        } else if (phase == 2) {
            cursorCol = 1;
            cursorRow = rows - 2; // Bottom-Left
        } else {
            cursorCol = cols - 2;
            cursorRow = rows - 2; // Bottom-Right
        }

        return new RenderFrame(buffer, cursorCol, cursorRow, true, true, 0x00FF00, 0.0f);
    }

    private static double normalize(double t) {
        if (t <= 0d) {
            return 0d;
        }
        return t % TOTAL_SECONDS;
    }

    private static void renderCheckerboard(TerminalBuffer buffer, int cols, int rows, int variant) {
        boolean chinese = variant == 0 || variant == 2;
        boolean inverted = variant >= 2;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // For Chinese, 1 logical block = 2 cells
                // For English, 1 logical block = 2 cells (A+B or C+D)
                int logicalCol = col / 2;
                boolean evenCell = ((row + logicalCol) & 1) == 0;
                if (inverted) {
                    evenCell = !evenCell;
                }
                int bg = evenCell ? BG_DARK_A : BG_DARK_B;
                int fg = evenCell ? FG_BRIGHT : FG_DARK;

                boolean firstHalf = (col & 1) == 0;
                char c;
                if (chinese) {
                    c = firstHalf ? (evenCell ? '中' : '文') : '\0';
                } else {
                    c = evenCell ? (firstHalf ? 'A' : 'B') : (firstHalf ? 'C' : 'D');
                }
                buffer.set(col, row, c, fg, bg);
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
}
