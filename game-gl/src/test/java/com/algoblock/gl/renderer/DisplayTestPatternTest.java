package com.algoblock.gl.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DisplayTestPatternTest {
    @Test
    void shouldRenderChineseCheckerboardAtStart() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2);
        DisplayTestPattern pattern = new DisplayTestPattern();

        pattern.renderTo(buffer, 0.2);

        assertEquals('中', buffer.cells()[0].c());
        assertEquals('\0', buffer.cells()[1].c());
        assertEquals('文', buffer.cells()[2].c());
        assertEquals('\0', buffer.cells()[3].c());
        
        assertEquals(0x101418, buffer.cells()[0].bg());
        assertEquals(0x101418, buffer.cells()[1].bg());
        assertEquals(0xD9DEE3, buffer.cells()[2].bg());
        assertEquals(0xD9DEE3, buffer.cells()[3].bg());
    }

    @Test
    void shouldRotateAllCheckerboardVariants() {
        TerminalBuffer buffer = new TerminalBuffer(4, 1);
        DisplayTestPattern pattern = new DisplayTestPattern();

        pattern.renderTo(buffer, 1.2);
        assertEquals('A', buffer.cells()[0].c());
        assertEquals('B', buffer.cells()[1].c());
        assertEquals('C', buffer.cells()[2].c());
        assertEquals('D', buffer.cells()[3].c());
        assertEquals(buffer.cells()[0].bg(), buffer.cells()[1].bg());
        assertEquals(buffer.cells()[2].bg(), buffer.cells()[3].bg());

        pattern.renderTo(buffer, 2.2);
        assertEquals(0xD9DEE3, buffer.cells()[0].bg());
        assertEquals(0xD9DEE3, buffer.cells()[1].bg());
        assertEquals('文', buffer.cells()[0].c());
        assertEquals('\0', buffer.cells()[1].c());
        assertEquals('中', buffer.cells()[2].c());
        assertEquals('\0', buffer.cells()[3].c());

        pattern.renderTo(buffer, 3.2);
        assertEquals('C', buffer.cells()[0].c());
        assertEquals('D', buffer.cells()[1].c());
        assertEquals('A', buffer.cells()[2].c());
        assertEquals('B', buffer.cells()[3].c());
    }

    @Test
    void shouldRenderSolidRedAndGreenPhases() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2);
        DisplayTestPattern pattern = new DisplayTestPattern();

        pattern.renderTo(buffer, 4.2);
        for (TerminalBuffer.Cell cell : buffer.cells()) {
            assertEquals(0xFF0000, cell.bg());
        }

        pattern.renderTo(buffer, 5.7);
        for (TerminalBuffer.Cell cell : buffer.cells()) {
            assertEquals(0x00FF00, cell.bg());
        }
    }

    @Test
    void shouldRenderRollingPhaseWithSmoothTail() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2);
        DisplayTestPattern pattern = new DisplayTestPattern();

        pattern.renderTo(buffer, 7.1);

        int red = 0;
        int highlighted = 0;
        for (TerminalBuffer.Cell cell : buffer.cells()) {
            if (cell.bg() == 0xFF0000) {
                red++;
            }
            if (cell.bg() != 0x1C2833) {
                highlighted++;
            }
        }
        assertEquals(1, red);
        assertTrue(highlighted > 1);
    }
}
