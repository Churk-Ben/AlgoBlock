package com.algoblock.core.blocks.fn;

import com.algoblock.api.EvalContext;
import com.algoblock.api.NullaryBlock;

public class ConstIntBlock extends NullaryBlock<Integer> {
    private final int value;

    public ConstIntBlock(int value) {
        this.value = value;
    }

    @Override
    public Integer evaluate(EvalContext ctx) {
        ctx.consumeStep();
        return value;
    }
}
