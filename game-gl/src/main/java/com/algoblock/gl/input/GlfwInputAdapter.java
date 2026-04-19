package com.algoblock.gl.input;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

import com.algoblock.gl.input.event.CharEvent;
import com.algoblock.gl.input.event.KeyEvent;
import com.algoblock.gl.input.event.WheelEvent;

public final class GlfwInputAdapter {
    private final InputEventQueue eventQueue;

    public GlfwInputAdapter(InputEventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }

    public void attach(long window) {
        glfwSetCharCallback(window, (w, codepoint) -> eventQueue.offer(new CharEvent((char) codepoint)));
        glfwSetScrollCallback(window, (w, xoffset, yoffset) -> eventQueue.offer(new WheelEvent(xoffset, yoffset)));
        glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            if (isAcceptedAction(action)) {
                KeyMapper.toInputKey(key).ifPresent(mappedKey -> eventQueue.offer(new KeyEvent(mappedKey)));
            }
        });
    }

    private static boolean isAcceptedAction(int action) {
        return action == GLFW_PRESS || action == GLFW_REPEAT;
    }

}
