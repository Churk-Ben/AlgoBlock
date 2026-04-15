package com.algoblock.gl.ui.tea;

import com.algoblock.core.engine.SubmissionResult;
import com.algoblock.core.levels.Level;
import java.util.List;

public record UiModel(
        Screen screen,
        Level level,
        String line,
        int cursorIndex,
        List<String> suggestions,
        SubmissionResult lastResult,
        long startEpochSeconds,
        long cursorSolidUntilMillis) {

    public enum Screen {
        START, GAME
    }

    public static UiModel initial(Level level, long nowEpochSeconds) {
        return new UiModel(Screen.START, level, "", 0, List.of(), null, nowEpochSeconds, 0L);
    }
}
