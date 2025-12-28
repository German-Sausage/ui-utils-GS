/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file Updater.java
 */
package com.mrbreaknfix.ui_utils.update;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.event.Subscribe;
import com.mrbreaknfix.ui_utils.event.events.OpenScreenEvent;
import com.mrbreaknfix.ui_utils.gui.screen.UpdateAvailableScreen;
import com.mrbreaknfix.ui_utils.gui.screen.UpdatedScreen;
import com.mrbreaknfix.ui_utils.persistance.Settings;
import com.mrbreaknfix.ui_utils.utils.Bulletin;
import com.mrbreaknfix.ui_utils.utils.UserAgent;
import com.mrbreaknfix.ui_utils.utils.VersionUtils;

import static com.mrbreaknfix.ui_utils.UiUtils.eventManager;
import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class Updater {
    public static final String currentModPath = VersionUtils.getModPath(UiUtils.MOD_ID);
    private static final int MAX_RETRIES = 3;
    public static boolean updated = false;
    public static boolean updateAvailable = false;

    public static void handleUpdate(String nextUpdateUrl, String expectedFileHash) {
        eventManager.addListener(Updater.class);
        if (!Settings.autoUpdate) {
            UiUtils.LOGGER.warn("Auto update is disabled, notifying user instead.");
            updateAvailable = true;
            return;
        }
        UiUtils.LOGGER.info("UPDATE PATH " + currentModPath);

        // Check if the current mod path is a valid .jar file (ex running in dev)
        if (!currentModPath.endsWith(".jar")) {
            UiUtils.LOGGER.warn("UI-Utils .jar file not found, skipping update.");
            return;
        }

        int attempts = 0;
        boolean success = false;

        while (attempts < MAX_RETRIES && !success) {
            try {
                attempts++;
                URL url = URI.create(nextUpdateUrl).toURL();
                String fileName = Path.of(url.getPath()).getFileName().toString();
                Path targetDir =
                        Path.of(currentModPath).getParent(); // parent folder of the old mod jar
                Path updateFile = targetDir.resolve(fileName);

                // Open a connection to the update URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", UserAgent.getUiUtilsUseragent());

                // Check if the connection was successful
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    UiUtils.LOGGER.error(
                            "Attempt "
                                    + attempts
                                    + ": Failed to fetch update, server responded with: "
                                    + responseCode);
                    if (attempts < MAX_RETRIES) {
                        UiUtils.LOGGER.info(
                                "Retrying... (" + (attempts + 1) + "/" + MAX_RETRIES + ")");
                        continue;
                    } else {
                        UiUtils.LOGGER.error("Max retries reached. Update failed.");
                        return;
                    }
                }

                // Download and write the file to disk
                try (InputStream in = connection.getInputStream()) {
                    Files.copy(in, updateFile, StandardCopyOption.REPLACE_EXISTING);
                    UiUtils.LOGGER.info("Update downloaded to " + updateFile.toAbsolutePath());
                }

                // Verify file hash
                String downloadedFileHash = calculateFileHash(updateFile);
                if (downloadedFileHash.equals(expectedFileHash)) {
                    UiUtils.LOGGER.info("File hash verified, update successful.");
                    success = true; // exit the loop on success

                    // Delete the old mod jar
                    removeOldJarFile();
                } else {
                    UiUtils.LOGGER.error(
                            "File hash mismatch! Expected: "
                                    + expectedFileHash
                                    + " but got: "
                                    + downloadedFileHash);
                    Files.delete(updateFile); // delete the corrupt file
                    if (attempts < MAX_RETRIES) {
                        UiUtils.LOGGER.info(
                                "Retrying... (" + (attempts + 1) + "/" + MAX_RETRIES + ")");
                    } else {
                        UiUtils.LOGGER.error("Max retries reached. Update failed.");
                    }
                }

            } catch (IOException | NoSuchAlgorithmException e) {
                UiUtils.LOGGER.error(
                        "Attempt "
                                + attempts
                                + ": Failed to download or verify update: "
                                + e.getMessage(),
                        e);
                if (attempts < MAX_RETRIES) {
                    UiUtils.LOGGER.info("Retrying... (" + (attempts + 1) + "/" + MAX_RETRIES + ")");
                } else {
                    UiUtils.LOGGER.error("Max retries reached. Update failed.");
                }
            }
        }
    }

    private static void removeOldJarFile() {
        // make sure it's a jar file
        if (!Updater.currentModPath.endsWith(".jar")) {
            UiUtils.LOGGER.error("Update failed... really badly.");
            System.exit(-1);
        }
        Path jarPath = Path.of(Updater.currentModPath);
        if (Files.exists(jarPath)) {
            try {
                Files.delete(jarPath);
                UiUtils.LOGGER.info(
                        "UI-Utils has updated! Please restart the game to apply the following changes:");
                for (int i = 0; i < Bulletin.changelog.size(); i++) {
                    UiUtils.LOGGER.info("- " + Bulletin.changelog.get(i));
                }
                updated = true;
            } catch (IOException e) {
                UiUtils.LOGGER.error("Failed to delete old jar file: " + e.getMessage());
            }
        } else {
            UiUtils.LOGGER.warn("Old jar file not found, nothing to delete.");
        }
    }

    // Method to calculate the hash (SHA-256) of the downloaded file
    private static String calculateFileHash(Path filePath)
            throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream stream = Files.newInputStream(filePath)) {
            byte[] byteArray = new byte[1024];
            int bytesRead;
            while ((bytesRead = stream.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesRead);
            }
        }
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    @Subscribe
    public void onScreenOpen(OpenScreenEvent event) {
        if (!(event.getScreen() instanceof UpdatedScreen) && updated) {
            event.cancel();
            mc.execute(() -> mc.setScreen(new UpdatedScreen()));
            eventManager.removeListener(Updater.class);
        }

        if (updateAvailable && !(event.getScreen() instanceof UpdateAvailableScreen)) {
            updateAvailable = false;
            event.cancel();
            mc.execute(() -> mc.setScreen(new UpdateAvailableScreen()));
        }

        if (!updated) eventManager.removeListener(Updater.class);
    }
}
