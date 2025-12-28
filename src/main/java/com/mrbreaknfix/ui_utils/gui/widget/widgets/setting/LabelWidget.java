/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file LabelWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets.setting;

import com.mrbreaknfix.ui_utils.gui.widget.Widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class LabelWidget extends Widget {
    private final Text text;
    private final int color;
    private final boolean shadow;

    public LabelWidget(String id, int x, int y, Text text, int color, boolean shadow) {
        super(id, x, y, mc.textRenderer.getWidth(text), mc.textRenderer.fontHeight, null);
        this.text = text;
        this.color = color;
        this.shadow = shadow;
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        if (!isVisible()) return;
        if (shadow) context.drawTextWithShadow(screen.getTextRenderer(), text, x, y, color);
        else context.drawText(screen.getTextRenderer(), text, x, y, color, false);
    }
}
