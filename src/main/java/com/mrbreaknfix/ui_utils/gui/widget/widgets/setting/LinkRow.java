/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file LinkRow.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets.setting;

import com.mrbreaknfix.ui_utils.gui.ScreenOverlay;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.ScrollPanelWidget;
import com.mrbreaknfix.ui_utils.utils.Browse;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public record LinkRow(String uri) implements SettingRow {

    @Override
    public void init(
            int y,
            int width,
            ScrollPanelWidget panel,
            ScreenOverlay overlay,
            TextRenderer textRenderer,
            ColorPickerOpener opener) {
        Text linkText = Text.literal(uri).formatted(Formatting.UNDERLINE);
        Integer aquaVal = Formatting.AQUA.getColorValue();
        Integer yellowVal = Formatting.YELLOW.getColorValue();
        int baseColor = (aquaVal != null ? aquaVal : 0x55FFFF) | 0xFF000000;
        int hoverColor = (yellowVal != null ? yellowVal : 0xFFFF55) | 0xFF000000;
        Runnable openAction = () -> Browse.openLink(uri);
        ClickableLabelWidget clickableLink =
                new ClickableLabelWidget(
                        "auth_uri_link",
                        5,
                        y + 5,
                        linkText,
                        baseColor,
                        hoverColor,
                        false,
                        openAction,
                        overlay);
        panel.addContent(clickableLink);
    }

    @Override
    public int height() {
        return 20;
    }
}
