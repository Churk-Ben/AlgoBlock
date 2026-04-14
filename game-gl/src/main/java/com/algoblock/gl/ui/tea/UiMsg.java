package com.algoblock.gl.ui.tea;

import com.algoblock.core.engine.SubmissionResult;

public sealed interface UiMsg permits UiMsg.CharTyped, UiMsg.KeyPressed, UiMsg.SubmitFinished, UiMsg.Tick {
    record CharTyped(char value) implements UiMsg {
    }

    record KeyPressed(int key) implements UiMsg {
    }

    record SubmitFinished(SubmissionResult result) implements UiMsg {
    }

    record Tick(long nowMillis) implements UiMsg {
    }
}
