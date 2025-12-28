/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file SchemaDumpCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.command.*;

public class SchemaDumpCommand extends BaseCommand {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        ArgumentNode rootSchema = ArgumentNode.literal("/", "Root of all commands.");
        Map<String, ArgumentNode[]> allFlagPools = new HashMap<>();

        for (Map.Entry<String, Command> entry : CommandSystem.commands.entrySet()) {
            Command cmd = entry.getValue();

            if (!cmd.visible()) {
                continue;
            }

            ArgumentNode schema = cmd.getArgumentSchema();
            if (schema != null) {
                rootSchema.then(schema);
            } else {
                String desc =
                        cmd.manual().lines().findFirst().orElse("").replace("NAME", "").trim();
                rootSchema.then(ArgumentNode.literal(entry.getKey(), desc));
            }

            Map<String, ArgumentNode[]> commandPools = cmd.getFlagPools();
            if (commandPools != null) {
                allFlagPools.putAll(commandPools);
            }
        }

        JsonObject finalJson = new JsonObject();
        finalJson.add("schema", GSON.toJsonTree(rootSchema));
        finalJson.add("flagPools", GSON.toJsonTree(allFlagPools));

        return CommandResult.of(true, "Schema for all visible (public) commands.", finalJson);
    }

    @Override
    public String manual() {
        return """
                NAME
                    schemadump - Dumps the entire public command structure as JSON.

                DESCRIPTION
                    Provides the full command and argument tree for all visible commands,
                    allowing for efficient client-side autocompletion and help generation.

                SYNOPSIS
                    schemadump
                """;
    }

    @Override
    public boolean visible() {
        return false;
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("schemadump", "Dumps the public command structure as JSON.");
    }
}
