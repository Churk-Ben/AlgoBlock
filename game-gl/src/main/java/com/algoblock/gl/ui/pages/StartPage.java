package com.algoblock.gl.ui.pages;

import com.algoblock.gl.input.intent.InputIntent;
import com.algoblock.gl.ui.components.CMatrixComponent;
import com.algoblock.gl.ui.effect.GlitchEffect;
import com.algoblock.gl.ui.tea.Program;
import com.algoblock.gl.ui.tea.UpdateResult;
import com.algoblock.gl.renderer.core.RenderFrame;
import com.algoblock.gl.renderer.core.TerminalBuffer;
import com.algoblock.gl.renderer.effect.GlitchState;
import com.algoblock.gl.renderer.effect.UiEffect;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

public class StartPage implements Program<StartPage.Model, StartPage.Msg, StartPage.Cmd> {

    private static final int BG = 0x0D1117;
    private final CMatrixComponent cmatrix = new CMatrixComponent();
    private final GlitchEffect glitchEffect = new GlitchEffect();
    private final String[] titleArt = loadRandomTitleArt();

    private static final String[] OPTIONS = { "Start Game", "Diagnostics", "Exit Game" };

    private static final String SFX_TYPE_IN = "/assets/audio/sfx/type-in.mp3";
    private static final String SFX_INTERACT = "/assets/audio/sfx/interact.mp3";

    public record Model(int selectedIndex) {
        public static Model init() {
            return new Model(0);
        }
    }

    public sealed interface Msg {
        record Intent(InputIntent intent) implements Msg {
        }
    }

    public sealed interface Cmd {
        record StartGame() implements Cmd {
        }

        record OpenDiagnostics() implements Cmd {
        }

        record Exit() implements Cmd {
        }

        record PlaySound(String resourcePath) implements Cmd {
        }
    }

    @Override
    public Model init() {
        return Model.init();
    }

    @Override
    public UpdateResult<Model, Cmd> update(Model model, Msg msg) {
        if (msg instanceof Msg.Intent intentMsg) {
            InputIntent intent = intentMsg.intent();
            if (intent instanceof InputIntent.NavigatePrev) {
                int next = model.selectedIndex() - 1;
                if (next < 0)
                    next = OPTIONS.length - 1;
                return new UpdateResult<>(new Model(next), List.of(new Cmd.PlaySound(SFX_INTERACT)));
            } else if (intent instanceof InputIntent.NavigateNext) {
                int next = (model.selectedIndex() + 1) % OPTIONS.length;
                return new UpdateResult<>(new Model(next), List.of(new Cmd.PlaySound(SFX_INTERACT)));
            } else if (intent instanceof InputIntent.Submit) {
                if (model.selectedIndex() == 0) {
                    return new UpdateResult<>(model,
                            List.of(new Cmd.StartGame(), new Cmd.PlaySound(SFX_TYPE_IN)));
                } else if (model.selectedIndex() == 1) {
                    return new UpdateResult<>(model,
                            List.of(new Cmd.OpenDiagnostics(), new Cmd.PlaySound(SFX_TYPE_IN)));
                } else {
                    return new UpdateResult<>(model,
                            List.of(new Cmd.Exit(), new Cmd.PlaySound(SFX_TYPE_IN)));
                }
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
            buffer.print(Math.max(0, titleStartCol), titleStartRow + i, line, 0xEEEEEE, BG);
        }

        // Draw options
        int optionsStartRow = titleStartRow + titleArt.length + 3;

        int maxOptLen = 0;
        for (String opt : OPTIONS) {
            maxOptLen = Math.max(maxOptLen, opt.length());
        }
        int boxWidth = maxOptLen + 12;
        int boxHeight = OPTIONS.length * 2 + 1;
        int boxX = (cols - boxWidth) / 2;
        int boxY = optionsStartRow - 1;

        com.algoblock.gl.ui.components.PanelComponent.drawBox(buffer, boxX, boxY, boxWidth, boxHeight, 0x555555, BG);

        int[] cursorInfo = com.algoblock.gl.ui.components.PanelComponent.drawLeftAlignedOptions(
                buffer, boxX, boxWidth, optionsStartRow, OPTIONS, model.selectedIndex(), 2, 2, 0x888888, 0xEEEEEE, BG);

        int cursorCol = cursorInfo[0];
        int cursorRow = cursorInfo[1];

        GlitchState glitch = glitchEffect.update(nowMillis);
        List<UiEffect> effects = new java.util.ArrayList<>();
        effects.add(new UiEffect.Crt(0.3f));
        if (glitch != null) {
            effects.add(new UiEffect.Glitch(glitch));
        }

        return new RenderFrame(buffer, cursorCol, cursorRow, true, true, 0x22CC22, List.copyOf(effects));
    }

    private static final String[] TITLE_RESOURCES = {
            "/assets/titles/ascii_title_chunky.txt",
            "/assets/titles/ascii_title_graffiti.txt",
            "/assets/titles/ascii_title_rectangles.txt"
    };

    private static final String[] FALLBACK_TITLE_ART = {
            "   _______ __               ______ __              __      ",
            "  |   _   |  |.-----.-----.|   __ \\  |.-----.----.|  |--.  ",
            "  |       |  ||  _  |  _  ||   __ <  ||  _  |  __||    <   ",
            "  |___|___|__||___  |_____||______/__||_____|____||__|__|  ",
            "              |_____|                                      "
    };

    private static String[] loadRandomTitleArt() {
        String selected = TITLE_RESOURCES[new Random().nextInt(TITLE_RESOURCES.length)];
        try (java.io.InputStream is = StartPage.class.getResourceAsStream(selected)) {
            if (is == null) {
                return FALLBACK_TITLE_ART;
            }
            String[] content = new String(is.readAllBytes(), StandardCharsets.UTF_8).split("\r?\n");
            List<String> lines = new java.util.ArrayList<>(List.of(content));
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
