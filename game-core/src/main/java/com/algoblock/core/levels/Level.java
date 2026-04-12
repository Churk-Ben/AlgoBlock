package com.algoblock.core.levels;

import java.util.List;

public record Level(
    int schemaVersion,
    int id,
    String title,
    String story,
    List<Integer> input,
    List<Integer> output,
    List<String> availableBlocks,
    List<String> forcedBlocks,
    List<List<String>> bonusCombos,
    int optimalSize,
    int timePar,
    int stepBudget,
    String discoveryHint
) {
}
