/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file UpdatedScreen.java
 */
package com.mrbreaknfix.ui_utils.gui.screen;

import com.mrbreaknfix.ui_utils.utils.Bulletin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class UpdatedScreen extends Screen {
    public UpdatedScreen() {
        super(Text.of("Ui-Utils has updated!"));
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(
                ButtonWidget.builder(
                                Text.of("Quit game"),
                                (button) -> {
                                    System.exit(0);
                                })
                        .width(115)
                        .position(5, 5)
                        .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        int centerX = width / 2;
        int y = 30;
        String message = "Ui-Utils has updated!";
        context.drawText(
                textRenderer,
                message,
                centerX - textRenderer.getWidth(message) / 2,
                y,
                0xFFFFFF,
                false);

        String subMessage = "Please restart your game to apply the following changes:";
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

    @Override
    public void close() {}
}
