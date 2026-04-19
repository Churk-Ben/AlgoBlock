package com.algoblock.gl.ui.pages;

import com.algoblock.core.engine.SubmissionResult;
import com.algoblock.core.levels.Level;
import com.algoblock.gl.input.InputKey;
import com.algoblock.gl.renderer.RenderFrame;
import com.algoblock.gl.renderer.TerminalBuffer;
import com.algoblock.gl.renderer.UiEffect;
import com.algoblock.gl.services.CompletionService;
import com.algoblock.gl.ui.SyntaxHighlighter;
import com.algoblock.gl.ui.components.CompleterComponent;
import com.algoblock.gl.ui.tea.Program;
import com.algoblock.gl.ui.tea.UpdateResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GamePage implements Program<GamePage.Model, GamePage.Msg, GamePage.Cmd> {
    private static final int BG = 0x0D1117;
    private static final int FG = 0xCDD9E5;
    private static final long CURSOR_SOLID_AFTER_EDIT_MS = 800L;

    private final SyntaxHighlighter highlighter = new SyntaxHighlighter();
    private final CompletionService completionService;

    public GamePage(CompletionService completionService) {
        this.completionService = completionService;
    }

    public record Model(
            Level level,
            String line,
            int cursorIndex,
            SubmissionResult lastResult,
            long startEpochSeconds,
            long cursorSolidUntilMillis,
            CompleterComponent.Model completerModel) {

        public static Model init(Level level, long startEpochSeconds) {
            return new Model(level, "", 0, null, startEpochSeconds, 0L, CompleterComponent.Model.init());
        }

        public Model withCompleterModel(CompleterComponent.Model newCompleterModel) {
            return new Model(level, line, cursorIndex, lastResult, startEpochSeconds, cursorSolidUntilMillis,
                    newCompleterModel);
        }
    }

    public sealed interface Msg {
        record CharTyped(char value) implements Msg {
        }

        record KeyPressed(InputKey key) implements Msg {
        }

        record SubmitFinished(SubmissionResult result) implements Msg {
        }
    }

    public sealed interface Cmd {
        record Submit(Level level, String source, long elapsedSeconds) implements Cmd {
        }

        record PlaySound(String resourcePath) implements Cmd {
        }
    }

    @Override
    public Model init() {
        // Will be called by AppProgram with proper arguments
        return null;
    }

    @Override
    public UpdateResult<Model, Cmd> update(Model model, Msg msg) {
        if (msg instanceof Msg.SubmitFinished submitFinished) {
            Model next = new Model(model.level(), model.line(), model.cursorIndex(), submitFinished.result(),
                    model.startEpochSeconds(), model.cursorSolidUntilMillis(), model.completerModel());
            return new UpdateResult<>(next, List.of());
        }

        if (msg instanceof Msg.CharTyped typed) {
            // Hide completer when typing normally
            UpdateResult<CompleterComponent.Model, Void> completerResult = CompleterComponent
                    .update(model.completerModel(), new CompleterComponent.Msg.Hide());

            String line = model.line();
            int cursor = clampCursor(model.cursorIndex(), line.length());
            String nextLine = line.substring(0, cursor) + typed.value() + line.substring(cursor);
            long solidUntil = System.currentTimeMillis() + CURSOR_SOLID_AFTER_EDIT_MS;
            Model next = new Model(model.level(), nextLine, cursor + 1, model.lastResult(), model.startEpochSeconds(),
                    solidUntil, completerResult.model());
            return new UpdateResult<>(next, List.of(new Cmd.PlaySound("/assets/audio/type_in.mp3")));
        }

        if (msg instanceof Msg.KeyPressed keyPressed) {
            InputKey key = keyPressed.key();

            // 1. If completer is active, route specific keys to it
            if (model.completerModel().active()) {
                if (key == InputKey.NAV_UP) {
                    UpdateResult<CompleterComponent.Model, Void> r = CompleterComponent.update(model.completerModel(),
                            new CompleterComponent.Msg.Prev());
                    return new UpdateResult<>(model.withCompleterModel(r.model()), List.of());
                }
                if (key == InputKey.NAV_DOWN) {
                    UpdateResult<CompleterComponent.Model, Void> r = CompleterComponent.update(model.completerModel(),
                            new CompleterComponent.Msg.Next());
                    return new UpdateResult<>(model.withCompleterModel(r.model()), List.of());
                }
                if (key == InputKey.SUBMIT || key == InputKey.TAB) {
                    // Confirm selection
                    int selectedIndex = model.completerModel().selectedIndex();
                    if (selectedIndex >= 0 && selectedIndex < model.completerModel().items().size()) {
                        String selected = model.completerModel().items().get(selectedIndex);
                        String line = model.line();
                        int cursor = clampCursor(model.cursorIndex(), line.length());
                        String prefix = currentPrefix(line, cursor);

                        // Replace prefix with selected item
                        String nextLine = line.substring(0, cursor - prefix.length()) + selected
                                + line.substring(cursor);
                        int nextCursor = cursor - prefix.length() + selected.length();

                        UpdateResult<CompleterComponent.Model, Void> r = CompleterComponent
                                .update(model.completerModel(), new CompleterComponent.Msg.Hide());
                        Model next = new Model(model.level(), nextLine, nextCursor, model.lastResult(),
                                model.startEpochSeconds(), System.currentTimeMillis() + CURSOR_SOLID_AFTER_EDIT_MS,
                                r.model());
                        return new UpdateResult<>(next, List.of());
                    }
                }
                if (key == InputKey.NAV_LEFT || key == InputKey.NAV_RIGHT || key == InputKey.BACKSPACE
                        || key == InputKey.DELETE) {
                    // Hide completer and let it fall through
                    UpdateResult<CompleterComponent.Model, Void> r = CompleterComponent.update(model.completerModel(),
                            new CompleterComponent.Msg.Hide());
                    model = model.withCompleterModel(r.model());
                }
            }

            // 2. Normal key handling
            String line = model.line();
            int cursor = clampCursor(model.cursorIndex(), line.length());

            if (key == InputKey.BACKSPACE && cursor > 0) {
                String nextLine = line.substring(0, cursor - 1) + line.substring(cursor);
                long solidUntil = System.currentTimeMillis() + CURSOR_SOLID_AFTER_EDIT_MS;
                Model next = new Model(model.level(), nextLine, cursor - 1, model.lastResult(),
                        model.startEpochSeconds(), solidUntil, model.completerModel());
                return new UpdateResult<>(next, List.of());
            }
            if (key == InputKey.DELETE && cursor < line.length()) {
                String nextLine = line.substring(0, cursor) + line.substring(cursor + 1);
                long solidUntil = System.currentTimeMillis() + CURSOR_SOLID_AFTER_EDIT_MS;
                Model next = new Model(model.level(), nextLine, cursor, model.lastResult(), model.startEpochSeconds(),
                        solidUntil, model.completerModel());
                return new UpdateResult<>(next, List.of());
            }
            if (key == InputKey.NAV_LEFT) {
                Model next = new Model(model.level(), line, Math.max(0, cursor - 1), model.lastResult(),
                        model.startEpochSeconds(), model.cursorSolidUntilMillis(), model.completerModel());
                return new UpdateResult<>(next, List.of());
            }
            if (key == InputKey.NAV_RIGHT) {
                Model next = new Model(model.level(), line, Math.min(line.length(), cursor + 1), model.lastResult(),
                        model.startEpochSeconds(), model.cursorSolidUntilMillis(), model.completerModel());
                return new UpdateResult<>(next, List.of());
            }
            if (key == InputKey.TAB) {
                String prefix = currentPrefix(line, cursor);
                Set<String> available = new HashSet<>(model.level().availableBlocks());
                List<String> suggestions = completionService.complete(prefix, available);

                if (!suggestions.isEmpty()) {
                    UpdateResult<CompleterComponent.Model, Void> r = CompleterComponent.update(model.completerModel(),
                            new CompleterComponent.Msg.Show(suggestions));
                    return new UpdateResult<>(model.withCompleterModel(r.model()), List.of());
                }
                return new UpdateResult<>(model, List.of());
            }
            if (key == InputKey.SUBMIT) {
                long elapsed = (System.currentTimeMillis() / 1000) - model.startEpochSeconds();
                Cmd.Submit command = new Cmd.Submit(model.level(), line, elapsed);
                Model next = new Model(model.level(), line, cursor, model.lastResult(), model.startEpochSeconds(),
                        model.cursorSolidUntilMillis(), model.completerModel());
                return new UpdateResult<>(next, List.of(command));
            }
        }
        return new UpdateResult<>(model, List.of());
    }

    @Override
    public RenderFrame view(Model model, TerminalBuffer buffer, long nowMillis) {
        buffer.print(0, 0, "Level " + model.level().id() + " - " + model.level().title(), 0x6CB6FF, BG);
        buffer.print(0, 1, model.level().story(), 0x9FB3C8, BG);
        buffer.print(0, 3, "> ", FG, BG);
        highlighter.highlight(buffer, 2, 3, model.line());

        SubmissionResult lastResult = model.lastResult();
        if (lastResult != null) {
            if (7 < buffer.rows()) {
                int color = lastResult.accepted() ? 0x3FB950 : 0xFF7B72;
                buffer.print(0, 7, "Result: " + lastResult.message(), color, BG);
            }
            if (8 < buffer.rows()) {
                buffer.print(0, 8, "Stars: " + lastResult.score().stars(), 0xE3B341, BG);
            }
        }

        // Render Completer Component
        if (model.completerModel().active()) {
            int visualCursorCol = 2 + visualOffset(model.line(), model.cursorIndex());
            // Draw completer below the input line
            CompleterComponent.view(model.completerModel(), buffer, visualCursorCol, 4);
        }

        int cursorCol = Math.min(buffer.cols() - 1, Math.max(0, 2 + visualOffset(model.line(), model.cursorIndex())));
        int cursorRow = Math.min(buffer.rows() - 1, 3);
        boolean forceSolidVisible = nowMillis < model.cursorSolidUntilMillis();
        boolean blinkVisible = forceSolidVisible || ((nowMillis / 500L) % 2L) == 0L;
        return new RenderFrame(buffer, cursorCol, cursorRow, blinkVisible, true, 0x79C0FF,
                List.of(new UiEffect.Crt(0.20f)));
    }

    private static int clampCursor(int cursor, int lineLength) {
        return Math.max(0, Math.min(lineLength, cursor));
    }

    private static String currentPrefix(String line, int cursor) {
        int i = Math.max(0, Math.min(cursor, line.length())) - 1;
        while (i >= 0) {
            char ch = line.charAt(i);
            if (!Character.isLetterOrDigit(ch) && ch != '_') {
                break;
            }
            i--;
        }
        return line.substring(i + 1, Math.max(0, Math.min(cursor, line.length())));
    }

    private static int visualOffset(String line, int cursorIndex) {
        int max = Math.max(0, Math.min(cursorIndex, line.length()));
        int width = 0;
        for (int i = 0; i < max; i++) {
            width += isWideCodePoint(line.charAt(i)) ? 2 : 1;
        }
        return width;
    }

    private static boolean isWideCodePoint(int codePoint) {
        return (codePoint >= 0x1100 && codePoint <= 0x115F)
                || (codePoint >= 0x2E80 && codePoint <= 0xA4CF)
                || (codePoint >= 0xAC00 && codePoint <= 0xD7A3)
                || (codePoint >= 0xF900 && codePoint <= 0xFAFF)
                || (codePoint >= 0xFE10 && codePoint <= 0xFE6F)
                || (codePoint >= 0xFF00 && codePoint <= 0xFF60)
                || (codePoint >= 0xFFE0 && codePoint <= 0xFFE6);
    }
}
