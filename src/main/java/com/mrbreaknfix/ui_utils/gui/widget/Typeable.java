/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file Typeable.java
 */
package com.mrbreaknfix.ui_utils.gui.widget;

import java.util.function.Function;

import org.lwjgl.glfw.GLFW;

import static java.lang.Character.toUpperCase;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public interface Typeable {
    @SuppressWarnings("unused")
    default boolean onKey(int key, int scan, int modifiers) {
        boolean shiftKeyPressed = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        boolean ctrlKeyPressed = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;

        if (mc.currentScreen != null) {
            String typed = GLFW.glfwGetKeyName(key, scan);
            if (key == GLFW.GLFW_KEY_ENTER) {
                onEnter();
                return true;
            } else if (key == GLFW.GLFW_KEY_BACKSPACE) {
                return onInput(
                        input -> {
                            if (!input.isEmpty()) {
                                return input.substring(0, input.length() - 1);
                            }
                            return input;
                        });
            } else if (key == GLFW.GLFW_KEY_SPACE) {
                return onInput(input -> input.concat(" "));
            } else if (key == GLFW.GLFW_KEY_V && ctrlKeyPressed) {
                return onInput(input -> input.concat(mc.keyboard.getClipboard()));
            }
        }
        return false;
    }

    default boolean onChar(int codePoint, int modifiers) {
        boolean shiftKeyPressed = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        if (mc.currentScreen != null) {
            String typed = Character.toString((char) codePoint);
            if (codePoint == GLFW.GLFW_KEY_BACKSPACE) {
                //                System.out.println("backspace");
                return onInput(
                        input -> {
                            if (!input.isEmpty()) {
                                return input.substring(0, input.length() - 1);
                            }
                            return input;
                        });
            } else if (codePoint == GLFW.GLFW_KEY_SPACE) {
                return onInput(input -> input.concat(" "));
            } else if (shiftKeyPressed) {
                return onInput(input -> input.concat(keyPressWithShift(typed)));
            } else {
                return onInput(input -> input.concat(typed));
            }
        }
        return false;
    }

    boolean onInput(Function<String, String> factory);

    static String keyPressWithShift(String s) {
        if (s.length() != 1) return s;
        char c = s.charAt(0);
        if (c >= 'a' && c <= 'z') {
            return String.valueOf(toUpperCase(c));
        }
        return switch (c) {
            case '1' -> "!";
            case '2' -> "@";
            case '3' -> "#";
            case '4' -> "$";
            case '5' -> "%";
            case '6' -> "^";
            case '7' -> "&";
            case '8' -> "*";
            case '9' -> "(";
            case '0' -> ")";
            case '-' -> "_";
            case '=' -> "+";
            case '[' -> "{";
            case ']' -> "}";
            case ';' -> ":";
            case '\'' -> "\"";
            case ',' -> "<";
            case '.' -> ">";
            case '/' -> "?";
            case '\\' -> "|";
            case '`' -> "~";
            default -> s;
        };
    }

    default void onEnter() {}
}
