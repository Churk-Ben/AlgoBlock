package com.algoblock.gl.renderer;

public class TextRenderer {
    private final FontAtlas fontAtlas;

    public TextRenderer(FontAtlas fontAtlas) {
        this.fontAtlas = fontAtlas;
    }

    public FontAtlas fontAtlas() {
        return fontAtlas;
    }

    public void upload(TerminalBuffer buffer) {
    }

    public void draw() {
    }
}
