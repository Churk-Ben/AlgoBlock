package com.algoblock.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EvalContext {
    private final List<?> input;
    private int stepBudget;
    private final List<String> trace;
    private final Map<String, Object> vars;

    public EvalContext(List<?> input, int stepBudget) {
        this.input = List.copyOf(input);
        this.stepBudget = stepBudget;
        this.trace = new ArrayList<>();
        this.vars = new HashMap<>();
    }

    public List<?> getInput() {
        return Collections.unmodifiableList(input);
    }

    public void consumeStep() {
        if (--stepBudget < 0) {
            throw new TLEException();
        }
    }

    public int remainingSteps() {
        return stepBudget;
    }

    public void log(String msg) {
        trace.add(msg);
    }

    public List<String> trace() {
        return Collections.unmodifiableList(trace);
    }

    public void putVar(String key, Object value) {
        vars.put(key, value);
    }

    public Object getVar(String key) {
        return vars.get(key);
    }
}
