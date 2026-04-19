package com.algoblock.gl.renderer;

import java.util.List;

public record RenderFrame(
        TerminalBuffer textBuffer,
        int cursorCol,
        int cursorRow,
        boolean cursorVisible,
        boolean cursorBlockStyle,
        int cursorColor,
        List<UiEffect> effects) {
}
