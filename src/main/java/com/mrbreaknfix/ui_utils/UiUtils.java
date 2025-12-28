/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file UiUtils.java
 */
package com.mrbreaknfix.ui_utils;

import com.mrbreaknfix.ui_utils.command.CommandSystem;
import com.mrbreaknfix.ui_utils.command.WebSocketCommandServer;
import com.mrbreaknfix.ui_utils.event.EventManager;
import com.mrbreaknfix.ui_utils.gui.UiUtilsOverlay;
import com.mrbreaknfix.ui_utils.pIIC.PIICClient;
import com.mrbreaknfix.ui_utils.utils.*;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UiUtils implements ClientModInitializer {
    public static final String MOD_ID = "ui_utils";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static PIICClient piicClient;
    public static final EventManager eventManager = new EventManager();

    public static MinecraftClient mc;
    public static WebSocketCommandServer webSocketCommandServer;
    public static UiUtilsOverlay overlay;
    public static KeyBinding restoreScreenKey;

    public static int WEBSOCKET_COMMAND_SERVER_PORT = 33534;
    public static boolean isDevModeEnabled = FabricLoader.getInstance().isDevelopmentEnvironment();
    public static String version;
    public static int COMMAND_SERVER_PORT;
    public static final SlotManager slotManager = new SlotManager();
    public static final PluginScanner pluginScanner = new PluginScanner();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing UI-Utils (Client)");

        mc = MinecraftClient.getInstance();

        version = VersionUtils.getModVersion(MOD_ID);
        isDevModeEnabled = version.contains("DEV");
        CommandSystem.init();

        eventManager.addListener(ScreenHistoryTracker.class);

        initializeOverlay();

        restoreScreenKey =
                KeyBindingHelper.registerKeyBinding(
                        new KeyBinding(
                                "Restore Screen ID: \"default\"",
                                InputUtil.Type.KEYSYM,
                                GLFW.GLFW_KEY_V,
                                new KeyBinding.Category(Identifier.of("ui_utils"))));

        ClientTickEvents.END_CLIENT_TICK.register(
                (client) -> {
                    while (restoreScreenKey.wasPressed()) {
                        try {
                            if (client.world != null) {
                                ScreenSaver.loadScreen("default");
                            }
                        } catch (Exception e) {
                            LOGGER.warn("Failed to load screen 'default'", e);
                        }
                    }
                });

        eventManager.addListener(this);

        String fabricApiVersion =
                FabricLoader.getInstance()
                        .getModContainer("fabric-api")
                        .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                        .orElse("unknown");
        String fabricLoaderVersion =
                FabricLoader.getInstance()
                        .getModContainer("fabricloader")
                        .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                        .orElse("unknown");

        DateActions.initialize();

        LOGGER.info(
                "UI-Utils initialized, fabric loader version: {}, mod version: {}, fabric api version: {}",
                fabricLoaderVersion,
                version,
                fabricApiVersion);
    }

    private synchronized void initializeOverlay() {
        if (overlay != null) {
            return;
        }
        LOGGER.info("Initializing UI-Utils Overlay.");
        overlay = new UiUtilsOverlay();
    }
}
