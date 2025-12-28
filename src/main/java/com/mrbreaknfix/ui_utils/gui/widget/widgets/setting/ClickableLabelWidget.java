/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ClickableLabelWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets.setting;

import com.mrbreaknfix.ui_utils.gui.ScreenOverlay;
import com.mrbreaknfix.ui_utils.gui.widget.Widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class ClickableLabelWidget extends Widget {
    private final Text baseText;
    private final int color;
    private final int hoverColor;
    private final boolean shadow;
    private final Runnable onClick;

    public ClickableLabelWidget(
            String id,
            int x,
            int y,
            Text text,
            int color,
            int hoverColor,
            boolean shadow,
            Runnable onClick,
            ScreenOverlay overlay) {
        super(id, x, y, mc.textRenderer.getWidth(text), mc.textRenderer.fontHeight, overlay);
        this.baseText = text;
        this.color = color;
        this.hoverColor = hoverColor;
        this.shadow = shadow;
        this.onClick = onClick;
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        boolean hovered = this.isMouseOver(mouseX, mouseY);
        int currentColor = hovered ? hoverColor : color;
        if ((currentColor & 0xFF000000) == 0) currentColor |= 0xFF000000;

        if (shadow) {
            context.drawTextWithShadow(screen.getTextRenderer(), baseText, x, y, currentColor);
        } else {
            context.drawText(screen.getTextRenderer(), baseText, x, y, currentColor, false);
        }
    }

    @Override
    public void onMouseClick(double mouseX, double mouseY, double button) {
        super.onMouseClick(mouseX, mouseY, button);

        if (onClick != null) {
            onClick.run();
        }
    }
}
