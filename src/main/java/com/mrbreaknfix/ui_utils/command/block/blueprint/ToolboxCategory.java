/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ToolboxCategory.java
 */
package com.mrbreaknfix.ui_utils.command.block.blueprint;

public enum ToolboxCategory {
    EVENTS("Events", "event_category", "#7ed321"),
    PLAYER("Player", "player_category", "#4a90e2"),
    SCREEN("Screen", "screen_category", "#50e3c2"),
    NETWORK("Network", "network_category", "#f5a623"),
    UTILITY("Utility", "utility_category", "#bd10e0"),
    COMMANDS("Commands", "command_category", "#9b9b9b");

    private final String displayName;
    private final String styleName;
    private final String color;

    ToolboxCategory(String displayName, String styleName, String color) {
        this.displayName = displayName;
        this.styleName = styleName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStyleName() {
        return styleName;
    }

    public String getColor() {
        return color;
    }
}
