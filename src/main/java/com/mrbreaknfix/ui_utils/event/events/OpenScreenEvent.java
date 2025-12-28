/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file OpenScreenEvent.java
 */
package com.mrbreaknfix.ui_utils.event.events;

import com.mrbreaknfix.ui_utils.event.Event;

import net.minecraft.client.gui.screen.Screen;

public class OpenScreenEvent extends Event {
    private final Screen screen;

    public OpenScreenEvent(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return this.screen;
    }
}
