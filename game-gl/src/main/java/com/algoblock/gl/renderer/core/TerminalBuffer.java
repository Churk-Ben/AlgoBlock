package com.algoblock.gl.renderer.core;

import java.util.Arrays;
import com.algoblock.gl.utils.TextUtil;

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

    public synchronized void set(int col, int row, char c, int fg, int bg) {
        if (col < 0 || row < 0 || col >= cols || row >= rows) {
            return;
        }
        cells[row * cols + col] = new Cell(c, fg, bg);
    }

    public synchronized void print(int col, int row, String text, int fg, int bg) {
        int cursor = col;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            set(cursor, row, c, fg, bg);
            cursor++;
            if (TextUtil.isWideCodePoint(c)) {
                set(cursor, row, '\0', fg, bg);
                cursor++;
            }
        }
    }

    public synchronized void clear() {
        Arrays.fill(cells, new Cell(' ', 0xCDD9E5, 0x0D1117));
    }

    public synchronized Cell[] cells() {
        return Arrays.copyOf(cells, cells.length);
    }
}
