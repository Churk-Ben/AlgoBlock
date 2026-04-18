package com.algoblock.gl.ui.tea;

import java.util.List;

/**
 * The result of an update function in TEA.
 * It contains the new model state and a list of side-effect commands to be executed.
 */
public record UpdateResult<M, Cmd>(M model, List<Cmd> commands) {
}
