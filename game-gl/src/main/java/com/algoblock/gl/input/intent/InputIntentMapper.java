package com.algoblock.gl.input.intent;

import com.algoblock.gl.input.InputKey;
import com.algoblock.gl.input.event.CharEvent;
import com.algoblock.gl.input.event.InputEvent;
import com.algoblock.gl.input.event.KeyEvent;
import com.algoblock.gl.input.event.PasteEvent;
import com.algoblock.gl.input.event.WheelEvent;

import java.util.ArrayList;
import java.util.List;

public final class InputIntentMapper {
    private static final long NAV_INTENT_TTL_MS = 160L;

    private InputIntentMapper() {
    }

    public static List<IntentEnvelope> map(InputEvent event, long nowMillis) {
        List<IntentEnvelope> intents = new ArrayList<>();

        if (event instanceof CharEvent c) {
            intents.add(persistent(new InputIntent.TextTyped(c.value()), nowMillis));
        } else if (event instanceof PasteEvent p) {
            intents.add(persistent(new InputIntent.PasteText(p.value()), nowMillis));
        } else if (event instanceof WheelEvent w) {
            if (w.yoffset() > 0) {
                intents.add(nav(new InputIntent.NavigatePrev(), nowMillis));
            } else if (w.yoffset() < 0) {
                intents.add(nav(new InputIntent.NavigateNext(), nowMillis));
            }
        } else if (event instanceof KeyEvent k) {
            InputKey key = k.key();
            switch (key) {
                case NAV_UP -> intents.add(nav(new InputIntent.NavigatePrev(), nowMillis));
                case NAV_DOWN -> intents.add(nav(new InputIntent.NavigateNext(), nowMillis));
                case NAV_LEFT -> intents.add(persistent(new InputIntent.MoveCursorLeft(), nowMillis));
                case NAV_RIGHT -> intents.add(persistent(new InputIntent.MoveCursorRight(), nowMillis));
                case SUBMIT -> intents.add(persistent(new InputIntent.Submit(), nowMillis));
                case CANCEL -> intents.add(persistent(new InputIntent.Cancel(), nowMillis));
                case BACKSPACE -> intents.add(persistent(new InputIntent.Backspace(), nowMillis));
                case DELETE -> intents.add(persistent(new InputIntent.Delete(), nowMillis));
                case TAB -> intents.add(persistent(new InputIntent.Tab(), nowMillis));
                default -> {
                }
            }
        }

        return intents;
    }

    private static IntentEnvelope nav(InputIntent intent, long nowMillis) {
        return new IntentEnvelope(intent, nowMillis, nowMillis + NAV_INTENT_TTL_MS);
    }

    private static IntentEnvelope persistent(InputIntent intent, long nowMillis) {
        return new IntentEnvelope(intent, nowMillis, null);
    }
}
