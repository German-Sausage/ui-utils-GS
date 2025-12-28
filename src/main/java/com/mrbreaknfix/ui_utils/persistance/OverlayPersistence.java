/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file OverlayPersistence.java
 */
package com.mrbreaknfix.ui_utils.persistance;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.gui.widget.Widget;

public class OverlayPersistence {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<File, Map<String, Pos>> allPositions = new ConcurrentHashMap<>();

    public static class Pos {
        public int x, y;

        public Pos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static synchronized void savePosition(String widgetId, int x, int y, File saveFile) {
        Map<String, Pos> positions =
                allPositions.computeIfAbsent(saveFile, f -> new ConcurrentHashMap<>());
        positions.put(widgetId, new Pos(x, y));
        saveToFile(saveFile, positions);
    }

    public static Pos getPosition(String widgetId, int defaultX, int defaultY, File saveFile) {
        Map<String, Pos> positions = allPositions.getOrDefault(saveFile, new ConcurrentHashMap<>());
        return positions.getOrDefault(widgetId, new Pos(defaultX, defaultY));
    }

    private static void saveToFile(File saveFile, Map<String, Pos> positions) {
        File tempFile = new File(saveFile.getAbsolutePath() + ".tmp");
        try {
            try (FileWriter writer = new FileWriter(tempFile)) {
                GSON.toJson(positions, writer);
                writer.flush();
            }

            Files.move(tempFile.toPath(), saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            UiUtils.LOGGER.error("Could not save overlay positions to " + saveFile, e);
        }
    }

    public static synchronized void loadFromFile(File saveFile) {
        if (!saveFile.exists()) {
            UiUtils.LOGGER.info("Overlay positions file does not exist: " + saveFile);
            allPositions.put(saveFile, new ConcurrentHashMap<>());
            return;
        }
        try (FileReader reader = new FileReader(saveFile)) {
            Type type = new TypeToken<Map<String, Pos>>() {}.getType();
            Map<String, Pos> loadedPositions = GSON.fromJson(reader, type);
            if (loadedPositions != null) {
                allPositions.put(saveFile, new ConcurrentHashMap<>(loadedPositions));
            } else {
                allPositions.put(saveFile, new ConcurrentHashMap<>());
            }
        } catch (JsonSyntaxException e) {
            UiUtils.LOGGER.error("Error parsing overlay positions JSON from " + saveFile, e);
        } catch (IOException e) {
            UiUtils.LOGGER.error("Could not load overlay positions from " + saveFile, e);
        }
    }

    public static void restoreWidgetPosition(
            String id, Widget widget, int defaultX, int defaultY, File saveFile) {
        if (!widget.isMovable()) return;
        Pos pos = getPosition(id, defaultX, defaultY, saveFile);
        widget.moveTo(pos.x, pos.y);
    }
}
