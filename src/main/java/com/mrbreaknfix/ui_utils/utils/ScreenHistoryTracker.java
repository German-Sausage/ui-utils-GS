/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ScreenHistoryTracker.java
 */
package com.mrbreaknfix.ui_utils.utils;

import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.event.Subscribe;
import com.mrbreaknfix.ui_utils.event.events.OpenScreenEvent;

import net.minecraft.client.gui.screen.Screen;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;
import static com.mrbreaknfix.ui_utils.UiUtils.webSocketCommandServer;

// todo: rename to ScreenTracker, fix null logging, test.x
public class ScreenHistoryTracker {

    private static Screen lastProcessedScreen = null;
    private static boolean hasProcessedFirstEvent = false;

    @Subscribe
    public void onScreenOpen(OpenScreenEvent event) {
        Screen currentScreen = event.getScreen();

        if (hasProcessedFirstEvent && currentScreen == lastProcessedScreen) {
            return;
        }

        lastProcessedScreen = currentScreen;
        hasProcessedFirstEvent = true;

        JsonObject payload = new JsonObject();

        if (currentScreen == null) {
            payload.addProperty("className", "null");
            payload.addProperty("title", "ingame");
            payload.addProperty(
                    "syncId",
                    mc.player == null
                            ? "null"
                            : String.valueOf(mc.player.currentScreenHandler.syncId));
            payload.addProperty(
                    "revision",
                    mc.player == null
                            ? "null"
                            : String.valueOf(mc.player.currentScreenHandler.getRevision()));
        } else {
            payload.addProperty("className", currentScreen.getClass().getSimpleName());
            payload.addProperty("title", currentScreen.getTitle().getString());

            payload.addProperty(
                    "syncId",
                    mc.player == null
                            ? "null"
                            : String.valueOf(mc.player.currentScreenHandler.syncId));
            payload.addProperty(
                    "revision",
                    mc.player == null
                            ? "null"
                            : String.valueOf(mc.player.currentScreenHandler.getRevision()));
        }

        if (webSocketCommandServer != null) {
            String payloadString = payload.toString();
            //            UiUtils.LOGGER.info("Broadcasting screen event: " + payloadString);
            webSocketCommandServer.broadcastToSubscribedClients(payloadString, "screen");
        }

        ScreenHistory.push(currentScreen);
    }
}
