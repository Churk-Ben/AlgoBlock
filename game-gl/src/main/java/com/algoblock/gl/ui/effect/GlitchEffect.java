package com.algoblock.gl.ui.effect;

import com.algoblock.gl.renderer.effect.GlitchState;

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

    // Glitch timing parameters
    private final double glitchEvaluationInterval = 0.5;
    private final float glitchProbability = 0.8f;
    private final float glitchMinDuration = 0.1f;
    private final float glitchMaxDuration = 0.15f;

    // First glitch strip parameters
    private final float glitch1MinHeight = 0.02f;
    private final float glitch1MaxHeight = 0.05f;
    private final float glitch1MaxOffset = 60.0f;
    private final float glitch1MinOffset = -30.0f;

    // Second glitch strip parameters
    private final float glitch2MinHeight = 0.01f;
    private final float glitch2MaxHeight = 0.03f;
    private final float glitch2MaxOffset = 40.0f;
    private final float glitch2MinOffset = -20.0f;

    /**
     * Updates the glitch effect state and returns a GlitchState if active.
     * 
     * @param nowMillis current time in milliseconds
     * @return GlitchState if a glitch is currently happening, null otherwise.
     */
    public GlitchState update(long nowMillis) {
        double timeSeconds = nowMillis / 1000.0;

        if (timeSeconds >= nextGlitchEvalTime) {
            nextGlitchEvalTime = timeSeconds + glitchEvaluationInterval;
            if (Math.random() < glitchProbability) {
                isGlitching = true;
                glitchEndTime = timeSeconds + glitchMinDuration + Math.random() * glitchMaxDuration;

                glitchY1 = (float) Math.random();
                glitchH1 = glitch1MinHeight + (float) Math.random() * glitch1MaxHeight;
                glitchOffset1 = (float) (Math.random() * (glitch1MaxOffset - glitch1MinOffset) + glitch1MinOffset);

                glitchY2 = (float) Math.random();
                glitchH2 = glitch2MinHeight + (float) Math.random() * glitch2MaxHeight;
                glitchOffset2 = (float) (Math.random() * (glitch2MaxOffset - glitch2MinOffset) + glitch2MinOffset);
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
