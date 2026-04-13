package com.algoblock.gl.renderer;

import java.util.Arrays;

public class TerminalBuffer {
    public record Cell(char c, int fg, int bg) {
    }

    private final int cols;
    private final int rows;
    private final Cell[] cells;

    public TerminalBuffer(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.cells = new Cell[cols * rows];
        clear();
    }

    public int cols() {
        return cols;
    }

    public int rows() {
        return rows;
    }

    public void set(int col, int row, char c, int fg, int bg) {
        if (col < 0 || row < 0 || col >= cols || row >= rows) {
            return;
        }
        cells[row * cols + col] = new Cell(c, fg, bg);
    }

    public void print(int col, int row, String text, int fg, int bg) {
        int cursor = col;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            set(cursor, row, c, fg, bg);
            cursor++;
            // If the character is wide (CJK), it occupies 2 cells. We place a placeholder in the next cell.
            if (isWideCodePoint(c)) {
                set(cursor, row, '\0', fg, bg);
                cursor++;
            }
        }
    }

    private static boolean isWideCodePoint(int codePoint) {
        return (codePoint >= 0x1100 && codePoint <= 0x115F)
                || (codePoint >= 0x2E80 && codePoint <= 0xA4CF)
                || (codePoint >= 0xAC00 && codePoint <= 0xD7A3)
                || (codePoint >= 0xF900 && codePoint <= 0xFAFF)
                || (codePoint >= 0xFE10 && codePoint <= 0xFE6F)
                || (codePoint >= 0xFF00 && codePoint <= 0xFF60)
                || (codePoint >= 0xFFE0 && codePoint <= 0xFFE6);
    }

    public void clear() {
        Arrays.fill(cells, new Cell(' ', 0xCDD9E5, 0x0D1117));
    }

    public Cell[] cells() {
        return cells;
    }
}
