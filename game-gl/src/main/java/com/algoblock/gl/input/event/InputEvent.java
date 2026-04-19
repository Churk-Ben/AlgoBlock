package com.algoblock.gl.input.event;

public sealed interface InputEvent permits CharEvent, KeyEvent, PasteEvent, WheelEvent {
}
