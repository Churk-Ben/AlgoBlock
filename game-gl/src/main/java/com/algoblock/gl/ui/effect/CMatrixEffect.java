package com.algoblock.gl.ui.effect;

import java.util.Random;

import com.algoblock.gl.renderer.core.TerminalBuffer;

public class CMatrixEffect {
    // Background color
    private static final int BG = 0x0D1117;

    // Matrix effect parameters
    private final float minSpeed = 8.0f;
    private final float maxSpeed = 20.0f;
    private final int minLength = 5;
    private final int maxLength = 20;
    private final float charUpdateProbability = 0.1f;
    private final int initialDropOffset = 10;

    // Color parameters
    private final int headColor = 0x55CC55;
    private final int bodyColor = 0x008800;
    private final int tailColor = 0x004400;
    private final int tailStartIndex = 3;

    // Random Charset
    private final String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

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
            if (random.nextFloat() < charUpdateProbability) {
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
                    int color = (j == 0) ? headColor : bodyColor;
                    if (j > drop.length - tailStartIndex) {
                        color = tailColor;
                    }
                    buffer.print(i, y, String.valueOf(drop.chars[j]), color, BG);
                }
            }
        }
    }

    private Drop createDrop(int rows) {
        Drop drop = new Drop();
        drop.y = -random.nextInt(initialDropOffset);
        drop.speed = minSpeed + random.nextFloat() * (maxSpeed - minSpeed);
        drop.length = minLength + random.nextInt(maxLength - minLength);
        drop.chars = new char[drop.length];
        for (int i = 0; i < drop.length; i++) {
            drop.chars[i] = getRandomChar();
        }
        return drop;
    }

    private char getRandomChar() {
        return charset.charAt(random.nextInt(charset.length()));
    }
}