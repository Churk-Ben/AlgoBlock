package com.algoblock.gl;

import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
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
import com.algoblock.core.engine.GameCoreService;
import com.algoblock.core.levels.Level;
import com.algoblock.core.levels.LevelLoader;
import com.algoblock.gl.input.GlfwInputAdapter;
import com.algoblock.gl.input.InputEventQueue;
import com.algoblock.gl.input.event.InputEvent;
import com.algoblock.gl.input.intent.IntentEnvelope;
import com.algoblock.gl.input.intent.InputIntentMapper;
import com.algoblock.gl.input.intent.InputIntentQueue;
import com.algoblock.gl.renderer.core.RenderFrame;
import com.algoblock.gl.renderer.core.TerminalBuffer;
import com.algoblock.gl.renderer.cursor.CursorRenderer;
import com.algoblock.gl.renderer.effect.EffectsRenderer;
import com.algoblock.gl.renderer.text.FontAtlas;
import com.algoblock.gl.renderer.text.TextRenderer;
import com.algoblock.gl.services.CompletionService;
import com.algoblock.gl.ui.app.AppCmd;
import com.algoblock.gl.ui.app.AppCmdHandler;
import com.algoblock.gl.ui.app.AppModel;
import com.algoblock.gl.ui.app.AppMsg;
import com.algoblock.gl.ui.app.AppProgram;
import com.algoblock.gl.ui.pages.GamePage;
import com.algoblock.gl.ui.pages.StartPage;
import com.algoblock.gl.ui.pages.diagnostics.DiagnosticsPage;
import com.algoblock.gl.ui.tea.TeaRuntime;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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

        AtomicInteger fbWidth = new AtomicInteger(1280);
        AtomicInteger fbHeight = new AtomicInteger(720);
        // Read initial size
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var w = stack.mallocInt(1);
            var h = stack.mallocInt(1);
            glfwGetFramebufferSize(window, w, h);
            fbWidth.set(w.get(0));
            fbHeight.set(h.get(0));
        }

        org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback(window, (w, width, height) -> {
            fbWidth.set(width);
            fbHeight.set(height);
        });

        LevelLoader levelLoader = new LevelLoader();
        Level level1 = levelLoader.loadFromResource("/levels/level-1.json");
        BlockRegistry registry = new BlockRegistry();
        GameCoreService service = new GameCoreService(registry);

        StartPage startPage = new StartPage();
        GamePage gamePage = new GamePage(new CompletionService(registry));
        DiagnosticsPage diagnosticsPage = new DiagnosticsPage();
        AppProgram program = new AppProgram(startPage, gamePage, diagnosticsPage, level1);
        AppCmdHandler cmdHandler = new AppCmdHandler(service);

        TeaRuntime<AppModel, AppMsg, AppCmd> uiRuntime = new TeaRuntime<>(program, cmdHandler);
        InputEventQueue eventQueue = new InputEventQueue();
        InputIntentQueue intentQueue = new InputIntentQueue();
        GlfwInputAdapter inputAdapter = new GlfwInputAdapter(eventQueue);
        inputAdapter.attach(window);

        Thread logicThread = new Thread(() -> {
            while (!glfwWindowShouldClose(window)) {
                try {
                    InputEvent event = eventQueue.take();
                    long nowMillis = System.currentTimeMillis();
                    List<IntentEnvelope> mappedIntents = InputIntentMapper.map(event, nowMillis);
                    for (IntentEnvelope envelope : mappedIntents) {
                        intentQueue.offer(envelope);
                    }

                    for (int i = 0; i < mappedIntents.size(); i++) {
                        IntentEnvelope envelope = intentQueue.take();
                        if (!envelope.isExpired(System.currentTimeMillis())) {
                            uiRuntime.dispatch(new AppMsg.Intent(envelope.intent()));
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "game-logic");
        logicThread.setDaemon(true);
        logicThread.start();

        Thread renderThread = new Thread(() -> {
            glfwMakeContextCurrent(window);
            glfwSwapInterval(1);
            GL.createCapabilities();

            TerminalBuffer uiBuffer = new TerminalBuffer(120, 40);

            FontAtlas fontAtlas = new FontAtlas(resolveFontPath(), 24, 1024, 1024);
            TextRenderer textRenderer = new TextRenderer(fontAtlas);
            CursorRenderer cursorRenderer = new CursorRenderer();
            EffectsRenderer effectsRenderer = new EffectsRenderer();

            while (!glfwWindowShouldClose(window)) {
                AppModel model = uiRuntime.snapshotModel();
                boolean isFontDiag = model.screen() == AppModel.Screen.DIAGNOSTICS &&
                        model.diagnosticsModel().state() == DiagnosticsPage.State.FONT_DIAGNOSTIC;
                textRenderer.setFontDiagnosticMode(isFontDiag);

                int viewportW = fbWidth.get();
                int viewportH = fbHeight.get();
                if (viewportW > 0 && viewportH > 0) {
                    textRenderer.setViewport(viewportW, viewportH);
                    int dynamicCols = textRenderer.visibleCols();
                    int dynamicRows = textRenderer.visibleRows();
                    if (uiBuffer.cols() != dynamicCols || uiBuffer.rows() != dynamicRows) {
                        uiBuffer = new TerminalBuffer(dynamicCols, dynamicRows);
                    }

                    glClearColor(0.05f, 0.07f, 0.09f, 1f);
                    glClear(GL_COLOR_BUFFER_BIT);

                    RenderFrame uiFrame = uiRuntime.render(uiBuffer, System.currentTimeMillis());
                    TerminalBuffer renderBuffer = uiFrame != null ? uiFrame.textBuffer() : uiBuffer;

                    textRenderer.upload(renderBuffer);
                    textRenderer.draw();
                    if (uiFrame != null) {
                        cursorRenderer.draw(uiFrame, textRenderer, glfwGetTime());
                        effectsRenderer.draw(uiFrame, textRenderer, glfwGetTime());
                    }
                    glfwSwapBuffers(window);
                }
            }
        }, "game-render");
        renderThread.start();

        while (!glfwWindowShouldClose(window)) {
            org.lwjgl.glfw.GLFW.glfwWaitEventsTimeout(0.1);
            glfwSetWindowTitle(window, "AlgoBlock");
        }

        try {
            renderThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        uiRuntime.close();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private static String resolveFontPath() {
        return "assets/fonts/MapleMono-NF-CN-unhinted/MapleMono-NF-CN-Regular.ttf";
    }
}
