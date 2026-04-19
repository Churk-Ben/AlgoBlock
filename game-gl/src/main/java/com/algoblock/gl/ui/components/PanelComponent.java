package com.algoblock.gl.ui.components;

import com.algoblock.gl.renderer.TerminalBuffer;

public class PanelComponent {

    /**
     * Draws a styled box with rounded corners and fills the background.
     * Useful for popup windows or isolating content blocks.
     */
    public static void drawBox(TerminalBuffer buffer, int x, int y, int width, int height, int fg, int bg) {
        if (width <= 0 || height <= 0)
            return;

        // Draw corners
        buffer.set(x, y, '╭', fg, bg);
        buffer.set(x + width - 1, y, '╮', fg, bg);
        buffer.set(x, y + height - 1, '╰', fg, bg);
        buffer.set(x + width - 1, y + height - 1, '╯', fg, bg);

        // Draw top and bottom borders
        for (int i = 1; i < width - 1; i++) {
            buffer.set(x + i, y, '─', fg, bg);
            buffer.set(x + i, y + height - 1, '─', fg, bg);
        }

        // Draw left and right borders, and fill background
        for (int j = 1; j < height - 1; j++) {
            buffer.set(x, y + j, '│', fg, bg);
            buffer.set(x + width - 1, y + j, '│', fg, bg);
            for (int i = 1; i < width - 1; i++) {
                buffer.set(x + i, y + j, ' ', fg, bg);
            }
        }
    }

    /**
     * Draws a styled box with a title centered on the top border.
     */
    public static void drawBoxWithTitle(TerminalBuffer buffer, int x, int y, int width, int height, String title,
            int fg, int bg, int titleFg) {
        drawBox(buffer, x, y, width, height, fg, bg);
        if (title != null && !title.isEmpty()) {
            int titleX = x + (width - title.length()) / 2;
            buffer.print(titleX, y, title, titleFg, bg);
        }
    }
}
