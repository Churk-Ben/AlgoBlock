package com.algoblock.core.blocks.io;

import com.algoblock.api.BlockMeta;
import com.algoblock.api.EvalContext;
import com.algoblock.api.NullaryBlock;
import java.util.ArrayList;
import java.util.List;

@BlockMeta(name = "_INPUT_", signature = "_INPUT_ -> List<?>", description = "输入序列", arity = 0)
public class InputBlock extends NullaryBlock<List<?>> {
    @Override
    public List<?> evaluate(EvalContext ctx) {
        ctx.consumeStep();
        return new ArrayList<>(ctx.getInput());
    }
}
