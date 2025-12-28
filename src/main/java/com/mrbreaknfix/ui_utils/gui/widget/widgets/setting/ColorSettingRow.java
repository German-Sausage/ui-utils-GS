/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ColorSettingRow.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets.setting;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mrbreaknfix.ui_utils.gui.ScreenOverlay;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.ColorButtonWidget;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.ScrollPanelWidget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

public class ColorSettingRow implements SettingRow {
    private final String name;
    private final Supplier<Integer> getter;
    private final Consumer<Integer> setter;

    public ColorSettingRow(String name, Supplier<Integer> getter, Consumer<Integer> setter) {
        this.name = name;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void init(
            int y,
            int width,
            ScrollPanelWidget panel,
            ScreenOverlay overlay,
            TextRenderer textRenderer,
            ColorPickerOpener opener) {
        Text label = Text.of(name);
        panel.addContent(new LabelWidget("label_" + name, 0, y + 5, label, Colors.WHITE, false));

        ColorButtonWidget colorButton =
                new ColorButtonWidget(
                        "color_" + name.replace(' ', '_'),
                        width - 20,
                        y,
                        20,
                        20,
                        getter.get(),
                        overlay);
        colorButton.setOnClick(() -> opener.open(colorButton, getter, setter));
        panel.addContent(colorButton);

        int labelWidth = textRenderer.getWidth(label);
        int dividerX = labelWidth + 5;
        int dividerWidth = Math.max(0, colorButton.getX() - (dividerX + 5));
        panel.addContent(new DividerWidget("div_" + name, dividerX, y + 10, dividerWidth));
    }

    @Override
    public int height() {
        return 24;
    }
}
