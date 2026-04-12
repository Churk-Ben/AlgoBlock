package com.algoblock.core.blocks.transform;

import com.algoblock.api.BinaryBlock;
import com.algoblock.api.Block;
import com.algoblock.api.BlockMeta;
import com.algoblock.api.EvalContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@BlockMeta(name = "Filter", signature = "Collection<T> x Pred -> List<T>", description = "逐元素过滤", arity = 2)
public class FilterBlock extends BinaryBlock<Object, Object, List<?>> {
    @Override
    public List<?> evaluate(EvalContext ctx) {
        ctx.consumeStep();
        Object rawLeft = left.evaluate(ctx);
        if (!(rawLeft instanceof Collection<?> collection)) {
            return List.of(rawLeft);
        }
        Block<?> predicate = right;
        List<Object> out = new ArrayList<>();
        for (Object item : collection) {
            ctx.putVar("it", item);
            Object keep = predicate.evaluate(ctx);
            if (keep instanceof Boolean b && b) {
                out.add(item);
            }
        }
        return out;
    }
}
