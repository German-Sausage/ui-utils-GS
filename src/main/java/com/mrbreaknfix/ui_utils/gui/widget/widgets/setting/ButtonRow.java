/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ButtonRow.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets.setting;

import com.mrbreaknfix.ui_utils.gui.GuiTheme;
import com.mrbreaknfix.ui_utils.gui.ScreenOverlay;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.ButtonWidget;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.ScrollPanelWidget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

public record ButtonRow(String name, String description, Runnable action, int buttonWidth)
        implements SettingRow {
    public ButtonRow(String name, String description, Runnable action) {
        this(name, description, action, 100);
    }

    @Override
    public void init(
            int y,
            int width,
            ScrollPanelWidget panel,
            ScreenOverlay overlay,
            TextRenderer textRenderer,
            ColorPickerOpener opener) {
        panel.addContent(
                new LabelWidget("label_" + name, 0, y + 2, Text.of(name), Colors.WHITE, false));
        if (description != null && !description.isEmpty()) {
            panel.addContent(
                    new LabelWidget(
                            "desc_" + name,
                            0,
                            y + 12,
                            Text.literal(description).formatted(Formatting.ITALIC),
                            GuiTheme.TEXT_SECONDARY,
                            false));
        }
        ButtonWidget actionButton =
                new ButtonWidget(
                        "action_" + name.replace(' ', '_'),
                        name,
                        width - buttonWidth,
                        y,
                        buttonWidth,
                        20,
                        overlay);
        actionButton.setOnClick(action);
        panel.addContent(actionButton);
        panel.addContent(new DividerWidget("div_" + name, 0, y + 30, width));
    }

    @Override
    public int height() {
        return 42;
    }
}
