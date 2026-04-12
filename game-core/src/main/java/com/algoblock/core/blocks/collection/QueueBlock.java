package com.algoblock.core.blocks.collection;

import com.algoblock.api.BlockMeta;
import com.algoblock.api.EvalContext;
import com.algoblock.api.UnaryBlock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@BlockMeta(name = "Queue", signature = "Collection<T> -> List<T>", description = "FIFO 保序输出", arity = 1)
public class QueueBlock extends UnaryBlock<Object, List<?>> {
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
