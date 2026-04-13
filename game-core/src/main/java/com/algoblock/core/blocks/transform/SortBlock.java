package com.algoblock.core.blocks.transform;

import com.algoblock.api.BlockMeta;
import com.algoblock.api.EvalContext;
import com.algoblock.api.UnaryBlock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@BlockMeta(name = "Sort", signature = "Collection<T> -> List<T>", description = "自然序排序", arity = 1)
public class SortBlock extends UnaryBlock<Object, List<?>> {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List<?> evaluate(EvalContext ctx) {
        ctx.consumeStep();
        Object value = child.evaluate(ctx);
        if (!(value instanceof Collection<?> collection)) {
            return List.of(value);
        }
        List list = new ArrayList<>(collection);
        if (!list.isEmpty() && !(list.get(0) instanceof Comparable)) {
            throw new RuntimeException("Sort requires Comparable elements, but got " + list.get(0).getClass().getSimpleName());
        }
        try {
            list.sort(Comparator.naturalOrder());
        } catch (ClassCastException e) {
            throw new RuntimeException("Sort requires mutually Comparable elements");
        }
        return list;
    }
}
