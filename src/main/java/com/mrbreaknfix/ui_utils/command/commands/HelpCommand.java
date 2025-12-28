/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file HelpCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.command.*;

public class HelpCommand extends BaseCommand {

    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (parsedArgs.isEmpty()) {
            List<Map.Entry<String, Command>> commandList =
                    CommandSystem.commands.entrySet().stream()
                            .filter(entry -> entry.getValue().visible())
                            .sorted(Map.Entry.comparingByKey())
                            .toList();

            int maxNameLength =
                    commandList.stream()
                            .map(Map.Entry::getKey)
                            .mapToInt(String::length)
                            .max()
                            .orElse(0);

            String format = "%-" + (maxNameLength + 2) + "s %s";

            StringBuilder sb = new StringBuilder("Available Commands:\n");
            JsonArray commandsJson = new JsonArray();

            for (Map.Entry<String, Command> entry : commandList) {
                String name = entry.getKey();
                Command cmd = entry.getValue();
                String description = "No description available.";

                ArgumentNode schema = cmd.getArgumentSchema();
                if (schema != null && schema.getDescription() != null) {
                    description = schema.getDescription();
                }

                sb.append(String.format(format, name, description)).append("\n");

                JsonObject cmdJson = new JsonObject();
                cmdJson.addProperty("name", name);
                cmdJson.addProperty("description", description);
                commandsJson.add(cmdJson);
            }

            sb.append("\nType 'man <command>' for more details on a specific command.");

            JsonObject jsonBody = new JsonObject();
            jsonBody.add("commands", commandsJson);

            return CommandResult.of(true, sb.toString().trim(), jsonBody);

        } else {
            String commandName = parsedArgs.getFirst().toString().toLowerCase();
            Command command = CommandSystem.commands.get(commandName);
            if (command != null) {
                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("command", commandName);
                jsonBody.addProperty("manual", command.manual());
                return CommandResult.of(true, command.manual(), jsonBody);
            } else {
                return CommandResult.of(false, "Unknown command: " + commandName);
            }
        }
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        ArgumentNode root =
                ArgumentNode.literal("help", "Lists available commands or shows details for one.");

        ArgumentNode[] commandNodes =
                CommandSystem.commands.keySet().stream()
                        .sorted()
                        .map(
                                cmdName ->
                                        ArgumentNode.literal(
                                                cmdName,
                                                "Get help for the '" + cmdName + "' command."))
                        .toArray(ArgumentNode[]::new);

        root.then(commandNodes);
        return root;
    }

    @Override
    public String manual() {
        return """
                NAME
                    help - List available commands or show manual for a specific command

                SYNOPSIS
                    help [command]

                DESCRIPTION
                    When run without arguments, `help` lists all available commands along with a
                    brief description of what they do.

                    When a command name is provided, it acts as an alias for `man <command>`,
                    showing the full manual for that specific command.

                EXAMPLES
                    help
                    help mcfw
                """;
    }
}
