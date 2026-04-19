package com.algoblock.gl.ui.pages.diagnostics;

import com.algoblock.gl.input.intent.InputIntent;
import com.algoblock.gl.renderer.core.RenderFrame;
import com.algoblock.gl.renderer.core.TerminalBuffer;
import com.algoblock.gl.ui.components.CMatrixComponent;
import com.algoblock.gl.ui.tea.Program;
import com.algoblock.gl.ui.tea.UpdateResult;

import java.util.List;

public class DiagnosticsPage implements Program<DiagnosticsPage.Model, DiagnosticsPage.Msg, DiagnosticsPage.Cmd> {

    public enum State {
        MENU,
        DISPLAY_TEST,
        FONT_DIAGNOSTIC
    }

    public record Model(State state, int selectedIndex) {
        public static Model init() {
            return new Model(State.MENU, 0);
        }
    }

    public sealed interface Msg {
        record Intent(InputIntent intent) implements Msg {
        }
    }

    public sealed interface Cmd {
        record ReturnToStart() implements Cmd {
        }

        record PlaySound(String resourcePath) implements Cmd {
        }
    }

    private final DisplayTestPattern displayTest = new DisplayTestPattern();
    private final FontDiagnosticTestPattern fontDiag = new FontDiagnosticTestPattern();
    private final CMatrixComponent cmatrix = new CMatrixComponent();
    private static final int BG = 0x0D1117;

    @Override
    public Model init() {
        return Model.init();
    }

    @Override
    public UpdateResult<Model, Cmd> update(Model model, Msg msg) {
        if (msg instanceof Msg.Intent intentMsg) {
            InputIntent intent = intentMsg.intent();
            if (model.state() == State.MENU) {
                if (intent instanceof InputIntent.NavigatePrev) {
                    int next = model.selectedIndex() - 1;
                    if (next < 0)
                        next = 2;
                    return new UpdateResult<>(new Model(State.MENU, next), List.of());
                } else if (intent instanceof InputIntent.NavigateNext) {
                    int next = (model.selectedIndex() + 1) % 3;
                    return new UpdateResult<>(new Model(State.MENU, next), List.of());
                } else if (intent instanceof InputIntent.Submit) {
                    if (model.selectedIndex() == 0) {
                        displayTest.reset();
                        return new UpdateResult<>(new Model(State.DISPLAY_TEST, model.selectedIndex()),
                                List.of(new Cmd.PlaySound("/assets/audio/type_in.mp3")));
                    } else if (model.selectedIndex() == 1) {
                        return new UpdateResult<>(new Model(State.FONT_DIAGNOSTIC, model.selectedIndex()),
                                List.of(new Cmd.PlaySound("/assets/audio/type_in.mp3")));
                    } else if (model.selectedIndex() == 2) {
                        return new UpdateResult<>(model,
                                List.of(new Cmd.ReturnToStart(), new Cmd.PlaySound("/assets/audio/type_in.mp3")));
                    }
                } else if (intent instanceof InputIntent.Cancel) {
                    return new UpdateResult<>(model, List.of(new Cmd.ReturnToStart()));
                }
            } else {
                // Inside a test
                if (intent instanceof InputIntent.Cancel || intent instanceof InputIntent.Submit) {
                    return new UpdateResult<>(new Model(State.MENU, model.selectedIndex()),
                            List.of(new Cmd.PlaySound("/assets/audio/type_in.mp3")));
                }
            }
        }
        return new UpdateResult<>(model, List.of());
    }

    @Override
    public RenderFrame view(Model model, TerminalBuffer buffer, long nowMillis) {
        buffer.clear();
        if (model.state() == State.DISPLAY_TEST) {
            return displayTest.renderTo(buffer, nowMillis / 1000.0);
        } else if (model.state() == State.FONT_DIAGNOSTIC) {
            fontDiag.renderTo(buffer, nowMillis / 1000.0);
            return new RenderFrame(buffer, -1, -1, false, false, 0, List.of());
        }

        // Render MENU
        cmatrix.update(buffer.cols(), buffer.rows(), nowMillis);
        cmatrix.render(buffer);

        int rows = buffer.rows();
        int cols = buffer.cols();
        String title = " Diagnostics ";
        String[] options = { "Display Test Pattern", "Font Diagnostic", "Return to Main Menu" };
        int startRow = rows / 4 + 4;

        int maxOptLen = 0;
        for (String opt : options) {
            maxOptLen = Math.max(maxOptLen, opt.length());
        }
        int boxWidth = maxOptLen + 12;
        int boxHeight = options.length * 2 + 3;
        int boxX = (cols - boxWidth) / 2;
        int boxY = startRow - 2;

        com.algoblock.gl.ui.components.PanelComponent.drawBoxWithTitle(
                buffer, boxX, boxY, boxWidth, boxHeight, title, 0x555555, BG, 0x22CC22);

        int[] cursorInfo = com.algoblock.gl.ui.components.PanelComponent.drawLeftAlignedOptions(
                buffer, boxX, boxWidth, startRow, options, model.selectedIndex(), 2, 2, 0x888888, 0xFFFFFF, BG);

        int cursorCol = cursorInfo[0];
        int cursorRow = cursorInfo[1];

        return new RenderFrame(buffer, cursorCol, cursorRow, true, true, 0x22CC22, List.of());
    }
}
