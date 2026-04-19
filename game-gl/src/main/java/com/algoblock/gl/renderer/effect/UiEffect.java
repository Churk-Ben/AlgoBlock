package com.algoblock.gl.renderer.effect;

public sealed interface UiEffect permits UiEffect.Crt, UiEffect.Glitch, UiEffect.Dim {
    record Crt(float strength) implements UiEffect {
    }

    record Glitch(GlitchState state) implements UiEffect {
    }

    record Dim(float opacity, int excludeX, int excludeY, int excludeWidth, int excludeHeight) implements UiEffect {
    }
}
