package com.algoblock.gl.renderer;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.glViewport;

public class TextRenderer {
    private final FontAtlas fontAtlas;
    private int viewportWidth = 1280;
    private int viewportHeight = 720;
    private boolean fontDiagnosticMode = false;
    private long lastDiagLogMs = 0L;

    public TextRenderer(FontAtlas fontAtlas) {
        this.fontAtlas = fontAtlas;
    }

    public FontAtlas fontAtlas() {
        return fontAtlas;
    }

    public void setViewport(int width, int height) {
        viewportWidth = Math.max(1, width);
        viewportHeight = Math.max(1, height);
    }

    public void setFontDiagnosticMode(boolean enabled) {
        this.fontDiagnosticMode = enabled;
    }

    public int visibleCols() {
        return Math.max(1, (int) Math.floor(viewportWidth / cellWidthPx()));
    }

    public int visibleRows() {
        return Math.max(1, (int) Math.floor(viewportHeight / cellHeightPx()));
    }

    public void upload(TerminalBuffer buffer) {
        glViewport(0, 0, viewportWidth, viewportHeight);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, viewportWidth, viewportHeight, 0.0, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        TerminalBuffer.Cell[] cells = buffer.cells();
        int cols = buffer.cols();
        int rows = buffer.rows();
        float marginX = 0f;
        float marginY = 0f;
        float cellHeight = cellHeightPx();
        float cellWidth = cellWidthPx();
        // float halfCellWidth = cellWidth * 0.5f;
        float baselineBias = Math.max(0f, (cellHeight - fontAtlas.lineHeightPx()) * 0.5f);

        for (TerminalBuffer.Cell cell : cells) {
            if (cell.c() != ' ') {
                fontAtlas.glyphFor(cell.c());
            }
        }

        glDisable(GL_TEXTURE_2D);
        glBegin(GL_QUADS);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                TerminalBuffer.Cell cell = cells[row * cols + col];
                float x = marginX + col * cellWidth;
                float y = marginY + row * cellHeight;
                int bg = cell.bg();
                glColor3f(((bg >> 16) & 0xFF) / 255f, ((bg >> 8) & 0xFF) / 255f, (bg & 0xFF) / 255f);
                glVertex2f(x, y);
                glVertex2f(x + cellWidth, y);
                glVertex2f(x + cellWidth, y + cellHeight);
                glVertex2f(x, y + cellHeight);
            }
        }
        glEnd();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_TEXTURE_2D);
        fontAtlas.bindTexture();
        glBegin(GL_QUADS);
        int nonSpaceCount = 0;
        int drawnGlyphCount = 0;
        int skippedNoBitmapCount = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                TerminalBuffer.Cell cell = cells[row * cols + col];
                char ch = cell.c();
                if (ch == ' ' || ch == '\0') {
                    continue;
                }
                nonSpaceCount++;
                int codePoint = ch;
                FontAtlas.GlyphInfo glyph = fontAtlas.glyphFor(codePoint);
                if (!glyph.hasBitmap()) {
                    skippedNoBitmapCount++;
                    continue;
                }

                boolean wide = isWideCodePoint(codePoint);
                float targetAdvance = wide ? (cellWidth * 2f) : cellWidth;
                float glyphScale = targetAdvance / Math.max(1f, glyph.advancePx());
                float glyphW = glyph.bitmapWidth() * glyphScale;
                float glyphH = glyph.bitmapHeight() * glyphScale;

                float cellX = marginX + col * cellWidth;
                float cellY = marginY + row * cellHeight;
                // If it's a wide character, it spans 2 cells, so we offset based on the 2-cell
                // width.
                float slotOffset = (targetAdvance - targetAdvance) * 0.5f; // which is 0
                float x0 = cellX + slotOffset + glyph.xOffset() * glyphScale;
                float baselineY = cellY + baselineBias + fontAtlas.ascentPx();
                float y0 = baselineY + glyph.yOffset() * glyphScale;
                float x1 = x0 + glyphW;
                float y1 = y0 + glyphH;

                int fg = cell.fg();
                glColor4f(((fg >> 16) & 0xFF) / 255f, ((fg >> 8) & 0xFF) / 255f, (fg & 0xFF) / 255f, 1f);
                glTexCoord2f(glyph.u0(), glyph.v0());
                glVertex2f(x0, y0);
                glTexCoord2f(glyph.u1(), glyph.v0());
                glVertex2f(x1, y0);
                glTexCoord2f(glyph.u1(), glyph.v1());
                glVertex2f(x1, y1);
                glTexCoord2f(glyph.u0(), glyph.v1());
                glVertex2f(x0, y1);
                drawnGlyphCount++;
            }
        }
        glEnd();
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);

        if (fontDiagnosticMode) {
            long now = System.currentTimeMillis();
            if (now - lastDiagLogMs >= 1000L) {
                lastDiagLogMs = now;
                System.out.printf("[TEXT-DIAG] nonSpace=%d drawn=%d skippedNoBitmap=%d%n",
                        nonSpaceCount, drawnGlyphCount, skippedNoBitmapCount);
            }
        }
    }

    public void draw() {
    }

    private float cellHeightPx() {
        return Math.max(10f, fontAtlas.lineHeightPx() + 4f);
    }

    private float cellWidthPx() {
        // Now cellWidth is the half-width (English character width)
        return Math.max(4f, (fontAtlas.cjkAdvancePx() * 0.5f) + 1f);
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
