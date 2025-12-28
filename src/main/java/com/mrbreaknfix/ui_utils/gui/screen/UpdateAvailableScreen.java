/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file UpdateAvailableScreen.java
 */
package com.mrbreaknfix.ui_utils.gui.screen;

import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.persistance.PersistentSettings;
import com.mrbreaknfix.ui_utils.persistance.Settings;
import com.mrbreaknfix.ui_utils.update.Updater;
import com.mrbreaknfix.ui_utils.utils.Bulletin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class UpdateAvailableScreen extends Screen {
    public UpdateAvailableScreen() {
        super(Text.of("A Ui-Utils update is available!"));
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 100;
        int buttonSpacing = 10;
        int centerX = width / 2;
        int yStart = 30 + (Bulletin.changelog.size() * 10) + 30; // y-position below the changelog

        this.addDrawableChild(
                ButtonWidget.builder(
                                Text.of("Yes"),
                                (button) -> {
                                    Settings.autoUpdate = true;
                                    PersistentSettings.setBoolean("update", true, Settings.file);
                                    PersistentSettings.save(Settings.file);
                                    UiUtils.LOGGER.info("Starting auto update...");
                                    Updater.handleUpdate(
                                            Bulletin.nextUpdateUrl, Bulletin.nextUpdateSHA256);
                                    this.close();
                                })
                        .width(buttonWidth)
                        .position(centerX - buttonWidth - buttonSpacing, yStart)
                        .build());

        this.addDrawableChild(
                ButtonWidget.builder(
                                Text.of("No"),
                                (button) -> {
                                    System.out.println(
                                            "User selected 'No'. Dismissing the update.");
                                    this.close();
                                })
                        .width(buttonWidth)
                        .position(centerX + buttonSpacing, yStart)
                        .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        int centerX = width / 2;
        int y = 30;
        String message = "A Ui-Utils update is available!";
        context.drawText(
                textRenderer,
                message,
                centerX - textRenderer.getWidth(message) / 2,
                y,
                0xFFFFFF,
                false);

        String subMessage = "Would you like to auto update to apply these changes?";
        context.drawText(
                textRenderer,
                subMessage,
                centerX - textRenderer.getWidth(subMessage) / 2,
                y + 10,
                0xFFFFFF,
                false);

        for (int i = 0; i < Bulletin.changelog.size(); i++) {
            String change = "- " + Bulletin.changelog.get(i);
            context.drawText(
                    textRenderer,
                    Text.literal(change).formatted(Formatting.ITALIC, Formatting.GRAY),
                    centerX - textRenderer.getWidth(change) / 2,
                    y + 30 + (i * 10),
                    0xFFFFFF,
                    false);
        }
    }
}
