/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file IconButtonWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets;

import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.gui.BaseOverlay;
import com.mrbreaknfix.ui_utils.gui.GuiTheme;
import com.mrbreaknfix.ui_utils.gui.widget.HandCursor;
import com.mrbreaknfix.ui_utils.gui.widget.Noisy;
import com.mrbreaknfix.ui_utils.gui.widget.Widget;
import com.mrbreaknfix.ui_utils.utils.GuiUtils;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;

public class IconButtonWidget extends Widget implements HandCursor, Noisy {
    private Identifier icon;
    private Runnable onClick;

    public IconButtonWidget(
            String id, String iconPath, int x, int y, int width, int height, BaseOverlay overlay) {
        super(id, x, y, width, height, overlay);
        this.icon = Identifier.of(UiUtils.MOD_ID, iconPath);
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        int borderColor =
                this.isMouseOver(mouseX, mouseY)
                        ? GuiTheme.WIDGET_BORDER_HOVER
                        : GuiTheme.WIDGET_BORDER;

        GuiUtils.drawBorder(context, x, y, width, height, borderColor);

        int buttonX = x + 1;
        int buttonY = y + 1;
        int buttonWidth = width - 2;
        int buttonHeight = height - 2;

        context.fill(
                buttonX,
                buttonY,
                buttonX + buttonWidth,
                buttonY + buttonHeight,
                this.isMouseOver(mouseX, mouseY)
                        ? GuiTheme.BUTTON_BACKGROUND
                        : GuiTheme.BUTTON_BACKGROUND_HOVER);
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                icon,
                x,
                y,
                0,
                0,
                width,
                height,
                width,
                height,
                width,
                height);
    }

    @Override
    public void onMouseClick(double mouseX, double mouseY, double button) {
        if (button != 0) return;
        if (onClick != null) {
            onClick.run();
        } else {
            UiUtils.LOGGER.warn("ButtonWidget: onClick is null");
        }
    }
}
