/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file UiUtilsConfigScreen.java
 */
package com.mrbreaknfix.ui_utils.gui.screen;

import java.util.ArrayList;
import java.util.List;

import com.mrbreaknfix.ui_utils.gui.GuiTheme;
import com.mrbreaknfix.ui_utils.gui.ScreenOverlay;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.*;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.setting.*;
import com.mrbreaknfix.ui_utils.persistance.PersistentSettings;
import com.mrbreaknfix.ui_utils.persistance.Settings;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import org.lwjgl.glfw.GLFW;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class UiUtilsConfigScreen extends Screen {
    private final Screen parent;
    private final ScreenOverlay overlay;
    private ScrollPanelWidget scrollPanel;
    private ColorPickerWidget activeColorPicker = null;
    private static final int PANEL_PADDING = 5;

    public UiUtilsConfigScreen(Screen parent) {
        super(Text.of("UI Utils Config"));
        this.parent = parent;
        this.overlay = new ScreenOverlay();
    }

    @Override
    protected void init() {
        super.init();
        double scrollY = (this.scrollPanel != null) ? this.scrollPanel.getScrollY() : 0;
        this.overlay.init(this);
        buildUI(scrollY);
    }

    private void buildUI(double initialScrollY) {
        overlay.clearWidgets();
        overlay.clearTopLevelWidgets();
        activeColorPicker = null;

        scrollPanel =
                new ScrollPanelWidget(
                        "config_scroll_panel",
                        10,
                        20,
                        this.width - 20,
                        this.height - 55,
                        PANEL_PADDING,
                        this.overlay);
        overlay.addWidget(scrollPanel);

        List<SettingRow> rows = new ArrayList<>();

        rows.add(new HeaderRow("General Settings"));

        rows.add(
                new BooleanSettingRow(
                        "Auto Update",
                        "Adds UI-Utils features if available & compatible.",
                        () -> Settings.autoUpdate,
                        val -> {
                            Settings.autoUpdate = val;
                            PersistentSettings.setBoolean("update", val, Settings.file);
                            PersistentSettings.save(Settings.file);
                        }));

        rows.add(
                new BooleanSettingRow(
                        "Delay Chat Packets",
                        "Delays chat packets while \"Delay Packets\" is active.",
                        () -> Settings.delayChatPackets,
                        val -> {
                            Settings.delayChatPackets = val;
                            PersistentSettings.setBoolean("delay_chat_packets", val, Settings.file);
                            PersistentSettings.save(Settings.file);
                        }));

        rows.add(
                new BooleanSettingRow(
                        "Info Overlay",
                        "Shows additional information in the top right.",
                        () -> Settings.infoOverlay,
                        val -> {
                            Settings.infoOverlay = val;
                            PersistentSettings.setBoolean("info_overlay", val, Settings.file);
                            PersistentSettings.save(Settings.file);
                        }));

        rows.add(
                new BooleanSettingRow(
                        "Only In-Game",
                        "Only shows the overlay on in-game screens.",
                        () -> Settings.onlyShowOverlayIngame,
                        val -> {
                            Settings.onlyShowOverlayIngame = val;
                            PersistentSettings.setBoolean("only_ingame", val, Settings.file);
                            PersistentSettings.save(Settings.file);
                        }));

        rows.add(new SpacerRow(15));

        addThemeSettings(rows);

        int totalContentHeight = PANEL_PADDING;
        for (SettingRow row : rows) totalContentHeight += row.height();
        scrollPanel.setContentHeight(totalContentHeight);

        ColorPickerOpener opener =
                (sourceButton, getter, setter) -> {
                    if (activeColorPicker != null) {
                        overlay.removeTopLevelWidget(activeColorPicker);
                        activeColorPicker = null;
                    }
                    int pickerWidth = 150, pickerHeight = 140;
                    int absoluteButtonX =
                            scrollPanel.getX() + scrollPanel.getPadding() + sourceButton.getX();
                    int absoluteButtonY =
                            scrollPanel.getY()
                                    + scrollPanel.getPadding()
                                    + sourceButton.getY()
                                    - (int) scrollPanel.getScrollY();

                    // Ensure picker stays on screen
                    int pickerX = absoluteButtonX - pickerWidth - 5;
                    int pickerY =
                            Math.max(5, Math.min(absoluteButtonY, this.height - pickerHeight - 5));

                    activeColorPicker =
                            new ColorPickerWidget(
                                    "picker",
                                    pickerX,
                                    pickerY,
                                    pickerWidth,
                                    pickerHeight,
                                    getter.get(),
                                    newColor -> {
                                        setter.accept(newColor);
                                        sourceButton.setColor(newColor);
                                    });
                    overlay.addTopLevelWidget(activeColorPicker);
                };

        RowBuilder builder =
                new RowBuilder(
                        PANEL_PADDING,
                        scrollPanel.getContentWidth(),
                        this.scrollPanel,
                        this.overlay,
                        opener);

        for (SettingRow row : rows) builder.addRow(row);

        this.scrollPanel.setScrollY(initialScrollY);

        ButtonWidget resetButton =
                new ButtonWidget(
                        "reset_theme",
                        "Reset Theme",
                        this.width - 85,
                        this.height - 30,
                        75,
                        20,
                        this.overlay);

        resetButton.setOnClick(
                () -> {
                    GuiTheme.resetToDefaults();
                    this.client.setScreen(new UiUtilsConfigScreen(this.parent));
                });
        overlay.addWidget(resetButton);
    }

    private void addThemeSettings(List<SettingRow> rows) {
        rows.add(new HeaderRow("Theme Settings"));

        rows.add(
                new ColorSettingRow(
                        "Widget Background",
                        () -> GuiTheme.WIDGET_BACKGROUND,
                        c -> {
                            GuiTheme.WIDGET_BACKGROUND = c;
                            GuiTheme.save();
                        }));

        rows.add(
                new ColorSettingRow(
                        "Widget Border",
                        () -> GuiTheme.WIDGET_BORDER,
                        c -> {
                            GuiTheme.WIDGET_BORDER = c;
                            GuiTheme.save();
                        }));

        rows.add(
                new ColorSettingRow(
                        "Widget Border Hover",
                        () -> GuiTheme.WIDGET_BORDER_HOVER,
                        c -> {
                            GuiTheme.WIDGET_BORDER_HOVER = c;
                            GuiTheme.save();
                        }));

        rows.add(
                new ColorSettingRow(
                        "Widget Border Focused",
                        () -> GuiTheme.WIDGET_BORDER_FOCUSED,
                        c -> {
                            GuiTheme.WIDGET_BORDER_FOCUSED = c;
                            GuiTheme.save();
                        }));

        rows.add(
                new ColorSettingRow(
                        "Button Background",
                        () -> GuiTheme.BUTTON_BACKGROUND,
                        c -> {
                            GuiTheme.BUTTON_BACKGROUND = c;
                            GuiTheme.save();
                        }));

        rows.add(
                new ColorSettingRow(
                        "Button Background Hover",
                        () -> GuiTheme.BUTTON_BACKGROUND_HOVER,
                        c -> {
                            GuiTheme.BUTTON_BACKGROUND_HOVER = c;
                            GuiTheme.save();
                        }));

        rows.add(
                new ColorSettingRow(
                        "Primary Text",
                        () -> GuiTheme.TEXT_PRIMARY,
                        c -> {
                            GuiTheme.TEXT_PRIMARY = c;
                            GuiTheme.save();
                        }));

        rows.add(
                new ColorSettingRow(
                        "Secondary Text",
                        () -> GuiTheme.TEXT_SECONDARY,
                        c -> {
                            GuiTheme.TEXT_SECONDARY = c;
                            GuiTheme.save();
                        }));

        rows.add(new SpacerRow(10));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        overlay.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        GuiTheme.save();
        overlay.close();
        mc.setScreen(parent);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (activeColorPicker != null && !activeColorPicker.isMouseOver(mouseX, mouseY)) {
            overlay.removeTopLevelWidget(activeColorPicker);
            activeColorPicker = null;
        }
        return overlay.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        return overlay.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return overlay.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
                || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.getKeycode();
        int scanCode = input.scancode();
        int modifiers = input.modifiers();

        if (keyCode == GLFW.GLFW_KEY_ESCAPE && activeColorPicker != null) {
            overlay.removeTopLevelWidget(activeColorPicker);
            activeColorPicker = null;
            return true;
        }
        return overlay.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        return overlay.charTyped((char) input.codepoint(), input.modifiers())
                || super.charTyped(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private class RowBuilder {
        private int currentY;
        private final int width;
        private final ScrollPanelWidget panel;
        private final ScreenOverlay overlay;
        private final ColorPickerOpener opener;

        RowBuilder(
                int startY,
                int width,
                ScrollPanelWidget panel,
                ScreenOverlay overlay,
                ColorPickerOpener opener) {
            this.currentY = startY;
            this.width = width;
            this.panel = panel;
            this.overlay = overlay;
            this.opener = opener;
        }

        public void addRow(SettingRow row) {
            row.init(currentY, width, panel, overlay, textRenderer, opener);
            currentY += row.height();
        }
    }
}
