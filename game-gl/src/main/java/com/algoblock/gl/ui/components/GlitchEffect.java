package com.algoblock.gl.ui.components;

import com.algoblock.gl.renderer.GlitchState;

public class GlitchEffect {
    private double nextGlitchEvalTime = 0;
    private double glitchEndTime = 0;
    private boolean isGlitching = false;
    private float glitchOffset1 = 0;
    private float glitchOffset2 = 0;
    private float glitchY1 = 0;
    private float glitchH1 = 0;
    private float glitchY2 = 0;
    private float glitchH2 = 0;

    /**
     * Updates the glitch effect state and returns a GlitchState if active.
     * 
     * @param nowMillis current time in milliseconds
     * @return GlitchState if a glitch is currently happening, null otherwise.
     */
    public GlitchState update(long nowMillis) {
        double timeSeconds = nowMillis / 1000.0;

        if (timeSeconds >= nextGlitchEvalTime) {
            nextGlitchEvalTime = timeSeconds + 1.0;
            if (Math.random() < 0.5) {
                isGlitching = true;
                glitchEndTime = timeSeconds + 0.1 + Math.random() * 0.15; // 0.1s ~ 0.25s duration

                // Normalized coordinates [0.0, 1.0]
                glitchY1 = (float) Math.random();
                glitchH1 = 0.02f + (float) Math.random() * 0.05f;
                glitchOffset1 = (float) (Math.random() * 60 - 30); // Absolute pixel offset is fine, or we can scale it
                                                                   // later. Let's keep it absolute for now.

                glitchY2 = (float) Math.random();
                glitchH2 = 0.01f + (float) Math.random() * 0.03f;
                glitchOffset2 = (float) (Math.random() * 40 - 20);
            }
        }

        if (isGlitching && timeSeconds > glitchEndTime) {
            isGlitching = false;
        }

        if (isGlitching) {
            return new GlitchState(glitchY1, glitchH1, glitchOffset1, glitchY2, glitchH2, glitchOffset2);
        }

        return null;
    }
}
