package com.algoblock.core.blocks.transform;

import com.algoblock.api.BinaryBlock;
import com.algoblock.api.Block;
import com.algoblock.api.BlockMeta;
import com.algoblock.api.EvalContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@BlockMeta(name = "Map", signature = "Collection<T> x Func -> List<R>", description = "逐元素映射", arity = 2)
public class MapBlock extends BinaryBlock<Object, Object, List<?>> {
    @Override
    public List<?> evaluate(EvalContext ctx) {
        ctx.consumeStep();
        Object rawLeft = left.evaluate(ctx);
        if (!(rawLeft instanceof Collection<?> collection)) {
            return List.of(rawLeft);
        }
        Block<?> mapper = right;
        List<Object> out = new ArrayList<>(collection.size());
        for (Object item : collection) {
            ctx.putVar("it", item);
            out.add(mapper.evaluate(ctx));
        }
        return out;
    }
}
