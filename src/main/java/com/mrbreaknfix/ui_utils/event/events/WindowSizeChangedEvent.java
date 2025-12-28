/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file WindowSizeChangedEvent.java
 */
package com.mrbreaknfix.ui_utils.event.events;

import com.mrbreaknfix.ui_utils.event.Event;

public class WindowSizeChangedEvent extends Event {
    private final int width;
    private final int height;

    public WindowSizeChangedEvent(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
