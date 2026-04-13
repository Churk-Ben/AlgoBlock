package com.algoblock.gl.renderer;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTexSubImage2D;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBTruetype.stbtt_FreeBitmap;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointBitmap;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointHMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointBitmapBox;
import static org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;
import static org.lwjgl.stb.STBTruetype.stbtt_ScaleForPixelHeight;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

public class FontAtlas {
    public record GlyphInfo(
            int codePoint,
            float u0,
            float v0,
            float u1,
            float v1,
            int bitmapWidth,
            int bitmapHeight,
            int xOffset,
            int yOffset,
            float advancePx) {
        public boolean hasBitmap() {
            return bitmapWidth > 0 && bitmapHeight > 0;
        }
    }

    private final String fontPath;
    private final int fontSize;
    private final int atlasWidth;
    private final int atlasHeight;
    private final STBTTFontinfo fontInfo;
    private final ByteBuffer fontData;
    private final float scale;
    private final float ascentPx;
    private final float descentPx;
    private final float lineGapPx;
    private final float lineHeightPx;
    private final int textureId;
    private final int fallbackCodePoint;
    private final Map<Integer, GlyphInfo> glyphCache = new HashMap<>();
    private int debugGlyphLogCount = 0;
    private int penX = 1;
    private int penY = 1;
    private int rowHeight = 0;

    public FontAtlas(String fontPath, int fontSize, int atlasWidth, int atlasHeight) {
        this.fontPath = fontPath;
        this.fontSize = fontSize;
        this.atlasWidth = atlasWidth;
        this.atlasHeight = atlasHeight;
        if (fontSize <= 0) {
            throw new IllegalArgumentException("fontSize must be > 0");
        }
        if (atlasWidth <= 0 || atlasHeight <= 0) {
            throw new IllegalArgumentException("atlas size must be > 0");
        }

        this.fontData = loadFontData(fontPath);
        this.fontInfo = STBTTFontinfo.create();
        if (!stbtt_InitFont(fontInfo, fontData)) {
            throw new IllegalStateException("stbtt_InitFont failed for " + fontPath);
        }
        this.scale = stbtt_ScaleForPixelHeight(fontInfo, fontSize);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            var a = stack.mallocInt(1);
            var d = stack.mallocInt(1);
            var g = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, a, d, g);
            this.ascentPx = a.get(0) * scale;
            this.descentPx = d.get(0) * scale;
            this.lineGapPx = g.get(0) * scale;
            this.lineHeightPx = Math.max(1f, (a.get(0) - d.get(0) + g.get(0)) * scale);
        }

        this.textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, atlasWidth, atlasHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);

        GlyphInfo tofu = ensureGlyph('\u25A1');
        this.fallbackCodePoint = tofu.hasBitmap() || tofu.advancePx() > 0f ? '\u25A1' : '?';
        ensureGlyph(fallbackCodePoint);
    }

    private static ByteBuffer loadFontData(String fontPath) {
        try {
            byte[] bytes;
            Path fontFile = Path.of(fontPath);
            if (Files.exists(fontFile)) {
                bytes = Files.readAllBytes(fontFile);
            } else {
                try (java.io.InputStream is = FontAtlas.class.getResourceAsStream("/" + fontPath)) {
                    if (is == null) {
                        throw new IllegalStateException("font not found on filesystem or classpath: " + fontPath);
                    }
                    bytes = is.readAllBytes();
                }
            }
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes).flip();
            return buffer;
        } catch (IOException e) {
            throw new IllegalStateException("failed to read font: " + fontPath, e);
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

    public float lineHeightPx() {
        return lineHeightPx;
    }

    public float ascentPx() {
        return ascentPx;
    }

    public float descentPx() {
        return descentPx;
    }

    public float lineGapPx() {
        return lineGapPx;
    }

    public int textureId() {
        return textureId;
    }

    public void bindTexture() {
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public GlyphInfo glyphFor(int codePoint) {
        GlyphInfo glyph = ensureGlyph(codePoint);
        if (glyph.hasBitmap()) {
            return glyph;
        }
        if (codePoint == ' ') {
            return glyph;
        }
        return glyphCache.get(fallbackCodePoint);
    }

    public float cjkAdvancePx() {
        GlyphInfo han = glyphFor('\u6C49');
        if (han != null && han.advancePx() > 0f) {
            return han.advancePx();
        }
        return glyphFor('M').advancePx() * 2f;
    }

    private GlyphInfo ensureGlyph(int codePoint) {
        GlyphInfo cached = glyphCache.get(codePoint);
        if (cached != null) {
            return cached;
        }

        int glyphW;
        int glyphH;
        int xOffset;
        int yOffset;
        int advance;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var ix0 = stack.mallocInt(1);
            var iy0 = stack.mallocInt(1);
            var ix1 = stack.mallocInt(1);
            var iy1 = stack.mallocInt(1);
            var adv = stack.mallocInt(1);
            var lsb = stack.mallocInt(1);
            stbtt_GetCodepointBitmapBox(fontInfo, codePoint, scale, scale, ix0, iy0, ix1, iy1);
            stbtt_GetCodepointHMetrics(fontInfo, codePoint, adv, lsb);
            xOffset = ix0.get(0);
            yOffset = iy0.get(0);
            glyphW = Math.max(0, ix1.get(0) - xOffset);
            glyphH = Math.max(0, iy1.get(0) - yOffset);
            advance = adv.get(0);
        }

        float advancePx = Math.max(0f, advance * scale);
        ByteBuffer bitmap;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var bw = stack.mallocInt(1);
            var bh = stack.mallocInt(1);
            var bxo = stack.mallocInt(1);
            var byo = stack.mallocInt(1);
            bitmap = stbtt_GetCodepointBitmap(fontInfo, scale, scale, codePoint, bw, bh, bxo, byo);
            if (bitmap == null) {
                GlyphInfo info = new GlyphInfo(codePoint, 0f, 0f, 0f, 0f, 0, 0, xOffset, yOffset, advancePx);
                glyphCache.put(codePoint, info);
                logGlyphDiagnostic(codePoint, 0, 0, xOffset, yOffset, advancePx, false, 0);
                return info;
            }
            glyphW = Math.max(0, bw.get(0));
            glyphH = Math.max(0, bh.get(0));
            xOffset = bxo.get(0);
            yOffset = byo.get(0);
        }
        if (glyphW == 0 || glyphH == 0) {
            stbtt_FreeBitmap(bitmap, 0L);
            GlyphInfo info = new GlyphInfo(codePoint, 0f, 0f, 0f, 0f, 0, 0, xOffset, yOffset, advancePx);
            glyphCache.put(codePoint, info);
            logGlyphDiagnostic(codePoint, glyphW, glyphH, xOffset, yOffset, advancePx, false, 0);
            return info;
        }

        placeGlyph(glyphW, glyphH, codePoint);
        int x = penX;
        int y = penY;
        penX += glyphW + 1;
        rowHeight = Math.max(rowHeight, glyphH + 1);

        ByteBuffer rgba = BufferUtils.createByteBuffer(glyphW * glyphH * 4);
        int alphaMax = 0;
        for (int i = 0; i < glyphW * glyphH; i++) {
            int alpha = bitmap.get(i) & 0xFF;
            alphaMax = Math.max(alphaMax, alpha);
            rgba.put((byte) 0xFF);
            rgba.put((byte) 0xFF);
            rgba.put((byte) 0xFF);
            rgba.put((byte) alpha);
        }
        rgba.flip();

        glBindTexture(GL_TEXTURE_2D, textureId);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, glyphW, glyphH, GL_RGBA, GL_UNSIGNED_BYTE, rgba);
        glBindTexture(GL_TEXTURE_2D, 0);
        stbtt_FreeBitmap(bitmap, 0L);

        float u0 = x / (float) atlasWidth;
        float v0 = y / (float) atlasHeight;
        float u1 = (x + glyphW) / (float) atlasWidth;
        float v1 = (y + glyphH) / (float) atlasHeight;
        GlyphInfo info = new GlyphInfo(codePoint, u0, v0, u1, v1, glyphW, glyphH, xOffset, yOffset, advancePx);
        glyphCache.put(codePoint, info);
        logGlyphDiagnostic(codePoint, glyphW, glyphH, xOffset, yOffset, advancePx, true, alphaMax);
        return info;
    }

    private void logGlyphDiagnostic(int codePoint, int w, int h, int xOffset, int yOffset, float advancePx,
            boolean hasBitmap, int alphaMax) {
        if (debugGlyphLogCount >= 80) {
            return;
        }
        debugGlyphLogCount++;
        String ch = printable(codePoint);
        System.out.printf(
                "[FONT-DIAG] #%d cp=U+%04X ch=%s bitmap=%s w=%d h=%d xOff=%d yOff=%d adv=%.2f alphaMax=%d atlasPen=(%d,%d)%n",
                debugGlyphLogCount, codePoint, ch, hasBitmap, w, h, xOffset, yOffset, advancePx, alphaMax, penX, penY);
    }

    private static String printable(int codePoint) {
        if (!Character.isValidCodePoint(codePoint) || Character.isISOControl(codePoint)) {
            return "<non-print>";
        }
        return "'" + new String(Character.toChars(codePoint)) + "'";
    }

    private void placeGlyph(int glyphW, int glyphH, int codePoint) {
        if (penX + glyphW + 1 > atlasWidth) {
            penX = 1;
            penY += rowHeight;
            rowHeight = 0;
        }
        if (penY + glyphH + 1 > atlasHeight) {
            throw new IllegalStateException(
                    "font atlas is full while inserting code point: U+" + Integer.toHexString(codePoint));
        }
    }
}
