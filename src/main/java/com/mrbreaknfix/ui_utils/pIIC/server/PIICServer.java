/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file PIICServer.java
 */
package com.mrbreaknfix.ui_utils.pIIC.server;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

// Persistent, Inter-Instance Communication Server
public class PIICServer extends WebSocketServer {

    public static final int PROTOCOL_VERSION = 1;
    public static final String READY_SIGNAL = "p.IIC_SERVER_READY";

    private final List<String> allowedOrigins;
    private final Map<WebSocket, InstanceInfo> instanceClients = new ConcurrentHashMap<>();
    private final Set<WebSocket> browserClients = ConcurrentHashMap.newKeySet();
    private final AtomicInteger nextPort = new AtomicInteger(33534);
    private final ScheduledExecutorService shutdownExecutor =
            Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> shutdownTask;
    private final Gson gson = new Gson();

    public PIICServer(int port, boolean devMode) {
        super(new InetSocketAddress(port));
        this.setConnectionLostTimeout(15);
        this.setReuseAddr(true);
        this.allowedOrigins =
                new ArrayList<>(
                        List.of(
                                "https://mrbreaknfix.com",
                                "https://cmd-utils.com",
                                "https://ui-utils.com"));
        if (devMode) {
            System.out.println(
                    "p.IIC server running in DEVELOPMENT MODE. Accepting localhost origins.");
            this.allowedOrigins.addAll(
                    List.of("http://localhost", "https://localhost", "http://localhost:5173"));
        } else {
            System.out.println("p.IIC server running in PRODUCTION MODE.");
        }
    }

    public static void main(String[] args) {
        boolean devMode = Arrays.asList(args).contains("--dev");
        int port = 33532;
        PIICServer server = new PIICServer(port, devMode);
        server.start();
    }

    @Override
    public void onStart() {
        System.out.println(READY_SIGNAL);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String origin = handshake.getFieldValue("Origin");
        if (origin != null && this.allowedOrigins.stream().anyMatch(origin::equals)) {
            System.out.println("Authorized browser client connected from origin: " + origin);
            browserClients.add(conn);
            cancelShutdown();
            JsonObject welcomeMsg = new JsonObject();
            welcomeMsg.addProperty("type", "authorized");
            welcomeMsg.addProperty("message", "Welcome to the p.IIC discovery service.");
            conn.send(gson.toJson(welcomeMsg));
            sendInstanceList(conn);
        } else {
            System.out.println(
                    "Client connected (assumed to be game instance): "
                            + conn.getRemoteSocketAddress());
            cancelShutdown();
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        boolean wasInstance = instanceClients.remove(conn) != null;
        boolean wasBrowser = browserClients.remove(conn);
        if (wasInstance) {
            System.out.println("Instance client disconnected: " + conn.getRemoteSocketAddress());
            broadcastUpdate();
        } else if (wasBrowser) {
            System.out.println("Browser client disconnected: " + conn.getRemoteSocketAddress());
        }
        scheduleShutdown();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JsonObject msg = gson.fromJson(message, JsonObject.class);
            String type = msg.get("type").getAsString();
            if (browserClients.contains(conn)) {
                if ("list_instances".equals(type)) {
                    sendInstanceList(conn);
                }
                return;
            }
            if (!instanceClients.containsKey(conn)) {
                if ("register".equals(type)) {
                    handleRegister(conn, msg.getAsJsonObject("data"));
                } else {
                    conn.close(1008, "Protocol violation: Client must register first.");
                }
                return;
            }
            switch (type) {
                case "list_instances" -> sendInstanceList(conn);
                case "broadcast" -> handleBroadcast(conn, msg);
                case "unicast" -> handleUnicast(conn, msg);
                case "shutdown_for_update" -> handleShutdownForUpdate(conn);
                default ->
                        System.err.println(
                                "Unknown message type '"
                                        + type
                                        + "' from registered instance client.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    private void handleRegister(WebSocket conn, JsonObject data) {
        int clientVersion = data.get("protocolVersion").getAsInt();
        if (clientVersion > PROTOCOL_VERSION) {
            JsonObject response = new JsonObject();
            response.addProperty("type", "update_required");
            conn.send(gson.toJson(response));
            return;
        }
        String name = data.get("name").getAsString();
        int port = nextPort.getAndIncrement();
        String id = UUID.randomUUID().toString();
        InstanceInfo info = new InstanceInfo(id, name, port, clientVersion);
        instanceClients.put(conn, info);
        JsonObject replyData = new JsonObject();
        replyData.addProperty("port", port);
        replyData.addProperty("id", id);
        JsonObject replyMsg = new JsonObject();
        replyMsg.addProperty("type", "registered");
        replyMsg.add("data", replyData);
        conn.send(gson.toJson(replyMsg));
        System.out.printf(
                "Registered instance '%s' (ID: %s, v%d) on port %d\n",
                name, id, clientVersion, port);
        broadcastUpdate();
    }

    private void handleShutdownForUpdate(WebSocket conn) {
        System.out.println("Received shutdown_for_update command. Shutting down gracefully.");
        JsonObject shutdownMessage = new JsonObject();
        shutdownMessage.addProperty("type", "server_shutdown");
        shutdownMessage.addProperty("reason", "A newer version of the p.IIC server is starting.");
        broadcast(gson.toJson(shutdownMessage));
        try {
            stop(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.exit(0);
    }

    private void handleBroadcast(WebSocket sender, JsonObject message) {
        InstanceInfo senderInfo = instanceClients.get(sender);
        if (senderInfo == null) return;
        message.addProperty("fromId", senderInfo.id());

        String payloadType = "unknown";
        if (message.has("payload") && message.get("payload").isJsonObject()) {
            JsonObject payload = message.getAsJsonObject("payload");
            if (payload.has("action")) {
                payloadType = payload.get("action").getAsString();
            }
        }
        System.out.printf(
                "[BROADCAST] Relaying '%s' message from sender '%s' to all %d clients.\n",
                payloadType, senderInfo.id(), instanceClients.size());

        broadcast(gson.toJson(message));
    }

    private void handleUnicast(WebSocket sender, JsonObject message) {
        InstanceInfo senderInfo = instanceClients.get(sender);
        if (senderInfo == null) {
            System.err.println("Unicast from unregistered client dropped.");
            return;
        }

        String targetId = message.get("targetId").getAsString();
        message.addProperty("fromId", senderInfo.id());

        String payloadType = "unknown";
        if (message.has("payload") && message.get("payload").isJsonObject()) {
            JsonObject payload = message.getAsJsonObject("payload");
            if (payload.has("action")) {
                payloadType = payload.get("action").getAsString();
            }
        }
        System.out.printf(
                "[UNICAST] Relaying '%s' message from sender '%s' to target '%s'.\n",
                payloadType, senderInfo.id(), targetId);

        String messageString = gson.toJson(message);

        for (Map.Entry<WebSocket, InstanceInfo> entry : instanceClients.entrySet()) {
            if (entry.getValue().id().equals(targetId)) {
                entry.getKey().send(messageString);
                return;
            }
        }
        System.err.printf(
                "[UNICAST] FAILED: Could not find target instance with ID '%s'.\n", targetId);
    }

    private void sendInstanceList(WebSocket conn) {
        List<InstanceInfo> instanceList = new ArrayList<>(instanceClients.values());
        JsonObject updateMsg = new JsonObject();
        updateMsg.addProperty("type", "instance_update");
        updateMsg.add("data", gson.toJsonTree(instanceList));
        conn.send(gson.toJson(updateMsg));
    }

    private void broadcastUpdate() {
        List<InstanceInfo> instanceList = new ArrayList<>(instanceClients.values());
        JsonObject updateMsg = new JsonObject();
        updateMsg.addProperty("type", "instance_update");
        updateMsg.add("data", gson.toJsonTree(instanceList));
        broadcast(gson.toJson(updateMsg));
    }

    private synchronized void scheduleShutdown() {
        if (instanceClients.isEmpty()) {
            System.out.println(
                    "Last instance client disconnected. Scheduling shutdown in 5 seconds...");
            shutdownTask =
                    shutdownExecutor.schedule(
                            () -> {
                                if (instanceClients.isEmpty()) {
                                    System.out.println("Shutting down.");
                                    try {
                                        stop(1000);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                    System.exit(0);
                                }
                            },
                            5,
                            TimeUnit.SECONDS);
        }
    }

    private synchronized void cancelShutdown() {
        if (shutdownTask != null && !shutdownTask.isDone()) {
            System.out.println("New connection, cancelling shutdown.");
            shutdownTask.cancel(false);
        }
    }
}
