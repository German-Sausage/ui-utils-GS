/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file LoopCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;
import com.mrbreaknfix.ui_utils.command.CommandSystem;

public class LoopCommand extends BaseCommand {

    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (parsedArgs.size() < 2) {
            return CommandResult.of(false, "Usage: loop <times> <command...>");
        }
        int times;
        try {
            times = Integer.parseInt((String) parsedArgs.getFirst());
        } catch (NumberFormatException e) {
            return CommandResult.of(false, "Invalid number of times specified.");
        }
        String commandToRun =
                parsedArgs.stream().skip(1).map(Object::toString).collect(Collectors.joining(" "));
        if (commandToRun.isEmpty()) {
            return CommandResult.of(false, "No command provided to loop.");
        }

        StringBuilder resultsText = new StringBuilder();
        JsonArray resultsJson = new JsonArray();
        boolean overallSuccess = true;

        for (int i = 0; i < times; i++) {
            List<CommandResult<?>> loopResults = CommandSystem.executeCommand(commandToRun);
            String singleResult =
                    loopResults.stream()
                            .map(CommandResult::toString)
                            .collect(Collectors.joining("\n"));
            resultsText
                    .append("Loop ")
                    .append(i + 1)
                    .append(": ")
                    .append(singleResult)
                    .append("\n");

            JsonObject iterationResult = new JsonObject();
            iterationResult.addProperty("iteration", i + 1);
            iterationResult.addProperty("result_text", singleResult);
            iterationResult.addProperty(
                    "success", loopResults.stream().allMatch(CommandResult::success));
            resultsJson.add(iterationResult);

            if (loopResults.stream().anyMatch(r -> !r.success())) {
                resultsText.append("Loop terminated due to failure.");
                overallSuccess = false;
                break;
            }
        }

        JsonObject finalJsonBody = new JsonObject();
        finalJsonBody.addProperty("command_looped", commandToRun);
        finalJsonBody.addProperty("iterations_requested", times);
        finalJsonBody.addProperty("iterations_completed", resultsJson.size());
        finalJsonBody.add("results", resultsJson);

        return CommandResult.of(overallSuccess, resultsText.toString().trim(), null, finalJsonBody);
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("loop", "Execute a command multiple times.")
                .then(
                        ArgumentNode.argument("<times>", "How many times to execute.")
                                .then(
                                        ArgumentNode.nestedCommand(
                                                "<command...>",
                                                "The command and arguments to loop.")));
    }

    @Override
    public String manual() {
        return """
                NAME
                    loop - Execute a command multiple times.

                SYNOPSIS
                    loop <times> <command...>

                DESCRIPTION
                    Runs the specified command and its arguments a given number of times.

                EXAMPLES
                    loop 5 chat Hello!
                    loop 10 click 0 0 QUICK_MOVE
                """;
    }
}
