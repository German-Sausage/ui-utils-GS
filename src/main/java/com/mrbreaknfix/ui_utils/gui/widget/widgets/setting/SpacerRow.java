/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file SpacerRow.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets.setting;

import com.mrbreaknfix.ui_utils.gui.ScreenOverlay;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.ScrollPanelWidget;

import net.minecraft.client.font.TextRenderer;

public record SpacerRow(int height) implements SettingRow {

    @Override
    public void init(
            int y,
            int width,
            ScrollPanelWidget panel,
            ScreenOverlay overlay,
            TextRenderer textRenderer,
            ColorPickerOpener opener) {}
}
