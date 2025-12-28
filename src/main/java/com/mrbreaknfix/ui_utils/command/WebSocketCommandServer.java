/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file WebSocketCommandServer.java
 */
package com.mrbreaknfix.ui_utils.command;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.Constants;
import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.event.events.WSClientConnectedEvent;
import com.mrbreaknfix.ui_utils.webintegration.InformationProvider;

import net.fabricmc.loader.api.FabricLoader;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import static com.mrbreaknfix.ui_utils.UiUtils.eventManager;
import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class WebSocketCommandServer extends WebSocketServer {

    private static final Gson gson = new Gson();
    private static final List<String> ALLOWED_ORIGINS =
            new ArrayList<>(
                    List.of(
                            "https://mrbreaknfix.com",
                            "https://cmd-utils.com",
                            "https://ui-utils.com"));
    public static final ConcurrentHashMap<WebSocket, String> connections =
            new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<WebSocket, Boolean> authorizedClients =
            new ConcurrentHashMap<>();
    public static boolean informationProviderActive = false;
    public static final InformationProvider informationProvider = new InformationProvider();
    private static final ConcurrentHashMap<String, Set<String>> clientSubscriptions =
            new ConcurrentHashMap<>();

    public WebSocketCommandServer(int port) {
        super(new InetSocketAddress(port));
        this.setConnectionLostTimeout(10);
        this.setReuseAddr(true);
        UiUtils.LOGGER.info(
                "WebSocket Server configured to run on port {} with a 10-second connection-lost timeout and SO_REUSEADDR.",
                port);
    }

    public void init() {
        if (UiUtils.isDevModeEnabled) {
            ALLOWED_ORIGINS.add("http://localhost");
            ALLOWED_ORIGINS.add("https://localhost");
            ALLOWED_ORIGINS.add("http://localhost:5173");
        }

        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    try {
                                        UiUtils.LOGGER.info(
                                                "Stopping WebSocket server via shutdown hook...");
                                        this.stop(1000);
                                        UiUtils.LOGGER.info("WebSocket server stopped.");
                                    } catch (InterruptedException e) {
                                        UiUtils.LOGGER.error(
                                                "Error while stopping WebSocket server", e);
                                        Thread.currentThread().interrupt();
                                    }
                                }));

        new Thread(
                        () -> {
                            try {
                                this.start();
                            } catch (Exception e) {
                                UiUtils.LOGGER.error(
                                        "Failed to start WebSocket server: {}", e.getMessage());
                            }
                        })
                .start();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String origin = handshake.getFieldValue("Origin");
        boolean isAuthorized = origin != null && ALLOWED_ORIGINS.stream().anyMatch(origin::equals);

        if (isAuthorized) {
            String clientId = UUID.randomUUID().toString();
            connections.put(conn, clientId);
            authorizedClients.put(conn, true);
            UiUtils.LOGGER.info(
                    "WebSocket connection opened from allowed origin: {} with ID: {}",
                    origin,
                    clientId);

            try {
                JsonObject welcomeMessage = new JsonObject();
                welcomeMessage.addProperty("type", "connected");
                welcomeMessage.addProperty(
                        "message", "Welcome to the UiUtils WebSocket Command Server!");
                welcomeMessage.addProperty("client_id", clientId);
                welcomeMessage.addProperty("origin", origin);
                welcomeMessage.addProperty("authorized", true);

                String mcName = mc.getSession().getUsername();
                UUID uuid = mc.getSession().getUuidOrNull();
                welcomeMessage.addProperty("mcname", (mcName == null) ? "Player" : mcName);
                welcomeMessage.addProperty(
                        "uuid",
                        FabricLoader.getInstance().isDevelopmentEnvironment()
                                ? "Dev"
                                : ((uuid == null) ? "N/A" : uuid.toString()));

                welcomeMessage.addProperty("uiutils_api_version", Constants.API_VERSION);
                welcomeMessage.addProperty("mc_version", Constants.MINECRAFT_VERSION);
                welcomeMessage.addProperty("uiutils_version", Constants.UIUTILS_VERSION);
                welcomeMessage.addProperty("fabric_api_version", Constants.FABRIC_API_VERSION);
                welcomeMessage.addProperty(
                        "fabric_loader_version", Constants.FABRIC_LOADER_VERSION);
                welcomeMessage.addProperty(
                        "cmd_server_host_url", "http://localhost:" + UiUtils.COMMAND_SERVER_PORT);

                eventManager.trigger(new WSClientConnectedEvent(conn, clientId));
                conn.send(gson.toJson(welcomeMessage));
            } catch (Exception e) {
                UiUtils.LOGGER.error("CRITICAL ERROR building or sending welcome message: ", e);
                conn.close(1011, "Internal Server Error");
            }
        } else {
            conn.close(1008, "Blocked Origin");
            UiUtils.LOGGER.warn("WebSocket: Blocked request from non-allowed origin: {}", origin);
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String clientId = connections.remove(conn);
        authorizedClients.remove(conn);

        if (clientId != null) {
            clientSubscriptions.remove(clientId);
            UiUtils.LOGGER.info(
                    "WebSocket connection closed for client ID: {}. Cleaned up state. (Code: {}, Reason: {})",
                    clientId,
                    code,
                    reason);
            updateInformationProviderState();
        } else {
            UiUtils.LOGGER.info(
                    "WebSocket connection closed for unregistered client: {} (Code: {}, Reason: {})",
                    conn.getRemoteSocketAddress(),
                    code,
                    reason);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        JsonObject json = null;
        try {
            json = gson.fromJson(message, JsonObject.class);

            if (!authorizedClients.getOrDefault(conn, false)) {
                conn.send(gson.toJson(errorMessage("Unauthorized access.")));
                return;
            }

            String requestId = json.has("requestId") ? json.get("requestId").getAsString() : null;

            if (json.has("cmd")) {
                String command = json.get("cmd").getAsString();
                List<CommandResult<?>> results = CommandSystem.executeCommand(command);

                CommandResult<?> result =
                        results.isEmpty()
                                ? CommandResult.of(false, "No command was executed.")
                                : results.getFirst();

                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("type", "cmd_result");
                responseJson.addProperty("message", result.message());
                responseJson.addProperty("success", result.success());
                if (result.jsonBody() != null) {
                    responseJson.add("body", result.jsonBody());
                }
                if (requestId != null) {
                    responseJson.addProperty("requestId", requestId);
                }
                conn.send(gson.toJson(responseJson));
            }

            handleSubscription(conn, json, "subscribe", true);
            handleSubscription(conn, json, "unsubscribe", false);

        } catch (Exception e) {
            UiUtils.LOGGER.error("Failed to parse WebSocket message: {}", e.getMessage());
            JsonObject errorJson = errorMessage("Invalid JSON format");
            if (json != null && json.has("requestId")) {
                errorJson.addProperty("requestId", json.get("requestId").getAsString());
            }
            conn.send(gson.toJson(errorJson));
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        UiUtils.LOGGER.error(
                "WebSocket error for connection {}: {}",
                conn != null ? conn.getRemoteSocketAddress() : "N/A",
                ex.getMessage());
    }

    @Override
    public void onStart() {
        UiUtils.LOGGER.info("WebSocket Server started successfully on port {}", getPort());
        UiUtils.WEBSOCKET_COMMAND_SERVER_PORT = getPort();
    }

    public void broadcastToSubscribedClients(String message, String subscription) {
        JsonObject broadcastMessage = new JsonObject();
        broadcastMessage.addProperty("type", "broadcast");
        broadcastMessage.addProperty("subscription", subscription);
        broadcastMessage.addProperty("message", message);
        String finalMessage = gson.toJson(broadcastMessage);

        for (WebSocket conn : connections.keySet()) {
            String clientId = connections.get(conn);
            if (isSubscribed(clientId, subscription)) {
                try {
                    if (conn.isOpen()) {
                        conn.send(finalMessage);
                    }
                } catch (Exception e) {
                    UiUtils.LOGGER.debug(
                            "Failed to send broadcast to client {}, connection might be closed.",
                            clientId);
                }
            }
        }
    }

    private void handleSubscription(
            WebSocket conn, JsonObject json, String action, boolean isSubscribing) {
        if (!json.has(action)) return;
        JsonElement element = json.get(action);
        String clientId = connections.get(conn);
        if (clientId == null) return;
        List<String> topics = new ArrayList<>();
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            topics.add(element.getAsString());
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement el : array) {
                if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                    topics.add(el.getAsString());
                }
            }
        } else {
            return;
        }
        List<String> validTopics =
                topics.stream()
                        .filter(topic -> topic != null && !topic.trim().isEmpty())
                        .collect(Collectors.toList());
        if (validTopics.isEmpty()) return;
        if (isSubscribing) subscribeToTopics(clientId, validTopics);
        else unsubscribeFromTopics(clientId, validTopics);
    }

    private void subscribeToTopics(String clientId, List<String> topics) {
        Set<String> subscriptions =
                clientSubscriptions.computeIfAbsent(clientId, k -> ConcurrentHashMap.newKeySet());
        boolean changed = subscriptions.addAll(topics);
        if (changed) {
            UiUtils.LOGGER.info("Client {} subscribed to: {}", clientId, String.join(", ", topics));
            updateInformationProviderState();
        }
    }

    private void unsubscribeFromTopics(String clientId, List<String> topics) {
        Set<String> subscriptions = clientSubscriptions.get(clientId);
        if (subscriptions != null) {
            boolean changed = subscriptions.removeAll(topics);
            if (changed) {
                UiUtils.LOGGER.info(
                        "Client {} unsubscribed from: {}", clientId, String.join(", ", topics));
                updateInformationProviderState();
            }
        }
    }

    private boolean isSubscribed(String clientId, String topic) {
        if (clientId == null) return false;
        Set<String> subscriptions = clientSubscriptions.get(clientId);
        return subscriptions != null && subscriptions.contains(topic);
    }

    private void updateInformationProviderState() {
        boolean hasSubscriptions =
                clientSubscriptions.values().stream().anyMatch(set -> !set.isEmpty());

        if (hasSubscriptions && !informationProviderActive) {
            informationProvider.enable();
            informationProviderActive = true;
            UiUtils.LOGGER.info("Information Provider activated due to active subscriptions.");
        } else if (!hasSubscriptions && informationProviderActive) {
            informationProvider.disable();
            informationProviderActive = false;
            UiUtils.LOGGER.info("Information Provider deactivated due to no active subscriptions.");
        }
    }

    private JsonObject errorMessage(String msg) {
        JsonObject errorJson = new JsonObject();
        errorJson.addProperty("type", "error");
        errorJson.addProperty("message", msg);
        return errorJson;
    }
}
