package com.algoblock.gl.renderer;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.glViewport;

public class CursorRenderer {
    public void draw(RenderFrame frame, TextRenderer textRenderer) {
        if (frame == null || !frame.cursorVisible()) {
            return;
        }

        glViewport(0, 0, textRenderer.viewportWidth(), textRenderer.viewportHeight());
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, textRenderer.viewportWidth(), textRenderer.viewportHeight(), 0.0, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        float cellWidth = textRenderer.cellWidthPx();
        float cellHeight = textRenderer.cellHeightPx();
        float x = frame.cursorCol() * cellWidth;
        float y = frame.cursorRow() * cellHeight;
        float w = frame.cursorBlockStyle() ? cellWidth : Math.max(2.0f, cellWidth * 0.15f);
        int color = frame.cursorColor();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBegin(GL_QUADS);
        glColor4f(
                ((color >> 16) & 0xFF) / 255f,
                ((color >> 8) & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                frame.cursorBlockStyle() ? 0.35f : 0.9f);
        glVertex2f(x, y);
        glVertex2f(x + w, y);
        glVertex2f(x + w, y + cellHeight);
        glVertex2f(x, y + cellHeight);
        glEnd();
        glDisable(GL_BLEND);
    }
}
