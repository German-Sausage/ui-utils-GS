/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file UserAgent.java
 */
package com.mrbreaknfix.ui_utils.utils;

import net.fabricmc.loader.api.FabricLoader;

import static com.mrbreaknfix.ui_utils.Constants.MINECRAFT_VERSION;
import static com.mrbreaknfix.ui_utils.Constants.UIUTILS_VERSION;
import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class UserAgent {
    public static String getUiUtilsUseragent() {
        String fabricVersion =
                FabricLoader.getInstance()
                        .getModContainer("fabricloader")
                        .map(
                                modContainer ->
                                        modContainer.getMetadata().getVersion().getFriendlyString())
                        .orElse("unknown");

        String uuid =
                FabricLoader.getInstance().isDevelopmentEnvironment()
                        ? "Dev"
                        : mc.getSession().getUuidOrNull().toString();

        return "UiUtils/%s (Minecraft/%s; Fabric/%s; UUID=%s)"
                .formatted(UIUTILS_VERSION, MINECRAFT_VERSION, fabricVersion, uuid);
    }
}
