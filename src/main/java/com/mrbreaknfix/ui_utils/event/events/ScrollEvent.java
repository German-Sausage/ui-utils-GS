/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ScrollEvent.java
 */
package com.mrbreaknfix.ui_utils.event.events;

import com.mrbreaknfix.ui_utils.event.Event;

public class ScrollEvent extends Event {
    private final double horizontal;
    private final double vertical;
    private final double scaledX;
    private final double scaledY;

    //            ScrollEvent clickEvent = new ScrollEvent(horizontal, vertical, d, e);
    public ScrollEvent(double horizontal, double vertical, double d, double e) {
        this.horizontal = horizontal;
        this.vertical = vertical;
        this.scaledX = d;
        this.scaledY = e;
    }

    public double getHorizontal() {
        return horizontal;
    }

    public double getVertical() {
        return vertical;
    }

    public double getScaledX() {
        return scaledX;
    }

    public double getScaledY() {
        return scaledY;
    }

    public boolean isHorizontal() {
        return horizontal != 0;
    }

    public boolean isVertical() {
        return vertical != 0;
    }
}
