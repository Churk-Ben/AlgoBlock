package com.algoblock.gl.ui.tea;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.algoblock.core.engine.BlockRegistry;
import com.algoblock.core.engine.ScoreResult;
import com.algoblock.core.engine.SubmissionResult;
import com.algoblock.core.levels.Level;
import com.algoblock.gl.ui.Completer;
import java.util.List;
import org.junit.jupiter.api.Test;

class UiUpdateTest {
    @Test
    void shouldInsertAtCursorAndMoveLeftRight() {
        UiUpdate update = new UiUpdate(new Completer(new BlockRegistry()));
        UiModel model = UiModel.initial(level(), 1L);
        // Transition to GAME screen
        model = update.update(model, new UiMsg.KeyPressed(257)).model(); // enter

        model = update.update(model, new UiMsg.CharTyped('a')).model();
        model = update.update(model, new UiMsg.CharTyped('c')).model();
        model = update.update(model, new UiMsg.KeyPressed(263)).model(); // left
        model = update.update(model, new UiMsg.CharTyped('b')).model();

        assertEquals("abc", model.line());
        assertEquals(2, model.cursorIndex());
        assertTrue(model.cursorSolidUntilMillis() > 0L);
    }

    @Test
    void shouldSupportBackspaceAndDelete() {
        UiUpdate update = new UiUpdate(new Completer(new BlockRegistry()));
        UiModel model = new UiModel(UiModel.Screen.GAME, level(), "abcd", 2, List.of(), null, 1L, 0L);

        model = update.update(model, new UiMsg.KeyPressed(259)).model(); // backspace
        assertEquals("acd", model.line());
        assertEquals(1, model.cursorIndex());

        model = update.update(model, new UiMsg.KeyPressed(261)).model(); // delete
        assertEquals("ad", model.line());
        assertEquals(1, model.cursorIndex());
    }

    @Test
    void shouldGenerateSuggestionsOnTab() {
        UiUpdate update = new UiUpdate(new Completer(new BlockRegistry()));
        UiModel model = new UiModel(UiModel.Screen.GAME, level(), "Ma", 2, List.of(), null, 1L, 0L);

        UiModel next = update.update(model, new UiMsg.KeyPressed(258)).model(); // tab

        assertTrue(next.suggestions().contains("Map"));
    }

    @Test
    void shouldEmitSubmitCommandAndApplySubmitResult() {
        UiUpdate update = new UiUpdate(new Completer(new BlockRegistry()));
        UiModel model = new UiModel(UiModel.Screen.GAME, level(), "Map _INPUT_", 11, List.of("Map"), null, 1L, 0L);

        UiUpdate.UiUpdateResult result = update.update(model, new UiMsg.KeyPressed(257)); // enter
        assertEquals(1, result.commands().size());
        UiCommand.Submit submit = assertInstanceOf(UiCommand.Submit.class, result.commands().get(0));
        assertEquals("Map _INPUT_", submit.source());
        assertNotNull(submit.level());

        SubmissionResult submissionResult = new SubmissionResult(
                true,
                new ScoreResult(true, true, true, 3),
                List.of(),
                List.of(2, 4, 6),
                "AC");
        UiModel afterSubmit = update.update(result.model(), new UiMsg.SubmitFinished(submissionResult)).model();
        assertEquals(0, afterSubmit.suggestions().size());
        assertEquals("AC", afterSubmit.lastResult().message());
    }

    private static Level level() {
        return new Level(
                1,
                1,
                "L1",
                "story",
                List.of(1, 2, 3),
                List.of(2, 4, 6),
                List.of("Map", "Filter", "_INPUT_"),
                List.of(),
                List.of(),
                3,
                30,
                10,
                "hint");
    }
}
