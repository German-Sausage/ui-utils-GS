/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ClickEvent.java
 */
package com.mrbreaknfix.ui_utils.event.events.mouse;

import com.mrbreaknfix.ui_utils.event.Event;

public class ClickEvent extends Event {
    private final int action;
    private final int mods;
    private final double scaledX;
    private final double scaledY;
    private final double button;

    public ClickEvent(int action, int mods, int button, double scaledX, double scaledY) {
        this.action = action;
        this.mods = mods;
        this.scaledX = scaledX;
        this.scaledY = scaledY;
        this.button = button;
    }

    public double getButton() {
        return button;
    }

    public int getAction() {
        return action;
    }

    public int getMods() {
        return mods;
    }

    public double getScaledX() {
        return scaledX;
    }

    public double getScaledY() {
        return scaledY;
    }
}
