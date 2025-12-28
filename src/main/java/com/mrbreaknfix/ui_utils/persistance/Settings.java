/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file Settings.java
 */
package com.mrbreaknfix.ui_utils.persistance;

import java.io.File;

import com.mrbreaknfix.ui_utils.gui.GuiTheme;

public class Settings {
    public static File file = new File("ui_utils_settings.json");
    public static boolean autoUpdate,
            delayChatPackets,
            infoOverlay,
            completedDragTutorial,
            onlyShowOverlayIngame,
            aprilFoolsDisabled;

    public static void loadSettings() {
        autoUpdate = PersistentSettings.getBoolean("update", true, file);
        aprilFoolsDisabled = PersistentSettings.getBoolean("april_fools_disabled", false, file);
        onlyShowOverlayIngame = PersistentSettings.getBoolean("only_ingame", true, file);
        delayChatPackets = PersistentSettings.getBoolean("delay_chat_packets", true, file);
        infoOverlay = PersistentSettings.getBoolean("info_overlay", true, file);
        completedDragTutorial =
                PersistentSettings.getBoolean("completed_drag_tutorial", false, file);

        GuiTheme.load();

        PersistentSettings.save(file);
    }
}
