/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file PreInit.java
 */
package com.mrbreaknfix.ui_utils;

import com.mrbreaknfix.ui_utils.persistance.Settings;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class PreInit implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        Settings.loadSettings();
    }
}
