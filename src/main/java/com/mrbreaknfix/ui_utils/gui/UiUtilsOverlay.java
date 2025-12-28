/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file UiUtilsOverlay.java
 */
package com.mrbreaknfix.ui_utils.gui;

import java.io.File;

import com.mrbreaknfix.ui_utils.ScreenSaver;
import com.mrbreaknfix.ui_utils.event.Subscribe;
import com.mrbreaknfix.ui_utils.event.events.OpenScreenEvent;
import com.mrbreaknfix.ui_utils.event.events.ScreenResizeEvent;
import com.mrbreaknfix.ui_utils.event.events.mouse.ClickEvent;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.ButtonWidget;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.ChatWidget;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.FabricatePacketWidget;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.SettingsWidget;
import com.mrbreaknfix.ui_utils.packet.Mcfw;
import com.mrbreaknfix.ui_utils.persistance.PersistentSettings;
import com.mrbreaknfix.ui_utils.persistance.Settings;
import com.mrbreaknfix.ui_utils.utils.Color;
import com.mrbreaknfix.ui_utils.utils.UIActions;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import static com.mrbreaknfix.ui_utils.UiUtils.*;

public class UiUtilsOverlay extends BaseOverlay {

    public UiUtilsOverlay() {
        super(new File("ui_utils_overlay.json"));
        load();
    }

    ButtonWidget sendPacketsButton;
    ButtonWidget delayPacketsButton;
    private boolean sendPackets = true;
    private boolean delayPackets = false;

    public void createWidgets() {
        int fabricatePacketX = 0, fabricatePacketY = 0;
        int saveGuiX = 0, saveGuiY = 140;
        int getPluginsX = 0, getPluginsY = 160;
        int dragTutorialX = 0, dragTutorialY = 180;
        int delayPacketsX = 0, delayPacketsY = 20;
        int chatBoxX = 0, chatBoxY = 120;
        int loadGuiX = 58, loadGuiY = 140;
        int sendPacketsX = 0, sendPacketsY = 100;
        int desyncX = 0, desyncY = 80;
        int cwopX = 0, cwopY = 60;
        int leaveNSendX = 0, leaveNSendY = 40;

        ButtonWidget toggleButton =
                new ButtonWidget("cwop", "Close without Packet", cwopX, cwopY, 115, 20, this);
        toggleButton.setOnClick(() -> mc.execute(UIActions::CWoP));

        delayPacketsButton =
                new ButtonWidget(
                        "delay_packets",
                        "Delay Packets: false",
                        delayPacketsX,
                        delayPacketsY,
                        115,
                        20,
                        this);
        delayPacketsButton.setOnClick(
                () -> {
                    delayPackets = !delayPackets;
                    delayPacketsButton.setText("Delay Packets: " + delayPackets);
                    mc.execute(() -> UIActions.setDelayPackets(delayPackets));
                });

        ButtonWidget disconnectAndSendPackets =
                new ButtonWidget(
                        "leavensendpackets",
                        "Leave & Send Packets",
                        leaveNSendX,
                        leaveNSendY,
                        115,
                        20,
                        this);
        disconnectAndSendPackets.setOnClick(() -> mc.execute(UIActions::disconnectAndSendPackets));

        sendPacketsButton =
                new ButtonWidget(
                        "send_packets",
                        "Send Packets: true",
                        sendPacketsX,
                        sendPacketsY,
                        115,
                        20,
                        this);
        sendPacketsButton.setOnClick(
                () -> {
                    sendPackets = !sendPackets;
                    sendPacketsButton.setText("Send Packets: " + sendPackets);
                    mc.execute(() -> UIActions.setSendPackets(sendPackets));
                });

        FabricatePacketWidget fabricatePacketWidget =
                new FabricatePacketWidget(fabricatePacketX, fabricatePacketY, 115, 159, this);

        ButtonWidget desyncButton =
                new ButtonWidget("desync", "Desync", desyncX, desyncY, 115, 20, this);
        desyncButton.setOnClick(() -> mc.execute(UIActions::desync));

        ButtonWidget saveGuiButton =
                new ButtonWidget("save_gui", "Save GUI", saveGuiX, saveGuiY, 57, 20, this);
        saveGuiButton.setOnClick(() -> ScreenSaver.saveScreen("default"));

        ButtonWidget loadGuiButton =
                new ButtonWidget("load_gui", "Load GUI", loadGuiX, loadGuiY, 57, 20, this);
        loadGuiButton.setOnClick(
                () -> {
                    try {
                        ScreenSaver.loadScreen("default");
                    } catch (Exception ignored) {
                    }
                });

        ChatWidget chatBox = new ChatWidget("chat_box", chatBoxX, chatBoxY, 115, 20, this);

        ButtonWidget getPluginsButton =
                new ButtonWidget(
                        "get_plugins", "Get Plugins", getPluginsX, getPluginsY, 115, 20, this);
        getPluginsButton.setOnClick(() -> mc.execute(pluginScanner::getPlugins));

        ButtonWidget dragTutorial =
                new ButtonWidget(
                        "drag_tutorial", "Click Me!", dragTutorialX, dragTutorialY, 115, 20, this);

        String[] messages = {
            "Right click to drag!",
            "Or right click, then",
            "Use arrow keys!",
            "Try it on me!",
            "If the UI is too large",
            "Lower your GUI Scale",
            "Click to remove me!",
        };

        final int[] index = {0};

        dragTutorial.setOnClick(
                () -> {
                    if (index[0] < messages.length) {
                        dragTutorial.setText(messages[index[0]]);
                        index[0]++;
                    } else {
                        Settings.completedDragTutorial = true;
                        PersistentSettings.setBoolean(
                                "completed_drag_tutorial", true, Settings.file);
                        PersistentSettings.save(Settings.file);
                        this.removeWidget(dragTutorial);
                    }
                });

        if (!Settings.completedDragTutorial) this.addWidget(dragTutorial);
        System.out.println(
                "Completed drag tutorial: "
                        + Settings.completedDragTutorial
                        + " adding widget: "
                        + !Settings.completedDragTutorial);
        this.addWidget(toggleButton);
        this.addWidget(delayPacketsButton);
        this.addWidget(disconnectAndSendPackets);
        this.addWidget(sendPacketsButton);
        this.addWidget(fabricatePacketWidget);
        this.addWidget(desyncButton);
        this.addWidget(saveGuiButton);
        this.addWidget(loadGuiButton);
        this.addWidget(chatBox);
        this.addWidget(getPluginsButton);

        SettingsWidget sw = new SettingsWidget("sw", 24, 24, this);
        sw.setMovable(false);
        this.addWidget(sw);

        this.loadWidgetPositions();
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        super.render(context, screen, mouseX, mouseY, deltaTicks);

        if (!Settings.infoOverlay) return;
        // find the top right corner of the screen
        int x = mc.getWindow().getScaledWidth() - 5;
        int y = 3;
        int bgColor = 0x7f000000;
        int textColor = 0xb2e200ff;

        // todo: add setting to disable the overlay on certain screens, ex if it matches
        // "meteordevelopment"
        Text text = Text.of("Screen: " + screen.getClass().getName());
        int textWidth = mc.textRenderer.getWidth(text);
        int FH = mc.textRenderer.fontHeight;
        context.fill(x - textWidth - 5, y - 4, x + 5, y + FH + 2, bgColor);
        context.drawText(mc.textRenderer, text, x - textWidth, y, textColor, false);

        // Mcfw status
        String mcfwStatus = Mcfw.enabled ? "On" : "Off";
        int mcfwColor =
                Mcfw.enabled
                        ? Color.LIME.brighter().brighter().getHex()
                        : textColor; // Green for enabled, purple for disabled
        Text mcfwText =
                Text.literal("MCFW: ")
                        .append(
                                Text.literal(mcfwStatus)
                                        .styled(style -> style.withColor(mcfwColor)));

        int mcfwWidth = mc.textRenderer.getWidth(mcfwText);
        context.fill(x - mcfwWidth - 5, y + FH + 2, x + 5, y + FH * 2 + 6, bgColor);
        context.drawText(mc.textRenderer, mcfwText, x - mcfwWidth, y + FH + 4, textColor, false);
        y += FH + 4;

        ServerInfo server = mc.getCurrentServerEntry();
        Text versionText =
                Text.of("Version: " + (server != null ? server.version.getString() : "null"));
        int versionWidth = mc.textRenderer.getWidth(versionText);
        context.fill(x - versionWidth - 5, y + FH * 2 + 6, x + 5, y + FH * 3 + 10, bgColor);
        context.drawText(
                mc.textRenderer, versionText, x - versionWidth, y + FH * 2 + 8, textColor, false);

        y += 2;
        // Server info
        String b = mc.getNetworkHandler() != null ? mc.getNetworkHandler().getBrand() : "null";
        Text brand = Text.of("Server: " + b);
        int brandWidth = mc.textRenderer.getWidth(brand);
        context.fill(x - brandWidth - 5, y + FH, x + 5, y + FH * 2 + 4, bgColor);
        context.drawText(mc.textRenderer, brand, x - brandWidth, y + FH + 2, textColor, false);
    }

    @Subscribe
    public void onClick(ClickEvent event) {
        if (mc.currentScreen == null) return;

        double mx = event.getScaledX();
        double my = event.getScaledY();

        if (this.onMouseClick(mx, my, event.getAction(), event.getButton())) {
            event.cancel();
        }
    }

    @Subscribe
    public void onScreen(OpenScreenEvent event) {
        if (event.getScreen() == null) this.resetCursor();
    }

    public void reload() {
        this.unload();
        this.load();
        LOGGER.info("Reloaded overlay");
        eventManager.trigger(
                new ScreenResizeEvent(
                        mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight()));
    }

    public void unload() {
        this.clearWidgets();
        eventManager.removeListener(this);
        LOGGER.info("Unloaded overlay");
    }

    public void load() {
        LOGGER.info("Initializing overlay");

        this.createWidgets();
        eventManager.addListener(this);

        LOGGER.info("Loaded overlay");
    }
}
