package com.algoblock.gl.input.event;

import com.algoblock.gl.input.InputKey;

public record KeyEvent(InputKey key) implements InputEvent {
}
