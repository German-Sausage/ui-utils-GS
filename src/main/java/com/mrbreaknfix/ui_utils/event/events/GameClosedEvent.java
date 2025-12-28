/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file GameClosedEvent.java
 */
package com.mrbreaknfix.ui_utils.event.events;

import com.mrbreaknfix.ui_utils.event.Event;

import net.minecraft.util.crash.CrashReport;

public class GameClosedEvent extends Event {
    // CRASHED or SOFT
    public static class Crashed extends GameClosedEvent {
        private final CrashReport report;

        public Crashed(CrashReport report) {
            this.report = report;
        }

        @SuppressWarnings("unused")
        public CrashReport getCrashReport() {
            return report;
        }
    }

    public static class Soft extends GameClosedEvent {}
}
