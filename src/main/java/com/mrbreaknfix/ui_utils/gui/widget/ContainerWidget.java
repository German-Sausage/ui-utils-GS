/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ContainerWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget;

import net.minecraft.client.gui.DrawContext;

public interface ContainerWidget {
    void renderDebugHighlight(DrawContext context, double mouseX, double mouseY);

    Widget getHoveredChild(double mouseX, double mouseY);
}
