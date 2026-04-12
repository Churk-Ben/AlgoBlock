package com.algoblock.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.algoblock.api.EvalContext;
import com.algoblock.api.Block;
import com.algoblock.core.engine.BlockRegistry;
import com.algoblock.core.engine.ParseException;
import com.algoblock.core.engine.Parser;
import java.util.List;
import org.junit.jupiter.api.Test;

class ParserTest {
    @Test
    void shouldParseNestedExpression() {
        Parser parser = new Parser(new BlockRegistry());
        Block<?> root = parser.parse("Array<PopEach<PrioQueue<_INPUT_>>>");
        Object result = root.evaluate(new EvalContext(List.of(5, 3, 4, 1, 2), 1000));
        assertEquals(List.of(1, 2, 3, 4, 5), result);
    }

    @Test
    void shouldParseMapExpression() {
        Parser parser = new Parser(new BlockRegistry());
        Block<?> root = parser.parse("Map<_INPUT_><DoubleOp>");
        Object result = root.evaluate(new EvalContext(List.of(1, 2, 3), 1000));
        assertEquals(List.of(2, 4, 6), result);
    }

    @Test
    void shouldFailOnArityMismatch() {
        Parser parser = new Parser(new BlockRegistry());
        assertThrows(ParseException.class, () -> parser.parse("Array<_INPUT_><Sort<_INPUT_>>"));
    }
}
