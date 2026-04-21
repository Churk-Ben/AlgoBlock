package com.algoblock.gl.ui.pages.diagnostics;

import com.algoblock.gl.renderer.core.TerminalBuffer;
import com.algoblock.gl.utils.TextUtil;

public class FontDiagnosticTestPattern {
    private static final int BG_IDLE = 0x0D1117; // 背景色
    // private static final int GREY_A = 0x555555; // 灰度色01
    private static final int GREY_B = 0x888888; // 灰度色02
    private static final int GREY_C = 0xFFFFFF; // 文本颜色
    private static final int CURSOR = 0x22CC22; // 游标颜色

    public void renderTo(TerminalBuffer buffer, double timeSeconds) {
        int cols = buffer.cols();
        int rows = buffer.rows();

        // Fill background
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                buffer.set(col, row, ' ', GREY_C, BG_IDLE);
            }
        }

        putLine(buffer, 1, "     === Font Diagnostic Test ===", CURSOR, BG_IDLE);
        putLine(buffer, 3, "     ASCII:   ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789", GREY_C, BG_IDLE);
        putLine(buffer, 4, "     Symbols: <> () {} [] @#%&*!? .,:;|\\/~`", GREY_C, BG_IDLE);
        putLine(buffer, 5, "     CJK:     中文测试 汉字宽度 对齐 验证", GREY_C, BG_IDLE);
        putLine(buffer, 6, "     Mix:     A中B文C汉D字E", GREY_C, BG_IDLE);

        putLine(buffer, 8, "     Check stdout [FONT-DIAG] for missing glyphs.", GREY_B, BG_IDLE);
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
            if (TextUtil.isWideCodePoint(c) && cursor < buffer.cols()) {
                buffer.set(cursor, row, '\0', fg, bg);
                cursor++;
            }
        }
        for (int i = cursor; i < buffer.cols(); i++) {
            buffer.set(i, row, ' ', fg, bg);
        }
    }
}
