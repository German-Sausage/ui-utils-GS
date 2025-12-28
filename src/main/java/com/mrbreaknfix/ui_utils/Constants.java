/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file Constants.java
 */
package com.mrbreaknfix.ui_utils;

import com.mrbreaknfix.ui_utils.utils.VersionUtils;

import static com.mrbreaknfix.ui_utils.UiUtils.MOD_ID;

public class Constants {
    public static final String UIUTILS_VERSION = VersionUtils.getModVersion(MOD_ID);
    public static final String MINECRAFT_VERSION = VersionUtils.getModVersion("minecraft");
    public static final String FABRIC_LOADER_VERSION = VersionUtils.getModVersion("fabricloader");
    public static final String FABRIC_API_VERSION = VersionUtils.getModVersion("fabric-api");
    public static final String API_VERSION = "1.0.0"; // This should be updated with each API change
}
