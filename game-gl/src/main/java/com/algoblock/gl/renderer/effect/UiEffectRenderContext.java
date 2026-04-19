package com.algoblock.gl.renderer.effect;

import com.algoblock.gl.renderer.core.RenderFrame;
import com.algoblock.gl.renderer.text.TextRenderer;

public record UiEffectRenderContext(
                RenderFrame frame,
                TextRenderer textRenderer,
                int viewportWidth,
                int viewportHeight,
                double timeSeconds) {
}
