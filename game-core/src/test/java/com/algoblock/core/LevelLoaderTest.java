package com.algoblock.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.algoblock.core.levels.Level;
import com.algoblock.core.levels.LevelLoader;
import java.util.List;
import org.junit.jupiter.api.Test;

class LevelLoaderTest {
    @Test
    void shouldLoadFirstFourLevels() {
        LevelLoader loader = new LevelLoader();
        List<Level> levels = loader.loadRange(1, 4);
        assertEquals(4, levels.size());
        assertEquals("Hello Sort", levels.get(0).title());
        assertFalse(levels.get(1).availableBlocks().isEmpty());
    }
}
