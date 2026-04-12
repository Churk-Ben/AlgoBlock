package com.algoblock.gl.renderer;

import java.util.Arrays;

public class TerminalBuffer {
    public record Cell(char c, int fg, int bg) {}

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
        for (int i = 0; i < text.length(); i++) {
            set(col + i, row, text.charAt(i), fg, bg);
        }
    }

    public void clear() {
        Arrays.fill(cells, new Cell(' ', 0xCDD9E5, 0x0D1117));
    }

    public Cell[] cells() {
        return cells;
    }
}
