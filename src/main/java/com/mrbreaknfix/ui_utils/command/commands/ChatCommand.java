/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ChatCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.List;

import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class ChatCommand extends BaseCommand {

    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (parsedArgs.isEmpty()) {
            return CommandResult.of(false, "No message provided.");
        }
        if (mc == null || mc.player == null || mc.getNetworkHandler() == null) {
            return CommandResult.of(false, "Cannot send chat: not in a world.");
        }

        String messageStr =
                String.join(
                        " ",
                        parsedArgs.stream().map(Object::toString).toArray(CharSequence[]::new));
        boolean isCommand = messageStr.startsWith("/");

        if (isCommand) {
            mc.player.networkHandler.sendChatCommand(messageStr.substring(1));
        } else {
            mc.player.networkHandler.sendChatMessage(messageStr);
        }

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("message", messageStr);
        jsonBody.addProperty("isCommand", isCommand);

        return CommandResult.of(true, "Sent: " + messageStr, jsonBody);
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("chat", "Send a chat message or command to the server.")
                .then(ArgumentNode.argument("<message...>", "The message or command to send."));
    }

    @Override
    public String manual() {
        return """
                NAME
                    chat - Send a chat message to the server

                SYNOPSIS
                    chat <message>

                DESCRIPTION
                    Sends a chat message to the server. If the message starts with a '/', it will be sent as a command.

                EXAMPLES
                    chat Hello, world!
                    chat /help
                """;
    }
}
