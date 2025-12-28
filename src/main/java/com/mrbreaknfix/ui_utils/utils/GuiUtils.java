/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file GuiUtils.java
 */
package com.mrbreaknfix.ui_utils.utils;

import net.minecraft.client.gui.DrawContext;

public class GuiUtils { //

    /**
     * Draws a 1-pixel thick, unfilled rectangle. This is an immediate-mode replacement for the old
     * drawBorder method.
     *
     * @param context The DrawContext.
     * @param x The top-left x-coordinate.
     * @param y The top-left y-coordinate.
     * @param width The width of the border.
     * @param height The height of the border.
     * @param color The color of the border in ARGB format.
     */
    public static void drawBorder(
            DrawContext context, int x, int y, int width, int height, int color) {
        // Top line
        context.fill(x, y, x + width, y + 1, color);
        // Bottom line
        context.fill(x, y + height - 1, x + width, y + height, color);
        // Left line
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        // Right line
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }
}
