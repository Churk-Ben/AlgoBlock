package com.algoblock.gl.ui.pages.diagnostics;

import com.algoblock.gl.input.intent.InputIntent;
import com.algoblock.gl.renderer.core.RenderFrame;
import com.algoblock.gl.renderer.core.TerminalBuffer;
import com.algoblock.gl.ui.effect.GlitchEffect;

import com.algoblock.gl.renderer.effect.UiEffect;
import com.algoblock.gl.renderer.cursor.CursorState;
import com.algoblock.gl.ui.components.CMatrixComponent;
import com.algoblock.gl.ui.components.PanelComponent;
import com.algoblock.gl.ui.tea.Program;
import com.algoblock.gl.ui.tea.UpdateResult;
import com.algoblock.gl.utils.TextUtil;

import java.util.List;

// 诊断页面
public class DiagnosticsPage implements Program<DiagnosticsPage.Model, DiagnosticsPage.Msg, DiagnosticsPage.Cmd> {
    // 页面状态
    public enum State {
        MENU,
        DISPLAY_TEST,
        FONT_DIAGNOSTIC
    }

    // 页面模型
    public record Model(State state, int selectedIndex) {
        public static Model init() {
            return new Model(State.MENU, 0);
        }
    }

    // 页面消息
    public sealed interface Msg {
        record Intent(InputIntent intent) implements Msg {
        }
    }

    // 页面命令
    public sealed interface Cmd {
        record ReturnToStart() implements Cmd {
        }

        record PlaySound(String resourcePath) implements Cmd {
        }
    }

    // 页面组件
    private final DisplayTestPattern displayTest = new DisplayTestPattern();
    private final FontDiagnosticTestPattern fontDiag = new FontDiagnosticTestPattern();

    private final CMatrixComponent cmatrix = new CMatrixComponent();
    private final GlitchEffect glitchEffect = new GlitchEffect();

    private static final String SFX_CURSOR_MOVE = "/assets/audio/sfx/cursor-move.mp3";

    private static final int BG = 0x0D1117;
    private static final int BORDER = 0x555555;
    private static final int CURSOR = 0x22CC22;
    private static final int NORMAL_TEXT = 0x888888;
    private static final int HOVER_TEXT = 0xFFFFFF;

    @Override
    public Model init() {
        return Model.init();
    }

    @Override
    public UpdateResult<Model, Cmd> update(Model model, Msg msg) {
        if (msg instanceof Msg.Intent(InputIntent intent)) {
            if (model.state() == State.MENU) {
                // 菜单导航
                if (intent instanceof InputIntent.NavigatePrev) {
                    int next = model.selectedIndex() == 0 ? 2 : model.selectedIndex() - 1;
                    return new UpdateResult<>(
                            new Model(State.MENU, next),
                            List.of(new Cmd.PlaySound(SFX_CURSOR_MOVE)));

                } else if (intent instanceof InputIntent.NavigateNext) {
                    int next = (model.selectedIndex() + 1) % 3;
                    return new UpdateResult<>(
                            new Model(State.MENU, next),
                            List.of(new Cmd.PlaySound(SFX_CURSOR_MOVE)));

                } else if (intent instanceof InputIntent.Submit) {
                    switch (model.selectedIndex()) {
                        case 0 -> {
                            displayTest.reset();
                            return new UpdateResult<>(
                                    new Model(State.DISPLAY_TEST, model.selectedIndex()),
                                    List.of(new Cmd.PlaySound(SFX_CURSOR_MOVE)));
                        }
                        case 1 -> {
                            return new UpdateResult<>(
                                    new Model(State.FONT_DIAGNOSTIC, model.selectedIndex()),
                                    List.of(new Cmd.PlaySound(SFX_CURSOR_MOVE)));
                        }
                        case 2 -> {
                            return new UpdateResult<>(
                                    model,
                                    List.of(new Cmd.ReturnToStart(), new Cmd.PlaySound(SFX_CURSOR_MOVE)));
                        }
                        default -> {
                            return new UpdateResult<>(
                                    model,
                                    List.of(new Cmd.PlaySound(SFX_CURSOR_MOVE)));
                        }
                    }
                } else if (intent instanceof InputIntent.Cancel) {
                    return new UpdateResult<>(
                            model,
                            List.of(new Cmd.ReturnToStart(), new Cmd.PlaySound(SFX_CURSOR_MOVE)));
                }
            } else {
                // 在测试中
                if (intent instanceof InputIntent.Cancel || intent instanceof InputIntent.Submit) {
                    return new UpdateResult<>(
                            new Model(State.MENU, model.selectedIndex()),
                            List.of(new Cmd.PlaySound(SFX_CURSOR_MOVE)));
                }
            }
        }
        return new UpdateResult<>(model, null);
    }

    @Override
    public RenderFrame view(Model model, TerminalBuffer buffer, long nowMillis) {
        buffer.clear();

        switch (model.state()) {
            case State.DISPLAY_TEST -> {
                return displayTest.renderTo(buffer, nowMillis / 1000.0);
            }

            case State.FONT_DIAGNOSTIC -> {
                return fontDiag.renderTo(buffer, nowMillis / 1000.0);
            }

            case State.MENU -> {
                cmatrix.update(buffer.cols(), buffer.rows(), nowMillis);
                cmatrix.render(buffer);

                int rows = buffer.rows();
                int cols = buffer.cols();
                String title = " Live ";
                String[] options = { "Test Render", "Test Font", "chroot" };
                int startRow = rows / 4 + 4;

                int maxOptLen = 0;
                for (String opt : options) {
                    maxOptLen = Math.max(maxOptLen, TextUtil.getDisplayWidth(opt));
                }
                int boxWidth = maxOptLen + 12;
                int boxHeight = options.length * 2 + 3;
                int boxX = (cols - boxWidth) / 2;
                int boxY = startRow - 2;

                PanelComponent.drawBoxWithTitle(
                        buffer,
                        boxX, boxY, boxWidth, boxHeight,
                        title,
                        BORDER, BG, HOVER_TEXT);

                int[] cursorInfo = PanelComponent.drawLeftAlignedOptions(
                        buffer,
                        boxX, boxWidth, startRow,
                        options,
                        model.selectedIndex(), 2, 2,
                        NORMAL_TEXT, HOVER_TEXT, BG);

                int cursorCol = cursorInfo[0];
                int cursorRow = cursorInfo[1];

                UiEffect.Glitch glitch = glitchEffect.update(nowMillis);
                List<UiEffect> effects = new java.util.ArrayList<>();
                effects.add(new UiEffect.Crt(0.3f));
                if (glitch != null) {
                    effects.add(glitch);
                }

                CursorState cursor = new CursorState(cursorCol, cursorRow, true, true, CURSOR);
                return new RenderFrame(buffer, cursor, effects);

            }

            default -> {
                return new RenderFrame(buffer, null, null);
            }
        }
    }
}
