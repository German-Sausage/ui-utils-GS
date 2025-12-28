/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ButtonWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets;

import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.gui.BaseOverlay;
import com.mrbreaknfix.ui_utils.gui.GuiTheme;
import com.mrbreaknfix.ui_utils.gui.widget.HandCursor;
import com.mrbreaknfix.ui_utils.gui.widget.Noisy;
import com.mrbreaknfix.ui_utils.gui.widget.Widget;
import com.mrbreaknfix.ui_utils.utils.GuiUtils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class ButtonWidget extends Widget implements HandCursor, Noisy {
    private String text;
    private Runnable onClick;

    public ButtonWidget(
            String id, String text, int x, int y, int width, int height, BaseOverlay overlay) {
        super(id, x, y, width, height, overlay);
        this.text = text;
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        if (!isVisible()) return;

        boolean hovered = isMouseOver(mouseX, mouseY);
        int borderColor = hovered ? GuiTheme.WIDGET_BORDER_HOVER : GuiTheme.WIDGET_BORDER;

        GuiUtils.drawBorder(context, x, y, width, height, borderColor);

        int buttonX = x + GuiTheme.BORDER_WIDTH;
        int buttonY = y + GuiTheme.BORDER_WIDTH;
        int buttonWidth = width - (GuiTheme.BORDER_WIDTH * 2);
        int buttonHeight = height - (GuiTheme.BORDER_WIDTH * 2);
        int bgColor = hovered ? GuiTheme.BUTTON_BACKGROUND_HOVER : GuiTheme.BUTTON_BACKGROUND;
        context.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, bgColor);

        int textWidth = screen.getTextRenderer().getWidth(text);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - screen.getTextRenderer().fontHeight) / 2 + 1;
        context.drawText(
                screen.getTextRenderer(), text, textX, textY, GuiTheme.TEXT_PRIMARY, false);
    }

    @Override
    public void onMouseClick(double mouseX, double mouseY, double button) {
        if (button != 0 || !isVisible()) return;

        if (onClick != null) {
            onClick.run();
        } else {
            UiUtils.LOGGER.warn("ButtonWidget: {} onClick is null", this.getId());
        }
    }
}
