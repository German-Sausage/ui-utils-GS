/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file SyncSendCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;
import com.mrbreaknfix.ui_utils.pIIC.SyncExecutionCoordinator;
import com.mrbreaknfix.ui_utils.pIIC.server.InstanceInfo;

public class SyncSendCommand extends BaseCommand {

    private static final Gson GSON = new Gson();

    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (parsedArgs.isEmpty()) {
            return CommandResult.of(false, "Usage: syncsend <command...>");
        }

        if (UiUtils.piicClient == null || !UiUtils.piicClient.isOpen()) {
            return CommandResult.of(false, "Cannot syncsend: Not connected to the p.IIC manager.");
        }

        String commandToRun =
                parsedArgs.stream().map(Object::toString).collect(Collectors.joining(" "));

        List<String> allInstanceIds =
                UiUtils.piicClient.getInstanceCache().stream()
                        .map(InstanceInfo::id)
                        .collect(Collectors.toList());

        if (allInstanceIds.size() <= 1) {
            return CommandResult.of(
                    true, "No other instances found to synchronize with. Command not sent.");
        }

        UiUtils.LOGGER.info("--- Initiating Synchronized Command ---");
        UiUtils.LOGGER.info("Command: '{}'", commandToRun);
        UiUtils.LOGGER.info("Target Instances ({}): {}", allInstanceIds.size(), allInstanceIds);

        SyncExecutionCoordinator.start(commandToRun, allInstanceIds);

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("command", commandToRun);
        jsonBody.add("target_ids", GSON.toJsonTree(allInstanceIds));

        return CommandResult.of(
                true, "Initiating synchronized execution for: " + commandToRun, jsonBody);
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal(
                        "syncsend",
                        "Sends a command to be executed synchronously across all instances.")
                .then(ArgumentNode.nestedCommand("<command...>", "The command to synchronize."));
    }

    @Override
    public String manual() {
        return """
                NAME
                    syncsend - Synchronously execute a command across all connected instances.

                SYNOPSIS
                    syncsend <command...>

                DESCRIPTION
                    Initiates a multi-phase protocol to execute a command on all connected
                    game instances (including this one) at as close to the same moment
                    as possible.

                    This is useful for coordinated actions where timing is important. The
                    command uses a prepare/ready/execute sequence to ensure all clients
                    are ready before the final "go" signal is sent.

                    After execution, each client reports back its timing data, which is
                    logged by the initiating client, allowing for performance analysis
                    and timeline visualization.

                EXAMPLES
                    # Have all clients send the same chat message at once.
                    syncsend chat Hello, synchronized world!

                    # Have all clients perform a complex inventory click simultaneously.
                    syncsend click 0 9 QUICK_MOVE

                    # Close all open GUIs at the same time.
                    syncsend close
                """;
    }
}
