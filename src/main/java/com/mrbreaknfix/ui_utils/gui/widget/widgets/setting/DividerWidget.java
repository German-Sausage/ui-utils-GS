/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file DividerWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets.setting;

import com.mrbreaknfix.ui_utils.gui.GuiTheme;
import com.mrbreaknfix.ui_utils.gui.widget.Widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class DividerWidget extends Widget {
    public DividerWidget(String id, int x, int y, int width) {
        super(id, x, y, width, 1, null);
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        if (!isVisible()) return;
        context.fill(x, y, x + width, y + 1, GuiTheme.WIDGET_BORDER);
    }
}
