/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file PacketMetadata.java
 */
package com.mrbreaknfix.ui_utils.packet;

import java.lang.reflect.Constructor;

import net.minecraft.network.packet.Packet;

public final class PacketMetadata {

    /**
     * Holds all necessary reflected information about a packet's primary constructor.
     *
     * @param key A user-friendly key for the command (e.g., "play.hand_swing").
     * @param packetClass The actual Class of the packet.
     * @param constructor The reflected Constructor object to be invoked.
     * @param usageString A generated help string showing user-provided arguments.
     */
    public record PacketInfo(
            String key,
            Class<? extends Packet<?>> packetClass,
            Constructor<?> constructor,
            String usageString) {}
}
