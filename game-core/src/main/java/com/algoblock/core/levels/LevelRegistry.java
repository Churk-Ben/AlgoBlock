package com.algoblock.core.levels;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelRegistry {
    private final Map<Integer, Level> map = new HashMap<>();

    public void register(Level level) {
        map.put(level.id(), level);
    }

    public Level find(int id) {
        return map.get(id);
    }

    public List<Level> all() {
        return map.values().stream().sorted((a, b) -> Integer.compare(a.id(), b.id())).toList();
    }
}
