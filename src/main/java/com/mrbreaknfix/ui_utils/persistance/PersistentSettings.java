/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file PersistentSettings.java
 */
package com.mrbreaknfix.ui_utils.persistance;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.UiUtils;

public class PersistentSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, JsonObject> fileCache = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> dirtyFiles = new ConcurrentHashMap<>();

    private static synchronized JsonObject getCache(File file) {
        String path = file.getAbsolutePath();
        if (!fileCache.containsKey(path)) {
            loadFromFile(file);
        }
        return fileCache.get(path);
    }

    public static synchronized void save(File file) {
        String path = file.getAbsolutePath();
        if (dirtyFiles.getOrDefault(path, false)) {
            saveToFile(file, getCache(file));
            dirtyFiles.put(path, false);
        }
    }

    private static void saveToFile(File file, JsonObject data) {
        File tempFile = new File(file.getAbsolutePath() + ".tmp");
        try {
            try (FileWriter writer = new FileWriter(tempFile)) {
                GSON.toJson(data, writer);
                writer.flush();
            }

            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            UiUtils.LOGGER.error("Failed to save persistent settings to " + file.getName(), e);
        }
    }

    private static synchronized void loadFromFile(File file) {
        String path = file.getAbsolutePath();
        if (!file.exists()) {
            UiUtils.LOGGER.info(
                    "Settings file not found. A new one will be created on save: "
                            + file.getName());
            fileCache.put(path, new JsonObject());
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            fileCache.put(path, (json != null) ? json : new JsonObject());
        } catch (Exception e) {
            UiUtils.LOGGER.error(
                    "Failed to load persistent settings from {}, creating fresh settings.",
                    file.getName(),
                    e);
            fileCache.put(path, new JsonObject());
        }
    }

    public static synchronized void setBoolean(String key, boolean value, File file) {
        getCache(file).addProperty(key, value);
        dirtyFiles.put(file.getAbsolutePath(), true);
    }

    public static synchronized void setString(String key, String value, File file) {
        getCache(file).addProperty(key, value);
        dirtyFiles.put(file.getAbsolutePath(), true);
    }

    public static synchronized void setNumber(String key, Number value, File file) {
        getCache(file).addProperty(key, value);
        dirtyFiles.put(file.getAbsolutePath(), true);
    }

    public static void setInt(String key, int value, File file) {
        setNumber(key, value, file);
    }

    public static boolean getBoolean(String key, boolean defaultValue, File file) {
        JsonObject cache = getCache(file);
        if (cache.has(key)
                && cache.get(key).isJsonPrimitive()
                && cache.get(key).getAsJsonPrimitive().isBoolean()) {
            return cache.get(key).getAsBoolean();
        }
        setBoolean(key, defaultValue, file);
        return defaultValue;
    }

    public static String getString(String key, String defaultValue, File file) {
        JsonObject cache = getCache(file);
        if (cache.has(key)
                && cache.get(key).isJsonPrimitive()
                && cache.get(key).getAsJsonPrimitive().isString()) {
            return cache.get(key).getAsString();
        }
        setString(key, defaultValue, file);
        return defaultValue;
    }

    public static int getInt(String key, int defaultValue, File file) {
        JsonObject cache = getCache(file);
        if (cache.has(key)
                && cache.get(key).isJsonPrimitive()
                && cache.get(key).getAsJsonPrimitive().isNumber()) {
            return cache.get(key).getAsInt();
        }
        setInt(key, defaultValue, file);
        return defaultValue;
    }
}
