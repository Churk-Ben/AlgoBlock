package com.algoblock.gl.ui.pages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.algoblock.core.engine.BlockRegistry;
import com.algoblock.core.engine.ScoreResult;
import com.algoblock.core.engine.SubmissionResult;
import com.algoblock.core.levels.Level;
import com.algoblock.gl.input.InputKey;
import com.algoblock.gl.services.CompletionService;
import com.algoblock.gl.ui.components.CompleterComponent;
import com.algoblock.gl.ui.tea.UpdateResult;

import java.util.List;
import org.junit.jupiter.api.Test;

class GamePageTest {
    @Test
    void shouldInsertAtCursorAndMoveLeftRight() {
        GamePage page = new GamePage(new CompletionService(new BlockRegistry()));
        GamePage.Model model = GamePage.Model.init(level(), 1L);

        model = page.update(model, new GamePage.Msg.CharTyped('a')).model();
        model = page.update(model, new GamePage.Msg.CharTyped('c')).model();
        model = page.update(model, new GamePage.Msg.KeyPressed(InputKey.NAV_LEFT)).model();
        model = page.update(model, new GamePage.Msg.CharTyped('b')).model();

        assertEquals("abc", model.line());
        assertEquals(2, model.cursorIndex());
        assertTrue(model.cursorSolidUntilMillis() > 0L);
    }

    @Test
    void shouldSupportBackspaceAndDelete() {
        GamePage page = new GamePage(new CompletionService(new BlockRegistry()));
        GamePage.Model model = new GamePage.Model(level(), "abcd", 2, null, 1L, 0L, CompleterComponent.Model.init());

        model = page.update(model, new GamePage.Msg.KeyPressed(InputKey.BACKSPACE)).model();
        assertEquals("acd", model.line());
        assertEquals(1, model.cursorIndex());

        model = page.update(model, new GamePage.Msg.KeyPressed(InputKey.DELETE)).model();
        assertEquals("ad", model.line());
        assertEquals(1, model.cursorIndex());
    }

    @Test
    void shouldGenerateSuggestionsOnTab() {
        GamePage page = new GamePage(new CompletionService(new BlockRegistry()));
        GamePage.Model model = new GamePage.Model(level(), "Ma", 2, null, 1L, 0L, CompleterComponent.Model.init());

        GamePage.Model next = page.update(model, new GamePage.Msg.KeyPressed(InputKey.TAB)).model();

        assertTrue(next.completerModel().active());
        assertTrue(next.completerModel().items().contains("Map"));
    }

    @Test
    void shouldEmitSubmitCommandAndApplySubmitResult() {
        GamePage page = new GamePage(new CompletionService(new BlockRegistry()));
        GamePage.Model model = new GamePage.Model(level(), "Map _INPUT_", 11, null, 1L, 0L,
                CompleterComponent.Model.init());

        UpdateResult<GamePage.Model, GamePage.Cmd> result = page.update(model,
                new GamePage.Msg.KeyPressed(InputKey.SUBMIT));
        assertEquals(1, result.commands().size());
        GamePage.Cmd.Submit submit = assertInstanceOf(GamePage.Cmd.Submit.class, result.commands().get(0));
        assertEquals("Map _INPUT_", submit.source());
        assertNotNull(submit.level());

        SubmissionResult submissionResult = new SubmissionResult(
                true,
                new ScoreResult(true, true, true, 3),
                List.of(),
                List.of(2, 4, 6),
                "AC");
        GamePage.Model afterSubmit = page.update(result.model(), new GamePage.Msg.SubmitFinished(submissionResult))
                .model();
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
