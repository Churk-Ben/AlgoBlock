package com.algoblock.gl.renderer.effect;

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

import com.algoblock.gl.renderer.core.RenderFrame;
import com.algoblock.gl.renderer.text.TextRenderer;

public class EffectsRenderer {
    private int screenTexture = 0;

    public void draw(RenderFrame frame, TextRenderer textRenderer, double timeSeconds) {
        if (frame == null || frame.effects() == null || frame.effects().isEmpty()) {
            return;
        }

        float crtStrength = 0f;
        GlitchState glitch = null;
        for (UiEffect effect : frame.effects()) {
            if (effect instanceof UiEffect.Crt crt) {
                crtStrength = Math.max(crtStrength, crt.strength());
            } else if (effect instanceof UiEffect.Glitch glitchEffect) {
                glitch = glitchEffect.state();
            }
        }
        if (crtStrength <= 0f && glitch == null) {
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

        if (crtStrength > 0f) {
            float animated = (float) ((Math.sin(timeSeconds * 1.7) + 1.0) * 0.5);
            // Make stripe (scanline) effect more prominent
            float stripeAlpha = Math.max(0.05f, crtStrength * 0.25f);
            float vignetteAlpha = Math.max(0.03f, crtStrength * 0.16f) * (0.7f + animated * 0.3f);
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

        // --- Glitch Effect Logic ---
        if (glitch != null) {
            if (screenTexture == 0) {
                screenTexture = org.lwjgl.opengl.GL11.glGenTextures();
                org.lwjgl.opengl.GL11.glBindTexture(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, screenTexture);
                org.lwjgl.opengl.GL11.glTexParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                        org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER, org.lwjgl.opengl.GL11.GL_NEAREST);
                org.lwjgl.opengl.GL11.glTexParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                        org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER, org.lwjgl.opengl.GL11.GL_NEAREST);
                org.lwjgl.opengl.GL11.glTexParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                        org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S, org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE);
                org.lwjgl.opengl.GL11.glTexParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                        org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T, org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE);
                org.lwjgl.opengl.GL11.glBindTexture(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, 0);
            }

            org.lwjgl.opengl.GL11.glBindTexture(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, screenTexture);
            // Copy current screen content into texture
            org.lwjgl.opengl.GL11.glCopyTexImage2D(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, 0, org.lwjgl.opengl.GL11.GL_RGB,
                    0, 0, viewportWidth, viewportHeight, 0);

            org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_BLEND);

            // Convert normalized coordinates to pixel coordinates
            float gy1 = glitch.y1() * viewportHeight;
            float gh1 = glitch.h1() * viewportHeight;
            float gy2 = glitch.y2() * viewportHeight;
            float gh2 = glitch.h2() * viewportHeight;

            drawGlitchStrip(viewportWidth, viewportHeight, gy1, gh1, glitch.offset1());
            drawGlitchStrip(viewportWidth, viewportHeight, gy2, gh2, glitch.offset2());

            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
        }
    }

    private void drawGlitchStrip(int vw, int vh, float sy, float sh, float offset) {
        float vTop = (vh - sy) / (float) vh;
        float vBottom = (vh - (sy + sh)) / (float) vh;

        // Chromatic aberration by rendering channels with slight horizontal offsets
        // Red channel
        org.lwjgl.opengl.GL11.glColorMask(true, false, false, true);
        drawStripQuad(vw, vh, sy, sh, offset + 4f, vTop, vBottom);

        // Green channel
        org.lwjgl.opengl.GL11.glColorMask(false, true, false, true);
        drawStripQuad(vw, vh, sy, sh, offset, vTop, vBottom);

        // Blue channel
        org.lwjgl.opengl.GL11.glColorMask(false, false, true, true);
        drawStripQuad(vw, vh, sy, sh, offset - 4f, vTop, vBottom);

        // Restore color mask
        org.lwjgl.opengl.GL11.glColorMask(true, true, true, true);
    }

    private void drawStripQuad(int vw, int vh, float sy, float sh, float offset, float vTop, float vBottom) {
        org.lwjgl.opengl.GL11.glColor4f(1f, 1f, 1f, 1f);
        org.lwjgl.opengl.GL11.glBegin(org.lwjgl.opengl.GL11.GL_QUADS);

        org.lwjgl.opengl.GL11.glTexCoord2f(0f, vTop);
        org.lwjgl.opengl.GL11.glVertex2f(offset, sy);

        org.lwjgl.opengl.GL11.glTexCoord2f(1f, vTop);
        org.lwjgl.opengl.GL11.glVertex2f(vw + offset, sy);

        org.lwjgl.opengl.GL11.glTexCoord2f(1f, vBottom);
        org.lwjgl.opengl.GL11.glVertex2f(vw + offset, sy + sh);

        org.lwjgl.opengl.GL11.glTexCoord2f(0f, vBottom);
        org.lwjgl.opengl.GL11.glVertex2f(offset, sy + sh);

        org.lwjgl.opengl.GL11.glEnd();
    }
}
