package com.algoblock.gl.input;

public class KeyMapper {
    public static boolean isSubmit(int key) {
        return key == 257;
    }

    public static boolean isBackspace(int key) {
        return key == 259;
    }

    public static boolean isTab(int key) {
        return key == 258;
    }
}
