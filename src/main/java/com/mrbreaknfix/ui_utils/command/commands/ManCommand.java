/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ManCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.List;

import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.command.*;

public class ManCommand extends BaseCommand {
    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (parsedArgs.isEmpty()) {
            return CommandResult.of(false, "Usage: man <command>");
        }

        String commandName = parsedArgs.getFirst().toString().toLowerCase();
        Command command = CommandSystem.commands.get(commandName);
        if (command != null && command.visible()) {
            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("command", commandName);
            jsonBody.addProperty("manual", command.manual());
            return CommandResult.of(true, command.manual(), jsonBody);
        } else {
            return CommandResult.of(false, "Unknown command: " + commandName);
        }
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        ArgumentNode root = ArgumentNode.literal("man", "Show the manual of a given command.");

        ArgumentNode[] commandNodes =
                CommandSystem.commands.values().stream()
                        .filter(Command::visible)
                        .map(
                                cmd ->
                                        ArgumentNode.literal(
                                                cmd.getClass()
                                                        .getSimpleName()
                                                        .toLowerCase()
                                                        .replace("command", ""),
                                                "Show the manual for the '"
                                                        + cmd.getClass()
                                                                .getSimpleName()
                                                                .toLowerCase()
                                                                .replace("command", "")
                                                        + "' command."))
                        .toArray(ArgumentNode[]::new);

        root.then(commandNodes);
        return root;
    }

    @Override
    public String manual() {
        return """
                NAME
                    man - Show the manual of a given command

                SYNOPSIS
                    man <command>

                DESCRIPTION
                    Show the manual of a given command.

                EXAMPLES
                    man help
                    man math
                """;
    }
}
