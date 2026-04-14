package com.algoblock.gl.ui.tea;

import com.algoblock.core.engine.GameCoreService;
import com.algoblock.core.engine.SubmissionResult;
import com.algoblock.gl.renderer.RenderFrame;
import com.algoblock.gl.renderer.TerminalBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class UiRuntime implements AutoCloseable {
    private final UiUpdate update;
    private final UiView view;
    private final GameCoreService service;
    private final ExecutorService commandExecutor;
    private final AtomicReference<UiModel> modelRef;

    public UiRuntime(UiUpdate update, UiView view, GameCoreService service, UiModel initialModel) {
        this.update = update;
        this.view = view;
        this.service = service;
        this.modelRef = new AtomicReference<>(initialModel);
        this.commandExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "ui-command-executor");
            t.setDaemon(true);
            return t;
        });
    }

    public synchronized void dispatch(UiMsg msg) {
        UiModel current = modelRef.get();
        UiUpdate.UiUpdateResult result = update.update(current, msg);
        modelRef.set(result.model());
        for (UiCommand command : result.commands()) {
            execute(command);
        }
    }

    public synchronized UiModel snapshotModel() {
        return modelRef.get();
    }

    public RenderFrame render(TerminalBuffer buffer, long nowMillis) {
        return view.render(snapshotModel(), buffer, nowMillis);
    }

    @Override
    public void close() {
        commandExecutor.shutdownNow();
    }

    private void execute(UiCommand command) {
        if (command instanceof UiCommand.Submit submit) {
            commandExecutor.submit(() -> {
                SubmissionResult result = service.submit(submit.level(), submit.source(), submit.elapsedSeconds());
                dispatch(new UiMsg.SubmitFinished(result));
            });
        }
    }
}
