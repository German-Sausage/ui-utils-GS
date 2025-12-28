/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file GuiTheme.java
 */
package com.mrbreaknfix.ui_utils.gui;

import com.mrbreaknfix.ui_utils.persistance.PersistentSettings;
import com.mrbreaknfix.ui_utils.persistance.Settings;
import com.mrbreaknfix.ui_utils.utils.Color;

public class GuiTheme {
    // Default values are kept for resetting
    private static final int DEFAULT_WIDGET_BACKGROUND = new Color(0xFF090038).getHex();
    private static final int DEFAULT_WIDGET_BORDER = new Color(0xFF303030).getHex();
    private static final int DEFAULT_WIDGET_BORDER_HOVER = new Color(0xFF505050).getHex();
    private static final int DEFAULT_WIDGET_BORDER_FOCUSED = new Color(0xFFBD5AC4).getHex();
    private static final int DEFAULT_BUTTON_BACKGROUND = new Color(0xFF690070).getHex();
    private static final int DEFAULT_BUTTON_BACKGROUND_HOVER =
            new Color(0xFF690070).brighter().getHex();
    private static final int DEFAULT_TEXT_PRIMARY = Color.WHITE.getHex();
    private static final int DEFAULT_TEXT_SECONDARY = Color.GRAY.getHex();
    private static final int DEFAULT_TEXT_PLACEHOLDER = new Color(0xFF808080).getHex();

    // --- General Widget Colors ---
    public static int WIDGET_BACKGROUND = DEFAULT_WIDGET_BACKGROUND;
    public static int WIDGET_BORDER = DEFAULT_WIDGET_BORDER;
    public static int WIDGET_BORDER_HOVER = DEFAULT_WIDGET_BORDER_HOVER;
    public static int WIDGET_BORDER_FOCUSED = DEFAULT_WIDGET_BORDER_FOCUSED;

    // --- Button Colors ---
    public static int BUTTON_BACKGROUND = DEFAULT_BUTTON_BACKGROUND;
    public static int BUTTON_BACKGROUND_HOVER = DEFAULT_BUTTON_BACKGROUND_HOVER;

    // --- Text Colors ---
    public static int TEXT_PRIMARY = DEFAULT_TEXT_PRIMARY;
    public static int TEXT_SECONDARY = DEFAULT_TEXT_SECONDARY;
    public static int TEXT_PLACEHOLDER = DEFAULT_TEXT_PLACEHOLDER;

    // --- Text Input ---
    public static final int TEXT_INPUT_SELECTION = 0x7F0066FF;

    // --- Sizing & Padding ---
    public static final int PADDING = 4;
    public static final int BORDER_WIDTH = 1;

    public static void save() {
        // Use setInt for saving color values
        PersistentSettings.setInt("theme_widget_background", WIDGET_BACKGROUND, Settings.file);
        PersistentSettings.setInt("theme_widget_border", WIDGET_BORDER, Settings.file);
        PersistentSettings.setInt("theme_widget_border_hover", WIDGET_BORDER_HOVER, Settings.file);
        PersistentSettings.setInt(
                "theme_widget_border_focused", WIDGET_BORDER_FOCUSED, Settings.file);
        PersistentSettings.setInt("theme_button_background", BUTTON_BACKGROUND, Settings.file);
        PersistentSettings.setInt(
                "theme_button_background_hover", BUTTON_BACKGROUND_HOVER, Settings.file);
        PersistentSettings.setInt("theme_text_primary", TEXT_PRIMARY, Settings.file);
        PersistentSettings.setInt("theme_text_secondary", TEXT_SECONDARY, Settings.file);
        PersistentSettings.setInt("theme_text_placeholder", TEXT_PLACEHOLDER, Settings.file);
    }

    public static void load() {
        // Use getInt for loading color values
        WIDGET_BACKGROUND =
                PersistentSettings.getInt(
                        "theme_widget_background", DEFAULT_WIDGET_BACKGROUND, Settings.file);
        WIDGET_BORDER =
                PersistentSettings.getInt(
                        "theme_widget_border", DEFAULT_WIDGET_BORDER, Settings.file);
        WIDGET_BORDER_HOVER =
                PersistentSettings.getInt(
                        "theme_widget_border_hover", DEFAULT_WIDGET_BORDER_HOVER, Settings.file);
        WIDGET_BORDER_FOCUSED =
                PersistentSettings.getInt(
                        "theme_widget_border_focused",
                        DEFAULT_WIDGET_BORDER_FOCUSED,
                        Settings.file);
        BUTTON_BACKGROUND =
                PersistentSettings.getInt(
                        "theme_button_background", DEFAULT_BUTTON_BACKGROUND, Settings.file);
        BUTTON_BACKGROUND_HOVER =
                PersistentSettings.getInt(
                        "theme_button_background_hover",
                        DEFAULT_BUTTON_BACKGROUND_HOVER,
                        Settings.file);
        TEXT_PRIMARY =
                PersistentSettings.getInt(
                        "theme_text_primary", DEFAULT_TEXT_PRIMARY, Settings.file);
        TEXT_SECONDARY =
                PersistentSettings.getInt(
                        "theme_text_secondary", DEFAULT_TEXT_SECONDARY, Settings.file);
        TEXT_PLACEHOLDER =
                PersistentSettings.getInt(
                        "theme_text_placeholder", DEFAULT_TEXT_PLACEHOLDER, Settings.file);
    }

    public static void resetToDefaults() {
        WIDGET_BACKGROUND = DEFAULT_WIDGET_BACKGROUND;
        WIDGET_BORDER = DEFAULT_WIDGET_BORDER;
        WIDGET_BORDER_HOVER = DEFAULT_WIDGET_BORDER_HOVER;
        WIDGET_BORDER_FOCUSED = DEFAULT_WIDGET_BORDER_FOCUSED;
        BUTTON_BACKGROUND = DEFAULT_BUTTON_BACKGROUND;
        BUTTON_BACKGROUND_HOVER = DEFAULT_BUTTON_BACKGROUND_HOVER;
        TEXT_PRIMARY = DEFAULT_TEXT_PRIMARY;
        TEXT_SECONDARY = DEFAULT_TEXT_SECONDARY;
        TEXT_PLACEHOLDER = DEFAULT_TEXT_PLACEHOLDER;
        save();
    }
}
