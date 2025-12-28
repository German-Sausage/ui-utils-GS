/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file SlotManager.java
 */
package com.mrbreaknfix.ui_utils.utils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class SlotManager {
    private boolean shouldDrawSlotIDs, isPicking, shouldStopPicking, shouldRenderHighlightedSlot;
    private Color slotIDTextColor;
    private static int highlightedSlotID = -1;
    private static final int slotSize = 32;
    private Color highlightColor;

    public SlotManager() {
        this.shouldDrawSlotIDs = false;
        this.slotIDTextColor = new Color(0xFF2986cc).darker().darker().darker().darker();
        this.highlightColor = new Color(0xFFc700ff);
    }

    public void drawSlotID(DrawContext context, Slot slot) {
        if (mc.currentScreen == null) return;
        Text text =
                Text.literal(String.valueOf(slot.id))
                        .styled(style -> style.withColor(slotIDTextColor.getHex()));

        int textWidth = mc.textRenderer.getWidth(text);

        int textX = slot.x + slotSize / 4 - textWidth / 2;
        int textY = slot.y + slotSize / 4 - 4;

        context.drawText(
                mc.currentScreen.getTextRenderer(), text, textX, textY, Colors.PURPLE, false);
    }

    public void drawHighlightedOnSlot(DrawContext context, Slot slot) {
        context.fill(
                slot.x,
                slot.y,
                slot.x + slotSize / 2,
                slot.y + slotSize / 2,
                highlightColor.getHex());
        drawSlotID(context, slot);
    }

    public void setHighlightedSlotID(int id) {
        highlightedSlotID = id;
    }

    public int getHighlightedSlotID() {
        return highlightedSlotID;
    }

    public void setShouldDrawSlotIDs(boolean shouldDrawSlotIDs) {
        this.shouldDrawSlotIDs = shouldDrawSlotIDs;
    }

    // color setters
    public void setSlotIDTextColor(Color color) {
        this.slotIDTextColor = color;
    }

    public void setHighlightColor(Color color) {
        this.highlightColor = color;
    }

    public boolean shouldDrawSlotIDs() {
        return shouldDrawSlotIDs;
    }

    public void setPicking(boolean isPicking) {
        this.isPicking = isPicking;
    }

    public boolean isPicking() {
        return isPicking;
    }

    public void setShouldStopPicking(boolean shouldStopPicking) {
        this.shouldStopPicking = shouldStopPicking;
    }

    public boolean shouldStopPicking() {
        return shouldStopPicking;
    }

    public boolean shouldRenderHighlightedSlot() {
        return shouldRenderHighlightedSlot;
    }

    public void setShouldRenderHighlightedSlot(boolean shouldRenderHighlightedSlot) {
        this.shouldRenderHighlightedSlot = shouldRenderHighlightedSlot;
    }
}
