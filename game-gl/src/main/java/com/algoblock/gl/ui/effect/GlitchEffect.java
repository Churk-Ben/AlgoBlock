package com.algoblock.gl.ui.effect;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

import com.algoblock.gl.renderer.effect.*;

public class GlitchEffect implements UiEffectRenderer<UiEffect.Glitch> {
    // 屏幕纹理
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

    // 震动参数
    private final double glitchEvaluationInterval = 0.5;
    private final float glitchProbability = 0.8f;
    private final float glitchMinDuration = 0.1f;
    private final float glitchMaxDuration = 0.15f;

    // 震动条参数
    private final float glitch1MinHeight = 0.02f;
    private final float glitch1MaxHeight = 0.05f;
    private final float glitch1MaxOffset = 60.0f;
    private final float glitch1MinOffset = -30.0f;

    // 震动条2参数
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
        if (effect == null || isNoOp(effect)) {
            return;
        }

        int viewportWidth = context.viewportWidth();
        int viewportHeight = context.viewportHeight();

        if (screenTexture == 0) {
            screenTexture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, screenTexture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        glBindTexture(GL_TEXTURE_2D, screenTexture);
        glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 0, 0, viewportWidth, viewportHeight, 0);

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);

        float gy1 = effect.y1() * viewportHeight;
        float gh1 = effect.h1() * viewportHeight;
        float gy2 = effect.y2() * viewportHeight;
        float gh2 = effect.h2() * viewportHeight;

        drawGlitchStrip(viewportWidth, viewportHeight, gy1, gh1, effect.offset1());
        drawGlitchStrip(viewportWidth, viewportHeight, gy2, gh2, effect.offset2());

        glDisable(GL_TEXTURE_2D);
    }

    public UiEffect.Glitch update(long nowMillis) {
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
            return new UiEffect.Glitch(glitchY1, glitchH1, glitchOffset1, glitchY2, glitchH2, glitchOffset2);
        }

        return null;
    }

    private boolean isNoOp(UiEffect.Glitch effect) {
        return effect.y1() == 0f && effect.h1() == 0f && effect.offset1() == 0f &&
                effect.y2() == 0f && effect.h2() == 0f && effect.offset2() == 0f;
    }

    private void drawGlitchStrip(int vw, int vh, float sy, float sh, float offset) {
        float vTop = (vh - sy) / (float) vh;
        float vBottom = (vh - (sy + sh)) / (float) vh;

        glColorMask(true, false, false, true);
        drawStripQuad(vw, sy, sh, offset + 4f, vTop, vBottom);
        glColorMask(false, true, false, true);
        drawStripQuad(vw, sy, sh, offset, vTop, vBottom);
        glColorMask(false, false, true, true);
        drawStripQuad(vw, sy, sh, offset - 4f, vTop, vBottom);
        glColorMask(true, true, true, true);
    }

    private void drawStripQuad(int vw, float sy, float sh, float offset, float vTop, float vBottom) {
        glColor4f(1f, 1f, 1f, 1f);
        glBegin(GL_QUADS);
        glTexCoord2f(0f, vTop);
        glVertex2f(offset, sy);
        glTexCoord2f(1f, vTop);
        glVertex2f(vw + offset, sy);
        glTexCoord2f(1f, vBottom);
        glVertex2f(vw + offset, sy + sh);
        glTexCoord2f(0f, vBottom);
        glVertex2f(offset, sy + sh);
        glEnd();
    }
}
