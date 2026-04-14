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

    public static boolean isLeft(int key) {
        return key == 263;
    }

    public static boolean isRight(int key) {
        return key == 262;
    }

    public static boolean isDelete(int key) {
        return key == 261;
    }
}
