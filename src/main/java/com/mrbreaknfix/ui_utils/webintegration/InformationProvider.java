/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file InformationProvider.java
 */
package com.mrbreaknfix.ui_utils.webintegration;

import com.mrbreaknfix.ui_utils.event.Subscribe;
import com.mrbreaknfix.ui_utils.event.events.TickEvent;
import com.mrbreaknfix.ui_utils.event.events.UserApiServiceCreatedEvent;

import static com.mrbreaknfix.ui_utils.UiUtils.*;

public class InformationProvider {
    public void enable() {
        LOGGER.info("Information Provider registered");
        eventManager.addListener(this);
    }

    public void disable() {
        LOGGER.info("Information Provider unregistered");
        eventManager.removeListener(this);
    }

    @Subscribe
    public void onTick(TickEvent.Post event) {
        //        System.out.println("Tick event received, broadcasting to WebSocket clients.");
        webSocketCommandServer.broadcastToSubscribedClients("", "tick");
    }

    @Subscribe
    public void onUsernameChanged(UserApiServiceCreatedEvent event) {
        String username = mc.getSession().getUsername();
        String message = String.format("{\"username\":\"%s\"}", username);
        webSocketCommandServer.broadcastToSubscribedClients(message, "username");
        LOGGER.info("Username changed to: {}", username);
    }

    /*    @Subscribe
    public void onWindowSizeChanged(WindowSizeChangedEvent event) {
        int width = event.getWidth();
        int height = event.getHeight();
        sendWindowInfo(width, height, mc.getWindow().getX(), mc.getWindow().getY());
    }

    @Subscribe
    public void onWindowPosChanged(WindowPosChangedEvent event) {
        Window window = mc.getWindow();
        sendWindowInfo(window.getWidth(), window.getHeight(), event.getX(), event.getY());
    }*/

    /*    @Subscribe
    public void onSubscribe(WebSocketSubscribeEvent event) {
        if (event.isSubscribing() && Objects.equals(event.getEventType(), "window")) {
            Window window = mc.getWindow();
            int width = window.getWidth();
            int height = window.getHeight();
            int x = window.getX();
            int y = window.getY();
            sendWindowInfo(width, height, x, y);
        }
    }

    private void sendWindowInfo(int width, int frameHeight, int x, int rawY) {
        Window window = mc.getWindow();
        long handle = window.getHandle();

        IntBuffer topBuffer = BufferUtils.createIntBuffer(1);
        GLFW.glfwGetWindowFrameSize(handle, null, topBuffer, null, null);
        int titleBarHeight = topBuffer.get(0);

        int adjustedY = rawY - titleBarHeight;
        int fullWindowHeight = frameHeight + titleBarHeight;

        String message = String.format("{\"width\":%d,\"height\":%d,\"x\":%d,\"y\":%d}", width, fullWindowHeight, x, adjustedY);
        webSocketCommandServer.broadcastToSubscribedClients(message, "window");
    }*/
}
