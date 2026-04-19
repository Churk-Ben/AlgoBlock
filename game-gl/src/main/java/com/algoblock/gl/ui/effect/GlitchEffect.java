package com.algoblock.gl.ui.effect;

import com.algoblock.gl.renderer.effect.GlitchState;
import com.algoblock.gl.renderer.effect.UiEffect;
import com.algoblock.gl.renderer.effect.UiEffectRenderContext;
import com.algoblock.gl.renderer.effect.UiEffectRenderer;

public class GlitchEffect implements UiEffectRenderer<UiEffect.Glitch> {
    private int screenTexture = 0;
    private double nextGlitchEvalTime = 0;
    private double glitchEndTime = 0;
    private boolean isGlitching = false;
    private float glitchOffset1 = 0;
    private float glitchOffset2 = 0;
    private float glitchY1 = 0;
    private float glitchH1 = 0;
    private float glitchY2 = 0;
    private float glitchH2 = 0;

    private final double glitchEvaluationInterval = 0.5;
    private final float glitchProbability = 0.8f;
    private final float glitchMinDuration = 0.1f;
    private final float glitchMaxDuration = 0.15f;

    private final float glitch1MinHeight = 0.02f;
    private final float glitch1MaxHeight = 0.05f;
    private final float glitch1MaxOffset = 60.0f;
    private final float glitch1MinOffset = -30.0f;

    private final float glitch2MinHeight = 0.01f;
    private final float glitch2MaxHeight = 0.03f;
    private final float glitch2MaxOffset = 40.0f;
    private final float glitch2MinOffset = -20.0f;

    @Override
    public Class<UiEffect.Glitch> effectType() {
        return UiEffect.Glitch.class;
    }

    @Override
    public void render(UiEffect.Glitch effect, UiEffectRenderContext context) {
        GlitchState glitch = effect != null ? effect.state() : null;
        if (glitch == null) {
            return;
        }
        int viewportWidth = context.viewportWidth();
        int viewportHeight = context.viewportHeight();

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
        org.lwjgl.opengl.GL11.glCopyTexImage2D(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, 0, org.lwjgl.opengl.GL11.GL_RGB,
                0, 0, viewportWidth, viewportHeight, 0);

        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_BLEND);

        float gy1 = glitch.y1() * viewportHeight;
        float gh1 = glitch.h1() * viewportHeight;
        float gy2 = glitch.y2() * viewportHeight;
        float gh2 = glitch.h2() * viewportHeight;

        drawGlitchStrip(viewportWidth, viewportHeight, gy1, gh1, glitch.offset1());
        drawGlitchStrip(viewportWidth, viewportHeight, gy2, gh2, glitch.offset2());

        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
    }

    public GlitchState update(long nowMillis) {
        double timeSeconds = nowMillis / 1000.0;

        if (timeSeconds >= nextGlitchEvalTime) {
            nextGlitchEvalTime = timeSeconds + glitchEvaluationInterval;
            if (Math.random() < glitchProbability) {
                isGlitching = true;
                glitchEndTime = timeSeconds + glitchMinDuration + Math.random() * glitchMaxDuration;

                glitchY1 = (float) Math.random();
                glitchH1 = glitch1MinHeight + (float) Math.random() * glitch1MaxHeight;
                glitchOffset1 = (float) (Math.random() * (glitch1MaxOffset - glitch1MinOffset) + glitch1MinOffset);

                glitchY2 = (float) Math.random();
                glitchH2 = glitch2MinHeight + (float) Math.random() * glitch2MaxHeight;
                glitchOffset2 = (float) (Math.random() * (glitch2MaxOffset - glitch2MinOffset) + glitch2MinOffset);
            }
        }

        if (isGlitching && timeSeconds > glitchEndTime) {
            isGlitching = false;
        }

        if (isGlitching) {
            return new GlitchState(glitchY1, glitchH1, glitchOffset1, glitchY2, glitchH2, glitchOffset2);
        }

        return null;
    }

    private void drawGlitchStrip(int vw, int vh, float sy, float sh, float offset) {
        float vTop = (vh - sy) / (float) vh;
        float vBottom = (vh - (sy + sh)) / (float) vh;

        org.lwjgl.opengl.GL11.glColorMask(true, false, false, true);
        drawStripQuad(vw, sy, sh, offset + 4f, vTop, vBottom);

        org.lwjgl.opengl.GL11.glColorMask(false, true, false, true);
        drawStripQuad(vw, sy, sh, offset, vTop, vBottom);

        org.lwjgl.opengl.GL11.glColorMask(false, false, true, true);
        drawStripQuad(vw, sy, sh, offset - 4f, vTop, vBottom);

        org.lwjgl.opengl.GL11.glColorMask(true, true, true, true);
    }

    private void drawStripQuad(int vw, float sy, float sh, float offset, float vTop, float vBottom) {
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
