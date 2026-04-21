package com.algoblock.gl.input.intent;

import java.util.ArrayList;
import java.util.List;

import com.algoblock.gl.input.InputKey;
import com.algoblock.gl.input.event.CharEvent;
import com.algoblock.gl.input.event.InputEvent;
import com.algoblock.gl.input.event.KeyEvent;
import com.algoblock.gl.input.event.PasteEvent;
import com.algoblock.gl.input.event.WheelEvent;

public final class InputIntentMapper {
    private static final long NAV_INTENT_TTL_MS = 160L;

    public static List<IntentEnvelope> map(InputEvent event, long nowMillis) {
        List<IntentEnvelope> intents = new ArrayList<>();

        if (event instanceof CharEvent(char value)) {
            intents.add(persistent(new InputIntent.TextTyped(value), nowMillis));
        } else if (event instanceof PasteEvent(String value)) {
            intents.add(persistent(new InputIntent.PasteText(value), nowMillis));
        } else if (event instanceof KeyEvent(InputKey key)) {
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
        } else if (event instanceof WheelEvent(double xoffset, double yoffset)) {
            // 鼠标没有副滚轮, 暂时先这么处理吧
            if (yoffset > 0) {
                intents.add(nav(new InputIntent.NavigatePrev(), nowMillis));
            } else if (yoffset < 0) {
                intents.add(nav(new InputIntent.NavigateNext(), nowMillis));
            } else if (xoffset > 0) {
                intents.add(nav(new InputIntent.MoveCursorRight(), nowMillis));
            } else if (xoffset < 0) {
                intents.add(nav(new InputIntent.MoveCursorLeft(), nowMillis));
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
