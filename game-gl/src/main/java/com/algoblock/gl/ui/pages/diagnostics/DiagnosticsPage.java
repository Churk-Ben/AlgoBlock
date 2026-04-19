package com.algoblock.gl.ui.pages.diagnostics;

import com.algoblock.gl.input.KeyMapper;
import com.algoblock.gl.renderer.RenderFrame;
import com.algoblock.gl.renderer.TerminalBuffer;
import com.algoblock.gl.ui.tea.Program;
import com.algoblock.gl.ui.tea.UpdateResult;
import com.algoblock.gl.ui.components.CMatrixEffect;

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
        record KeyPressed(int key) implements Msg {
        }

        record MouseScrolled(double xoffset, double yoffset) implements Msg {
        }
    }

    public sealed interface Cmd {
        record ReturnToStart() implements Cmd {
        }
    }

    private final DisplayTestPattern displayTest = new DisplayTestPattern();
    private final FontDiagnosticTestPattern fontDiag = new FontDiagnosticTestPattern();
    private final CMatrixEffect cmatrix = new CMatrixEffect();
    private static final int BG = 0x0D1117;

    @Override
    public Model init() {
        return Model.init();
    }

    @Override
    public UpdateResult<Model, Cmd> update(Model model, Msg msg) {
        if (msg instanceof Msg.KeyPressed keyPressed) {
            int key = keyPressed.key();
            if (model.state() == State.MENU) {
                if (KeyMapper.isUp(key)) {
                    int next = model.selectedIndex() - 1;
                    if (next < 0)
                        next = 2;
                    return new UpdateResult<>(new Model(State.MENU, next), List.of());
                } else if (KeyMapper.isDown(key)) {
                    int next = (model.selectedIndex() + 1) % 3;
                    return new UpdateResult<>(new Model(State.MENU, next), List.of());
                } else if (KeyMapper.isSubmit(key)) {
                    if (model.selectedIndex() == 0) {
                        return new UpdateResult<>(new Model(State.DISPLAY_TEST, model.selectedIndex()), List.of());
                    } else if (model.selectedIndex() == 1) {
                        return new UpdateResult<>(new Model(State.FONT_DIAGNOSTIC, model.selectedIndex()), List.of());
                    } else if (model.selectedIndex() == 2) {
                        return new UpdateResult<>(model, List.of(new Cmd.ReturnToStart()));
                    }
                } else if (key == 256) { // ESC
                    return new UpdateResult<>(model, List.of(new Cmd.ReturnToStart()));
                }
            } else {
                // Inside a test
                if (key == 256 || KeyMapper.isSubmit(key)) { // ESC or ENTER to exit test
                    return new UpdateResult<>(new Model(State.MENU, model.selectedIndex()), List.of());
                }
            }
        } else if (msg instanceof Msg.MouseScrolled scrolled) {
            if (model.state() == State.MENU) {
                if (scrolled.yoffset() > 0) { // Scroll up
                    int next = model.selectedIndex() - 1;
                    if (next < 0)
                        next = 2;
                    return new UpdateResult<>(new Model(State.MENU, next), List.of());
                } else if (scrolled.yoffset() < 0) { // Scroll down
                    int next = (model.selectedIndex() + 1) % 3;
                    return new UpdateResult<>(new Model(State.MENU, next), List.of());
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
        String title = " System Diagnostics ";
        // int titleCol = Math.max(0, (cols - title.length()) / 2);

        String[] options = { "Display Test Pattern", "Font Diagnostic", "Return to Main Menu" };
        int startRow = rows / 4 + 4;
        // boolean blinkVisible = ((nowMillis / 500L) % 2L) == 0L;

        int maxOptLen = 0;
        for (String opt : options) {
            maxOptLen = Math.max(maxOptLen, opt.length());
        }
        int boxWidth = maxOptLen + 12; // Padding
        int boxHeight = options.length * 2 + 1;
        int boxX = (cols - boxWidth) / 2;
        int boxY = startRow - 1;

        com.algoblock.gl.ui.components.PanelComponent.drawBoxWithTitle(
                buffer, boxX, boxY, boxWidth, boxHeight, title, 0x555555, BG, 0x00FF00);

        int cursorCol = -1;
        int cursorRow = -1;

        for (int i = 0; i < options.length; i++) {
            String text = options[i];
            int textCol = (cols - text.length()) / 2;
            int textRow = startRow + i * 2;

            if (i == model.selectedIndex()) {
                buffer.print(Math.max(0, textCol), textRow, text, 0xFFFFFF, BG);
                cursorCol = Math.max(0, textCol - 2);
                cursorRow = textRow;
            } else {
                buffer.print(Math.max(0, textCol), textRow, text, 0x888888, BG);
            }
        }

        return new RenderFrame(buffer, cursorCol, cursorRow, true, true, 0x00FF00, List.of());
    }
}