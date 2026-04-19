package com.algoblock.gl.ui.pages;

import com.algoblock.gl.input.KeyMapper;
import com.algoblock.gl.renderer.RenderFrame;
import com.algoblock.gl.renderer.TerminalBuffer;
import com.algoblock.gl.ui.tea.Program;
import com.algoblock.gl.ui.tea.UpdateResult;

import com.algoblock.gl.ui.components.GlitchEffect;
import com.algoblock.gl.renderer.GlitchState;
import com.algoblock.gl.renderer.UiEffect;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

public class StartPage implements Program<StartPage.Model, StartPage.Msg, StartPage.Cmd> {

    private static final int BG = 0x0D1117;
    private final CMatrixEffect cmatrix = new CMatrixEffect();
    private final GlitchEffect glitchEffect = new GlitchEffect();
    private final String[] titleArt = loadRandomTitleArt();

    private static final String[] FALLBACK_TITLE_ART = {
            "   _______ __               ______ __              __      ",
            "  |   _   |  |.-----.-----.|   __ \\  |.-----.----.|  |--.  ",
            "  |       |  ||  _  |  _  ||   __ <  ||  _  |  __||    <   ",
            "  |___|___|__||___  |_____||______/__||_____|____||__|__|  ",
            "              |_____|                                      "
    };

    public record Model() {
        public static Model init() {
            return new Model();
        }
    }

    public sealed interface Msg {
        record KeyPressed(int key) implements Msg {
        }
    }

    public sealed interface Cmd {
        record StartGame() implements Cmd {
        }
    }

    @Override
    public Model init() {
        return Model.init();
    }

    @Override
    public UpdateResult<Model, Cmd> update(Model model, Msg msg) {
        if (msg instanceof Msg.KeyPressed keyPressed) {
            if (KeyMapper.isSubmit(keyPressed.key())) {
                return new UpdateResult<>(model, List.of(new Cmd.StartGame()));
            }
        }
        return new UpdateResult<>(model, List.of());
    }

    @Override
    public RenderFrame view(Model model, TerminalBuffer buffer, long nowMillis) {
        cmatrix.update(buffer.cols(), buffer.rows(), nowMillis);
        cmatrix.render(buffer);

        int rows = buffer.rows();
        int cols = buffer.cols();

        // Draw title
        int titleStartRow = rows / 4;
        for (int i = 0; i < titleArt.length; i++) {
            String line = titleArt[i];
            int titleStartCol = (cols - line.length()) / 2;
            buffer.print(Math.max(0, titleStartCol), titleStartRow + i, line, 0x00FF00, BG);
        }

        // Draw options
        int optionsStartRow = titleStartRow + titleArt.length + 3;
        String startText = "> Press ENTER to Start Game <";
        int startCol = (cols - startText.length()) / 2;
        boolean blinkVisible = ((nowMillis / 500L) % 2L) == 0L;
        if (blinkVisible) {
            buffer.print(Math.max(0, startCol), optionsStartRow, startText, 0xFFFFFF, BG);
        }

        String placeholderText = "  [ Options (WIP) ]  ";
        int placeholderCol = (cols - placeholderText.length()) / 2;
        buffer.print(Math.max(0, placeholderCol), optionsStartRow + 2, placeholderText, 0x888888, BG);

        GlitchState glitch = glitchEffect.update(nowMillis);
        List<UiEffect> effects = new java.util.ArrayList<>();
        effects.add(new UiEffect.Crt(0.3f));
        if (glitch != null) {
            effects.add(new UiEffect.Glitch(glitch));
        }

        return new RenderFrame(buffer, -1, -1, false, false, 0, List.copyOf(effects));
    }

    private static final String[] TITLE_RESOURCES = {
            "/assets/titles/ascii_title_ansi_shadow.txt",
            "/assets/titles/ascii_title_chunky.txt",
            "/assets/titles/ascii_title_dos_rebel.txt",
            "/assets/titles/ascii_title_graffiti.txt",
            "/assets/titles/ascii_title_nscript.txt",
            "/assets/titles/ascii_title_rectangles.txt"
    };

    private static String[] loadRandomTitleArt() {
        String selected = TITLE_RESOURCES[new Random().nextInt(TITLE_RESOURCES.length)];
        try (java.io.InputStream is = StartPage.class.getResourceAsStream(selected)) {
            if (is == null) {
                return FALLBACK_TITLE_ART;
            }
            List<String> lines = new java.util.ArrayList<>(
                    List.of(new String(is.readAllBytes(), StandardCharsets.UTF_8).split("\r?\n")));
            trimTrailingEmptyLines(lines);
            if (lines.isEmpty()) {
                return FALLBACK_TITLE_ART;
            }
            return lines.toArray(String[]::new);
        } catch (IOException e) {
            return FALLBACK_TITLE_ART;
        }
    }

    private static void trimTrailingEmptyLines(List<String> lines) {
        int last = lines.size() - 1;
        while (last >= 0 && lines.get(last).trim().isEmpty()) {
            lines.remove(last);
            last--;
        }
    }

    private static class CMatrixEffect {
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
}
