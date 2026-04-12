package com.algoblock.core.engine;

import java.util.List;

public class Judge {
    public boolean check(Object result, List<?> expected) {
        if (!(result instanceof List<?> actual)) {
            return false;
        }
        return actual.equals(expected);
    }
}
