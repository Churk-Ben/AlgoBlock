package com.algoblock.gl.renderer;

import java.nio.file.Files;
import java.nio.file.Path;

public class FontAtlas {
    private final String fontPath;
    private final int fontSize;
    private final int atlasWidth;
    private final int atlasHeight;

    public FontAtlas(String fontPath, int fontSize, int atlasWidth, int atlasHeight) {
        this.fontPath = fontPath;
        this.fontSize = fontSize;
        this.atlasWidth = atlasWidth;
        this.atlasHeight = atlasHeight;
        if (!Files.exists(Path.of(fontPath))) {
            throw new IllegalArgumentException("Font not found: " + fontPath);
        }
    }

    public String fontPath() {
        return fontPath;
    }

    public int fontSize() {
        return fontSize;
    }

    public int atlasWidth() {
        return atlasWidth;
    }

    public int atlasHeight() {
        return atlasHeight;
    }
}
