/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ScreenHistory.java
 */
package com.mrbreaknfix.ui_utils.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.screen.Screen;

import org.jetbrains.annotations.Nullable;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class ScreenHistory {
    private static final List<Screen> history = new ArrayList<>();
    // Pointer to the current screen in the list. -1 means history is empty.
    private static int currentIndex = -1;
    private static final int MAX_HISTORY_SIZE = 50;
    private static boolean isNavigating =
            false; // Prevents feedback loops from back/forward commands

    public static void push(@Nullable Screen screen) {
        if (isNavigating) {
            return;
        }

        // opening a new screen clears the "forward" history.
        if (currentIndex < history.size() - 1) {
            history.subList(currentIndex + 1, history.size()).clear();
        }

        // Don't add the same screen instance twice in a row.
        if (currentIndex > -1 && history.get(currentIndex) == screen) {
            return;
        }

        // push a null screen to represent a "no screen" / closed state
        if (screen == null && currentIndex > -1 && history.get(currentIndex) == null) {
            return;
        }

        history.add(screen);
        currentIndex++;

        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
            currentIndex--; // Adjust index since we removed from the start.
        }
    }

    /** Navigates one step back in the history, if possible. */
    public static void back() {
        if (canGoBack()) {
            isNavigating = true;
            currentIndex--;
            mc.execute(() -> mc.setScreen(history.get(currentIndex)));
            isNavigating = false;
        }
    }

    /** Navigates one step forward in the history, if possible. */
    public static void forward() {
        if (canGoForward()) {
            isNavigating = true;
            currentIndex++;
            mc.execute(() -> mc.setScreen(history.get(currentIndex)));
            isNavigating = false;
        }
    }

    /**
     * Loads a screen from a specific user-facing index in the history.
     *
     * @param displayIndex The index to load (0 is the current screen).
     * @throws IndexOutOfBoundsException if the index is invalid.
     */
    public static void loadByDisplayIndex(int displayIndex) throws IndexOutOfBoundsException {
        // Translate user-facing index (0=newest) to internal ArrayList index.
        int internalIndex = (history.size() - 1) - displayIndex;
        if (internalIndex < 0 || internalIndex >= history.size()) {
            throw new IndexOutOfBoundsException("History index out of bounds: " + displayIndex);
        }

        isNavigating = true;
        currentIndex = internalIndex;
        mc.execute(() -> mc.setScreen(history.get(currentIndex)));
        isNavigating = false;
    }

    public static boolean canGoBack() {
        return currentIndex > 0;
    }

    public static boolean canGoForward() {
        return currentIndex < history.size() - 1;
    }

    /** Returns a reversed view of the history for display (newest first). */
    public static List<Screen> getDisplayHistory() {
        List<Screen> reversed = new ArrayList<>(history);
        Collections.reverse(reversed);
        return reversed;
    }

    /** Returns the user-facing index of the current screen (0 is newest). */
    public static int getDisplayCurrentIndex() {
        return (history.size() - 1) - currentIndex;
    }

    public static Screen getCurrentScreen() {
        if (currentIndex < 0 || currentIndex >= history.size()) {
            return null; // No current screen
        }
        return history.get(currentIndex);
    }
}
