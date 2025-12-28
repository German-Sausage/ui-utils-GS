/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file CursorManager.java
 */
package com.mrbreaknfix.ui_utils.utils;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class CursorManager {
    private static long window = 0;
    private static boolean initialized = false;
    private static long currentCursor = -1;

    private static final Map<Integer, Long> loadedCursors = new HashMap<>();

    public static final int ARROW = GLFW.GLFW_ARROW_CURSOR;
    public static final int IBEAM = GLFW.GLFW_IBEAM_CURSOR;
    public static final int CROSSHAIR = GLFW.GLFW_CROSSHAIR_CURSOR;
    public static final int HAND = GLFW.GLFW_HAND_CURSOR;
    public static final int HRESIZE = GLFW.GLFW_HRESIZE_CURSOR;
    public static final int VRESIZE = GLFW.GLFW_VRESIZE_CURSOR;

    private static void tryInit() {
        if (initialized) return;

        if (mc == null || mc.currentScreen == null) return;

        //        System.out.println("Initializing CursorManager...");
        window = mc.getWindow().getHandle();
        //        System.out.println("Initializing CursorManager with window handle: " + window);
        initialized = true;
    }

    private static long getOrCreateCursor(int type) {
        if (loadedCursors.containsKey(type)) {
            return loadedCursors.get(type);
        }

        // If GLFW is not yet ready, return 0
        if (!initialized) return 0;

        long cursor = GLFW.glfwCreateStandardCursor(type);
        loadedCursors.put(type, cursor);
        return cursor;
    }

    // Set the current cursor
    public static void setCursor(int type) {
        tryInit(); // Ensure GLFW and window are initialized before changing cursor

        if (!initialized || window == 0) return;

        long targetCursor = getOrCreateCursor(type);
        if (targetCursor == 0) return;

        if (currentCursor != targetCursor) {
            GLFW.glfwSetCursor(window, targetCursor);
            currentCursor = targetCursor;
        }
    }

    public static void setArrow() {
        setCursor(ARROW);
    }

    public static void setPointer() {
        setCursor(HAND);
    }

    public static void setIBeam() {
        setCursor(IBEAM);
    }

    public static void setCrosshair() {
        setCursor(CROSSHAIR);
    }

    public static void setHResize() {
        setCursor(HRESIZE);
    }

    public static void setVResize() {
        setCursor(VRESIZE);
    }

    public static void destroy() {
        if (!initialized) return;

        for (long cursor : loadedCursors.values()) {
            if (cursor != 0) GLFW.glfwDestroyCursor(cursor);
        }
        loadedCursors.clear();
        currentCursor = -1;
        initialized = false;
    }
}
