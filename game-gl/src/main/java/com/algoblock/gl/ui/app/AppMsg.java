package com.algoblock.gl.ui.app;

import com.algoblock.core.engine.SubmissionResult;

public sealed interface AppMsg {
    record CharTyped(char value) implements AppMsg {
    }

    record KeyPressed(int key) implements AppMsg {
    }

    record SubmitFinished(SubmissionResult result) implements AppMsg {
    }
}
