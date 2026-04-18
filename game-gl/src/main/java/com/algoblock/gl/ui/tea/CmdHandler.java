package com.algoblock.gl.ui.tea;

import java.util.function.Consumer;

/**
 * A handler for side-effect commands in The Elm Architecture.
 * Implementations should execute the given command and optionally dispatch messages
 * back to the application.
 */
public interface CmdHandler<Cmd, Msg> {
    void handle(Cmd cmd, Consumer<Msg> dispatch);
    
    default void close() {
        // Optional lifecycle cleanup
    }
}
