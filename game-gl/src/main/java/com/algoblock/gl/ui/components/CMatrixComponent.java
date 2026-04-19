package com.algoblock.gl.ui.components;

import java.util.Random;

import com.algoblock.gl.renderer.core.TerminalBuffer;

public class CMatrixComponent {
    private static final int BG = 0x0D1117;

    private final float minSpeed = 8.0f;
    private final float maxSpeed = 20.0f;
    private final int minLength = 5;
    private final int maxLength = 20;
    private final float charUpdateProbability = 0.1f;
    private final int initialDropOffset = 10;

    private final int headColor = 0x55CC55;
    private final int bodyColor = 0x008800;
    private final int tailColor = 0x004400;
    private final int tailStartIndex = 3;

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
        if (cols <= 0 || rows <= 0) {
            drops = new Drop[0];
            lastUpdate = nowMillis;
            return;
        }

        if (drops == null) {
            drops = new Drop[cols];
            for (int i = 0; i < cols; i++) {
                drops[i] = createDrop(rows, true);
            }
        } else if (drops.length != cols) {
            Drop[] resized = new Drop[cols];
            int keep = Math.min(drops.length, cols);
            System.arraycopy(drops, 0, resized, 0, keep);
            for (int i = keep; i < cols; i++) {
                resized[i] = createDrop(rows, true);
            }
            drops = resized;
        }

        long dt = nowMillis - lastUpdate;
        if (lastUpdate == 0) {
            dt = 0;
        }
        lastUpdate = nowMillis;

        for (int i = 0; i < cols; i++) {
            Drop drop = drops[i];
            drop.y += drop.speed * (dt / 1000f);
            if (drop.y - drop.length > rows) {
                drops[i] = createDrop(rows, false);
            }
            if (random.nextFloat() < charUpdateProbability) {
                drop.chars[random.nextInt(drop.length)] = getRandomChar();
            }
        }
    }

    public void render(TerminalBuffer buffer) {
        if (drops == null || drops.length == 0) {
            return;
        }
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

    private Drop createDrop(int rows, boolean scatterAcrossScreen) {
        Drop drop = new Drop();
        drop.speed = minSpeed + random.nextFloat() * (maxSpeed - minSpeed);
        drop.length = minLength + random.nextInt(maxLength - minLength);
        if (scatterAcrossScreen) {
            int span = Math.max(1, rows + drop.length + initialDropOffset);
            drop.y = random.nextInt(span) - drop.length;
        } else {
            drop.y = -random.nextInt(initialDropOffset);
        }
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
