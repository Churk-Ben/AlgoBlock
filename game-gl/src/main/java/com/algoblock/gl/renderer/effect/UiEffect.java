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

    record Glitch(float y1, float h1, float offset1, float y2, float h2, float offset2) implements UiEffect {
        @Override
        public Glitch merge(UiEffect other) {
            if (!(other instanceof Glitch g))
                return this;

            if (this.y1 == 0f && this.h1 == 0f && this.offset1 == 0f &&
                    this.y2 == 0f && this.h2 == 0f && this.offset2 == 0f) {
                return g;
            } else {
                return this;
            }
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
