/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file VersionUtils.java
 */
package com.mrbreaknfix.ui_utils.utils;

import com.mrbreaknfix.ui_utils.UiUtils;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;

public class VersionUtils {
    public static String getModVersion(String modId) {
        if (FabricLoader.getInstance().getModContainer(modId).isEmpty()) {
            UiUtils.LOGGER.error("Mod with id {} not found", modId);
        }

        ModMetadata modMetadata =
                FabricLoader.getInstance().getModContainer(modId).isPresent()
                        ? FabricLoader.getInstance().getModContainer(modId).get().getMetadata()
                        : null;

        return modMetadata != null ? modMetadata.getVersion().getFriendlyString() : "null";
    }

    public static String getModPath(String modId) {
        if (FabricLoader.getInstance().getModContainer(modId).isEmpty()) {
            UiUtils.LOGGER.error("Mod with id {} not found", modId);
        }

        return FabricLoader.getInstance().getModContainer(modId).isPresent()
                ? FabricLoader.getInstance()
                        .getModContainer(modId)
                        .get()
                        .getOrigin()
                        .getPaths()
                        .getFirst()
                        .toString()
                : "null";
    }
}
