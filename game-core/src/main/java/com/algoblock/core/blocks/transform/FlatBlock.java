package com.algoblock.core.blocks.transform;

import com.algoblock.api.BlockMeta;
import com.algoblock.api.EvalContext;
import com.algoblock.api.UnaryBlock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@BlockMeta(name = "Flat", signature = "Collection<Collection<T>> -> List<T>", description = "展平一层", arity = 1)
public class FlatBlock extends UnaryBlock<Object, List<?>> {
    @Override
    public List<?> evaluate(EvalContext ctx) {
        ctx.consumeStep();
        Object value = child.evaluate(ctx);
        if (!(value instanceof Collection<?> collection)) {
            return List.of(value);
        }
        List<Object> out = new ArrayList<>();
        for (Object item : collection) {
            if (item instanceof Collection<?> nested) {
                out.addAll(nested);
            } else {
                out.add(item);
            }
        }
        return out;
    }
}
