/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file WSClientConnectedEvent.java
 */
package com.mrbreaknfix.ui_utils.event.events;

import com.mrbreaknfix.ui_utils.event.Event;

import org.java_websocket.WebSocket;

@SuppressWarnings("unused")
public class WSClientConnectedEvent extends Event {
    private final WebSocket conn;
    private final String clientId;

    public WSClientConnectedEvent(WebSocket conn, String clientId) {
        this.conn = conn;
        this.clientId = clientId;
    }

    public WebSocket getConn() {
        return conn;
    }

    public String getClientId() {
        return clientId;
    }
}
