package com.algoblock.gl.ui.tea;

import com.algoblock.core.engine.SubmissionResult;
import com.algoblock.gl.renderer.RenderFrame;
import com.algoblock.gl.renderer.TerminalBuffer;
import com.algoblock.gl.ui.SyntaxHighlighter;

public class UiView {
    private static final int BG = 0x0D1117;
    private static final int FG = 0xCDD9E5;
    private final SyntaxHighlighter highlighter = new SyntaxHighlighter();

    public RenderFrame render(UiModel model, TerminalBuffer buffer, long nowMillis) {
        buffer.clear();
        buffer.print(0, 0, "Level " + model.level().id() + " - " + model.level().title(), 0x6CB6FF, BG);
        buffer.print(0, 1, model.level().story(), 0x9FB3C8, BG);
        buffer.print(0, 3, "> ", FG, BG);
        highlighter.highlight(buffer, 2, 3, model.line());

        int row = 5;
        for (String suggestion : model.suggestions().stream().limit(6).toList()) {
            if (row >= buffer.rows()) {
                break;
            }
            buffer.print(0, row++, suggestion, 0x3FB950, BG);
        }

        SubmissionResult lastResult = model.lastResult();
        if (lastResult != null) {
            if (7 < buffer.rows()) {
                int color = lastResult.accepted() ? 0x3FB950 : 0xFF7B72;
                buffer.print(0, 7, "Result: " + lastResult.message(), color, BG);
            }
            if (8 < buffer.rows()) {
                buffer.print(0, 8, "Stars: " + lastResult.score().stars(), 0xE3B341, BG);
            }
        }

        int cursorCol = Math.min(buffer.cols() - 1, Math.max(0, 2 + visualOffset(model.line(), model.cursorIndex())));
        int cursorRow = Math.min(buffer.rows() - 1, 3);
        boolean forceSolidVisible = nowMillis < model.cursorSolidUntilMillis();
        boolean blinkVisible = forceSolidVisible || ((nowMillis / 500L) % 2L) == 0L;
        return new RenderFrame(buffer, cursorCol, cursorRow, blinkVisible, true, 0x79C0FF, 0.20f);
    }

    private static int visualOffset(String line, int cursorIndex) {
        int max = Math.max(0, Math.min(cursorIndex, line.length()));
        int width = 0;
        for (int i = 0; i < max; i++) {
            width += isWideCodePoint(line.charAt(i)) ? 2 : 1;
        }
        return width;
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
