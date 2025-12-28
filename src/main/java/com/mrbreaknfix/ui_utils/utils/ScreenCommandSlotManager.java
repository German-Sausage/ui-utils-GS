/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ScreenCommandSlotManager.java
 */
package com.mrbreaknfix.ui_utils.utils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class ScreenCommandSlotManager {
    private static int highlightedSlotId = -1;
    private static boolean shouldRenderHighlight = false;
    private static boolean shouldDrawIds = false;

    public static int getHighlightedSlotId() {
        return highlightedSlotId;
    }

    public static void setHighlightedSlotId(int id) {
        highlightedSlotId = id;
    }

    public static boolean shouldRenderHighlight() {
        return shouldRenderHighlight;
    }

    public static void setShouldRenderHighlight(boolean should) {
        shouldRenderHighlight = should;
    }

    public static boolean shouldDrawIds() {
        return shouldDrawIds;
    }

    public static void setShouldDrawIds(boolean should) {
        shouldDrawIds = should;
    }

    public static void drawHighlightOnSlot(DrawContext context, Slot slot) {
        if (shouldRenderHighlight && slot.id == highlightedSlotId) {
            context.fill(
                    slot.x,
                    slot.y,
                    slot.x + 16,
                    slot.y + 16,
                    0x80_00FF00); // Semi-transparent green
        }
    }

    public static void drawSlotId(DrawContext context, Slot slot) {
        if (shouldDrawIds) {
            String idStr = String.valueOf(slot.id);
            context.drawTextWithShadow(
                    mc.textRenderer, Text.literal(idStr), slot.x, slot.y, 0xFFFFFFFF);
        }
    }
}
