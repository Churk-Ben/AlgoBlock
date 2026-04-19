package com.algoblock.gl.input.intent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class InputIntentQueue {
    private final BlockingQueue<IntentEnvelope> queue = new LinkedBlockingQueue<>();

    public void offer(IntentEnvelope envelope) {
        queue.offer(envelope);
    }

    public IntentEnvelope take() throws InterruptedException {
        return queue.take();
    }
}
