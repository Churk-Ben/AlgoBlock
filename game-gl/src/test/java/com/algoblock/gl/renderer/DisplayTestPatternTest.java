package com.algoblock.gl.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DisplayTestPatternTest {
    @Test
    void shouldRenderCheckerboardAtStart() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2);
        DisplayTestPattern pattern = new DisplayTestPattern();

        pattern.renderTo(buffer, 0.2);

        assertEquals(0x111111, buffer.cells()[0].bg());
        assertEquals(0x222222, buffer.cells()[1].bg());
        assertEquals(0x222222, buffer.cells()[4].bg());
        assertEquals(0x111111, buffer.cells()[5].bg());
    }

    @Test
    void shouldRenderSolidRedAndGreenPhases() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2);
        DisplayTestPattern pattern = new DisplayTestPattern();

        pattern.renderTo(buffer, 1.7);
        for (TerminalBuffer.Cell cell : buffer.cells()) {
            assertEquals(0xFF0000, cell.bg());
        }

        pattern.renderTo(buffer, 3.4);
        for (TerminalBuffer.Cell cell : buffer.cells()) {
            assertEquals(0x00FF00, cell.bg());
        }
    }

    @Test
    void shouldRenderRollingPhaseWithSingleActiveAndTrailingCells() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2);
        DisplayTestPattern pattern = new DisplayTestPattern();

        pattern.renderTo(buffer, 5.2);

        int red = 0;
        int green = 0;
        for (TerminalBuffer.Cell cell : buffer.cells()) {
            if (cell.bg() == 0xFF0000) {
                red++;
            } else if (cell.bg() == 0x00FF00) {
                green++;
            }
        }
        assertEquals(1, red);
        assertEquals(1, green);
    }
}
