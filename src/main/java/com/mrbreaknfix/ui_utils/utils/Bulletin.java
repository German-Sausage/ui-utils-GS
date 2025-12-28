/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file Bulletin.java
 */
package com.mrbreaknfix.ui_utils.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.event.Subscribe;
import com.mrbreaknfix.ui_utils.event.events.InitEvent;
import com.mrbreaknfix.ui_utils.update.Updater;

// todo: rewrite bulletin system
public class Bulletin {
    public String nextUpdateVersion;
    public String nextUpdateMinecraftVersion;
    public static String nextUpdateUrl;
    public static String nextUpdateSHA256;
    public boolean autoUpdateCompatibleForCurrentVersion;
    public boolean hasAnnouncement;
    public boolean hasUpdate;

    public String announcementId;
    public String announcementTitle;
    public String announcementBody;

    public static List<String> changelog = new ArrayList<>();

    @Subscribe
    public void onInit(InitEvent event) {
        fetchBulletin();
    }

    public void fetchBulletin() {
        String endpoint = "https://api.ui-utils.com/bulletin";
        String json = ApiUtils.getRequest(endpoint);

        if (json == null || json.isEmpty()) {
            //            System.out.println("[UI-Utils] No update info received.");
            return;
        }

        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            if (jsonObject.has("updateAvailable")
                    && !jsonObject.get("updateAvailable").getAsBoolean()) {
                UiUtils.LOGGER.info("No updates available.");
            } else {
                handleNewUpdate(jsonObject);
            }

            // Handle announcements
            if (jsonObject.has("announcements")) {
                JsonObject announcements = jsonObject.getAsJsonObject("announcements");
                if (announcements.has("title") && announcements.has("message")) {
                    announcementTitle = announcements.get("title").getAsString();
                    announcementBody = announcements.get("message").getAsString();
                    announcementId =
                            announcements.has("id")
                                    ? announcements.get("id").getAsString()
                                    : "unknown";
                    hasAnnouncement = true;
                    UiUtils.LOGGER.info(
                            "[UI-Utils Bulletin] {}: {}", announcementTitle, announcementBody);
                }
            }

        } catch (Exception e) {
            UiUtils.LOGGER.error(
                    String.format("Failed to parse update response: %s", e.getMessage()));
        }
    }

    private void handleNewUpdate(JsonObject jsonObject) {
        if (jsonObject.has("updateAvailable") && jsonObject.get("updateAvailable").getAsBoolean()) {
            nextUpdateVersion =
                    jsonObject.has("nextUpdateVersion")
                            ? jsonObject.get("nextUpdateVersion").getAsString()
                            : "unknown";

            nextUpdateMinecraftVersion =
                    jsonObject.has("nextUpdateMinecraftVersion")
                            ? jsonObject.get("nextUpdateMinecraftVersion").getAsString()
                            : "unknown";

            nextUpdateUrl =
                    jsonObject.has("nextUpdateUrl")
                            ? jsonObject.get("nextUpdateUrl").getAsString()
                            : "unknown";

            nextUpdateSHA256 =
                    jsonObject.has("SHA256") ? jsonObject.get("SHA256").getAsString() : "unknown";

            UiUtils.LOGGER.info(
                    String.format(
                            "[UI-Utils Bulletin] Next update version: %s for Minecraft %s",
                            nextUpdateVersion, nextUpdateMinecraftVersion));

            if (nextUpdateVersion.equals(UiUtils.version)) {
                UiUtils.LOGGER.info("[UI-Utils Bulletin] You're already up to date.");
                return;
            }

            hasUpdate = true;
            UiUtils.LOGGER.info(
                    String.format("[UI-Utils Bulletin] Update available: %s", nextUpdateVersion));

            if (jsonObject.has("autoUpdateCompatibleVersions")) {
                JsonArray compatibleVersions =
                        jsonObject.getAsJsonArray("autoUpdateCompatibleVersions");
                boolean canAutoUpdate = false;

                for (JsonElement element : compatibleVersions) {
                    if (element.getAsString().equals(UiUtils.version)) {
                        canAutoUpdate = true;
                        break;
                    }
                }

                autoUpdateCompatibleForCurrentVersion = canAutoUpdate;
                UiUtils.LOGGER.info(
                        canAutoUpdate
                                ? "[UI-Utils Bulletin] Auto update compatible."
                                : "[UI-Utils Bulletin] Auto update not compatible.");

            } else {
                UiUtils.LOGGER.info(
                        "[UI-Utils Bulletin] No auto update compatible versions found.");
            }

            // Handle changelog for new update
            if (jsonObject.has("changelog") && jsonObject.get("changelog").isJsonArray()) {
                changelog.clear();
                JsonArray array = jsonObject.getAsJsonArray("changelog");
                for (JsonElement element : array) {
                    changelog.add(element.getAsString());
                }

                UiUtils.LOGGER.info("[UI-Utils Bulletin] Changelog:");
                for (String line : changelog) {
                    UiUtils.LOGGER.info(" - " + line);
                }
            }

            // Handle update process
            if (autoUpdateCompatibleForCurrentVersion) {
                UiUtils.LOGGER.info("[UI-Utils Bulletin] Starting update process...");
                Updater.handleUpdate(nextUpdateUrl, nextUpdateSHA256);
            } else {
                UiUtils.LOGGER.info("[UI-Utils Bulletin] Update not compatible with auto update.");
            }
        }
    }
}
