package com.algoblock.gl.ui.pages;

import com.algoblock.core.engine.SubmissionResult;
import com.algoblock.core.levels.Level;
import com.algoblock.gl.input.intent.InputIntent;
import com.algoblock.gl.renderer.core.RenderFrame;
import com.algoblock.gl.renderer.core.TerminalBuffer;
import com.algoblock.gl.renderer.effect.UiEffect;
import com.algoblock.gl.renderer.cursor.CursorState;
import com.algoblock.gl.services.CompletionService;
import com.algoblock.gl.ui.SyntaxHighlighter;
import com.algoblock.gl.ui.components.CompleterComponent;
import com.algoblock.gl.ui.components.PanelComponent;
import com.algoblock.gl.ui.tea.Program;
import com.algoblock.gl.ui.tea.UpdateResult;
import com.algoblock.gl.utils.TextUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 这一坨, 我之后再看吧
public class GamePage implements Program<GamePage.Model, GamePage.Msg, GamePage.Cmd> {
    private static final int BG = 0x0D1117;
    private static final int FG = 0xCDD9E5;
    private static final int DIM_FG = 0x9FB3C8;
    private static final int PANEL_BORDER = 0x555555;
    private static final int PANEL_TITLE = 0x22CC22;
    private static final int CURSOR_COLOR = 0x22CC22;
    private static final int TOP_MIN_HEIGHT = 6;
    private static final int BOTTOM_MIN_HEIGHT = 5;
    private static final int MIDDLE_MIN_HEIGHT = 5;
    private static final long CURSOR_SOLID_AFTER_EDIT_MS = 800L;

    private static final String SFX_TYPE_IN = "/assets/audio/sfx/type-in.mp3";
    private static final String SFX_INTERACT = "/assets/audio/sfx/interact.mp3";

    private final SyntaxHighlighter highlighter = new SyntaxHighlighter();
    private final CompletionService completionService;

    public GamePage(CompletionService completionService) {
        this.completionService = completionService;
    }

    public record Model(
            Level level,
            String line,
            int cursorIndex,
            SubmissionResult lastResult,
            long startEpochSeconds,
            long cursorSolidUntilMillis,
            CompleterComponent.Model completerModel,
            boolean paused,
            int pauseSelectedIndex) {

        public static Model init(Level level, long startEpochSeconds) {
            return new Model(level, "", 0, null, startEpochSeconds, 0L, CompleterComponent.Model.init(), false, 0);
        }

        public Model withCompleterModel(CompleterComponent.Model newCompleterModel) {
            return new Model(level, line, cursorIndex, lastResult, startEpochSeconds, cursorSolidUntilMillis,
                    newCompleterModel, paused, pauseSelectedIndex);
        }

        public Model withPause(boolean paused, int pauseSelectedIndex) {
            return new Model(level, line, cursorIndex, lastResult, startEpochSeconds, cursorSolidUntilMillis,
                    completerModel, paused, pauseSelectedIndex);
        }
    }

    public sealed interface Msg {
        record Intent(InputIntent intent) implements Msg {
        }

        record SubmitFinished(SubmissionResult result) implements Msg {
        }
    }

    public sealed interface Cmd {
        record Submit(Level level, String source, long elapsedSeconds) implements Cmd {
        }

        record PlaySound(String resourcePath) implements Cmd {
        }

        record ReturnToStart() implements Cmd {
        }
    }

    @Override
    public Model init() {
        // Will be called by AppProgram with proper arguments
        return null;
    }

    @Override
    public UpdateResult<Model, Cmd> update(Model model, Msg msg) {
        if (msg instanceof Msg.SubmitFinished(SubmissionResult result)) {
            Model next = new Model(model.level(), model.line(), model.cursorIndex(), result,
                    model.startEpochSeconds(), model.cursorSolidUntilMillis(), model.completerModel(), model.paused(),
                    model.pauseSelectedIndex());
            return new UpdateResult<>(next, List.of());
        }

        if (msg instanceof Msg.Intent(InputIntent intent1) && intent1 instanceof InputIntent.TextTyped(char value)) {
            if (model.paused())
                return new UpdateResult<>(model, List.of());
            // Hide completer when typing normally
            UpdateResult<CompleterComponent.Model, Void> completerResult = CompleterComponent
                    .update(model.completerModel(), new CompleterComponent.Msg.Hide());

            String line = model.line();
            int cursor = clampCursor(model.cursorIndex(), line.length());
            String nextLine = line.substring(0, cursor) + value + line.substring(cursor);
            long solidUntil = System.currentTimeMillis() + CURSOR_SOLID_AFTER_EDIT_MS;
            Model next = new Model(model.level(), nextLine, cursor + 1, model.lastResult(), model.startEpochSeconds(),
                    solidUntil, completerResult.model(), model.paused(), model.pauseSelectedIndex());
            return new UpdateResult<>(next, List.of(new Cmd.PlaySound(SFX_TYPE_IN)));
        }

        if (msg instanceof Msg.Intent(InputIntent intent)) {

            if (model.paused()) {
                if (intent instanceof InputIntent.Cancel) {
                    return new UpdateResult<>(model.withPause(false, model.pauseSelectedIndex()), List.of());
                } else if (intent instanceof InputIntent.NavigatePrev) {
                    int next = model.pauseSelectedIndex() - 1;
                    if (next < 0)
                        next = 1;

                    return new UpdateResult<>(model.withPause(true, next), List.of(new Cmd.PlaySound(SFX_INTERACT)));
                } else if (intent instanceof InputIntent.NavigateNext) {
                    int next = (model.pauseSelectedIndex() + 1) % 2;
                    return new UpdateResult<>(model.withPause(true, next), List.of(new Cmd.PlaySound(SFX_INTERACT)));
                } else if (intent instanceof InputIntent.Submit) {
                    if (model.pauseSelectedIndex() == 0) {
                        return new UpdateResult<>(model.withPause(false, model.pauseSelectedIndex()),
                                List.of(new Cmd.PlaySound(SFX_TYPE_IN)));
                    } else if (model.pauseSelectedIndex() == 1) {
                        return new UpdateResult<>(model,
                                List.of(new Cmd.PlaySound(SFX_TYPE_IN), new Cmd.ReturnToStart()));
                    }
                }
                return new UpdateResult<>(model, List.of());
            }

            if (intent instanceof InputIntent.Cancel) {
                return new UpdateResult<>(model.withPause(true, 0), List.of());
            }

            if (intent instanceof InputIntent.PasteText(String value) && value != null && !value.isEmpty()) {
                UpdateResult<CompleterComponent.Model, Void> completerResult = CompleterComponent
                        .update(model.completerModel(), new CompleterComponent.Msg.Hide());
                String line = model.line();
                int cursor = clampCursor(model.cursorIndex(), line.length());
                String nextLine = line.substring(0, cursor) + value + line.substring(cursor);
                long solidUntil = System.currentTimeMillis() + CURSOR_SOLID_AFTER_EDIT_MS;
                Model next = new Model(model.level(), nextLine, cursor + value.length(), model.lastResult(),
                        model.startEpochSeconds(), solidUntil, completerResult.model(), model.paused(),
                        model.pauseSelectedIndex());
                return new UpdateResult<>(next, List.of());
            }

            // 1. If completer is active, route specific keys to it
            if (model.completerModel().active()) {
                if (intent instanceof InputIntent.NavigatePrev) {
                    UpdateResult<CompleterComponent.Model, Void> r = CompleterComponent.update(model.completerModel(),
                            new CompleterComponent.Msg.Prev());
                    return new UpdateResult<>(model.withCompleterModel(r.model()),
                            List.of(new Cmd.PlaySound(SFX_INTERACT)));
                }
                if (intent instanceof InputIntent.NavigateNext) {
                    UpdateResult<CompleterComponent.Model, Void> r = CompleterComponent.update(model.completerModel(),
                            new CompleterComponent.Msg.Next());
                    return new UpdateResult<>(model.withCompleterModel(r.model()),
                            List.of(new Cmd.PlaySound(SFX_INTERACT)));
                }
                if (intent instanceof InputIntent.Submit || intent instanceof InputIntent.Tab) {
                    // Confirm selection
                    int selectedIndex = model.completerModel().selectedIndex();
                    if (selectedIndex >= 0 && selectedIndex < model.completerModel().items().size()) {
                        String selected = model.completerModel().items().get(selectedIndex);
                        String line = model.line();
                        int cursor = clampCursor(model.cursorIndex(), line.length());
                        String prefix = currentPrefix(line, cursor);

                        // Replace prefix with selected item
                        String nextLine = line.substring(0, cursor - prefix.length()) + selected
                                + line.substring(cursor);
                        int nextCursor = cursor - prefix.length() + selected.length();
                        if (selected.endsWith("<>")) {
                            nextCursor--;
                        }

                        UpdateResult<CompleterComponent.Model, Void> r = CompleterComponent
                                .update(model.completerModel(), new CompleterComponent.Msg.Hide());
                        Model next = new Model(model.level(), nextLine, nextCursor, model.lastResult(),
                                model.startEpochSeconds(), System.currentTimeMillis() + CURSOR_SOLID_AFTER_EDIT_MS,
                                r.model(), model.paused(), model.pauseSelectedIndex());
                        return new UpdateResult<>(next, List.of(new Cmd.PlaySound(SFX_TYPE_IN)));
                    }
                }
                if (intent instanceof InputIntent.MoveCursorLeft || intent instanceof InputIntent.MoveCursorRight
                        || intent instanceof InputIntent.Backspace || intent instanceof InputIntent.Delete) {
                    // Hide completer and let it fall through
                    UpdateResult<CompleterComponent.Model, Void> r = CompleterComponent.update(model.completerModel(),
                            new CompleterComponent.Msg.Hide());
                    model = model.withCompleterModel(r.model());
                }
            }

            // 2. Normal key handling
            String line = model.line();
            int cursor = clampCursor(model.cursorIndex(), line.length());

            if (intent instanceof InputIntent.Backspace && cursor > 0) {
                String nextLine = line.substring(0, cursor - 1) + line.substring(cursor);
                long solidUntil = System.currentTimeMillis() + CURSOR_SOLID_AFTER_EDIT_MS;
                Model next = new Model(model.level(), nextLine, cursor - 1, model.lastResult(),
                        model.startEpochSeconds(), solidUntil, model.completerModel(), model.paused(),
                        model.pauseSelectedIndex());
                return new UpdateResult<>(next, List.of());
            }
            if (intent instanceof InputIntent.Delete && cursor < line.length()) {
                String nextLine = line.substring(0, cursor) + line.substring(cursor + 1);
                long solidUntil = System.currentTimeMillis() + CURSOR_SOLID_AFTER_EDIT_MS;
                Model next = new Model(model.level(), nextLine, cursor, model.lastResult(), model.startEpochSeconds(),
                        solidUntil, model.completerModel(), model.paused(), model.pauseSelectedIndex());
                return new UpdateResult<>(next, List.of());
            }
            if (intent instanceof InputIntent.MoveCursorLeft) {
                Model next = new Model(model.level(), line, Math.max(0, cursor - 1), model.lastResult(),
                        model.startEpochSeconds(), model.cursorSolidUntilMillis(), model.completerModel(),
                        model.paused(), model.pauseSelectedIndex());
                return new UpdateResult<>(next, List.of());
            }
            if (intent instanceof InputIntent.MoveCursorRight) {
                Model next = new Model(model.level(), line, Math.min(line.length(), cursor + 1), model.lastResult(),
                        model.startEpochSeconds(), model.cursorSolidUntilMillis(), model.completerModel(),
                        model.paused(), model.pauseSelectedIndex());
                return new UpdateResult<>(next, List.of());
            }
            if (intent instanceof InputIntent.Tab) {
                String prefix = currentPrefix(line, cursor);
                Set<String> available = new HashSet<>(model.level().availableBlocks());
                List<String> suggestions = completionService.complete(prefix, available);

                if (!suggestions.isEmpty()) {
                    UpdateResult<CompleterComponent.Model, Void> r = CompleterComponent.update(model.completerModel(),
                            new CompleterComponent.Msg.Show(suggestions));
                    return new UpdateResult<>(model.withCompleterModel(r.model()), List.of());
                }
                return new UpdateResult<>(model, List.of());
            }
            if (intent instanceof InputIntent.Submit) {
                long elapsed = (System.currentTimeMillis() / 1000) - model.startEpochSeconds();
                Cmd.Submit command = new Cmd.Submit(model.level(), line, elapsed);
                Model next = new Model(model.level(), line, cursor, model.lastResult(), model.startEpochSeconds(),
                        model.cursorSolidUntilMillis(), model.completerModel(), model.paused(),
                        model.pauseSelectedIndex());
                return new UpdateResult<>(next, List.of(command, new Cmd.PlaySound(SFX_TYPE_IN)));
            }
        }
        return new UpdateResult<>(model, List.of());
    }

    @Override
    public RenderFrame view(Model model, TerminalBuffer buffer, long nowMillis) {
        buffer.clear();
        int cols = buffer.cols();
        int rows = buffer.rows();

        if (cols < 8 || rows < 8) {
            int fallbackCol = Math.min(cols - 1, Math.max(0, 2 + visualOffset(model.line(), model.cursorIndex())));
            int fallbackRow = Math.min(rows - 1, 0);
            return new RenderFrame(buffer, new CursorState(fallbackCol, fallbackRow, true, true, CURSOR_COLOR),
                    List.of(new UiEffect.Crt(0.30f)));
        }

        int topHeight = Math.max(TOP_MIN_HEIGHT, rows / 4);
        int bottomHeight = Math.max(BOTTOM_MIN_HEIGHT, rows / 5 + 2);
        int overflow = topHeight + bottomHeight + MIDDLE_MIN_HEIGHT - rows;
        if (overflow > 0) {
            int reduceTop = Math.min(overflow, Math.max(0, topHeight - 4));
            topHeight -= reduceTop;
            overflow -= reduceTop;
            if (overflow > 0) {
                bottomHeight -= Math.min(overflow, Math.max(0, bottomHeight - BOTTOM_MIN_HEIGHT));
            }
        }
        int middleHeight = Math.max(1, rows - topHeight - bottomHeight);

        int topY = 0;
        int middleY = topY + topHeight;
        int bottomY = middleY + middleHeight;

        int middleGap = cols >= 40 ? 1 : 0;
        int leftWidth = cols >= 72 ? Math.max(24, cols / 3) : Math.max(20, cols / 2);
        leftWidth = Math.min(Math.max(10, leftWidth), Math.max(10, cols - 14 - middleGap));
        int rightX = leftWidth + middleGap;
        int rightWidth = cols - rightX;
        if (rightWidth < 12) {
            rightWidth = 12;
            rightX = Math.max(10, cols - rightWidth);
            leftWidth = Math.max(10, rightX - middleGap);
        }

        PanelComponent.drawBoxWithTitle(buffer, 0, topY, cols, topHeight, " Level Brief ", PANEL_BORDER, BG,
                PANEL_TITLE);
        PanelComponent.drawBoxWithTitle(buffer, 0, middleY, leftWidth, middleHeight, " Blocks ", PANEL_BORDER, BG,
                PANEL_TITLE);
        PanelComponent.drawBoxWithTitle(buffer, rightX, middleY, rightWidth, middleHeight, " Workspace ",
                PANEL_BORDER,
                BG, PANEL_TITLE);
        PanelComponent.drawBoxWithTitle(buffer, 0, bottomY, cols, bottomHeight, " Answer ", PANEL_BORDER, BG,
                PANEL_TITLE);

        drawTopPanel(model, buffer, 0, topY, cols, topHeight);
        drawBlocksPanel(model, buffer, 0, middleY, leftWidth, middleHeight);
        drawWorkspacePanel(buffer, rightX, middleY, rightWidth, middleHeight);

        int inputRow = drawAnswerPanel(model, buffer, 0, bottomY, cols, bottomHeight);
        int textCol = 4;
        int cursorCol = Math.min(cols - 1, Math.max(0, textCol + visualOffset(model.line(), model.cursorIndex())));
        int cursorRow = Math.min(rows - 1, Math.max(bottomY + 1, inputRow));

        if (model.completerModel().active() && !model.paused()) {
            int maxItems = Math.max(1, Math.min(10, model.completerModel().items().size()));
            int topRow = inputRow - (maxItems + 2);
            int anchorCol = Math.max(2, cursorCol);
            CompleterComponent.view(model.completerModel(), buffer, anchorCol, topRow, maxItems,
                    Math.max(18, cols / 3));
        }

        List<UiEffect> effects = new ArrayList<>();
        if (model.paused()) {
            String title = " Paused ";
            String[] options = { "Resume Game", "Return to Main Menu" };

            int maxOptLen = 0;
            for (String opt : options) {
                maxOptLen = Math.max(maxOptLen, opt.length());
            }
            int boxWidth = maxOptLen + 12;
            int boxHeight = options.length * 2 + 3;
            int boxX = (cols - boxWidth) / 2;
            int boxY = (rows - boxHeight) / 2;

            effects.add(new UiEffect.Dim(0.85f, boxX, boxY, boxWidth, boxHeight));

            PanelComponent.drawBoxWithTitle(buffer, boxX, boxY, boxWidth, boxHeight, title, 0x888888, BG, 0xFFFFFF);
            int[] pauseCursorInfo = PanelComponent.drawLeftAlignedOptions(
                    buffer, boxX, boxWidth, boxY + 2, options, model.pauseSelectedIndex(), 2, 2, 0x888888, 0xFFFFFF,
                    BG);

            cursorCol = pauseCursorInfo[0];
            cursorRow = pauseCursorInfo[1];
        }

        boolean forceSolidVisible = model.paused() || nowMillis < model.cursorSolidUntilMillis();
        boolean blinkVisible = forceSolidVisible || ((nowMillis / 500L) % 2L) == 0L;

        effects.add(new UiEffect.Crt(0.30f));

        return new RenderFrame(buffer, new CursorState(cursorCol, cursorRow, blinkVisible, true, CURSOR_COLOR),
                effects);
    }

    private void drawTopPanel(Model model, TerminalBuffer buffer, int x, int y, int width, int height) {
        int innerX = x + 2;
        int innerWidth = Math.max(1, width - 4);
        int row = y + 1;
        int endRow = y + height - 2;
        if (row > endRow) {
            return;
        }

        Level level = model.level();
        printClipped(buffer, innerX, row++, innerWidth, "Level " + level.id() + " - " + level.title(), 0x6CB6FF);
        if (row <= endRow) {
            row = printWrapped(buffer, innerX, row, innerWidth, endRow, level.story(), DIM_FG);
        }
        if (row <= endRow) {
            String goal = "Goal: " + level.input() + " -> " + level.output();
            printClipped(buffer, innerX, row, innerWidth, goal, FG);
        }
    }

    private void drawBlocksPanel(Model model, TerminalBuffer buffer, int x, int y, int width, int height) {
        int innerX = x + 2;
        int innerWidth = Math.max(1, width - 4);
        int row = y + 1;
        int endRow = y + height - 2;
        if (row > endRow) {
            return;
        }

        printClipped(buffer, innerX, row++, innerWidth, "Available Blocks:", 0xE3B341);
        row = printBlockList(buffer, innerX, row, innerWidth, endRow, model.level().availableBlocks(), FG);
        if (row <= endRow) {
            row++;
        }
        if (row <= endRow) {
            printClipped(buffer, innerX, row++, innerWidth, "Forced Blocks:", 0xE3B341);
            printBlockList(buffer, innerX, row, innerWidth, endRow, model.level().forcedBlocks(), FG);
        }
    }

    private void drawWorkspacePanel(TerminalBuffer buffer, int x, int y, int width, int height) {
        int innerX = x + 2;
        int innerWidth = Math.max(1, width - 4);
        int row = y + 1;
        int endRow = y + height - 2;
        if (row > endRow) {
            return;
        }
        printClipped(buffer, innerX, row, innerWidth, "Reserved for upcoming features.", DIM_FG);
    }

    private int drawAnswerPanel(Model model, TerminalBuffer buffer, int x, int y, int width, int height) {
        int inputRow = Math.min(buffer.rows() - 1, y + 1);
        int promptCol = x + 2;
        int textCol = promptCol + 2;
        printClipped(buffer, promptCol, inputRow, Math.max(1, width - 4), "> ", FG);
        highlighter.highlight(buffer, textCol, inputRow, model.line());

        int statusRow = Math.min(buffer.rows() - 1, y + height - 2);
        SubmissionResult lastResult = model.lastResult();
        if (lastResult != null && statusRow > inputRow) {
            int resultColor = lastResult.accepted() ? 0x3FB950 : 0xFF7B72;
            int stars = lastResult.score() != null ? lastResult.score().stars() : 0;
            String status = "Result: " + lastResult.message() + "  Stars: " + stars;
            printClipped(buffer, promptCol, statusRow, Math.max(1, width - 4), status, resultColor);
        } else if (statusRow > inputRow) {
            printClipped(buffer, promptCol, statusRow, Math.max(1, width - 4),
                    "Tab: complete | Enter: submit | Esc: back", DIM_FG);
        }
        return inputRow;
    }

    private int printBlockList(TerminalBuffer buffer, int x, int startRow, int width, int endRow, List<String> blocks,
            int fg) {
        int row = startRow;
        if (blocks == null || blocks.isEmpty()) {
            if (row <= endRow) {
                printClipped(buffer, x, row++, width, "- (none)", DIM_FG);
            }
            return row;
        }
        for (String block : blocks) {
            if (row > endRow) {
                break;
            }
            printClipped(buffer, x, row++, width, "- " + block, fg);
        }
        return row;
    }

    private int printWrapped(TerminalBuffer buffer, int x, int startRow, int width, int endRow, String text, int fg) {
        if (text == null || text.isEmpty()) {
            return startRow;
        }
        int row = startRow;
        List<String> lines = splitByWidth(text, Math.max(1, width));
        for (String line : lines) {
            if (row > endRow) {
                break;
            }
            printClipped(buffer, x, row++, width, line, fg);
        }
        return row;
    }

    private void printClipped(TerminalBuffer buffer, int x, int y, int width, String text, int fg) {
        if (y < 0 || y >= buffer.rows() || width <= 0 || x >= buffer.cols()) {
            return;
        }
        int safeX = Math.max(0, x);
        int safeWidth = Math.min(width - (safeX - x), buffer.cols() - safeX);
        if (safeWidth <= 0) {
            return;
        }
        String content = text == null ? "" : text;
        if (content.length() > safeWidth) {
            if (safeWidth >= 2) {
                content = content.substring(0, safeWidth - 1) + ".";
            } else {
                content = content.substring(0, safeWidth);
            }
        }
        buffer.print(safeX, y, content, fg, BG);
    }

    private List<String> splitByWidth(String text, int width) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(text.length(), start + width);
            lines.add(text.substring(start, end));
            start = end;
        }
        return lines;
    }

    private static int clampCursor(int cursor, int lineLength) {
        return Math.max(0, Math.min(lineLength, cursor));
    }

    private static String currentPrefix(String line, int cursor) {
        int i = Math.max(0, Math.min(cursor, line.length())) - 1;
        while (i >= 0) {
            char ch = line.charAt(i);
            if (!Character.isLetterOrDigit(ch) && ch != '_') {
                break;
            }
            i--;
        }
        return line.substring(i + 1, Math.max(0, Math.min(cursor, line.length())));
    }

    private static int visualOffset(String line, int cursorIndex) {
        int max = Math.max(0, Math.min(cursorIndex, line.length()));
        int width = 0;
        for (int i = 0; i < max; i++) {
            width += TextUtil.isWideCodePoint(line.charAt(i)) ? 2 : 1;
        }
        return width;
    }
}
