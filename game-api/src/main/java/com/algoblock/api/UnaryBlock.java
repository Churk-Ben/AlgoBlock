package com.algoblock.api;

public abstract class UnaryBlock<I, O> extends Block<O> {
    protected Block<I> child;

    public void setChild(Block<I> child) {
        this.child = child;
    }

    public Block<I> child() {
        return child;
    }

    @Override
    public int nodeCount() {
        return 1 + (child == null ? 0 : child.nodeCount());
    }
}
