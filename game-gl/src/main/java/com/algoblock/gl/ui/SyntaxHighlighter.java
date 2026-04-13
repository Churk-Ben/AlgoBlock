package com.algoblock.gl.ui;

import com.algoblock.gl.renderer.TerminalBuffer;

public class SyntaxHighlighter {
    public void highlight(TerminalBuffer buffer, int startCol, int row, String source) {
        int cursor = startCol;
        for (char c : source.toCharArray()) {
            int fg = Character.isLetter(c) || c == '_' ? 0x79C0FF : 0xCDD9E5;
            buffer.set(cursor, row, c, fg, 0x0D1117);
            cursor++;
            if (isWideCodePoint(c)) {
                buffer.set(cursor, row, '\0', fg, 0x0D1117);
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
}
