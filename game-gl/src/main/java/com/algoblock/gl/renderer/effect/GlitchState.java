package com.algoblock.gl.renderer.effect;

/**
 * State for the glitch effect to be passed in a RenderFrame.
 * All coordinates are normalized [0.0, 1.0] relative to the viewport size.
 */
public record GlitchState(
        float y1, float h1, float offset1,
        float y2, float h2, float offset2) {
}