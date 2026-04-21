package com.algoblock.gl.renderer.effect;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glViewport;

import java.util.HashMap;
import java.util.Map;

import com.algoblock.gl.renderer.core.RenderFrame;
import com.algoblock.gl.renderer.text.TextRenderer;
import com.algoblock.gl.ui.effect.CrtEffect;
import com.algoblock.gl.ui.effect.DimEffect;
import com.algoblock.gl.ui.effect.GlitchEffect;

public class EffectsRenderer {
    private final Map<Class<? extends UiEffect>, UiEffectRenderer<? extends UiEffect>> renderers = new HashMap<>();

    public EffectsRenderer() {
        register(new DimEffect());
        register(new CrtEffect());
        register(new GlitchEffect());
    }

    public void draw(RenderFrame frame, TextRenderer textRenderer, double timeSeconds) {
        if (frame == null || frame.effects() == null || frame.effects().isEmpty()) {
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

        UiEffectRenderContext context = new UiEffectRenderContext(
                frame,
                textRenderer,
                viewportWidth,
                viewportHeight,
                timeSeconds);

        Map<Class<? extends UiEffect>, UiEffect> mergedEffects = mergeEffects(frame.effects());
        for (UiEffect effect : mergedEffects.values()) {
            render(effect, context);
        }
    }

    private Map<Class<? extends UiEffect>, UiEffect> mergeEffects(Iterable<UiEffect> effects) {
        Map<Class<? extends UiEffect>, UiEffect> merged = new HashMap<>();
        for (UiEffect effect : effects) {
            UiEffect existing = merged.get(effect.getClass());
            merged.put(effect.getClass(), existing != null ? existing.merge(effect) : effect);
        }
        return merged;
    }

    private <T extends UiEffect> void register(UiEffectRenderer<T> renderer) {
        renderers.put(renderer.effectType(), renderer);
    }

    @SuppressWarnings("unchecked")
    private <T extends UiEffect> void render(T effect, UiEffectRenderContext context) {
        if (effect == null) {
            return;
        }
        UiEffectRenderer<T> renderer = (UiEffectRenderer<T>) renderers.get(effect.getClass());
        if (renderer != null) {
            renderer.render(effect, context);
        }
    }
}
