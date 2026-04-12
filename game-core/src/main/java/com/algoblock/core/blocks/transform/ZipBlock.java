package com.algoblock.core.blocks.transform;

import com.algoblock.api.BinaryBlock;
import com.algoblock.api.BlockMeta;
import com.algoblock.api.EvalContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@BlockMeta(name = "Zip", signature = "Collection<T> x Int -> List<List<T>>", description = "按长度分组", arity = 2)
public class ZipBlock extends BinaryBlock<Object, Object, List<List<?>>> {
    @Override
    public List<List<?>> evaluate(EvalContext ctx) {
        ctx.consumeStep();
        Object rawLeft = left.evaluate(ctx);
        Object rawRight = right.evaluate(ctx);
        if (!(rawLeft instanceof Collection<?> collection)) {
            return List.of(List.of(rawLeft));
        }
        int size = 2;
        if (rawRight instanceof Number number) {
            size = Math.max(1, number.intValue());
        }
        List<List<?>> out = new ArrayList<>();
        List<Object> current = new ArrayList<>(size);
        for (Object item : collection) {
            current.add(item);
            if (current.size() == size) {
                out.add(new ArrayList<>(current));
                current.clear();
            }
        }
        if (!current.isEmpty()) {
            out.add(new ArrayList<>(current));
        }
        return out;
    }
}
