package com.algoblock.api;

public abstract class Block<O> {
    public abstract O evaluate(EvalContext ctx);

    public String signature() {
        return "?";
    }

    public ValidationResult validate() {
        return ValidationResult.OK;
    }

    public int nodeCount() {
        return 1;
    }
}
