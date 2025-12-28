/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file CodeRow.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets.setting;

import com.mrbreaknfix.ui_utils.gui.ScreenOverlay;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.ButtonWidget;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.ScrollPanelWidget;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.TextInputWidget;

import net.minecraft.client.font.TextRenderer;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public record CodeRow(String code) implements SettingRow {

    @Override
    public void init(
            int y,
            int width,
            ScrollPanelWidget panel,
            ScreenOverlay overlay,
            TextRenderer textRenderer,
            ColorPickerOpener opener) {
        TextInputWidget codeInput =
                new TextInputWidget("auth_code_display", 5, y, width - 95, 25, overlay);
        codeInput.setText(this.code);
        codeInput.setEditable(false);
        panel.addContent(codeInput);

        ButtonWidget copyButton =
                new ButtonWidget("auth_copy_code", "Copy Code", width - 85, y, 80, 20, overlay);
        copyButton.setOnClick(() -> mc.keyboard.setClipboard(this.code));
        panel.addContent(copyButton);
    }

    @Override
    public int height() {
        return 35;
    }
}
