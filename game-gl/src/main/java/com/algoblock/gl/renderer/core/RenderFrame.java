package com.algoblock.gl.renderer.core;

import com.algoblock.gl.renderer.effect.UiEffect;
import com.algoblock.gl.renderer.cursor.CursorState;

import java.util.List;

public record RenderFrame(TerminalBuffer textBuffer, CursorState cursorState, List<UiEffect> effects) {
}
