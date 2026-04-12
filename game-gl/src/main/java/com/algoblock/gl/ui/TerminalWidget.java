package com.algoblock.gl.ui;

import com.algoblock.core.engine.BlockRegistry;
import com.algoblock.core.engine.GameCoreService;
import com.algoblock.core.engine.SubmissionResult;
import com.algoblock.core.levels.Level;
import com.algoblock.gl.input.CharEvent;
import com.algoblock.gl.input.InputEvent;
import com.algoblock.gl.input.KeyEvent;
import com.algoblock.gl.input.KeyMapper;
import com.algoblock.gl.renderer.TerminalBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TerminalWidget {
    private final TerminalBuffer buffer;
    private final SyntaxHighlighter highlighter;
    private final Completer completer;
    private final GameCoreService service;
    private final StringBuilder line = new StringBuilder();
    private long startEpochSeconds = System.currentTimeMillis() / 1000;
    private Level level;
    private List<String> suggestions = List.of();

    public TerminalWidget(TerminalBuffer buffer, BlockRegistry registry, Level level) {
        this.buffer = buffer;
        this.highlighter = new SyntaxHighlighter();
        this.completer = new Completer(registry);
        this.service = new GameCoreService(registry);
        this.level = level;
        refresh();
    }

    public void setLevel(Level level) {
        this.level = level;
        line.setLength(0);
        suggestions = List.of();
        startEpochSeconds = System.currentTimeMillis() / 1000;
    }

    public void onEvent(InputEvent event) {
        if (event instanceof CharEvent c) {
            line.append(c.value());
            refresh();
            return;
        }
        if (event instanceof KeyEvent keyEvent) {
            if (KeyMapper.isBackspace(keyEvent.key()) && !line.isEmpty()) {
                line.deleteCharAt(line.length() - 1);
                refresh();
                return;
            }
            if (KeyMapper.isTab(keyEvent.key())) {
                String prefix = currentPrefix();
                Set<String> available = new HashSet<>(level.availableBlocks());
                suggestions = completer.complete(prefix, available);
                refresh();
                return;
            }
            if (KeyMapper.isSubmit(keyEvent.key())) {
                long elapsed = (System.currentTimeMillis() / 1000) - startEpochSeconds;
                SubmissionResult result = service.submit(level, line.toString(), elapsed);
                renderResult(result);
            }
        }
    }

    private String currentPrefix() {
        int i = line.length() - 1;
        while (i >= 0 && (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '_')) {
            i--;
        }
        return line.substring(i + 1);
    }

    private void refresh() {
        buffer.clear();
        buffer.print(0, 0, "Level " + level.id() + " - " + level.title(), 0x6CB6FF, 0x0D1117);
        buffer.print(0, 1, level.story(), 0x9FB3C8, 0x0D1117);
        buffer.print(0, 3, "> ", 0xCDD9E5, 0x0D1117);
        highlighter.highlight(buffer, 3, line.toString());
        if (!suggestions.isEmpty()) {
            int row = 5;
            for (String s : suggestions.stream().limit(6).toList()) {
                buffer.print(0, row++, s, 0x3FB950, 0x0D1117);
            }
        }
    }

    private void renderResult(SubmissionResult result) {
        buffer.print(0, 7, "Result: " + result.message(), result.accepted() ? 0x3FB950 : 0xFF7B72, 0x0D1117);
        buffer.print(0, 8, "Stars: " + result.score().stars(), 0xE3B341, 0x0D1117);
    }
}
