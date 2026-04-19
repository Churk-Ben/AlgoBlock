package com.algoblock.gl.ui.components;

import com.algoblock.gl.renderer.core.TerminalBuffer;
import com.algoblock.gl.ui.tea.UpdateResult;

import java.util.List;

public class CompleterComponent {
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

    public static void view(Model model, TerminalBuffer buffer, int startCol, int startRow) {
        if (!model.active() || model.items().isEmpty()) {
            return;
        }

        int limit = Math.min(6, model.items().size());
        for (int i = 0; i < limit; i++) {
            if (startRow + i >= buffer.rows()) {
                break;
            }
            String item = model.items().get(i);
            boolean selected = (i == model.selectedIndex());

            int bg = selected ? 0x3A3D41 : 0x1E1E1E;
            int fg = selected ? 0xFFFFFF : 0xCDD9E5;

            // Render the item with padding
            String displayText = " " + item + " ";
            buffer.print(startCol, startRow + i, displayText, fg, bg);
        }
    }
}
