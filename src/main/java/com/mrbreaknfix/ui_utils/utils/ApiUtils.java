/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ApiUtils.java
 */
package com.mrbreaknfix.ui_utils.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.mrbreaknfix.ui_utils.UiUtils.LOGGER;

public class ApiUtils {

    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public static void startPinger() {
        // Initial delay 0, then repeat every 5 minutes
        scheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        postRequest("https://api.ui-utils.com/online", "");
                    } catch (Exception e) {
                        LOGGER.error("Error pinging UI-Utils online API", e);
                    }
                },
                0,
                5,
                TimeUnit.MINUTES);
    }

    public static void shutdown() {
        try {
            postRequest("https://api.ui-utils.com/offline", "");
        } catch (Exception e) {
            LOGGER.error("Error during shutdown ping", e);
        }
        scheduler.shutdown();
    }

    public static String postRequest(String urlString, String jsonInputString) {
        return sendUiUtilsAPIRequest(urlString, "POST", jsonInputString);
    }

    public static String getRequest(String urlString) {
        return sendUiUtilsAPIRequest(urlString, "GET", "");
    }

    private static @NotNull HttpURLConnection getHttpURLConnection(
            String method, String jsonInputString, URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("User-Agent", UserAgent.getUiUtilsUseragent());

        if (method.equals("POST")) {
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }
        return connection;
    }

    private static @Nullable String sendUiUtilsAPIRequest(
            String urlString, String method, String jsonInputString) {
        try {
            URL url = URI.create(urlString).toURL();
            HttpURLConnection connection = getHttpURLConnection(method, jsonInputString, url);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                try (InputStreamReader errorReader =
                        new InputStreamReader(connection.getErrorStream())) {
                    StringBuilder errorResponse = new StringBuilder();
                    int ch;
                    while ((ch = errorReader.read()) != -1) {
                        errorResponse.append((char) ch);
                    }
                    throw new RuntimeException("Unauthorized: %s".formatted(errorResponse));
                }
            }

            if (responseCode == 530) { // 530 means the server is down
                return null;
            }

            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                StringBuilder response = new StringBuilder();
                int ch;
                while ((ch = reader.read()) != -1) {
                    response.append((char) ch);
                }
                return response.toString();
            }

        } catch (IOException e) {
            LOGGER.error("Error: {}", e.getMessage());
            return null;
        }
    }
}
