package com.algoblock.gl.ui.components;

import com.algoblock.gl.renderer.core.TerminalBuffer;
import com.algoblock.gl.utils.TextUtil;

public class PanelComponent {

    /**
     * Draws a styled box with rounded corners and fills the background.
     * Useful for popup windows or isolating content blocks.
     */
    public static void drawBox(TerminalBuffer buffer, int x, int y, int width, int height, int fg, int bg) {
        if (width <= 0 || height <= 0)
            return;

        // Draw corners
        // buffer.set(x, y, '╭', fg, bg);
        // buffer.set(x + width - 1, y, '╮', fg, bg);
        // buffer.set(x, y + height - 1, '╰', fg, bg);
        // buffer.set(x + width - 1, y + height - 1, '╯', fg, bg);
        buffer.set(x, y, '┌', fg, bg);
        buffer.set(x + width - 1, y, '┐', fg, bg);
        buffer.set(x, y + height - 1, '└', fg, bg);
        buffer.set(x + width - 1, y + height - 1, '┘', fg, bg);

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
            int titleWidth = TextUtil.getDisplayWidth(title);
            int titleX = x + (width - titleWidth) / 2;
            buffer.print(titleX, y, title, titleFg, bg);
        }
    }

    /**
     * Draws left-aligned options within a panel, reserving space on the left for a
     * cursor.
     * The entire block of options is centered horizontally within the specified box
     * width,
     * but the text itself is left-aligned (baseline alignment).
     * Useful for tab menus or settings lists.
     *
     * @param buffer              The terminal buffer to draw on.
     * @param boxX                The X coordinate of the box's left border.
     * @param boxWidth            The total width of the box.
     * @param startY              The Y coordinate where the first option will be
     *                            drawn.
     * @param options             The list of string options to display.
     * @param selectedIndex       The index of the currently selected option.
     * @param lineSpacing         The vertical spacing between options (e.g., 1 for
     *                            no gap, 2 for one blank line).
     * @param cursorReservedSpace The number of columns to reserve on the left for
     *                            the cursor (e.g., 2).
     * @param normalFg            The foreground color for unselected options.
     * @param selectedFg          The foreground color for the selected option.
     * @param bg                  The background color for all options.
     * @return an array containing {cursorCol, cursorRow} of the currently selected
     *         option.
     */
    public static int[] drawLeftAlignedOptions(TerminalBuffer buffer, int boxX, int boxWidth, int startY,
            String[] options, int selectedIndex, int lineSpacing, int cursorReservedSpace,
            int normalFg, int selectedFg, int bg) {
        int cursorCol = -1;
        int cursorRow = -1;

        // Calculate maximum option length to center the left-aligned block
        int maxOptLen = 0;
        for (String opt : options) {
            maxOptLen = Math.max(maxOptLen, TextUtil.getDisplayWidth(opt));
        }

        // Total width of the text block = reserved space for cursor + max text length
        int blockWidth = cursorReservedSpace + maxOptLen;
        // Start X for the entire block (centered within the box)
        int blockStartX = boxX + (boxWidth - blockWidth) / 2;
        // The text starts after the reserved space
        int textStartX = blockStartX + cursorReservedSpace;

        for (int i = 0; i < options.length; i++) {
            String text = options[i];
            int textRow = startY + i * lineSpacing;

            if (i == selectedIndex) {
                buffer.print(textStartX, textRow, text, selectedFg, bg);
                cursorCol = blockStartX; // Position cursor in the reserved space
                cursorRow = textRow;
            } else {
                buffer.print(textStartX, textRow, text, normalFg, bg);
            }
        }

        return new int[] { cursorCol, cursorRow };
    }
}