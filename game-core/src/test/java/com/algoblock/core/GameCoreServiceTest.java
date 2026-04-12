package com.algoblock.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.algoblock.core.engine.BlockRegistry;
import com.algoblock.core.engine.GameCoreService;
import com.algoblock.core.engine.SubmissionResult;
import com.algoblock.core.levels.Level;
import com.algoblock.core.levels.LevelLoader;
import org.junit.jupiter.api.Test;

class GameCoreServiceTest {
    @Test
    void shouldAcceptLevel2ReferenceExpression() {
        LevelLoader loader = new LevelLoader();
        Level level = loader.loadFromResource("/levels/level-2.json");
        GameCoreService service = new GameCoreService(new BlockRegistry());
        SubmissionResult result = service.submit(level, "Array<PopEach<PrioQueue<_INPUT_>>>", 20);
        assertTrue(result.accepted());
        assertTrue(result.score().stars() >= 1);
    }
}
