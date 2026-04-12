package com.algoblock.core.engine;

public record Token(Type type, String text) {
    public enum Type {
        IDENT,
        NUMBER,
        LT,
        GT,
        COMMA,
        EOF
    }
}
