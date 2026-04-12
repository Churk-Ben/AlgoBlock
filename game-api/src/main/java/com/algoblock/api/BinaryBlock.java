package com.algoblock.api;

public abstract class BinaryBlock<A, B, O> extends Block<O> {
    protected Block<A> left;
    protected Block<B> right;

    public void setLeft(Block<A> left) {
        this.left = left;
    }

    public void setRight(Block<B> right) {
        this.right = right;
    }

    public Block<A> left() {
        return left;
    }

    public Block<B> right() {
        return right;
    }

    @Override
    public int nodeCount() {
        int leftCount = left == null ? 0 : left.nodeCount();
        int rightCount = right == null ? 0 : right.nodeCount();
        return 1 + leftCount + rightCount;
    }
}
