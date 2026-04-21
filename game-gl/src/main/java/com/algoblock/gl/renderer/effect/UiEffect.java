package com.algoblock.gl.renderer.effect;

public sealed interface UiEffect permits UiEffect.Crt, UiEffect.Glitch, UiEffect.Dim {
    UiEffect merge(UiEffect other);

    record Crt(float strength) implements UiEffect {
        @Override
        public Crt merge(UiEffect other) {
            if (!(other instanceof Crt(float strength1)))
                return this;
            return new Crt(Math.max(this.strength, strength1));
        }
    }

    record Glitch(com.algoblock.gl.ui.effect.GlitchState state) implements UiEffect {
        @Override
        public Glitch merge(UiEffect other) {
            if (!(other instanceof Glitch g))
                return this;
            return this.state == null ? g : this;
        }
    }

    record Dim(float opacity, int excludeX, int excludeY, int excludeWidth, int excludeHeight) implements UiEffect {
        @Override
        public Dim merge(UiEffect other) {
            if (!(other instanceof Dim(float opacity1, int x, int y, int width, int height)))
                return this;
            if (this.opacity >= opacity1)
                return this;
            return new Dim(opacity1, x, y, width, height);
        }
    }
}
