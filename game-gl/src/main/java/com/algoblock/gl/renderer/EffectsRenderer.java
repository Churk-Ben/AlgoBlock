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

public class EffectsRenderer {
    public void draw(RenderFrame frame, TextRenderer textRenderer, double timeSeconds) {
        if (frame == null || frame.effectStrength() <= 0f) {
            return;
        }

        int viewportWidth = textRenderer.viewportWidth();
        int viewportHeight = textRenderer.viewportHeight();
        glViewport(0, 0, viewportWidth, viewportHeight);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, viewportWidth, viewportHeight, 0.0, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        float strength = frame.effectStrength();
        float animated = (float) ((Math.sin(timeSeconds * 1.7) + 1.0) * 0.5);
        float stripeAlpha = Math.max(0.02f, strength * 0.12f);
        float vignetteAlpha = Math.max(0.03f, strength * 0.16f) * (0.7f + animated * 0.3f);
        float stripeStep = Math.max(2f, textRenderer.cellHeightPx() * 0.75f);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBegin(GL_QUADS);
        glColor4f(0f, 0f, 0f, stripeAlpha);
        for (float y = 0; y < viewportHeight; y += stripeStep * 2f) {
            glVertex2f(0f, y);
            glVertex2f(viewportWidth, y);
            glVertex2f(viewportWidth, Math.min(viewportHeight, y + stripeStep));
            glVertex2f(0f, Math.min(viewportHeight, y + stripeStep));
        }

        float edge = Math.max(24f, Math.min(viewportWidth, viewportHeight) * 0.08f);
        glColor4f(0f, 0f, 0f, vignetteAlpha);
        glVertex2f(0f, 0f);
        glVertex2f(viewportWidth, 0f);
        glVertex2f(viewportWidth, edge);
        glVertex2f(0f, edge);

        glVertex2f(0f, viewportHeight - edge);
        glVertex2f(viewportWidth, viewportHeight - edge);
        glVertex2f(viewportWidth, viewportHeight);
        glVertex2f(0f, viewportHeight);

        glVertex2f(0f, 0f);
        glVertex2f(edge, 0f);
        glVertex2f(edge, viewportHeight);
        glVertex2f(0f, viewportHeight);

        glVertex2f(viewportWidth - edge, 0f);
        glVertex2f(viewportWidth, 0f);
        glVertex2f(viewportWidth, viewportHeight);
        glVertex2f(viewportWidth - edge, viewportHeight);
        glEnd();

        glDisable(GL_BLEND);
    }
}
