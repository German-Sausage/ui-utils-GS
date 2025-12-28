/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file SettingsWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets;

import com.mrbreaknfix.ui_utils.event.Subscribe;
import com.mrbreaknfix.ui_utils.event.events.ScreenResizeEvent;
import com.mrbreaknfix.ui_utils.gui.BaseOverlay;
import com.mrbreaknfix.ui_utils.gui.screen.UiUtilsConfigScreen;
import com.mrbreaknfix.ui_utils.gui.widget.Widget;
import com.mrbreaknfix.ui_utils.utils.Browse;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import static com.mrbreaknfix.ui_utils.UiUtils.*;

public class SettingsWidget extends Widget {
    private final IconButtonWidget configButton;
    private final IconButtonWidget cliButton;
    private final IconButtonWidget scriptMcButton;

    public SettingsWidget(String id, int width, int height, BaseOverlay overlay) {
        super(id, 0, 0, width, height, overlay);

        configButton = new IconButtonWidget("cfgi", "communism.png", 0, 0, 24, 24, overlay);
        configButton.setOnClick(
                () ->
                        mc.execute(
                                () -> {
                                    mc.setScreen(new UiUtilsConfigScreen(mc.currentScreen));
                                    System.out.println("Initializing UI Utils Config Screen");
                                }));
        this.addChild(configButton);

        cliButton = new IconButtonWidget("cli", "cli.png", 0, 0, 24, 24, overlay);
        cliButton.setOnClick(
                () ->
                        Browse.openLink(
                                "https://mrbreaknfix.com/terminal?api=http://localhost:"
                                        + COMMAND_SERVER_PORT
                                        + "&wscs=ws://localhost:"
                                        + WEBSOCKET_COMMAND_SERVER_PORT));
        this.addChild(cliButton);

        scriptMcButton = new IconButtonWidget("scriptmc", "scriptmc.png", 0, 0, 24, 24, overlay);
        scriptMcButton.setOnClick(
                () ->
                        Browse.openLink(
                                "https://mrbreaknfix.com/scriptmc?api=http://localhost:"
                                        + COMMAND_SERVER_PORT
                                        + "&wscs=ws://localhost:"
                                        + WEBSOCKET_COMMAND_SERVER_PORT));
        this.addChild(scriptMcButton);

        eventManager.addListener(this);

        if (mc.getWindow() != null) {
            repositionWidgets(mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
        }
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {}

    @Subscribe
    public void onResize(ScreenResizeEvent event) {
        repositionWidgets(event.getWidth(), event.getHeight());
    }

    private void repositionWidgets(int screenWidth, int screenHeight) {
        int buttonSize = 24;
        int gap = 4;
        int padding = 4;

        int configX = screenWidth - buttonSize - padding;
        int configY = screenHeight - buttonSize - padding;

        this.moveTo(configX, configY);

        configButton.moveTo(configX, configY);
        cliButton.moveTo(configX - (buttonSize + gap), configY);
        scriptMcButton.moveTo(configX - (buttonSize + gap) * 2, configY);
    }
}
