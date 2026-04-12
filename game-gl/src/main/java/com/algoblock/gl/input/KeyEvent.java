package com.algoblock.gl.input;

public record KeyEvent(int key, int action, int mods) implements InputEvent {
}
