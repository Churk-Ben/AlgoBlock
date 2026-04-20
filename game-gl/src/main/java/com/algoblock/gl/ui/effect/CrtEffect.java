package com.algoblock.gl.ui.effect;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;

import com.algoblock.gl.renderer.effect.UiEffect;
import com.algoblock.gl.renderer.effect.UiEffectRenderContext;
import com.algoblock.gl.renderer.effect.UiEffectRenderer;

public class CrtEffect implements UiEffectRenderer<UiEffect.Crt> {

    // 扫描线参数
    private final float scanSpeed = 20.0f;
    private final float scanFrequency = 0.1f;
    private final float scanStripeStep = 2.0f;
    private final float scanGlowThreshold = 0.7f;
    private final float scanGlowAlphaMult = 0.15f;
    private final float scanDarkAlphaMult = 0.25f;

    // 暗角(Vignette)参数
    private final double vignetteAnimFreq = 1.7;
    private final float vignetteBaseAlpha = 0.03f;
    private final float vignetteStrengthMult = 0.16f;
    private final float vignetteMinEdge = 24.0f;
    private final float vignetteEdgeRatio = 0.1f;

    @Override
    public Class<UiEffect.Crt> effectType() {
        return UiEffect.Crt.class;
    }

    @Override
    public void render(UiEffect.Crt effect, UiEffectRenderContext context) {
        if (effect == null || effect.strength() <= 0f) {
            return;
        }

        float strength = effect.strength();
        int viewportWidth = context.viewportWidth();
        int viewportHeight = context.viewportHeight();
        double timeSeconds = context.timeSeconds();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // --- 1. 绘制扫描线 ---
        glBegin(GL_QUADS);
        for (float y = 0; y < viewportHeight; y += scanStripeStep) {
            float phase = (float) ((y - timeSeconds * scanSpeed) * scanFrequency);
            float sine = (float) Math.sin(phase);
            float intensity = (sine + 1.0f) * 0.5f;

            if (intensity > scanGlowThreshold) {
                float glowAlpha = (intensity - scanGlowThreshold) * scanGlowAlphaMult * strength;
                glColor4f(0.1f, 1.0f, 0.2f, glowAlpha);
            } else {
                float darkAlpha = (1.0f - intensity) * scanDarkAlphaMult * strength;
                glColor4f(0.0f, 0.0f, 0.0f, darkAlpha);
            }

            glVertex2f(0f, y);
            glVertex2f(viewportWidth, y);
            glVertex2f(viewportWidth, Math.min(viewportHeight, y + scanStripeStep));
            glVertex2f(0f, Math.min(viewportHeight, y + scanStripeStep));
        }

        // --- 2. 边缘暗角 (Vignette) ---
        float animated = (float) ((Math.sin(timeSeconds * vignetteAnimFreq) + 1.0) * 0.5);
        float vignetteAlpha = Math.max(vignetteBaseAlpha, strength * vignetteStrengthMult) * (0.7f + animated * 0.3f);
        float edge = Math.max(vignetteMinEdge, Math.min(viewportWidth, viewportHeight) * vignetteEdgeRatio);

        glColor4f(0f, 0f, 0f, vignetteAlpha);
        // Top
        glVertex2f(0f, 0f);
        glVertex2f(viewportWidth, 0f);
        glVertex2f(viewportWidth, edge);
        glVertex2f(0f, edge);
        // Bottom
        glVertex2f(0f, viewportHeight - edge);
        glVertex2f(viewportWidth, viewportHeight - edge);
        glVertex2f(viewportWidth, viewportHeight);
        glVertex2f(0f, viewportHeight);
        // Left
        glVertex2f(0f, 0f);
        glVertex2f(edge, 0f);
        glVertex2f(edge, viewportHeight);
        glVertex2f(0f, viewportHeight);
        // Right
        glVertex2f(viewportWidth - edge, 0f);
        glVertex2f(viewportWidth, 0f);
        glVertex2f(viewportWidth, viewportHeight);
        glVertex2f(viewportWidth - edge, viewportHeight);

        glEnd();
        glDisable(GL_BLEND);
    }
}