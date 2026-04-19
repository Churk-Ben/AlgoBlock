package com.algoblock.gl.ui.app;

import com.algoblock.core.engine.SubmissionResult;
import com.algoblock.gl.input.InputKey;

public sealed interface AppMsg {
    record CharTyped(char value) implements AppMsg {
    }

    record KeyPressed(InputKey key) implements AppMsg {
    }

    record MouseScrolled(double xoffset, double yoffset) implements AppMsg {
    }

    record SubmitFinished(SubmissionResult result) implements AppMsg {
    }
}
