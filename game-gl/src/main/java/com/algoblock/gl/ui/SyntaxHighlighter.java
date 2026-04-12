package com.algoblock.gl.ui;

import com.algoblock.gl.renderer.TerminalBuffer;

public class SyntaxHighlighter {
    public void highlight(TerminalBuffer buffer, int row, String source) {
        int col = 0;
        for (char c : source.toCharArray()) {
            int fg = Character.isLetter(c) || c == '_' ? 0x79C0FF : 0xCDD9E5;
            buffer.set(col++, row, c, fg, 0x0D1117);
        }
    }
}
