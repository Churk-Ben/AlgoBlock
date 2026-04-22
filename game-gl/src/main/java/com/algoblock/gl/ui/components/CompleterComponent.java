package com.algoblock.gl.ui.components;

import com.algoblock.gl.renderer.core.TerminalBuffer;
import com.algoblock.gl.ui.tea.UpdateResult;
import com.algoblock.gl.utils.TextUtil;

import java.util.List;

public class CompleterComponent {
    private static final int BG = 0x0D1117;
    private static final int BORDER = 0x555555;
    private static final int FG = 0xCDD9E5;
    private static final int SELECTED_FG = 0xFFFFFF;
    private static final int SELECTED_BG = 0x1F6FEB;

    public record Model(
            boolean active,
            List<String> items,
            int selectedIndex) {
        public static Model init() {
            return new Model(false, List.of(), 0);
        }
    }

    public sealed interface Msg {
        record Show(List<String> items) implements Msg {
        }

        record Hide() implements Msg {
        }

        record Next() implements Msg {
        }

        record Prev() implements Msg {
        }
    }

    public static UpdateResult<Model, Void> update(Model model, Msg msg) {
        if (msg instanceof Msg.Show show) {
            return new UpdateResult<>(new Model(true, show.items(), 0), List.of());
        }
        if (msg instanceof Msg.Hide) {
            return new UpdateResult<>(new Model(false, List.of(), 0), List.of());
        }
        if (msg instanceof Msg.Next) {
            if (!model.active() || model.items().isEmpty()) {
                return new UpdateResult<>(model, List.of());
            }
            int nextIndex = (model.selectedIndex() + 1) % model.items().size();
            return new UpdateResult<>(new Model(true, model.items(), nextIndex), List.of());
        }
        if (msg instanceof Msg.Prev) {
            if (!model.active() || model.items().isEmpty()) {
                return new UpdateResult<>(model, List.of());
            }
            int nextIndex = (model.selectedIndex() - 1 + model.items().size()) % model.items().size();
            return new UpdateResult<>(new Model(true, model.items(), nextIndex), List.of());
        }
        return new UpdateResult<>(model, List.of());
    }

    public static void view(Model model, TerminalBuffer buffer, int startCol, int startRow, int maxItems,
            int maxWidth) {
        if (!model.active() || model.items().isEmpty()) {
            return;
        }

        int rows = buffer.rows();
        int cols = buffer.cols();
        if (rows <= 0 || cols <= 0) {
            return;
        }

        int visibleCount = Math.max(1, Math.min(maxItems, model.items().size()));
        int longest = 0;
        for (int i = 0; i < model.items().size(); i++) {
            longest = Math.max(longest, TextUtil.getDisplayWidth(model.items().get(i)));
        }

        int widthCap = Math.max(16, maxWidth);
        int width = Math.min(widthCap, Math.max(18, longest + 4));
        width = Math.min(width, cols);
        if (width < 4) {
            return;
        }
        int height = visibleCount + 2;
        if (height > rows) {
            return;
        }

        int x = Math.max(0, Math.min(startCol, cols - width));
        int y = Math.max(0, Math.min(startRow, rows - height));
        PanelComponent.drawBox(buffer, x, y, width, height, BORDER, BG);

        for (int i = 0; i < visibleCount; i++) {
            int row = y + 1 + i;
            if (row < 0 || row >= rows) {
                continue;
            }
            String item = model.items().get(i);
            boolean selected = (i == model.selectedIndex());
            int fg = selected ? SELECTED_FG : FG;
            int bg = selected ? SELECTED_BG : BG;
            String display = (selected ? "> " : "  ") + item;
            if (TextUtil.getDisplayWidth(display) > width - 2) {
                if (width - 2 >= 2) {
                    display = display.substring(0, width - 3) + "...";
                } else {
                    display = display.substring(0, width - 2);
                }
            }
            buffer.print(x + 1, row, padRight(display, width - 2), fg, bg);
        }
    }

    private static String padRight(String text, int width) {
        int displayWidth = TextUtil.getDisplayWidth(text);
        if (displayWidth >= width) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text);
        for (int i = 0; i < width - displayWidth; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
