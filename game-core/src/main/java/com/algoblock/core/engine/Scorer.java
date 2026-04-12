package com.algoblock.core.engine;

import com.algoblock.api.Block;
import com.algoblock.core.levels.Level;

public class Scorer {
    public ScoreResult score(boolean correct, Block<?> root, long elapsedSeconds, Level level) {
        boolean minimal = root.nodeCount() <= level.optimalSize();
        boolean speed = elapsedSeconds <= level.timePar();
        int stars = (correct ? 1 : 0) + (minimal ? 1 : 0) + (speed ? 1 : 0);
        return new ScoreResult(correct, minimal, speed, stars);
    }
}
