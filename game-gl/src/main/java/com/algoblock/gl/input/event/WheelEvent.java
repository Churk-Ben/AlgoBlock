package com.algoblock.gl.input.event;

public record WheelEvent(double xoffset, double yoffset) implements InputEvent {
}