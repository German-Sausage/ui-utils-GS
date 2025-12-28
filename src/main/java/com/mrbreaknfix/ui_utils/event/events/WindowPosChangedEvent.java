/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file WindowPosChangedEvent.java
 */
package com.mrbreaknfix.ui_utils.event.events;

import com.mrbreaknfix.ui_utils.event.Event;

public class WindowPosChangedEvent extends Event {
    private final int x;
    private final int y;

    public WindowPosChangedEvent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
