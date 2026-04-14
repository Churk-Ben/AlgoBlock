package com.algoblock.gl.renderer;

public record RenderFrame(
        TerminalBuffer textBuffer,
        int cursorCol,
        int cursorRow,
        boolean cursorVisible,
        boolean cursorBlockStyle,
        int cursorColor,
        float effectStrength) {
}
