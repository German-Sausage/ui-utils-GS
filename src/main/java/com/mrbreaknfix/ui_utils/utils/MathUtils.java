/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file MathUtils.java
 */
package com.mrbreaknfix.ui_utils.utils;

@SuppressWarnings("unused")
public class MathUtils {
    // clamp

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(float delta) {
        return Math.max(0, Math.min(1, delta));
    }

    // lerp
    public static double lerp(double start, double end, double delta) {
        return start + (end - start) * delta;
    }

    public static float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    public static int lerp(int start, int end, float delta) {
        return (int) (start + (end - start) * delta);
    }
}
