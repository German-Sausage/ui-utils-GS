/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file JoinServerCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.List;

import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;

import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.resource.language.I18n;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class JoinServerCommand extends BaseCommand {
    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (parsedArgs.isEmpty()) {
            return CommandResult.of(false, "No server IP provided.");
        }
        String ip = parsedArgs.getFirst().toString();
        mc.execute(
                () -> {
                    ServerInfo si =
                            new ServerInfo(
                                    I18n.translate("selectServer.defaultName"),
                                    ip,
                                    ServerInfo.ServerType.OTHER);
                    ConnectScreen.connect(
                            mc.currentScreen, mc, ServerAddress.parse(ip), si, false, null);
                });

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("ip", ip);
        return CommandResult.of(true, "Joining server: " + ip, jsonBody);
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("joinserver", "Join a Minecraft server.")
                .then(ArgumentNode.argument("<ip>", "The IP address of the server to join."));
    }

    @Override
    public String manual() {
        return """
                NAME
                    joinserver - Join a server

                SYNOPSIS
                    joinserver <ip>

                DESCRIPTION
                    Connects to the Minecraft server at the given IP address.

                EXAMPLES
                    joinserver 2b2t.org
                    joinserver play.hypixel.net
                """;
    }
}
