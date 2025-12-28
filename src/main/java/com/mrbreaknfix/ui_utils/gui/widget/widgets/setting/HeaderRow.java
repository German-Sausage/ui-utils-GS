/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file HeaderRow.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets.setting;

import com.mrbreaknfix.ui_utils.gui.ScreenOverlay;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.ScrollPanelWidget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

public class HeaderRow implements SettingRow {
    private final Text text;

    public HeaderRow(String text) {
        this.text = Text.literal(text).formatted(Formatting.UNDERLINE);
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
                new LabelWidget("header_" + text.getString(), 0, y, text, Colors.WHITE, false));
    }

    @Override
    public int height() {
        return 20;
    }
}
