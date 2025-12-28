/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file WebSocketSubscribeEvent.java
 */
package com.mrbreaknfix.ui_utils.event.events;

import com.mrbreaknfix.ui_utils.event.Event;

import org.java_websocket.WebSocket;

@SuppressWarnings("unused")
public class WebSocketSubscribeEvent extends Event {
    private final String clientId;
    private final String eventType;
    private final boolean subscribe;
    private final WebSocket conn;

    public WebSocketSubscribeEvent(
            String clientId, String eventType, boolean subscribe, WebSocket conn) {
        this.clientId = clientId;
        this.eventType = eventType;
        this.subscribe = subscribe;
        this.conn = conn;
    }

    public WebSocket getConnection() {
        return conn;
    }

    public String getClientId() {
        return clientId;
    }

    public String getEventType() {
        return eventType;
    }

    public boolean isSubscribing() {
        return subscribe;
    }

    public boolean isUnsubscribing() {
        return !subscribe;
    }
}
