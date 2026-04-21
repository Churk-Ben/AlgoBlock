package com.algoblock.gl.ui.pages.diagnostics;

import com.algoblock.gl.renderer.core.RenderFrame;
import com.algoblock.gl.renderer.core.TerminalBuffer;
import com.algoblock.gl.renderer.cursor.CursorState;

public class DisplayTestPattern {
    // 棋盘测试测试
    private static final double CHECKERBOARD_VARIANT_SECONDS = 1.0;
    private static final int CHECKERBOARD_VARIANTS = 4;

    // 全屏颜色测试
    private static final double FULL_RED_SECONDS = 1.0;
    private static final double FULL_GREEN_SECONDS = 1.0;
    private static final double FULL_BLUE_SECONDS = 1.0;

    // 游标跳转测试
    private static final double ROLLING_SECONDS = 4.0;

    // 总测试时间
    private static final double CHECKERBOARD_SECONDS = CHECKERBOARD_VARIANT_SECONDS * CHECKERBOARD_VARIANTS;
    private static final double TOTAL_SECONDS = CHECKERBOARD_SECONDS + FULL_RED_SECONDS +
            FULL_GREEN_SECONDS + FULL_BLUE_SECONDS + ROLLING_SECONDS;

    private static final int BG_IDLE = 0x0D1117; // 背景色
    private static final int GREY_A = 0x555555; // 灰度色01
    private static final int GREY_B = 0x888888; // 灰度色02
    private static final int GREY_C = 0xFFFFFF; // 文本颜色
    private static final int BG_RED = 0xFF0000; // 红色
    private static final int BG_GREEN = 0x00FF00; // 绿色
    private static final int BG_BLUE = 0x0000FF; // 蓝色
    private static final int CURSOR = 0x22CC22; // 游标颜色

    private double startTime = -1.0;

    public void reset() {
        startTime = -1.0;
    }

    public RenderFrame renderTo(TerminalBuffer buffer, double timeSeconds) {
        if (startTime < 0) {
            startTime = timeSeconds;
        }
        double t = normalize(timeSeconds - startTime);
        int cols = buffer.cols();
        int rows = buffer.rows();

        // Checkerboard test
        if (t < CHECKERBOARD_SECONDS) {
            int variant = Math.min(CHECKERBOARD_VARIANTS - 1, (int) (t / CHECKERBOARD_VARIANT_SECONDS));
            renderCheckerboard(buffer, cols, rows, variant);
            return null;
        }
        t -= CHECKERBOARD_SECONDS;

        // Full screen color test
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

        if (t < FULL_BLUE_SECONDS) {
            fill(buffer, cols, rows, BG_BLUE);
            return null;
        }
        t -= FULL_BLUE_SECONDS;

        // Cursor jump test
        fill(buffer, cols, rows, BG_IDLE);
        int col = 0;
        int row = 0;
        int phase = (int) (t * 2.0) % 4;

        switch (phase) {
            case 0:
                col = 4;
                row = 4;
                break;
            case 1:
                col = cols - 5;
                row = 4;
                break;
            case 2:
                col = 4;
                row = rows - 5;
                break;
            default:
                col = cols - 5;
                row = rows - 5;
                break;
        }

        CursorState cursor = new CursorState(col, row, true, true, CURSOR);
        return new RenderFrame(buffer, cursor, null);
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
                int logicalCol = col / 2;
                boolean evenCell = ((row + logicalCol) & 1) == 0;
                if (inverted) {
                    evenCell = !evenCell;
                }
                int bg = evenCell ? BG_IDLE : GREY_A;
                int fg = GREY_C;

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
                buffer.set(col, row, ' ', GREY_B, bg); // 其实这个颜色没有用...
            }
        }
    }
}
