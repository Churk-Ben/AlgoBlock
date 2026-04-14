package com.algoblock.gl.ui.tea;

import com.algoblock.core.levels.Level;

public sealed interface UiCommand permits UiCommand.Submit {
    record Submit(Level level, String source, long elapsedSeconds) implements UiCommand {
    }
}
