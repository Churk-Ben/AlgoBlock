package com.algoblock.gl.ui.app;

import com.algoblock.core.levels.Level;

public sealed interface AppCmd {
    record Submit(Level level, String source, long elapsedSeconds) implements AppCmd {
    }

    record PlaySound(String resourcePath) implements AppCmd {
    }
}
