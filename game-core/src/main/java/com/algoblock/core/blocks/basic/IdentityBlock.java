package com.algoblock.core.blocks.basic;

import com.algoblock.api.BlockMeta;
import com.algoblock.api.EvalContext;
import com.algoblock.api.UnaryBlock;

@BlockMeta(name = "Identity", signature = "T -> T", description = "原样返回", arity = 1)
public class IdentityBlock extends UnaryBlock<Object, Object> {
    @Override
    public Object evaluate(EvalContext ctx) {
        ctx.consumeStep();
        return child.evaluate(ctx);
    }
}
