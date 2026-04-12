package com.algoblock.gl.renderer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glVertexPointer;
import static org.lwjgl.opengl.GL11.glViewport;

import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBEasyFont;

public class TextRenderer {
    private final FontAtlas fontAtlas;
    private final ByteBuffer vertexBuffer = BufferUtils.createByteBuffer(256 * 1024);
    private int viewportWidth = 1280;
    private int viewportHeight = 720;

    public TextRenderer(FontAtlas fontAtlas) {
        this.fontAtlas = fontAtlas;
    }

    public FontAtlas fontAtlas() {
        return fontAtlas;
    }

    public void setViewport(int width, int height) {
        viewportWidth = Math.max(1, width);
        viewportHeight = Math.max(1, height);
    }

    public void upload(TerminalBuffer buffer) {
        glViewport(0, 0, viewportWidth, viewportHeight);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, viewportWidth, viewportHeight, 0.0, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        TerminalBuffer.Cell[] cells = buffer.cells();
        int cols = buffer.cols();
        int rows = buffer.rows();
        float x = 16f;
        float yStart = 30f;
        float lineHeight = 18f;

        for (int row = 0; row < rows; row++) {
            int start = row * cols;
            int last = cols - 1;
            while (last >= 0 && cells[start + last].c() == ' ') {
                last--;
            }
            if (last < 0) {
                continue;
            }
            StringBuilder sb = new StringBuilder(last + 1);
            for (int col = 0; col <= last; col++) {
                sb.append(cells[start + col].c());
            }
            int color = cells[start].fg();
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            glColor3f(r, g, b);
            vertexBuffer.clear();
            int quads = STBEasyFont.stb_easy_font_print(x, yStart + row * lineHeight, sb.toString(), null,
                    vertexBuffer);
            vertexBuffer.flip();
            glEnableClientState(GL_VERTEX_ARRAY);
            glVertexPointer(2, GL_FLOAT, 16, vertexBuffer);
            glDrawArrays(GL_QUADS, 0, quads * 4);
            glDisableClientState(GL_VERTEX_ARRAY);
        }
    }

    public void draw() {
    }
}
