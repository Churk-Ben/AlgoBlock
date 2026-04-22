package com.algoblock.gl.ui.pages;

import com.algoblock.gl.input.intent.InputIntent;
import com.algoblock.gl.renderer.core.RenderFrame;
import com.algoblock.gl.renderer.core.TerminalBuffer;
import com.algoblock.gl.renderer.cursor.CursorState;
import com.algoblock.gl.renderer.effect.UiEffect;
import com.algoblock.gl.ui.components.CMatrixComponent;
import com.algoblock.gl.ui.components.PanelComponent;
import com.algoblock.gl.ui.effect.GlitchEffect;
import com.algoblock.gl.ui.tea.Program;
import com.algoblock.gl.ui.tea.UpdateResult;
import com.algoblock.gl.utils.TextUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StartPage implements Program<StartPage.Model, StartPage.Msg, StartPage.Cmd> {
    private static final String SFX_CURSOR_MOVE = "/assets/audio/sfx/cursor-move.mp3";
    private static final int BG = 0x0D1117;
    private static final int BORDER = 0x555555;
    private static final int CURSOR = 0x22CC22;
    private static final int NORMAL_TEXT = 0x888888;
    private static final int HOVER_TEXT = 0xFFFFFF;
    private static final String[] OPTIONS = { "Login (root)", "Live Environment", "Exit System" };
    private static final String[] TITLE_RESOURCES = {
            "/assets/titles/ascii_title_chunky.txt",
            "/assets/titles/ascii_title_graffiti.txt"
            // "/assets/titles/ascii_title_rectangles.txt"
    };
    private static final String[] FALLBACK_TITLE_ART = {
            "   _______ __               ______ __              __      ",
            "  |   _   |  |.-----.-----.|   __ \\  |.-----.----.|  |--.  ",
            "  |       |  ||  _  |  _  ||   __ <  ||  _  |  __||    <   ",
            "  |___|___|__||___  |_____||______/__||_____|____||__|__|  ",
            "              |_____|                                      "
    };

    // 页面组件
    private final CMatrixComponent cmatrix = new CMatrixComponent();
    private final GlitchEffect glitchEffect = new GlitchEffect();
    private final String[] titleArt = loadRandomTitleArt();

    // 页面模型
    public record Model(int selectedIndex) {
        public static Model init() {
            return new Model(0);
        }
    }

    // 页面消息
    public sealed interface Msg {
        record Intent(InputIntent intent) implements Msg {
        }
    }

    // 页面命令
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
        if (msg instanceof Msg.Intent(InputIntent intent)) {
            if (intent instanceof InputIntent.NavigatePrev) {
                int next = model.selectedIndex() == 0 ? OPTIONS.length - 1 : model.selectedIndex() - 1;
                return new UpdateResult<>(
                        new Model(next),
                        List.of(new Cmd.PlaySound(SFX_CURSOR_MOVE)));

            } else if (intent instanceof InputIntent.NavigateNext) {
                int next = (model.selectedIndex() + 1) % OPTIONS.length;
                return new UpdateResult<>(
                        new Model(next),
                        List.of(new Cmd.PlaySound(SFX_CURSOR_MOVE)));

            } else if (intent instanceof InputIntent.Submit) {
                switch (model.selectedIndex()) {
                    case 0 -> {
                        return new UpdateResult<>(
                                model,
                                List.of(new Cmd.StartGame(), new Cmd.PlaySound(SFX_CURSOR_MOVE)));
                    }
                    case 1 -> {
                        return new UpdateResult<>(
                                model,
                                List.of(new Cmd.OpenDiagnostics(), new Cmd.PlaySound(SFX_CURSOR_MOVE)));
                    }
                    case 2 -> {
                        return new UpdateResult<>(
                                model,
                                List.of(new Cmd.Exit(), new Cmd.PlaySound(SFX_CURSOR_MOVE)));
                    }
                    default -> {
                        return new UpdateResult<>(
                                model,
                                List.of(new Cmd.PlaySound(SFX_CURSOR_MOVE)));
                    }
                }
            }
        }
        return new UpdateResult<>(model, null);
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
            buffer.print(Math.max(0, titleStartCol), titleStartRow + i, line, HOVER_TEXT, BG);
        }

        // Draw options
        int optionsStartRow = titleStartRow + titleArt.length + 3;

        int maxOptLen = 0;
        for (String opt : OPTIONS) {
            maxOptLen = Math.max(maxOptLen, TextUtil.getDisplayWidth(opt));
        }
        int boxWidth = maxOptLen + 12;
        int boxHeight = OPTIONS.length * 2 + 1;
        int boxX = (cols - boxWidth) / 2;
        int boxY = optionsStartRow - 1;

        PanelComponent.drawBox(
                buffer,
                boxX, boxY, boxWidth, boxHeight,
                BORDER, BG);

        int[] cursorInfo = PanelComponent.drawLeftAlignedOptions(
                buffer,
                boxX, boxWidth, optionsStartRow,
                OPTIONS,
                model.selectedIndex(), 2, 2,
                NORMAL_TEXT, HOVER_TEXT, BG);

        int cursorCol = cursorInfo[0];
        int cursorRow = cursorInfo[1];

        UiEffect.Glitch glitch = glitchEffect.update(nowMillis);
        List<UiEffect> effects = new ArrayList<>();
        effects.add(new UiEffect.Crt(0.3f));
        if (glitch != null) {
            effects.add(glitch);
        }

        CursorState cursor = new CursorState(cursorCol, cursorRow, true, true, CURSOR);
        return new RenderFrame(buffer, cursor, List.copyOf(effects));
    }

    private static String[] loadRandomTitleArt() {
        String selected = TITLE_RESOURCES[new Random().nextInt(TITLE_RESOURCES.length)];
        try (InputStream is = StartPage.class.getResourceAsStream(selected)) {
            if (is == null) {
                return FALLBACK_TITLE_ART;
            }
            String[] content = new String(is.readAllBytes(), StandardCharsets.UTF_8).split("\r?\n");
            List<String> lines = new ArrayList<>(List.of(content));
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
