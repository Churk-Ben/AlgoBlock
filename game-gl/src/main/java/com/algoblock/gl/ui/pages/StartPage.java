package com.algoblock.gl.ui.pages;

import com.algoblock.gl.input.InputKey;
import com.algoblock.gl.renderer.RenderFrame;
import com.algoblock.gl.renderer.TerminalBuffer;
import com.algoblock.gl.ui.tea.Program;
import com.algoblock.gl.ui.tea.UpdateResult;

import com.algoblock.gl.ui.components.CMatrixEffect;
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

    public record Model(int selectedIndex) {
        public static Model init() {
            return new Model(0);
        }
    }

    public sealed interface Msg {
        record KeyPressed(InputKey key) implements Msg {
        }

        record MouseScrolled(double xoffset, double yoffset) implements Msg {
        }
    }

    public sealed interface Cmd {
        record StartGame() implements Cmd {
        }

        record OpenDiagnostics() implements Cmd {
        }
    }

    @Override
    public Model init() {
        return Model.init();
    }

    @Override
    public UpdateResult<Model, Cmd> update(Model model, Msg msg) {
        if (msg instanceof Msg.KeyPressed keyPressed) {
            InputKey key = keyPressed.key();
            if (key == InputKey.NAV_UP || key == InputKey.NAV_DOWN) {
                int next = model.selectedIndex() == 0 ? 1 : 0;
                return new UpdateResult<>(new Model(next), List.of());
            } else if (key == InputKey.SUBMIT) {
                if (model.selectedIndex() == 0) {
                    return new UpdateResult<>(model, List.of(new Cmd.StartGame()));
                } else {
                    return new UpdateResult<>(model, List.of(new Cmd.OpenDiagnostics()));
                }
            }
        } else if (msg instanceof Msg.MouseScrolled scrolled) {
            if (scrolled.yoffset() != 0) {
                int next = model.selectedIndex() == 0 ? 1 : 0;
                return new UpdateResult<>(new Model(next), List.of());
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
        String[] options = { "Start Game", "System Diagnostics" };
        // boolean blinkVisible = ((nowMillis / 500L) % 2L) == 0L;

        int maxOptLen = 0;
        for (String opt : options) {
            maxOptLen = Math.max(maxOptLen, opt.length());
        }
        int boxWidth = maxOptLen + 12; // Extra padding for "> <" and margins
        int boxHeight = options.length * 2 + 1;
        int boxX = (cols - boxWidth) / 2;
        int boxY = optionsStartRow - 1;

        com.algoblock.gl.ui.components.PanelComponent.drawBox(buffer, boxX, boxY, boxWidth, boxHeight, 0x555555, BG);

        int cursorCol = -1;
        int cursorRow = -1;

        for (int i = 0; i < options.length; i++) {
            String text = options[i];
            int textCol = (cols - text.length()) / 2;
            int textRow = optionsStartRow + i * 2;

            if (i == model.selectedIndex()) {
                buffer.print(Math.max(0, textCol), textRow, text, 0xFFFFFF, BG);
                cursorCol = Math.max(0, textCol - 2);
                cursorRow = textRow;
            } else {
                buffer.print(Math.max(0, textCol), textRow, text, 0x888888, BG);
            }
        }

        GlitchState glitch = glitchEffect.update(nowMillis);
        List<UiEffect> effects = new java.util.ArrayList<>();
        effects.add(new UiEffect.Crt(0.3f));
        if (glitch != null) {
            effects.add(new UiEffect.Glitch(glitch));
        }

        return new RenderFrame(buffer, cursorCol, cursorRow, true, true, 0x00FF00, List.copyOf(effects));
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
}
