package com.algoblock.gl.renderer;

public class FontDiagnosticTestPattern {
    public void renderTo(TerminalBuffer buffer, double timeSeconds) {
        buffer.clear();
        int cols = buffer.cols();
        int rows = buffer.rows();
        int wave = ((int) (timeSeconds * 2.0)) & 1;
        putLine(buffer, 0, "[Title] Text Test Pattern", 0xF8FCFF, 0x304A6E);
        putLine(buffer, 1, "ASCII:  ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789 +-*/=_[]{}", 0xFFD580, 0x1B1F24);
        putLine(buffer, 2, "latin:  MapleMono test -> TheQuickBrownFox_jumps_42", 0x9FE6A0, 0x1B1F24);
        putLine(buffer, 3, "CJK:    中文测试 汉字宽度 对齐 验证 你好世界", 0x7CC7FF, 0x1B1F24);
        putLine(buffer, 4, "mix:    A中B文C汉D字E  2:1 width visual check", 0xE7B4FF, 0x1B1F24);
        putLine(buffer, 5, "symbol: <> () {} [] @#%&*!? .,:;|\\/~`", 0xFF9AA2, 0x1B1F24);
        putLine(buffer, 7, "If only background shows: check stdout [FONT-DIAG] glyph lines.", 0xFFE48A, 0x2A1F1A);
        for (int row = 8; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int shade = ((row + col + wave) & 1) == 0 ? 0x101010 : 0x202020;
                buffer.set(col, row, ' ', 0xFFFFFF, shade);
            }
        }
    }

    private void putLine(TerminalBuffer buffer, int row, String text, int fg, int bg) {
        if (row < 0 || row >= buffer.rows()) {
            return;
        }
        int cursor = 0;
        for (int i = 0; i < text.length() && cursor < buffer.cols(); i++) {
            char c = text.charAt(i);
            buffer.set(cursor, row, c, fg, bg);
            cursor++;
            if (isWideCodePoint(c) && cursor < buffer.cols()) {
                buffer.set(cursor, row, '\0', fg, bg);
                cursor++;
            }
        }
        for (int i = cursor; i < buffer.cols(); i++) {
            buffer.set(i, row, ' ', fg, bg);
        }
    }

    private boolean isWideCodePoint(int codePoint) {
        return (codePoint >= 0x1100 && codePoint <= 0x115F)
                || (codePoint >= 0x2E80 && codePoint <= 0xA4CF)
                || (codePoint >= 0xAC00 && codePoint <= 0xD7A3)
                || (codePoint >= 0xF900 && codePoint <= 0xFAFF)
                || (codePoint >= 0xFE10 && codePoint <= 0xFE6F)
                || (codePoint >= 0xFF00 && codePoint <= 0xFF60)
                || (codePoint >= 0xFFE0 && codePoint <= 0xFFE6);
    }
}
