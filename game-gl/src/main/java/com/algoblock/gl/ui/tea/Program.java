package com.algoblock.gl.ui.tea;

import com.algoblock.gl.renderer.RenderFrame;
import com.algoblock.gl.renderer.TerminalBuffer;

/**
 * The core abstraction of The Elm Architecture (TEA).
 * A Program defines the model state, how it updates based on messages, and how it renders.
 */
public interface Program<M, Msg, Cmd> {
    M init();

    UpdateResult<M, Cmd> update(M model, Msg msg);

    RenderFrame view(M model, TerminalBuffer buffer, long nowMillis);
}
