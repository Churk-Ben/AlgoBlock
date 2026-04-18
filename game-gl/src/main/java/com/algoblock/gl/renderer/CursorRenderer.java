package com.algoblock.gl.renderer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CursorRenderer {
    private int shaderProgram = 0;
    private int uStartLoc;
    private int uEndLoc;
    private int uSizeLoc;
    private int uColorLoc;
    private int uBlockStyleLoc;

    private float animatedX = -1f;
    private float animatedY = -1f;
    private double lastTimeSeconds = 0;

    private void initShader() {
        if (shaderProgram != 0)
            return;

        int vert = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vert, loadShaderSource("/assets/shaders/cursor_vert.glsl"));
        glCompileShader(vert);

        int frag = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(frag, loadShaderSource("/assets/shaders/cursor_frag.glsl"));
        glCompileShader(frag);

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vert);
        glAttachShader(shaderProgram, frag);
        glLinkProgram(shaderProgram);

        uStartLoc = glGetUniformLocation(shaderProgram, "u_start");
        uEndLoc = glGetUniformLocation(shaderProgram, "u_end");
        uSizeLoc = glGetUniformLocation(shaderProgram, "u_size");
        uColorLoc = glGetUniformLocation(shaderProgram, "u_color");
        uBlockStyleLoc = glGetUniformLocation(shaderProgram, "u_blockStyle");
    }

    private String loadShaderSource(String resourcePath) {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new RuntimeException("Shader resource not found: " + resourcePath);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load shader: " + resourcePath, e);
        }
    }

    public void draw(RenderFrame frame, TextRenderer textRenderer, double timeSeconds) {
        if (frame == null || !frame.cursorVisible()) {
            lastTimeSeconds = timeSeconds;
            return;
        }

        initShader();

        glViewport(0, 0, textRenderer.viewportWidth(), textRenderer.viewportHeight());
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, textRenderer.viewportWidth(), textRenderer.viewportHeight(), 0.0, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        float cellWidth = textRenderer.cellWidthPx();
        float cellHeight = textRenderer.cellHeightPx();
        float targetX = frame.cursorCol() * cellWidth + cellWidth * 0.5f;
        float targetY = frame.cursorRow() * cellHeight + cellHeight * 0.5f;

        if (animatedX < 0) {
            animatedX = targetX;
            animatedY = targetY;
        }

        double dt = timeSeconds - lastTimeSeconds;
        lastTimeSeconds = timeSeconds;
        if (dt > 0.1)
            dt = 0.1;

        float t = 1.0f - (float) Math.exp(-dt * 20.0);
        animatedX += (targetX - animatedX) * t;
        animatedY += (targetY - animatedY) * t;

        int color = frame.cursorColor();
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = frame.cursorBlockStyle() ? 0.45f : 0.9f;

        float minX = Math.min(animatedX, targetX) - cellWidth * 2;
        float maxX = Math.max(animatedX, targetX) + cellWidth * 2;
        float minY = Math.min(animatedY, targetY) - cellHeight * 2;
        float maxY = Math.max(animatedY, targetY) + cellHeight * 2;

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glUseProgram(shaderProgram);
        glUniform2f(uStartLoc, targetX, targetY); // tail
        glUniform2f(uEndLoc, animatedX, animatedY); // head
        glUniform2f(uSizeLoc, cellWidth, cellHeight);
        glUniform4f(uColorLoc, r, g, b, a);
        glUniform1i(uBlockStyleLoc, frame.cursorBlockStyle() ? 1 : 0);

        glBegin(GL_QUADS);
        glTexCoord2f(minX, minY);
        glVertex2f(minX, minY);
        glTexCoord2f(maxX, minY);
        glVertex2f(maxX, minY);
        glTexCoord2f(maxX, maxY);
        glVertex2f(maxX, maxY);
        glTexCoord2f(minX, maxY);
        glVertex2f(minX, maxY);
        glEnd();

        glUseProgram(0);
        glDisable(GL_BLEND);
    }
}
