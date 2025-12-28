/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file UIActions.java
 */
package com.mrbreaknfix.ui_utils.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.primitives.Shorts;
import com.google.common.primitives.SignedBytes;
import com.mrbreaknfix.ui_utils.mixin.accessor.ClientConnectionAccessor;
import com.mrbreaknfix.ui_utils.persistance.Settings;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.BundleItemSelectedC2SPacket;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;

import static com.mrbreaknfix.ui_utils.UiUtils.LOGGER;
import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class UIActions {
    private static boolean sendPackets = true;
    private static boolean delayPackets = false;
    public static boolean shouldEditSign = true;
    public static ArrayList<Packet<?>> delayedUIPackets = new ArrayList<>();
    public static ArrayList<String> delayedChatMessages = new ArrayList<>();

    public static void sendBundleSelectPacket(int slot, int bundleId) {
        if (mc.getNetworkHandler() == null) {
            LOGGER.warn(
                    "Minecraft network handler was null while using 'sendBundleSelectPacket' in UI Utils.");
            return;
        }
        BundleItemSelectedC2SPacket packet =
                new BundleItemSelectedC2SPacket(slot, bundleId); // Corrected usage
        mc.getNetworkHandler().sendPacket(packet);
        LOGGER.info("Sent BundleSelect packet: Slot: {}, BundleID: {}", slot, bundleId);
        if (mc.player != null) {
            mc.player.sendMessage(
                    Text.literal("Sent BundleSelect: slot=" + slot + ", bundleId=" + bundleId)
                            .formatted(Formatting.GRAY),
                    false);
        }
    }

    // todo: allow user to choose which syncid to desync
    public static void desync() {
        if (mc.player == null) {
            LOGGER.warn("Minecraft player was null while using 'De-sync' in UI Utils.");
            return;
        }

        if (mc.getNetworkHandler() == null) {
            LOGGER.warn("Minecraft network handler was null while using 'De-sync' in UI Utils.");
            return;
        }

        mc.getNetworkHandler()
                .sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        mc.player.sendMessage(Text.of("De-sync"), false);
        LOGGER.info("De-sync");
    }

    public static void CWoP() {
        //        if (mc.player == null) {
        //            LOGGER.warn("Minecraft player was null while using 'CWoP' in UI Utils.");
        //            return;
        //        }

        mc.setScreen(null);
        LOGGER.info("CWoP");
    }

    public static void sendClickSlotPacket(
            int syncId,
            int revision,
            short slot,
            byte button,
            SlotActionType action,
            int timesToSend) {
        if (mc.getNetworkHandler() == null) {
            LOGGER.warn(
                    "Minecraft network handler was null while using 'sendClickSlotPacket' in UI Utils.");
            return;
        }

        if (mc.player == null) {
            LOGGER.warn("Minecraft player was null while using 'sendClickSlotPacket' in UI Utils.");
            return;
        }

        ClientPlayerInteractionManager interactionManager = mc.interactionManager;
        ScreenHandler screenHandler = mc.player.currentScreenHandler;

        for (int k = 0; k < timesToSend; k++) {
            if (interactionManager != null) {

                DefaultedList<Slot> defaultedList = screenHandler.slots;
                int i = defaultedList.size();
                List<ItemStack> list = Lists.newArrayListWithCapacity(i);

                for (Slot s : defaultedList) {
                    list.add(s.getStack().copy());
                }

                screenHandler.onSlotClick(slot, button, action, mc.player);
                Int2ObjectMap<ItemStackHash> int2ObjectMap = new Int2ObjectOpenHashMap<>();

                for (int j = 0; j < i; ++j) {
                    ItemStack itemStack = (ItemStack) list.get(j);
                    ItemStack itemStack2 = ((Slot) defaultedList.get(j)).getStack();
                    if (!ItemStack.areEqual(itemStack, itemStack2)) {
                        int2ObjectMap.put(
                                j,
                                ItemStackHash.fromItemStack(
                                        itemStack2, mc.getNetworkHandler().getComponentHasher()));
                    }
                }

                ItemStackHash itemStackHash =
                        ItemStackHash.fromItemStack(
                                screenHandler.getCursorStack(),
                                mc.getNetworkHandler().getComponentHasher());
                mc.getNetworkHandler()
                        .sendPacket(
                                new ClickSlotC2SPacket(
                                        syncId,
                                        screenHandler.getRevision(),
                                        Shorts.checkedCast((long) slot),
                                        SignedBytes.checkedCast((long) button),
                                        action,
                                        int2ObjectMap,
                                        itemStackHash));
            }
        }

        String s =
                "Sent ClickSlot packet: SyncID: %d, Revision: %d, Slot: %s, Button: %s, Action: %s, %d times."
                        .formatted(syncId, revision, slot, button, action, timesToSend);
        LOGGER.info(s);
    }

    public static void sendClickButtonPacket(int syncId, int buttonId, int timesToSend) {
        if (mc.getNetworkHandler() == null) {
            LOGGER.warn(
                    "Minecraft network handler was null while using 'sendClickButtonPacket' in UI Utils.");
            return;
        }
        if (mc.player == null) {
            LOGGER.warn(
                    "Minecraft player was null while using 'sendClickButtonPacket' in UI Utils.");
            return;
        }
        ButtonClickC2SPacket packet = new ButtonClickC2SPacket(syncId, buttonId);

        for (int i = 0; i < timesToSend; i++) {
            mc.getNetworkHandler().sendPacket(packet);
            ((ClientConnectionAccessor) mc.getNetworkHandler().getConnection())
                    .getChannel()
                    .writeAndFlush(packet);
        }
        //        mc.player.sendMessage(Text.of("Sent ClickButton packet: SyncID: " + syncId + ",
        // ButtonID: " + buttonId + ", " + timesToSend + " times."), false);
        LOGGER.info(
                "Sent ClickButton packet: SyncID: "
                        + syncId
                        + ", ButtonID: "
                        + buttonId
                        + ", "
                        + timesToSend
                        + " times.");
        if (mc.player != null) {
            //            mc.player.sendMessage(Text.literal("Sent ClickButton: syncId=" + syncId +
            // ",
            // buttonId=" + buttonId + ", times=" + timesToSend).formatted(Formatting.GRAY), false);
        }
    }

    public static void setSendPackets(boolean sendPackets) {
        UIActions.sendPackets = sendPackets;
        LOGGER.info("Send Packets: " + sendPackets);
    }

    public static void toggleSendPackets() {
        sendPackets = !sendPackets;
        LOGGER.info("Send Packets: " + sendPackets);
    }

    public static boolean setDelayPackets(boolean delayPackets) {
        UIActions.delayPackets = delayPackets;
        LOGGER.info("Delay Packets: " + delayPackets);

        if (!delayPackets && mc.getNetworkHandler() != null) {
            for (Packet<?> packet : delayedUIPackets) {
                mc.getNetworkHandler().sendPacket(packet);
                ((ClientConnectionAccessor) mc.getNetworkHandler().getConnection())
                        .getChannel()
                        .writeAndFlush(packet);
            }
            delayedUIPackets.clear();
        }

        if (!delayPackets && Settings.delayChatPackets) {
            for (String message : delayedChatMessages) {
                sendChatMessageInternal(message);
            }
            delayedChatMessages.clear();
        }

        return delayPackets;
    }

    public static void toggleDelayPackets() {

        delayPackets = !delayPackets;

        if (!delayPackets && mc.getNetworkHandler() != null) {
            for (Packet<?> packet : delayedUIPackets) {
                mc.getNetworkHandler().sendPacket(packet);
                ((ClientConnectionAccessor) mc.getNetworkHandler().getConnection())
                        .getChannel()
                        .writeAndFlush(packet);
            }
            delayedUIPackets.clear();
        }

        LOGGER.info("Delay Packets: " + delayPackets);
    }

    public static boolean shouldSendPackets() {
        return sendPackets;
    }

    public static boolean shouldDelayPackets() {
        return delayPackets;
    }

    public static void disconnectAndSendPackets() {
        if (mc.player == null) {
            LOGGER.warn(
                    "Minecraft player was null while using 'disconnectAndSendPackets' in UI Utils.");
            return;
        }

        if (mc.getNetworkHandler() == null) {
            LOGGER.warn(
                    "Minecraft network handler was null while using 'disconnectAndSendPackets' in UI Utils.");
            return;
        }
        delayPackets = false;
        for (Packet<?> packet : delayedUIPackets) {
            mc.getNetworkHandler().sendPacket(packet);
            ((ClientConnectionAccessor) mc.getNetworkHandler().getConnection())
                    .getChannel()
                    .writeAndFlush(packet);
        }

        mc.getNetworkHandler()
                .getConnection()
                .disconnect(Text.of("UI-Utils: Disconnected and sent packets"));

        LOGGER.info("Disconnected and sent packets");
        delayedUIPackets.clear();
    }

    public static void chat(String msg) {
        if (mc.player == null) {
            LOGGER.warn("Minecraft player was null while using 'chat' in UI Utils.");
            return;
        }

        if (mc.getNetworkHandler() == null) {
            LOGGER.warn("Minecraft network handler was null while using 'chat' in UI Utils.");
            return;
        }

        // handle delayed chat messages
        if (Settings.delayChatPackets && delayPackets) {
            delayedChatMessages.add(msg);
            mc.player.sendMessage(
                    Text.literal("[Ui-Utils] Delayed chat message: " + msg)
                            .formatted(Formatting.ITALIC, Formatting.LIGHT_PURPLE),
                    false);
            return;
        }

        // handle commands
        sendChatMessageInternal(msg);
    }

    private static void sendChatMessageInternal(String msg) {
        if (mc.player == null) {
            LOGGER.warn("Minecraft player was null while using 'chat' in UI Utils.");
            return;
        }

        if (mc.getNetworkHandler() == null) {
            LOGGER.warn("Minecraft network handler was null while using 'chat' in UI Utils.");
            return;
        }

        if (msg.startsWith("/")) {
            mc.getNetworkHandler().sendChatCommand(msg.substring(1));
        } else {
            mc.getNetworkHandler().sendChatMessage(msg);
        }
    }
}
