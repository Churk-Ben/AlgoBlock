package com.algoblock.gl.services;

import com.algoblock.api.BlockMeta;
import com.algoblock.core.engine.BlockRegistry;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class CompletionService {
    private final BlockRegistry registry;

    public CompletionService(BlockRegistry registry) {
        this.registry = registry;
    }

    public List<String> complete(String prefix, Set<String> availableBlocks) {
        return registry.allMeta().stream()
            .map(BlockMeta::name)
            .filter(name -> name.startsWith(prefix))
            .filter(name -> availableBlocks == null || availableBlocks.isEmpty() || availableBlocks.contains(name))
            .sorted(Comparator.naturalOrder())
            .toList();
    }
}
