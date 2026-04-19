package com.algoblock.gl.input.intent;

public sealed interface InputIntent permits
        InputIntent.NavigateNext,
        InputIntent.NavigatePrev,
        InputIntent.MoveCursorLeft,
        InputIntent.MoveCursorRight,
        InputIntent.Submit,
        InputIntent.Cancel,
        InputIntent.Backspace,
        InputIntent.Delete,
        InputIntent.Tab,
        InputIntent.TextTyped,
        InputIntent.PasteText {

    record NavigateNext() implements InputIntent {
    }

    record NavigatePrev() implements InputIntent {
    }

    record MoveCursorLeft() implements InputIntent {
    }

    record MoveCursorRight() implements InputIntent {
    }

    record Submit() implements InputIntent {
    }

    record Cancel() implements InputIntent {
    }

    record Backspace() implements InputIntent {
    }

    record Delete() implements InputIntent {
    }

    record Tab() implements InputIntent {
    }

    record TextTyped(char value) implements InputIntent {
    }

    record PasteText(String value) implements InputIntent {
    }
}
