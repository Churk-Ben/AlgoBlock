package com.algoblock.gl.ui.tea;

import com.algoblock.core.engine.SubmissionResult;
import com.algoblock.gl.renderer.RenderFrame;
import com.algoblock.gl.renderer.TerminalBuffer;
import com.algoblock.gl.ui.SyntaxHighlighter;
import java.util.Random;

public class UiView {
    private static final int BG = 0x0D1117;
    private static final int FG = 0xCDD9E5;
    private final SyntaxHighlighter highlighter = new SyntaxHighlighter();
    private final CMatrixEffect cmatrix = new CMatrixEffect();

    private static final String[] TITLE_ART = {
            "   _____  .__                __________.__                 __      ",
            "  /  _  \\ |  |   ____  ____  \\______   \\  |   ____   ____ |  | __  ",
            " /  /_\\  \\|  |  / ___\\/  _ \\  |    |  _/  |  /  _ \\_/ ___\\|  |/ /  ",
            "/    |    \\  |_/ /_/  >  <_> )|    |   \\  |_(  <_> )  \\___|    <   ",
            "\\____|__  /____/\\___  /\\____/ |______  /____/\\____/ \\___  >__|_ \\  ",
            "        \\/     /_____/               \\/                 \\/     \\/  "
    };

    public RenderFrame render(UiModel model, TerminalBuffer buffer, long nowMillis) {
        buffer.clear();

        if (model.screen() == UiModel.Screen.START) {
            return renderStartScreen(buffer, nowMillis);
        }

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

    private RenderFrame renderStartScreen(TerminalBuffer buffer, long nowMillis) {
        cmatrix.update(buffer.cols(), buffer.rows(), nowMillis);
        cmatrix.render(buffer);

        int rows = buffer.rows();
        int cols = buffer.cols();

        // Draw title
        int titleStartRow = rows / 4;
        for (int i = 0; i < TITLE_ART.length; i++) {
            String line = TITLE_ART[i];
            int titleStartCol = (cols - line.length()) / 2;
            buffer.print(Math.max(0, titleStartCol), titleStartRow + i, line, 0x00FF00, BG);
        }

        // Draw options
        int optionsStartRow = titleStartRow + TITLE_ART.length + 3;
        String startText = "> Press ENTER to Start Game <";
        int startCol = (cols - startText.length()) / 2;
        boolean blinkVisible = ((nowMillis / 500L) % 2L) == 0L;
        if (blinkVisible) {
            buffer.print(Math.max(0, startCol), optionsStartRow, startText, 0xFFFFFF, BG);
        }

        String placeholderText = "  [ Options (WIP) ]  ";
        int placeholderCol = (cols - placeholderText.length()) / 2;
        buffer.print(Math.max(0, placeholderCol), optionsStartRow + 2, placeholderText, 0x888888, BG);

        return new RenderFrame(buffer, -1, -1, false, false, 0, 0f);
    }

    private static class CMatrixEffect {
        private final Random random = new Random();
        private long lastUpdate = 0;
        private Drop[] drops;

        private static class Drop {
            float y;
            float speed;
            int length;
            char[] chars;
        }

        public void update(int cols, int rows, long nowMillis) {
            if (drops == null || drops.length != cols) {
                drops = new Drop[cols];
                for (int i = 0; i < cols; i++) {
                    drops[i] = createDrop(rows);
                    drops[i].y = random.nextInt(rows); // random initial positions
                }
            }

            long dt = nowMillis - lastUpdate;
            if (lastUpdate == 0)
                dt = 0;
            lastUpdate = nowMillis;

            for (int i = 0; i < cols; i++) {
                Drop drop = drops[i];
                drop.y += drop.speed * (dt / 1000f);
                if (drop.y - drop.length > rows) {
                    drops[i] = createDrop(rows);
                }
                // Randomly change some characters
                if (random.nextFloat() < 0.1f) {
                    drop.chars[random.nextInt(drop.length)] = getRandomChar();
                }
            }
        }

        public void render(TerminalBuffer buffer) {
            int rows = buffer.rows();
            int cols = buffer.cols();
            for (int i = 0; i < cols; i++) {
                Drop drop = drops[i];
                int headY = (int) drop.y;
                for (int j = 0; j < drop.length; j++) {
                    int y = headY - j;
                    if (y >= 0 && y < rows) {
                        int color = (j == 0) ? 0xFFFFFF : 0x00FF00;
                        if (j > drop.length - 3) {
                            color = 0x008800; // fade out tail
                        }
                        buffer.print(i, y, String.valueOf(drop.chars[j]), color, BG);
                    }
                }
            }
        }

        private Drop createDrop(int rows) {
            Drop drop = new Drop();
            drop.y = -random.nextInt(10);
            drop.speed = 10 + random.nextFloat() * 15;
            drop.length = 5 + random.nextInt(15);
            drop.chars = new char[drop.length];
            for (int i = 0; i < drop.length; i++) {
                drop.chars[i] = getRandomChar();
            }
            return drop;
        }

        private char getRandomChar() {
            // Select a random character from the set of glyphs
            final String glyphs = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
            return glyphs.charAt(random.nextInt(glyphs.length()));
        }
    }
}
