package com.algoblock.gl.renderer;

public class DisplayTestPattern {
    private static final double CHECKERBOARD_VARIANT_SECONDS = 1.0;
    private static final int CHECKERBOARD_VARIANTS = 4;
    private static final double CHECKERBOARD_SECONDS = CHECKERBOARD_VARIANT_SECONDS * CHECKERBOARD_VARIANTS;
    private static final double FULL_RED_SECONDS = 1.5;
    private static final double FULL_GREEN_SECONDS = 1.5;
    private static final double ROLLING_SECONDS = 8.0;
    private static final double TOTAL_SECONDS = CHECKERBOARD_SECONDS + FULL_RED_SECONDS + FULL_GREEN_SECONDS + ROLLING_SECONDS;

    private static final int BG_DARK_A = 0x101418;
    private static final int BG_DARK_B = 0xD9DEE3;
    private static final int BG_RED = 0xFF0000;
    private static final int BG_GREEN = 0x00FF00;
    private static final int BG_IDLE = 0x1C2833;
    private static final int FG_BRIGHT = 0xF8FCFF;
    private static final int FG_DARK = 0x0E1116;

    public void renderTo(TerminalBuffer buffer, double timeSeconds) {
        int cols = buffer.cols();
        int rows = buffer.rows();
        double t = normalize(timeSeconds);
        if (t < CHECKERBOARD_SECONDS) {
            int variant = Math.min(CHECKERBOARD_VARIANTS - 1, (int) (t / CHECKERBOARD_VARIANT_SECONDS));
            renderCheckerboard(buffer, cols, rows, variant);
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

    private static void renderCheckerboard(TerminalBuffer buffer, int cols, int rows, int variant) {
        boolean chinese = variant == 0 || variant == 2;
        boolean inverted = variant >= 2;
        char evenChar = chinese ? '中' : 'A';
        char oddChar = chinese ? '文' : 'B';
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                boolean evenCell = ((row + col) & 1) == 0;
                if (inverted) {
                    evenCell = !evenCell;
                }
                int bg = evenCell ? BG_DARK_A : BG_DARK_B;
                int fg = evenCell ? FG_BRIGHT : FG_DARK;
                char c = evenCell ? evenChar : oddChar;
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

    private static void renderRolling(TerminalBuffer buffer, int cols, int rows, double localTime) {
        int totalCells = Math.max(1, cols * rows);
        float continuous = (float) ((localTime / ROLLING_SECONDS) * totalCells);
        int head = ((int) Math.floor(continuous)) % totalCells;
        int tailLength = Math.max(3, Math.min(10, cols / 3));
        for (int i = 0; i < totalCells; i++) {
            int col = i % cols;
            int row = i / cols;
            int bg = BG_IDLE;
            int distance = (head - i + totalCells) % totalCells;
            if (distance == 0) {
                bg = BG_RED;
            } else if (distance <= tailLength) {
                float k = 1f - (distance / (float) (tailLength + 1));
                bg = lerpColor(BG_IDLE, 0xFFB000, k);
            }
            buffer.set(col, row, ' ', 0xFFFFFF, bg);
        }
    }

    private static int lerpColor(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = a & 0xFF;
        int br = (b >> 16) & 0xFF;
        int bg = (b >> 8) & 0xFF;
        int bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
