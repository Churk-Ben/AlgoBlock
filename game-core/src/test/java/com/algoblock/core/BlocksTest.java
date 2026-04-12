package com.algoblock.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.algoblock.api.Block;
import com.algoblock.api.EvalContext;
import com.algoblock.core.blocks.basic.ArrayBlock;
import com.algoblock.core.blocks.collection.PrioQueueBlock;
import com.algoblock.core.blocks.collection.StackBlock;
import com.algoblock.core.blocks.io.InputBlock;
import com.algoblock.core.blocks.transform.FilterBlock;
import com.algoblock.core.blocks.transform.FlatBlock;
import com.algoblock.core.blocks.transform.MapBlock;
import com.algoblock.core.blocks.transform.ReverseBlock;
import com.algoblock.core.blocks.transform.ZipBlock;
import com.algoblock.core.blocks.fn.DoubleOpBlock;
import com.algoblock.core.blocks.fn.EvenPredBlock;
import java.util.List;
import org.junit.jupiter.api.Test;

class BlocksTest {
    @Test
    void stackShouldReverse() {
        InputBlock input = new InputBlock();
        StackBlock stack = new StackBlock();
        stack.setChild((Block) input);
        assertEquals(List.of(3, 2, 1), stack.evaluate(new EvalContext(List.of(1, 2, 3), 100)));
    }

    @Test
    void prioQueueShouldSort() {
        InputBlock input = new InputBlock();
        PrioQueueBlock block = new PrioQueueBlock();
        block.setChild((Block) input);
        assertEquals(List.of(1, 2, 3), block.evaluate(new EvalContext(List.of(2, 1, 3), 100)));
    }

    @Test
    void mapAndFilterShouldWork() {
        InputBlock input = new InputBlock();
        MapBlock map = new MapBlock();
        map.setLeft((Block) input);
        map.setRight((Block) new DoubleOpBlock());

        FilterBlock filter = new FilterBlock();
        filter.setLeft((Block) map);
        filter.setRight((Block) new EvenPredBlock());

        assertEquals(List.of(2, 4, 6), filter.evaluate(new EvalContext(List.of(1, 2, 3), 1000)));
    }

    @Test
    void zipAndFlatShouldWork() {
        InputBlock input = new InputBlock();
        ZipBlock zip = new ZipBlock();
        zip.setLeft((Block) input);
        zip.setRight((Block) new com.algoblock.core.blocks.fn.ConstIntBlock(2));

        FlatBlock flat = new FlatBlock();
        flat.setChild((Block) zip);

        assertEquals(List.of(1, 2, 3, 4), flat.evaluate(new EvalContext(List.of(1, 2, 3, 4), 1000)));
    }

    @Test
    void arrayAndReverseShouldWork() {
        InputBlock input = new InputBlock();
        ArrayBlock array = new ArrayBlock();
        array.setChild((Block) input);
        ReverseBlock reverse = new ReverseBlock();
        reverse.setChild((Block) array);
        assertEquals(List.of(4, 3, 2, 1), reverse.evaluate(new EvalContext(List.of(1, 2, 3, 4), 100)));
    }
}
