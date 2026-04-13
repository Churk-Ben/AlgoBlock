package com.algoblock.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.algoblock.core.engine.BlockRegistry;
import com.algoblock.core.engine.GameCoreService;
import com.algoblock.core.engine.SubmissionResult;
import com.algoblock.core.levels.Level;
import com.algoblock.core.levels.LevelLoader;
import org.junit.jupiter.api.Test;
import java.util.List;

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

    @Test
    void shouldRejectInvalidSyntax() {
        Level level = new Level(1, 1, "test", "test", List.of(1), List.of(1), List.of("_INPUT_", "Invalid", "Syntax"),
                List.of(), List.of(), 5, 10, 100, "");
        GameCoreService service = new GameCoreService(new BlockRegistry());
        SubmissionResult result = service.submit(level, "Invalid<Syntax", 20);
        assertFalse(result.accepted());
        assertTrue(result.message().startsWith("解析失败"));
    }

    @Test
    void shouldRejectUnusedAvailableBlocks() {
        Level level = new Level(1, 1, "test", "test", List.of(1), List.of(1), List.of("_INPUT_", "Identity"), List.of(),
                List.of(), 5, 10, 100, "");
        GameCoreService service = new GameCoreService(new BlockRegistry());
        SubmissionResult result = service.submit(level, "Map<_INPUT_>", 20); // Map is not in availableBlocks
        assertFalse(result.accepted());
        assertEquals("使用了未开放积木", result.message());
    }

    @Test
    void shouldRejectMissingForcedBlocks() {
        Level level = new Level(1, 1, "test", "test", List.of(1), List.of(1), List.of("_INPUT_", "Identity", "Sort"),
                List.of("Sort"), List.of(), 5, 10, 100, "");
        GameCoreService service = new GameCoreService(new BlockRegistry());
        SubmissionResult result = service.submit(level, "Identity<_INPUT_>", 20);
        assertFalse(result.accepted());
        assertEquals("缺少必须积木", result.message());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    void shouldRejectNonComparableSort() {
        Level level = new Level(1, 1, "test", "test", (List) List.of(List.of(1)), (List) List.of(List.of(1)),
                List.of("_INPUT_", "Sort"), List.of(), List.of(), 5, 10, 100, "");
        GameCoreService service = new GameCoreService(new BlockRegistry());
        SubmissionResult result = service.submit(level, "Sort<_INPUT_>", 20);
        assertFalse(result.accepted());
        assertTrue(result.message().contains("运行错误") && result.message().contains("Sort requires Comparable elements"));
    }
}
