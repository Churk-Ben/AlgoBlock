package com.algoblock.gl;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F2;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import com.algoblock.core.engine.BlockRegistry;
import com.algoblock.core.levels.Level;
import com.algoblock.core.levels.LevelLoader;
import com.algoblock.gl.input.CharEvent;
import com.algoblock.gl.input.InputEvent;
import com.algoblock.gl.input.InputEventQueue;
import com.algoblock.gl.input.KeyEvent;
import com.algoblock.gl.renderer.DisplayTestPattern;
import com.algoblock.gl.renderer.FontAtlas;
import com.algoblock.gl.renderer.TerminalBuffer;
import com.algoblock.gl.renderer.TextRenderer;
import com.algoblock.gl.ui.TerminalWidget;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

public class Main {
    public static void main(String[] args) {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Failed to init GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        long window = glfwCreateWindow(1280, 720, "AlgoBlock", 0, 0);
        if (window == 0L) {
            throw new IllegalStateException("Failed to create window");
        }
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        GL.createCapabilities();

        LevelLoader levelLoader = new LevelLoader();
        Level level1 = levelLoader.loadFromResource("/levels/level-1.json");
        TerminalBuffer uiBuffer = new TerminalBuffer(120, 40);
        TerminalBuffer displayTestBuffer = new TerminalBuffer(120, 40);
        BlockRegistry registry = new BlockRegistry();
        TerminalWidget widget = new TerminalWidget(uiBuffer, registry, level1);
        DisplayTestPattern displayTestPattern = new DisplayTestPattern();
        AtomicBoolean displayTestMode = new AtomicBoolean(hasDisplayTestArg(args));
        FontAtlas fontAtlas = new FontAtlas(resolveFontPath(), 24, 1024, 1024);
        TextRenderer textRenderer = new TextRenderer(fontAtlas);
        InputEventQueue eventQueue = new InputEventQueue();

        glfwSetCharCallback(window, (w, codepoint) -> eventQueue.offer(new CharEvent((char) codepoint)));
        glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS || action == GLFW_RELEASE) {
                if (key == GLFW_KEY_F2 && action == GLFW_PRESS) {
                    displayTestMode.set(!displayTestMode.get());
                    return;
                }
                if (key == GLFW_KEY_ENTER || key == GLFW_KEY_BACKSPACE || key == GLFW_KEY_TAB) {
                    eventQueue.offer(new KeyEvent(key, action, mods));
                }
            }
        });

        Thread logicThread = new Thread(() -> {
            while (!glfwWindowShouldClose(window)) {
                try {
                    InputEvent event = eventQueue.take();
                    widget.onEvent(event);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "game-logic");
        logicThread.setDaemon(true);
        logicThread.start();

        while (!glfwWindowShouldClose(window)) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                var w = stack.mallocInt(1);
                var h = stack.mallocInt(1);
                glfwGetFramebufferSize(window, w, h);
                int viewportW = w.get(0);
                int viewportH = h.get(0);
                textRenderer.setViewport(viewportW, viewportH);
                int dynamicCols = textRenderer.visibleCols();
                int dynamicRows = textRenderer.visibleRows();
                if (displayTestBuffer.cols() != dynamicCols || displayTestBuffer.rows() != dynamicRows) {
                    displayTestBuffer = new TerminalBuffer(dynamicCols, dynamicRows);
                }
            }
            glClearColor(0.05f, 0.07f, 0.09f, 1f);
            glClear(GL_COLOR_BUFFER_BIT);
            TerminalBuffer renderBuffer;
            if (displayTestMode.get()) {
                displayTestPattern.renderTo(displayTestBuffer, glfwGetTime());
                renderBuffer = displayTestBuffer;
            } else {
                renderBuffer = uiBuffer;
            }
            textRenderer.upload(renderBuffer);
            textRenderer.draw();
            glfwSetWindowTitle(window, "AlgoBlock  t=" + String.format("%.1f", glfwGetTime()));
            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private static String resolveFontPath() {
        Path p1 = Path.of("assets/fonts/MapleMono-NF-CN-unhinted/MapleMono-NF-CN-Regular.ttf");
        if (Files.exists(p1)) {
            return p1.toString();
        }
        Path p2 = Path.of("../assets/fonts/MapleMono-NF-CN-unhinted/MapleMono-NF-CN-Regular.ttf");
        if (Files.exists(p2)) {
            return p2.toString();
        }
        throw new IllegalStateException("MapleMono-NF-CN font not found in assets/fonts");
    }

    private static boolean hasDisplayTestArg(String[] args) {
        return Arrays.stream(args).anyMatch("--display-test"::equalsIgnoreCase);
    }
}
