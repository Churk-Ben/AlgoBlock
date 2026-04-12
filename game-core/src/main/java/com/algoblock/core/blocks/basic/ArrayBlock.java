package com.algoblock.core.blocks.basic;

import com.algoblock.api.BlockMeta;
import com.algoblock.api.EvalContext;
import com.algoblock.api.UnaryBlock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@BlockMeta(name = "Array", signature = "Collection<T> -> List<T>", description = "包装为数组列表", arity = 1)
public class ArrayBlock extends UnaryBlock<Object, List<?>> {
    @Override
    public List<?> evaluate(EvalContext ctx) {
        ctx.consumeStep();
        Object value = child.evaluate(ctx);
        if (value instanceof Collection<?> collection) {
            return new ArrayList<>(collection);
        }
        return List.of(value);
    }
}
