package com.algoblock.gl.input;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;

import java.util.Optional;

public final class KeyMapper {
    public static Optional<InputKey> toInputKey(int glfwKey) {
        InputKey mapped = switch (glfwKey) {
            case GLFW_KEY_ENTER -> InputKey.SUBMIT;
            case GLFW_KEY_BACKSPACE -> InputKey.BACKSPACE;
            case GLFW_KEY_TAB -> InputKey.TAB;
            case GLFW_KEY_LEFT -> InputKey.NAV_LEFT;
            case GLFW_KEY_RIGHT -> InputKey.NAV_RIGHT;
            case GLFW_KEY_UP -> InputKey.NAV_UP;
            case GLFW_KEY_DOWN -> InputKey.NAV_DOWN;
            case GLFW_KEY_DELETE -> InputKey.DELETE;
            case GLFW_KEY_ESCAPE -> InputKey.CANCEL;
            default -> null;
        };
        return Optional.ofNullable(mapped);
    }
}
