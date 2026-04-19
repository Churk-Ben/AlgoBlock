package com.algoblock.gl.input;

public class KeyMapper {
    public static boolean isSubmit(int key) {
        return key == 257; // Enter
    }

    public static boolean isBackspace(int key) {
        return key == 259; // Backspace
    }

    public static boolean isTab(int key) {
        return key == 258; // Tab
    }

    public static boolean isSpace(int key) {
        return key == 32; // Space
    }

    public static boolean isLeft(int key) {
        return key == 263; // Left
    }

    public static boolean isRight(int key) {
        return key == 262; // Right
    }

    public static boolean isUp(int key) {
        return key == 265; // Up
    }

    public static boolean isDown(int key) {
        return key == 264; // Down
    }

    public static boolean isDelete(int key) {
        return key == 261; // Delete
    }
}
