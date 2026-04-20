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
import java.util.Random;

public class CrtEffect implements UiEffectRenderer<UiEffect.Crt> {
    private final Random random = new Random();
    private int screenTexture = 0; // 用于捕获屏幕的纹理ID

    // 扫描线参数
    private final float scanSpeed = 20.0f;
    private final float scanFrequency = 0.1f;
    private final float scanStripeStep = 2.0f;
    private final float scanGlowThreshold = 0.7f;
    private final float scanGlowAlphaMult = 0.15f;
    private final float scanDarkAlphaMult = 0.25f;

    // 色散(Glitch)参数
    private final double glitchEvalFreq = 15.0;
    private final float glitchProbability = 0.15f;
    private final int glitchMaxCount = 4;
    private final int glitchMinCount = 1;
    private final float glitchBaseHeightRatio = 0.01f;
    private final float glitchVarHeightRatio = 0.04f;
    private final float glitchMaxOffset = 40.0f;
    private final float glitchRGBShift = 4.0f;

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
        glEnd(); // 结束扫描线绘制，以便后续纹理捕获能将扫描线包含进去

        // --- 2. 屏幕纹理捕获与色散 (Glitch) ---
        random.setSeed((long) (timeSeconds * glitchEvalFreq));
        if (random.nextFloat() < glitchProbability * strength) {
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

            // 捕获当前屏幕内容（包含刚刚画上去的扫描线）
            org.lwjgl.opengl.GL11.glBindTexture(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, screenTexture);
            org.lwjgl.opengl.GL11.glCopyTexImage2D(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, 0, org.lwjgl.opengl.GL11.GL_RGB,
                    0, 0, viewportWidth, viewportHeight, 0);

            org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_BLEND); // 覆盖绘制

            int glitchCount = random.nextInt(glitchMaxCount) + glitchMinCount;
            for (int i = 0; i < glitchCount; i++) {
                float glitchY = random.nextFloat() * viewportHeight;
                float glitchHeight = (random.nextFloat() * glitchVarHeightRatio + glitchBaseHeightRatio)
                        * viewportHeight;

                // 随机水平偏移量
                float offset = (random.nextFloat() * 2f - 1f) * glitchMaxOffset * strength;

                float vTop = (viewportHeight - glitchY) / (float) viewportHeight;
                float vBottom = (viewportHeight - (glitchY + glitchHeight)) / (float) viewportHeight;

                // 分离RGB通道进行错位绘制，实现真实的色差效果
                // Red
                org.lwjgl.opengl.GL11.glColorMask(true, false, false, true);
                drawStripQuad(viewportWidth, glitchY, glitchHeight, offset + glitchRGBShift * strength, vTop, vBottom);
                // Green
                org.lwjgl.opengl.GL11.glColorMask(false, true, false, true);
                drawStripQuad(viewportWidth, glitchY, glitchHeight, offset, vTop, vBottom);
                // Blue
                org.lwjgl.opengl.GL11.glColorMask(false, false, true, true);
                drawStripQuad(viewportWidth, glitchY, glitchHeight, offset - glitchRGBShift * strength, vTop, vBottom);
            }

            // 恢复状态
            org.lwjgl.opengl.GL11.glColorMask(true, true, true, true);
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
            org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_BLEND);
        }

        // --- 3. 边缘暗角 (Vignette) ---
        glBegin(GL_QUADS);
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

    // 用于绘制带纹理坐标的色散条带
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