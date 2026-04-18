package com.algoblock.gl.ui.app;

import com.algoblock.core.levels.Level;
import com.algoblock.gl.ui.pages.GamePage;
import com.algoblock.gl.ui.pages.StartPage;

public record AppModel(
        Screen screen,
        StartPage.Model startModel,
        GamePage.Model gameModel,
        Level currentLevel) {

    public enum Screen {
        START, GAME
    }

    public static AppModel init(Level level, long startEpochSeconds) {
        return new AppModel(
                Screen.START,
                StartPage.Model.init(),
                GamePage.Model.init(level, startEpochSeconds),
                level);
    }
}
