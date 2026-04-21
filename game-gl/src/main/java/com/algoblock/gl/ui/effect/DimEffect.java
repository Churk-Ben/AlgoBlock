package com.algoblock.gl.ui.effect;

import static org.lwjgl.opengl.GL11.*;

import com.algoblock.gl.renderer.effect.*;
import com.algoblock.gl.renderer.text.TextRenderer;

public class DimEffect implements UiEffectRenderer<UiEffect.Dim> {
    // 此特效还需要思考一下

    @Override
    public Class<UiEffect.Dim> effectType() {
        return UiEffect.Dim.class;
    }

    @Override
    public void render(UiEffect.Dim dimEffect, UiEffectRenderContext context) {
        if (dimEffect == null || dimEffect.opacity() <= 0f) {
            return;
        }
        TextRenderer textRenderer = context.textRenderer();
        int viewportWidth = context.viewportWidth();
        int viewportHeight = context.viewportHeight();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBegin(GL_QUADS);
        glColor4f(0f, 0f, 0f, dimEffect.opacity());

        float cellWidth = textRenderer.cellWidthPx();
        var frame = context.frame();
        float cellHeight = textRenderer.cellHeightPx();
        int cols = frame.textBuffer() != null ? frame.textBuffer().cols() : 0;
        int rows = frame.textBuffer() != null ? frame.textBuffer().rows() : 0;
        float marginX = textRenderer.gridOffsetXPx(cols);
        float marginY = textRenderer.gridOffsetYPx(rows);

        if (dimEffect.excludeWidth() > 0 && dimEffect.excludeHeight() > 0) {
            float exX = marginX + dimEffect.excludeX() * cellWidth;
            float exY = marginY + dimEffect.excludeY() * cellHeight;
            float exW = dimEffect.excludeWidth() * cellWidth;
            float exH = dimEffect.excludeHeight() * cellHeight;

            glVertex2f(0f, 0f);
            glVertex2f(viewportWidth, 0f);
            glVertex2f(viewportWidth, exY);
            glVertex2f(0f, exY);

            glVertex2f(0f, exY + exH);
            glVertex2f(viewportWidth, exY + exH);
            glVertex2f(viewportWidth, viewportHeight);
            glVertex2f(0f, viewportHeight);

            glVertex2f(0f, exY);
            glVertex2f(exX, exY);
            glVertex2f(exX, exY + exH);
            glVertex2f(0f, exY + exH);

            glVertex2f(exX + exW, exY);
            glVertex2f(viewportWidth, exY);
            glVertex2f(viewportWidth, exY + exH);
            glVertex2f(exX + exW, exY + exH);
        } else {
            glVertex2f(0f, 0f);
            glVertex2f(viewportWidth, 0f);
            glVertex2f(viewportWidth, viewportHeight);
            glVertex2f(0f, viewportHeight);
        }

        glEnd();
        glDisable(GL_BLEND);
    }
}
