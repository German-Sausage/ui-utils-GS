/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ClientPlayNetworkHandlerAccessor.java
 */
package com.mrbreaknfix.ui_utils.mixin.accessor;

import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayNetworkHandler.class)
public interface ClientPlayNetworkHandlerAccessor {
    @Accessor("COMMAND_NODE_FACTORY")
    static CommandTreeS2CPacket.NodeFactory<ClientCommandSource> uiutils$getCommandNodeFactory() {
        return null;
    }

    @Accessor("chunkLoadDistance")
    int getChunkLoadDistance();

    @Accessor("messagePacker")
    MessageChain.Packer getMessagePacker();

    @Accessor("lastSeenMessagesCollector")
    LastSeenMessagesCollector getLastSeenMessagesCollector();

    @Accessor("combinedDynamicRegistries")
    DynamicRegistryManager.Immutable getCombinedDynamicRegistries();

    @Accessor("enabledFeatures")
    FeatureSet uiutils$getEnabledFeatures();

    @Accessor("combinedDynamicRegistries")
    DynamicRegistryManager.Immutable uiutils$getCombinedDynamicRegistries();
}
