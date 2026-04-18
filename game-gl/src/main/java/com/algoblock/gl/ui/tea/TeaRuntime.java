package com.algoblock.gl.ui.tea;

import com.algoblock.gl.renderer.RenderFrame;
import com.algoblock.gl.renderer.TerminalBuffer;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The generic runtime engine for a TEA application.
 * Manages the central model state, message dispatching, and command execution.
 */
public class TeaRuntime<M, Msg, Cmd> implements AutoCloseable {
    private final Program<M, Msg, Cmd> program;
    private final CmdHandler<Cmd, Msg> cmdHandler;
    private final AtomicReference<M> modelRef;

    public TeaRuntime(Program<M, Msg, Cmd> program, CmdHandler<Cmd, Msg> cmdHandler) {
        this.program = program;
        this.cmdHandler = cmdHandler;
        this.modelRef = new AtomicReference<>(program.init());
    }

    public synchronized void dispatch(Msg msg) {
        M current = modelRef.get();
        UpdateResult<M, Cmd> result = program.update(current, msg);
        modelRef.set(result.model());
        if (result.commands() != null) {
            for (Cmd cmd : result.commands()) {
                cmdHandler.handle(cmd, this::dispatch);
            }
        }
    }

    public synchronized M snapshotModel() {
        return modelRef.get();
    }

    public RenderFrame render(TerminalBuffer buffer, long nowMillis) {
        return program.view(snapshotModel(), buffer, nowMillis);
    }

    @Override
    public void close() {
        cmdHandler.close();
    }
}
