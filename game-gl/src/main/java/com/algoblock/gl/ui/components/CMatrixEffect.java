package com.algoblock.gl.ui.components;

import com.algoblock.gl.renderer.TerminalBuffer;
import java.util.Random;

public class CMatrixEffect {
    private static final int BG = 0x0D1117;
    private final Random random = new Random();
    private long lastUpdate = 0;
    private Drop[] drops;

    private static class Drop {
        float y;
        float speed;
        int length;
        char[] chars;
    }

    public void update(int cols, int rows, long nowMillis) {
        if (drops == null || drops.length != cols) {
            drops = new Drop[cols];
            for (int i = 0; i < cols; i++) {
                drops[i] = createDrop(rows);
                drops[i].y = random.nextInt(rows);
            }
        }

        long dt = nowMillis - lastUpdate;
        if (lastUpdate == 0)
            dt = 0;
        lastUpdate = nowMillis;

        for (int i = 0; i < cols; i++) {
            Drop drop = drops[i];
            drop.y += drop.speed * (dt / 1000f);
            if (drop.y - drop.length > rows) {
                drops[i] = createDrop(rows);
            }
            if (random.nextFloat() < 0.1f) {
                drop.chars[random.nextInt(drop.length)] = getRandomChar();
            }
        }
    }

    public void render(TerminalBuffer buffer) {
        int rows = buffer.rows();
        int cols = buffer.cols();
        for (int i = 0; i < cols; i++) {
            Drop drop = drops[i];
            int headY = (int) drop.y;
            for (int j = 0; j < drop.length; j++) {
                int y = headY - j;
                if (y >= 0 && y < rows) {
                    int color = (j == 0) ? 0x99FF99 : 0x008800;
                    if (j > drop.length - 3) {
                        color = 0x004400;
                    }
                    buffer.print(i, y, String.valueOf(drop.chars[j]), color, BG);
                }
            }
        }
    }

    private Drop createDrop(int rows) {
        Drop drop = new Drop();
        drop.y = -random.nextInt(10);
        drop.speed = 8 + random.nextFloat() * 12;
        drop.length = 5 + random.nextInt(15);
        drop.chars = new char[drop.length];
        for (int i = 0; i < drop.length; i++) {
            drop.chars[i] = getRandomChar();
        }
        return drop;
    }

    private char getRandomChar() {
        final String glyphs = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        return glyphs.charAt(random.nextInt(glyphs.length()));
    }
}