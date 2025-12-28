/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file DropdownOption.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets.dropdown;

import com.mrbreaknfix.ui_utils.gui.BaseOverlay;
import com.mrbreaknfix.ui_utils.gui.GuiTheme;
import com.mrbreaknfix.ui_utils.gui.widget.HandCursor;
import com.mrbreaknfix.ui_utils.gui.widget.Noisy;
import com.mrbreaknfix.ui_utils.gui.widget.Widget;
import com.mrbreaknfix.ui_utils.utils.GuiUtils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class DropdownOption extends Widget implements HandCursor, Noisy {

    private final DropdownWidget dropdown;
    private final int optionId;
    private final String text;

    public DropdownOption(
            DropdownWidget dropdown,
            String id,
            int optionId,
            String text,
            int x,
            int y,
            int width,
            int height,
            BaseOverlay overlay) {
        super(id, x, y, width, height, overlay);
        this.dropdown = dropdown;
        this.optionId = optionId;
        this.text = text;
        this.setZIndex(1000 + optionId); // High Z-index
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        float progress = dropdown.getAnimationProgress();
        if (progress <= 0) return;

        int totalOptionsHeight = dropdown.getHeight() * dropdown.getOptionCount();
        int animatedHeight = (int) (totalOptionsHeight * progress);

        // Define the clipping area starting from below the main dropdown box
        int scissorX = dropdown.getX();
        int scissorY = dropdown.getY() + dropdown.getHeight();

        // Apply the clipping mask (scissor)
        context.enableScissor(
                scissorX, scissorY, scissorX + dropdown.getWidth(), scissorY + animatedHeight);

        // Render the option itself
        boolean hovered = isMouseOver(mouseX, mouseY);
        int bgColor = hovered ? GuiTheme.BUTTON_BACKGROUND_HOVER : GuiTheme.WIDGET_BACKGROUND;

        GuiUtils.drawBorder(context, x, y, width, height, GuiTheme.WIDGET_BORDER);
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, bgColor);
        context.drawText(
                screen.getTextRenderer(),
                text,
                x + GuiTheme.PADDING,
                y + (height - screen.getTextRenderer().fontHeight) / 2 + 1,
                GuiTheme.TEXT_PRIMARY,
                false);

        // Disable the clipping mask so it doesn't affect other UI elements
        context.disableScissor();
    }

    @Override
    public void onMouseClick(double mouseX, double mouseY, double button) {
        if (button == 0) {
            dropdown.onOptionClicked(this.optionId);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        // The option is only considered "hoverable" if the animation is fully complete
        // to prevent clicking on options that are not yet fully visible.
        if (dropdown.getAnimationProgress() < 1.0f) {
            return false;
        }
        return super.isMouseOver(mouseX, mouseY);
    }
}
