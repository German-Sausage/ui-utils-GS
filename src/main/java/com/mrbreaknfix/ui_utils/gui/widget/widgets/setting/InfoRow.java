/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file InfoRow.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets.setting;

import com.mrbreaknfix.ui_utils.gui.GuiTheme;
import com.mrbreaknfix.ui_utils.gui.ScreenOverlay;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.ScrollPanelWidget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class InfoRow implements SettingRow {
    private final Text text;

    public InfoRow(String text) {
        this.text = Text.literal(text).formatted(Formatting.GRAY);
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
                new LabelWidget(
                        "info_" + text.getString(),
                        5,
                        y + 5,
                        text,
                        GuiTheme.TEXT_SECONDARY,
                        false));
    }

    @Override
    public int height() {
        return 20;
    }
}
