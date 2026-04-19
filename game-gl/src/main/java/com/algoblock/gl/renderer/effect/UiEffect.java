package com.algoblock.gl.renderer.effect;

public sealed interface UiEffect permits UiEffect.Crt, UiEffect.Glitch {
    record Crt(float strength) implements UiEffect {
    }

    record Glitch(GlitchState state) implements UiEffect {
    }
}
