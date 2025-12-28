/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file NumInputWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets;

import java.util.function.Function;

import com.mrbreaknfix.ui_utils.gui.BaseOverlay;
import com.mrbreaknfix.ui_utils.gui.GuiTheme;
import com.mrbreaknfix.ui_utils.gui.widget.Typeable;
import com.mrbreaknfix.ui_utils.gui.widget.Widget;
import com.mrbreaknfix.ui_utils.utils.GuiUtils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class NumInputWidget extends Widget implements Typeable {
    private String input = "";
    private String defaultInput = "";

    public NumInputWidget(String id, int x, int y, int width, int height, BaseOverlay overlay) {
        super(id, x, y, width, height, overlay);
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        if (!isVisible()) return;

        int borderColor =
                isFocused()
                        ? GuiTheme.WIDGET_BORDER_FOCUSED
                        : (isMouseOver(mouseX, mouseY)
                                ? GuiTheme.WIDGET_BORDER_HOVER
                                : GuiTheme.WIDGET_BORDER);
        GuiUtils.drawBorder(context, x, y, width, height, borderColor);
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, GuiTheme.WIDGET_BACKGROUND);

        String text = input.isEmpty() ? defaultInput : input;
        int textColor = input.isEmpty() ? GuiTheme.TEXT_PLACEHOLDER : GuiTheme.TEXT_PRIMARY;

        int textWidth = screen.getTextRenderer().getWidth(text);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - screen.getTextRenderer().fontHeight) / 2 + 1;
        context.drawText(screen.getTextRenderer(), text, textX, textY, textColor, false);
    }

    @Override
    public boolean onInput(Function<String, String> factory) {
        if (!isFocused()) return false;
        String modifiedInput = factory.apply(input);

        if (modifiedInput.matches("-?\\d*")) {
            input = modifiedInput;
            return true;
        }
        return false;
    }

    public String getTextOrDefault() {
        return input.isEmpty() ? defaultInput : input;
    }

    public int getInt() {
        try {
            String text = getTextOrDefault();
            if (text.isEmpty()) return 0;
            if (text.equals("-")) return -1;
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getDefaultInt() {
        try {
            if (defaultInput.isEmpty()) return 0;
            if (defaultInput.equals("-")) return -1;
            return Integer.parseInt(defaultInput);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setDefaultInput(String text) {
        this.defaultInput = text;
    }

    public void setText(String text) {
        this.input = text;
    }

    public void clearInput() {
        this.input = "";
    }

    public String getDefaultInput() {
        return defaultInput;
    }

    public String getInput() {
        return input;
    }
}
