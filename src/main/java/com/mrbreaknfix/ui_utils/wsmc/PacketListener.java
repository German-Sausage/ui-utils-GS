/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file PacketListener.java
 */
package com.mrbreaknfix.ui_utils.wsmc;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrbreaknfix.ui_utils.event.Subscribe;
import com.mrbreaknfix.ui_utils.event.events.PacketEvent;

import net.minecraft.network.packet.Packet;

import static com.mrbreaknfix.ui_utils.UiUtils.LOGGER;
import static com.mrbreaknfix.ui_utils.UiUtils.eventManager;

@SuppressWarnings("unused")
public class PacketListener {
    private static final PacketListener INSTANCE = new PacketListener();

    private PacketListener() {}

    public static PacketListener getInstance() {
        return INSTANCE;
    }

    public void init() {
        eventManager.addListener(this);
        LOGGER.info("PacketListener initialized");
    }

    @Subscribe
    private void onSendPacket(PacketEvent.Send event) {
        Packet<?> packet = event.packet;
        Map<String, Object> packetData = new HashMap<>();

        packetData.put("packetClass", packet.getClass().getName());

        // Add all fields from the packet
        for (Field field : packet.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(packet);
                packetData.put(field.getName(), value);
            } catch (IllegalAccessException e) {
                packetData.put(field.getName(), "[ACCESS DENIED]");
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(packetData);

        System.out.println("[Sent Packet] " + json);
    }

    @Subscribe
    private void onReadPacket(PacketEvent.Receive event) {
        System.out.println("Received packet: " + event.packet.getClass().getName());
    }
}
