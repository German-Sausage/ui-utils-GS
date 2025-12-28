/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file Mcfw.java
 */
package com.mrbreaknfix.ui_utils.packet;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.mrbreaknfix.ui_utils.UiUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Mcfw {
    public static boolean enabled = false;
    public static final Map<String, McfwFilterType> rules = new ConcurrentHashMap<>();
    public static final Queue<Packet<?>> delayedPackets = new ConcurrentLinkedQueue<>();

    public static McfwFilterType handlePacket(Packet<?> packet) {
        if (!enabled) {
            return null;
        }

        String name = PacketNameUtil.getPacketName(packet);
        McfwFilterType type = rules.get(name.toLowerCase());

        if (type == null) {
            type = rules.get("all");
        }

        if (rules.getOrDefault(name.toLowerCase(), McfwFilterType.ALLOW) == McfwFilterType.ALLOW
                && type == McfwFilterType.DROP) {
            return null;
        }

        if (type == null) {
            return null;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        switch (type) {
            case LOG:
                Text logMessage =
                        Text.literal("[MCFW] Logged: ")
                                .formatted(Formatting.AQUA)
                                .append(Text.literal(name).formatted(Formatting.WHITE));
                if (mc.player != null) mc.player.sendMessage(logMessage, false);
                UiUtils.LOGGER.info("[MCFW] Logged packet: {}", name);
                return null;

            case DELAY:
                delayedPackets.add(packet);
                break;

            case DROP:
                break;

            case ALLOW:
                return null;
        }
        return type;
    }

    public static int release() {
        int count = 0;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() == null) {
            return 0;
        }

        boolean wasEnabled = enabled;
        enabled = false;

        try {
            while (!delayedPackets.isEmpty()) {
                Packet<?> packet = delayedPackets.poll();
                if (packet != null) {
                    mc.getNetworkHandler().sendPacket(packet);
                    count++;
                }
            }
        } finally {
            enabled = wasEnabled;
        }

        return count;
    }

    public static int clear() {
        int count = delayedPackets.size();
        delayedPackets.clear();
        return count;
    }

    public static List<String> getAllPacketNamesForAutocomplete() {
        return PacketNameUtil.getAllPacketNames();
    }
}
