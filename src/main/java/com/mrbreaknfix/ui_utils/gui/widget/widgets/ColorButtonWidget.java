/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ColorButtonWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets;

import com.mrbreaknfix.ui_utils.gui.BaseOverlay;
import com.mrbreaknfix.ui_utils.gui.GuiTheme;
import com.mrbreaknfix.ui_utils.gui.widget.HandCursor;
import com.mrbreaknfix.ui_utils.gui.widget.Noisy;
import com.mrbreaknfix.ui_utils.gui.widget.Widget;
import com.mrbreaknfix.ui_utils.utils.GuiUtils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class ColorButtonWidget extends Widget implements HandCursor, Noisy {
    private int color;
    private Runnable onClick;

    public ColorButtonWidget(
            String id, int x, int y, int width, int height, int initialColor, BaseOverlay overlay) {
        super(id, x, y, width, height, overlay);
        this.color = initialColor;
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        if (!isVisible()) return;

        boolean hovered = isMouseOver(mouseX, mouseY);
        int borderColor =
                isFocused()
                        ? GuiTheme.WIDGET_BORDER_FOCUSED
                        : (hovered ? GuiTheme.WIDGET_BORDER_HOVER : GuiTheme.WIDGET_BORDER);

        GuiUtils.drawBorder(context, x, y, width, height, borderColor);

        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, this.color);
    }

    @Override
    public void onMouseClick(double mouseX, double mouseY, double button) {
        if (button == 0 && isVisible() && onClick != null) {
            onClick.run();
        }
    }
}
