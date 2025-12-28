/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file BooleanSettingRow.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets.setting;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mrbreaknfix.ui_utils.gui.GuiTheme;
import com.mrbreaknfix.ui_utils.gui.ScreenOverlay;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.ButtonWidget;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.ScrollPanelWidget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

public record BooleanSettingRow(
        String name, String description, Supplier<Boolean> getter, Consumer<Boolean> setter)
        implements SettingRow {

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
        panel.addContent(
                new LabelWidget(
                        "desc_" + name,
                        0,
                        y + 12,
                        Text.literal(description).formatted(Formatting.ITALIC),
                        GuiTheme.TEXT_SECONDARY,
                        false));
        ButtonWidget toggleButton =
                new ButtonWidget(
                        "toggle_" + name.replace(' ', '_'),
                        getter.get() ? "Enabled" : "Disabled",
                        width - 75,
                        y,
                        75,
                        20,
                        overlay);
        toggleButton.setOnClick(
                () -> {
                    boolean newValue = !getter.get();
                    setter.accept(newValue);
                    toggleButton.setText(newValue ? "Enabled" : "Disabled");
                });
        panel.addContent(toggleButton);
        panel.addContent(new DividerWidget("div_" + name, 0, y + 30, width));
    }

    @Override
    public int height() {
        return 42;
    }
}
