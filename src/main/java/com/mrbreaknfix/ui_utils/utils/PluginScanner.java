/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file PluginScanner.java
 */
package com.mrbreaknfix.ui_utils.utils;

import joptsimple.internal.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mrbreaknfix.ui_utils.event.Subscribe;
import com.mrbreaknfix.ui_utils.event.events.PacketEvent;
import com.mrbreaknfix.ui_utils.event.events.TickEvent;
import com.mrbreaknfix.ui_utils.mixin.accessor.ClientPlayNetworkHandlerAccessor;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static com.mrbreaknfix.ui_utils.UiUtils.eventManager;
import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class PluginScanner {
    private final List<String> plugins = new ArrayList<>();
    private final List<String> commandTreePlugins = new ArrayList<>();
    private final Random random = new Random();
    private final Set<String> VERSION_ALIASES =
            Set.of(
                    "version",
                    "ver",
                    "about",
                    "bukkit:version",
                    "bukkit:ver",
                    "bukkit:about"); // aliases for bukkit:version
    private boolean tick = false;
    private String alias;
    private int ticks = 0;

    public PluginScanner() {
        eventManager.addListener(this);
        //        System.out.println("PluginScanner initialized");
    }

    public void getPlugins() {
        plugins.addAll(commandTreePlugins);

        if (alias != null) {
            mc.getNetworkHandler()
                    .sendPacket(
                            new RequestCommandCompletionsC2SPacket(
                                    random.nextInt(200), alias + " "));
            tick = true;
        } else getPluginsJson();
    }

    @Subscribe
    private void onTick(TickEvent.Post event) {

        if (!tick) return;
        ticks++;

        if (ticks >= 100) getPluginsJson();
    }

    private void getPluginsJson() {
        plugins.sort(String.CASE_INSENSITIVE_ORDER);

        if (!plugins.isEmpty()) {
            System.out.printf(
                    "[Ui-Utils] Detected Plugins: %s ",
                    Strings.join(plugins.toArray(new String[0]), ", "));
            if (mc.player != null) {
                mc.player.sendMessage(
                        Text.literal("[Ui-Utils] Detected Plugins: ")
                                .formatted(Formatting.LIGHT_PURPLE)
                                .append(
                                        Text.literal(
                                                        Strings.join(
                                                                plugins.toArray(new String[0]),
                                                                ", "))
                                                .formatted(Formatting.GRAY)),
                        false);
            }
        } else {
            //            System.out.println("No plugins detected");
            if (mc.player != null) {
                mc.player.sendMessage(
                        Text.literal("[Ui-Utils] No plugins detected =[")
                                .formatted(Formatting.LIGHT_PURPLE),
                        false);
            }
        }

        tick = false;
        ticks = 0;
        plugins.clear();
    }

    @Subscribe
    private void onSendPacket(PacketEvent.Send event) {
        if (tick && event.packet instanceof RequestCommandCompletionsC2SPacket) event.cancel();
    }

    @Subscribe
    private void onReadPacket(PacketEvent.Receive event) {
        // should return the same set of plugins that command completing '/' would
        // the rationale is that since we should get this packet whenever we log into the server, we
        // can capture it
        // straight away and not need to send a command completion packet for essentially the same
        // results

        if (event.packet instanceof CommandTreeS2CPacket packet) {
            ClientPlayNetworkHandlerAccessor handler =
                    (ClientPlayNetworkHandlerAccessor) event.connection.getPacketListener();
            commandTreePlugins.clear();
            alias = null;

            // This gets the root node of the command tree. From there, all of its children have to
            // be of type
            // LiteralCommandNode, so we don't need to worry about checking or casting and can just
            // grab the name
            if (handler != null) {
                packet.getCommandTree(
                                CommandRegistryAccess.of(
                                        handler.uiutils$getCombinedDynamicRegistries(),
                                        handler.uiutils$getEnabledFeatures()),
                                ClientPlayNetworkHandlerAccessor.uiutils$getCommandNodeFactory())
                        .getChildren()
                        .forEach(
                                node -> {
                                    String[] split = node.getName().split(":");
                                    if (split.length > 1) {
                                        if (!commandTreePlugins.contains(split[0]))
                                            commandTreePlugins.add(split[0]);
                                    }

                                    // checking if any of the bukkit:version commands are available,
                                    // which we can also grab plugins from
                                    if (alias == null && VERSION_ALIASES.contains(node.getName())) {
                                        alias = node.getName();
                                    }
                                });
            }
        }

        if (!tick) return;

        try {
            if (event.packet instanceof CommandSuggestionsS2CPacket packet) {
                Suggestions matches = packet.getSuggestions();

                if (matches.isEmpty()) {
                    System.err.println("Command suggestions are empty.");
                    return;
                }

                for (Suggestion suggestion : matches.getList()) {
                    String pluginName = suggestion.getText();
                    if (!plugins.contains(pluginName.toLowerCase())) plugins.add(pluginName);
                }

                getPluginsJson();
            }
        } catch (Exception e) {
            System.err.println("Error while getting command suggestions: " + e.getMessage());
        }
    }
}
