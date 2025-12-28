/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file DesyncCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.List;

import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;

import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class DesyncCommand extends BaseCommand {
    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (mc.player == null) {
            return CommandResult.of(false, "Player is not in a world.");
        }
        if (mc.getNetworkHandler() == null) {
            return CommandResult.of(false, "Player is not connected to a server.");
        }
        if (mc.player.currentScreenHandler == mc.player.playerScreenHandler) {
            return CommandResult.of(false, "No screen is open to desync from.");
        }

        int syncId = mc.player.currentScreenHandler.syncId;
        mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(syncId));

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("syncId", syncId);

        return CommandResult.of(
                true, "Sent CloseHandledScreenC2SPacket for syncId: " + syncId, jsonBody);
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("desync", "Desyncs the current screen from the server.");
    }

    @Override
    public String manual() {
        return """
                NAME
                    desync - Desync from the server

                DESCRIPTION
                    Closes the current screen handler on the server side by sending a
                    CloseHandledScreenC2SPacket, while keeping the screen open on the client side.
                    This can be useful for various exploits or debugging.
                """;
    }
}
