package com.algoblock.gl.ui.app;

import com.algoblock.core.levels.Level;
import com.algoblock.gl.ui.pages.GamePage;
import com.algoblock.gl.ui.pages.StartPage;
import com.algoblock.gl.ui.pages.diagnostics.DiagnosticsPage;

public record AppModel(
        Screen screen,
        StartPage.Model startModel,
        GamePage.Model gameModel,
        DiagnosticsPage.Model diagnosticsModel,
        Level currentLevel) {

    public enum Screen {
        START, GAME, DIAGNOSTICS
    }

    public static AppModel init(Level level, long startEpochSeconds) {
        return new AppModel(
                Screen.START,
                StartPage.Model.init(),
                GamePage.Model.init(level, startEpochSeconds),
                DiagnosticsPage.Model.init(),
                level);
    }
}
