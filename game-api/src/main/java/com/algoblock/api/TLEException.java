package com.algoblock.api;

public class TLEException extends RuntimeException {
    public TLEException() {
        super("Step budget exceeded");
    }
}
