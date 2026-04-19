package com.algoblock.gl.input;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.algoblock.gl.input.event.InputEvent;

public class InputEventQueue {
    private final BlockingQueue<InputEvent> queue = new LinkedBlockingQueue<>();

    public void offer(InputEvent event) {
        queue.offer(event);
    }

    public InputEvent take() throws InterruptedException {
        return queue.take();
    }
}
