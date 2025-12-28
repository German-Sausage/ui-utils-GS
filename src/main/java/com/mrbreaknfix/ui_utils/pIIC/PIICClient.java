/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file PIICClient.java
 */
package com.mrbreaknfix.ui_utils.pIIC;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.command.CommandSystem;
import com.mrbreaknfix.ui_utils.pIIC.server.InstanceInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

// Persistent, Inter-Instance Communication Client
public class PIICClient extends WebSocketClient {
    private static final Gson GSON = new Gson();
    private static final int PROTOCOL_VERSION = 1;

    private final CompletableFuture<Integer> assignedPortFuture = new CompletableFuture<>();
    private String instanceId;
    private final String instanceName;

    private List<InstanceInfo> instanceCache = Collections.emptyList();
    private final Map<String, BiConsumer<String, JsonObject>> actionHandlers =
            new ConcurrentHashMap<>();

    public PIICClient(URI serverUri) {
        super(serverUri);
        String mcVersion = "Unknown";
        try {
            mcVersion = MinecraftClient.getInstance().getGameVersion();
        } catch (Exception ignored) {
        }
        this.instanceName = "Minecraft " + mcVersion;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        UiUtils.LOGGER.info("Connected to p.IIC manager. Registering instance...");
        registerInstance();

        SyncExecutionCoordinator.initialize(this);
    }

    @Override
    public void onMessage(String message) {
        UiUtils.LOGGER.debug("Received message from p.IIC: {}", message);
        JsonObject msg = GSON.fromJson(message, JsonObject.class);
        String type = msg.get("type").getAsString();

        switch (type) {
            case "registered" -> handleRegistration(msg);
            case "update_required" -> handleUpdateRequired();
            case "server_shutdown" ->
                    UiUtils.LOGGER.warn(
                            "p.IIC server is shutting down: {}", msg.get("reason").getAsString());
            case "instance_update" -> handleInstanceUpdate(msg);
            case "broadcast", "unicast" -> {
                String fromId = msg.has("fromId") ? msg.get("fromId").getAsString() : "SERVER";
                if (msg.has("payload") && msg.get("payload").isJsonObject()) {
                    routePayload(fromId, msg.getAsJsonObject("payload"));
                }
            }
        }
    }

    private void handleRegistration(JsonObject msg) {
        if (assignedPortFuture.isDone()) return;
        JsonObject data = msg.getAsJsonObject("data");
        int port = data.get("port").getAsInt();
        this.instanceId = data.get("id").getAsString();
        UiUtils.LOGGER.info(
                "Successfully registered with p.IIC. Assigned Port: {}, Instance ID: {}",
                port,
                instanceId);
        assignedPortFuture.complete(port);
    }

    private void handleInstanceUpdate(JsonObject msg) {
        Type listType = new TypeToken<List<InstanceInfo>>() {}.getType();
        this.instanceCache = GSON.fromJson(msg.get("data"), listType);
        UiUtils.LOGGER.info(
                "Instance list updated. {} instances now known.", this.instanceCache.size());
    }

    private void handleUpdateRequired() {
        UiUtils.LOGGER.warn("p.IIC server is outdated. Initiating takeover...");
        sendShutdownForUpdate();
        this.close();
    }

    /**
     * Routes an incoming payload to the appropriate registered handler based on its 'action'
     * property.
     */
    private void routePayload(String fromId, JsonObject payload) {
        if (!payload.has("action") || !payload.get("action").isJsonPrimitive()) return;

        String action = payload.get("action").getAsString();

        // New: General-purpose "show_message" handler remains here.
        if ("show_message".equals(action)) {
            handleShowMessage(payload);
            return;
        }

        // Delegate to a registered protocol handler.
        BiConsumer<String, JsonObject> handler = actionHandlers.get(action);
        if (handler != null) {
            try {
                handler.accept(fromId, payload);
            } catch (Exception e) {
                UiUtils.LOGGER.error("Error executing action handler for '{}'", action, e);
            }
        } else {
            UiUtils.LOGGER.warn("Received payload with unhandled action: {}", action);
        }
    }

    /**
     * Registers a handler for a specific payload action.
     *
     * @param action The 'action' string (e.g., "prepare_command").
     * @param handler A BiConsumer that accepts the sender's ID and the payload object.
     */
    public void registerActionHandler(String action, BiConsumer<String, JsonObject> handler) {
        actionHandlers.put(action, handler);
    }

    private void handleShowMessage(JsonObject payload) {
        MinecraftClient.getInstance()
                .execute(
                        () -> {
                            if (payload.has("text")
                                    && payload.get("text").isJsonPrimitive()
                                    && MinecraftClient.getInstance().player != null) {
                                String text = payload.get("text").getAsString();
                                MinecraftClient.getInstance()
                                        .player
                                        .sendMessage(Text.of("§8[§bBroadcast§8]§r " + text), false);
                            }
                        });
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        UiUtils.LOGGER.warn(
                "Disconnected from p.IIC manager. Reason: {}. Reconnection will be attempted.",
                reason);
        CommandSystem.handlePIICDisconnect();
    }

    @Override
    public void onError(Exception ex) {
        if (!assignedPortFuture.isDone()) {
            assignedPortFuture.completeExceptionally(ex);
        }
    }

    public void registerInstance() {
        JsonObject data = new JsonObject();
        data.addProperty("name", this.instanceName);
        data.addProperty("protocolVersion", PROTOCOL_VERSION);
        JsonObject message = new JsonObject();
        message.addProperty("type", "register");
        message.add("data", data);
        send(GSON.toJson(message));
    }

    private void sendShutdownForUpdate() {
        JsonObject message = new JsonObject();
        message.addProperty("type", "shutdown_for_update");
        send(GSON.toJson(message));
    }

    public void sendBroadcast(JsonObject payload) {
        JsonObject message = new JsonObject();
        message.addProperty("type", "broadcast");
        message.add("payload", payload);
        send(GSON.toJson(message));
    }

    public void sendUnicast(String targetId, JsonObject payload) {
        JsonObject message = new JsonObject();
        message.addProperty("type", "unicast");
        message.addProperty("targetId", targetId);
        message.add("payload", payload);
        send(GSON.toJson(message));
    }

    public int getAssignedPortBlocking() throws Exception {
        return assignedPortFuture.get(10, TimeUnit.SECONDS);
    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public List<InstanceInfo> getInstanceCache() {
        return this.instanceCache;
    }
}
