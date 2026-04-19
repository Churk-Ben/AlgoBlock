package com.algoblock.gl.ui.pages.diagnostics;

import com.algoblock.gl.renderer.core.TerminalBuffer;

public class FontDiagnosticTestPattern {
    public void renderTo(TerminalBuffer buffer, double timeSeconds) {
        int cols = buffer.cols();
        int rows = buffer.rows();

        // Fill background
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                buffer.set(col, row, ' ', 0xFFFFFF, 0x0D1117);
            }
        }

        putLine(buffer, 1, "     === Font Diagnostic Test ===", 0x22CC22, 0x0D1117);
        putLine(buffer, 3, "     ASCII:   ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789", 0xFFFFFF, 0x0D1117);
        putLine(buffer, 4, "     Symbols: <> () {} [] @#%&*!? .,:;|\\/~`", 0xFFFFFF, 0x0D1117);
        putLine(buffer, 5, "     CJK:     中文测试 汉字宽度 对齐 验证", 0xFFFFFF, 0x0D1117);
        putLine(buffer, 6, "     Mix:     A中B文C汉D字E", 0xFFFFFF, 0x0D1117);

        putLine(buffer, 8, "     Check stdout [FONT-DIAG] for missing glyphs.", 0x888888, 0x0D1117);
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
