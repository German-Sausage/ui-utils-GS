/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file Event.java
 */
package com.mrbreaknfix.ui_utils.event;

public abstract class Event {
    private boolean cancelled;

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }
}
