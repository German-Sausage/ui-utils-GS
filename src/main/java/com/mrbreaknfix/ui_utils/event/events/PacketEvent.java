/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file PacketEvent.java
 */
package com.mrbreaknfix.ui_utils.event.events;

import com.mrbreaknfix.ui_utils.event.Event;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;

public class PacketEvent extends Event {
    public static class Receive extends Event {
        public Packet<?> packet;
        public ClientConnection connection;

        public Receive(Packet<?> packet, ClientConnection connection) {
            this.packet = packet;
            this.connection = connection;
        }
    }

    public static class Send extends Event {
        public Packet<?> packet;
        public ClientConnection connection;

        public Send(Packet<?> packet, ClientConnection connection) {
            this.packet = packet;
            this.connection = connection;
        }
    }

    public static class Sent extends Event {
        public Packet<?> packet;
        public ClientConnection connection;

        public Sent(Packet<?> packet, ClientConnection connection) {
            this.packet = packet;
            this.connection = connection;
        }
    }
}
