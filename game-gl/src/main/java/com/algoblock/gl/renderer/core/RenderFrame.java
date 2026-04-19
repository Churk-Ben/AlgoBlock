package com.algoblock.gl.renderer.core;

import java.util.List;

import com.algoblock.gl.renderer.effect.UiEffect;

public record RenderFrame(
        TerminalBuffer textBuffer,
        int cursorCol,
        int cursorRow,
        boolean cursorVisible,
        boolean cursorBlockStyle,
        int cursorColor,
        List<UiEffect> effects) {
}
