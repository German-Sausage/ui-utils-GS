/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ScreenSaver.java
 */
package com.mrbreaknfix.ui_utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class ScreenSaver {

    public static Map<String, SavedScreen> savedScreens = new ConcurrentHashMap<>();

    static {
        savedScreens.put("default", new SavedScreen(null, null));
    }

    public static void saveScreen(String name) {
        if (mc.currentScreen == null) {
            throw new IllegalStateException("Cannot save when no screen is open.");
        }

        Screen currentScreen = mc.currentScreen;
        ScreenHandler currentScreenHandler =
                (currentScreen instanceof HandledScreen)
                        ? ((HandledScreen<?>) currentScreen).getScreenHandler()
                        : null;

        savedScreens.put(name, new SavedScreen(currentScreen, currentScreenHandler));
    }

    public static void loadScreen(String name) {
        SavedScreen saved = savedScreens.get(name);
        if (saved == null) {
            throw new IllegalArgumentException("No screen saved in slot: \"" + name + "\"");
        }

        mc.execute(
                () -> {
                    mc.setScreen(saved.screen());

                    if (mc.player != null && saved.screenHandler() != null) {
                        mc.player.currentScreenHandler = saved.screenHandler();
                    }
                });
    }

    public static String getInfo(String name) {
        SavedScreen saved = savedScreens.get(name);
        if (saved == null) {
            return null;
        }
        String className = saved.screen().getClass().getName();
        String title = saved.screen().getTitle().getString();
        return String.format("Class: %s, Title: %s", className, title);
    }

    public static boolean removeScreen(String slotName) {
        return savedScreens.remove(slotName) != null;
    }
}
