/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file DropdownWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets.dropdown;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.event.Subscribe;
import com.mrbreaknfix.ui_utils.event.events.mouse.ClickEvent;
import com.mrbreaknfix.ui_utils.gui.BaseOverlay;
import com.mrbreaknfix.ui_utils.gui.GuiTheme;
import com.mrbreaknfix.ui_utils.gui.widget.HandCursor;
import com.mrbreaknfix.ui_utils.gui.widget.Noisy;
import com.mrbreaknfix.ui_utils.gui.widget.Widget;
import com.mrbreaknfix.ui_utils.utils.GuiUtils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.glfw.GLFW;

public class DropdownWidget extends Widget implements HandCursor, Noisy {
    private final Map<Integer, String> options;
    private int selectedOption;
    private boolean expanded = false;
    private final List<DropdownOption> optionWidgets = new ArrayList<>();

    private float animationProgress = 0.0f;
    private static final float ANIMATION_DURATION_TICKS = 4.0f; // Animation duration

    public DropdownWidget(
            String id,
            int x,
            int y,
            int width,
            int height,
            Map<Integer, String> options,
            BaseOverlay overlay) {
        super(id, x, y, width, height, overlay);
        this.options = options;
        this.selectedOption = 0;
        UiUtils.eventManager.addListener(this);
    }

    @Override
    public void update(double mouseX, double mouseY, float deltaTicks) {
        // Animate open
        if (expanded && animationProgress < 1.0f) {
            animationProgress += deltaTicks / ANIMATION_DURATION_TICKS;
            animationProgress = MathHelper.clamp(animationProgress, 0.0f, 1.0f);
        }
        // Animate closed
        else if (!expanded && animationProgress > 0.0f) {
            animationProgress -= deltaTicks / ANIMATION_DURATION_TICKS;
            animationProgress = MathHelper.clamp(animationProgress, 0.0f, 1.0f);
            // Once the closing animation is finished, destroy the widgets
            if (animationProgress == 0.0f) {
                destroyOptions();
            }
        }
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        if (!isVisible()) return;

        // Render the main dropdown box
        int borderColor =
                isMouseOver(mouseX, mouseY) || expanded
                        ? GuiTheme.WIDGET_BORDER_HOVER
                        : GuiTheme.WIDGET_BORDER;
        //        context.fill(x, y, x + width, y + height, borderColor);
        GuiUtils.drawBorder(context, x, y, width, height, borderColor);
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, GuiTheme.WIDGET_BACKGROUND);

        String selectedText = options.get(selectedOption);
        if (selectedText != null) {
            context.drawText(
                    screen.getTextRenderer(),
                    selectedText,
                    x + GuiTheme.PADDING,
                    y + (height - screen.getTextRenderer().fontHeight) / 2 + 1,
                    GuiTheme.TEXT_PRIMARY,
                    false);
        }

        // Draw arrow indicator
        int arrowX = x + width - 10;
        int arrowY = y + height / 2;
        if (expanded) { // Up arrow
            context.fill(arrowX, arrowY, arrowX + 5, arrowY + 1, GuiTheme.TEXT_PRIMARY);
            context.fill(arrowX + 1, arrowY - 1, arrowX + 4, arrowY, GuiTheme.TEXT_PRIMARY);
            context.fill(arrowX + 2, arrowY - 2, arrowX + 3, arrowY - 1, GuiTheme.TEXT_PRIMARY);
        } else { // Down arrow
            context.fill(arrowX, arrowY, arrowX + 5, arrowY + 1, GuiTheme.TEXT_PRIMARY);
            context.fill(arrowX + 1, arrowY + 1, arrowX + 4, arrowY + 2, GuiTheme.TEXT_PRIMARY);
            context.fill(arrowX + 2, arrowY + 2, arrowX + 3, arrowY + 3, GuiTheme.TEXT_PRIMARY);
        }
    }

    private void createOptions() {
        if (!optionWidgets.isEmpty()) return; // Don't create if they already exist

        int optionY = this.y + height; // Start drawing options right below the main box
        for (Map.Entry<Integer, String> entry : options.entrySet()) {
            int optionId = entry.getKey();
            String optionText = entry.getValue();
            DropdownOption optionWidget =
                    new DropdownOption(
                            this,
                            "%s_opt_%d".formatted(this.getId(), optionId),
                            optionId,
                            optionText,
                            x,
                            optionY,
                            width,
                            height,
                            getOverlay());
            optionWidgets.add(optionWidget);
            getOverlay().addTopLevelWidget(optionWidget); // Add as a top-level widget
            optionY += height; // Increment by the full height to leave a 1px gap
        }
    }

    private void destroyOptions() {
        for (DropdownOption option : optionWidgets) {
            getOverlay().removeTopLevelWidget(option); // Remove from the overlay
        }
        optionWidgets.clear();
    }

    @Override
    public void onMouseClick(double mouseX, double mouseY, double button) {
        if (button != 0 || !isVisible()) return;

        expanded = !expanded;
        if (expanded) {
            createOptions();
        }
    }

    @Subscribe
    public void onScreenClicked(ClickEvent event) {
        // If the dropdown is expanded and the user clicks anywhere that is not this widget or one
        // of its options, close it.
        if (!expanded || event.getAction() != GLFW.GLFW_PRESS) return;

        double mouseX = event.getScaledX();
        double mouseY = event.getScaledY();

        boolean clickIsOnOption =
                optionWidgets.stream().anyMatch(w -> w.isMouseOver(mouseX, mouseY));

        if (!isMouseOver(mouseX, mouseY) && !clickIsOnOption) {
            expanded = false; // Trigger the collapse animation
        }
    }

    public void onOptionClicked(int optionId) {
        this.selectedOption = optionId;
        this.expanded = false; // Trigger the collapse animation
    }

    public float getAnimationProgress() {
        return this.animationProgress;
    }

    public int getOptionCount() {
        return this.options.size();
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public void close() {
        if (!this.isVisible()) {
            destroyOptions();
            this.animationProgress = 0.0f;
        } else {
            this.expanded = false; // Trigger the collapse animation
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        UiUtils.eventManager.removeListener(this);
    }
}
