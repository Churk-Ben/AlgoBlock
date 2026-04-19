package com.algoblock.gl.ui.app;

import com.algoblock.core.engine.SubmissionResult;
import com.algoblock.gl.input.intent.InputIntent;

public sealed interface AppMsg {
    record Intent(InputIntent intent) implements AppMsg {
    }

    record SubmitFinished(SubmissionResult result) implements AppMsg {
    }
}
