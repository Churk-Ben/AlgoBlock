package com.algoblock.gl.input;

public record WheelEvent(double xoffset, double yoffset) implements InputEvent {
}