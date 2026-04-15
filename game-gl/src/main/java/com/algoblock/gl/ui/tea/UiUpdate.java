package com.algoblock.gl.ui.tea;

import com.algoblock.core.engine.SubmissionResult;
import com.algoblock.gl.input.KeyMapper;
import com.algoblock.gl.ui.Completer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UiUpdate {
    private static final long CURSOR_SOLID_AFTER_EDIT_MS = 800L;
    private final Completer completer;

    public UiUpdate(Completer completer) {
        this.completer = completer;
    }

    public UiUpdateResult update(UiModel model, UiMsg msg) {
        if (msg instanceof UiMsg.CharTyped typed) {
            return onCharTyped(model, typed.value());
        }
        if (msg instanceof UiMsg.KeyPressed keyPressed) {
            return onKeyPressed(model, keyPressed.key());
        }
        if (msg instanceof UiMsg.SubmitFinished submitFinished) {
            UiModel next = with(model, model.line(), model.cursorIndex(), List.of(), submitFinished.result(),
                    model.cursorSolidUntilMillis());
            return new UiUpdateResult(next, List.of());
        }
        return new UiUpdateResult(model, List.of());
    }

    private UiUpdateResult onCharTyped(UiModel model, char value) {
        String line = model.line();
        int cursor = clampCursor(model.cursorIndex(), line.length());
        String nextLine = line.substring(0, cursor) + value + line.substring(cursor);
        long solidUntil = nowMillis() + CURSOR_SOLID_AFTER_EDIT_MS;
        UiModel next = with(model, nextLine, cursor + 1, List.of(), model.lastResult(), solidUntil);
        return new UiUpdateResult(next, List.of());
    }

    private UiUpdateResult onKeyPressed(UiModel model, int key) {
        String line = model.line();
        int cursor = clampCursor(model.cursorIndex(), line.length());

        if (KeyMapper.isBackspace(key) && cursor > 0) {
            String nextLine = line.substring(0, cursor - 1) + line.substring(cursor);
            long solidUntil = nowMillis() + CURSOR_SOLID_AFTER_EDIT_MS;
            UiModel next = with(model, nextLine, cursor - 1, List.of(), model.lastResult(), solidUntil);
            return new UiUpdateResult(next, List.of());
        }
        if (KeyMapper.isDelete(key) && cursor < line.length()) {
            String nextLine = line.substring(0, cursor) + line.substring(cursor + 1);
            long solidUntil = nowMillis() + CURSOR_SOLID_AFTER_EDIT_MS;
            UiModel next = with(model, nextLine, cursor, List.of(), model.lastResult(), solidUntil);
            return new UiUpdateResult(next, List.of());
        }
        if (KeyMapper.isLeft(key)) {
            UiModel next = with(model, line, Math.max(0, cursor - 1), model.suggestions(), model.lastResult(),
                    model.cursorSolidUntilMillis());
            return new UiUpdateResult(next, List.of());
        }
        if (KeyMapper.isRight(key)) {
            UiModel next = with(model, line, Math.min(line.length(), cursor + 1), model.suggestions(),
                    model.lastResult(), model.cursorSolidUntilMillis());
            return new UiUpdateResult(next, List.of());
        }
        if (KeyMapper.isTab(key)) {
            String prefix = currentPrefix(line, cursor);
            Set<String> available = new HashSet<>(model.level().availableBlocks());
            List<String> suggestions = completer.complete(prefix, available);
            UiModel next = with(model, line, cursor, suggestions, model.lastResult(), model.cursorSolidUntilMillis());
            return new UiUpdateResult(next, List.of());
        }
        if (KeyMapper.isSubmit(key)) {
            long elapsed = (System.currentTimeMillis() / 1000) - model.startEpochSeconds();
            UiCommand.Submit command = new UiCommand.Submit(model.level(), line, elapsed);
            UiModel next = with(model, line, cursor, List.of(), model.lastResult(), model.cursorSolidUntilMillis());
            return new UiUpdateResult(next, List.of(command));
        }
        return new UiUpdateResult(model, List.of());
    }

    private static UiModel with(
            UiModel base,
            String line,
            int cursorIndex,
            List<String> suggestions,
            SubmissionResult lastResult,
            long cursorSolidUntilMillis) {
        return new UiModel(
                base.level(),
                line,
                cursorIndex,
                suggestions,
                lastResult,
                base.startEpochSeconds(),
                cursorSolidUntilMillis);
    }

    private static long nowMillis() {
        return System.currentTimeMillis();
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

    public record UiUpdateResult(UiModel model, List<UiCommand> commands) {
    }
}
