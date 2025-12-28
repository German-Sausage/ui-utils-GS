/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file AtCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;
import com.mrbreaknfix.ui_utils.command.CommandSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AtCommand extends BaseCommand {

    private static final Timer scheduler = new Timer("AtCommandScheduler", true);
    private static final List<DateTimeFormatter> TIME_FORMATTERS =
            Arrays.asList(
                    new DateTimeFormatterBuilder().appendPattern("h:mm:ssa").toFormatter(Locale.US),
                    new DateTimeFormatterBuilder().appendPattern("h:mma").toFormatter(Locale.US),
                    DateTimeFormatter.ofPattern("HH:mm:ss"),
                    DateTimeFormatter.ofPattern("HH:mm"));

    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (parsedArgs.size() < 2) {
            return CommandResult.of(false, "Usage: at <time> <command...>");
        }
        String timeStr = parsedArgs.getFirst().toString();
        String commandToRun =
                parsedArgs.stream().skip(1).map(Object::toString).collect(Collectors.joining(" "));

        LocalTime targetTime = null;
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                targetTime = LocalTime.parse(timeStr.toUpperCase(), formatter);
                break;
            } catch (DateTimeParseException e) {
            }
        }
        if (targetTime == null) {
            return CommandResult.of(
                    false,
                    "Invalid time format. Use formats like '3:09:30PM', '15:09', or '10:30am'.");
        }

        LocalTime now = LocalTime.now();
        Duration duration = Duration.between(now, targetTime);
        if (duration.isNegative()) {
            duration = duration.plusDays(1);
        }
        long delayInMillis = duration.toMillis();

        TimerTask task =
                new TimerTask() {
                    @Override
                    public void run() {
                        MinecraftClient mc = MinecraftClient.getInstance();
                        mc.execute(
                                () -> {
                                    Text feedback =
                                            Text.literal("[AT] ")
                                                    .formatted(Formatting.GOLD)
                                                    .append(
                                                            Text.literal("Executing: ")
                                                                    .formatted(Formatting.GRAY))
                                                    .append(
                                                            Text.literal(commandToRun)
                                                                    .formatted(Formatting.WHITE));
                                    if (mc.player != null) {
                                        mc.player.sendMessage(feedback, false);
                                    }

                                    List<CommandResult<?>> results =
                                            CommandSystem.executeCommand(commandToRun);
                                    String resultMessage =
                                            results.stream()
                                                    .map(CommandResult::message)
                                                    .collect(Collectors.joining("\n"));

                                    if (UiUtils.webSocketCommandServer != null) {
                                        UiUtils.webSocketCommandServer.broadcastToSubscribedClients(
                                                resultMessage, "scheduled_command_result");
                                    }
                                });
                    }
                };

        scheduler.schedule(task, delayInMillis);

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("scheduled_command", commandToRun);
        jsonBody.addProperty("target_time", targetTime.toString());
        jsonBody.addProperty("delay_ms", delayInMillis);

        String friendlyDuration = formatDuration(duration);
        return CommandResult.of(
                true,
                "Command scheduled to run in " + friendlyDuration + " (at " + targetTime + ").",
                null,
                jsonBody);
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%d hours, %d minutes, and %d seconds", hours, minutes, seconds);
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("at", "Schedules a command to run at a specific time.")
                .then(
                        ArgumentNode.argument(
                                        "<time>",
                                        "The time to run the command (e.g., '3:09PM', '15:09:30').")
                                .then(
                                        ArgumentNode.nestedCommand(
                                                "<command...>",
                                                "The full command and its arguments to execute.")));
    }

    @Override
    public String manual() {
        return """
                NAME
                    at - Schedules a command to run at a specific time.

                SYNOPSIS
                    at <time> <command...>

                DESCRIPTION
                    Executes a given command at a specified time of day. If the time has
                    already passed for the current day, the command will be scheduled for
                    the next day at that time.

                SUPPORTED TIME FORMATS
                    - 3:09:30PM (12-hour with seconds and AM/PM)
                    - 3:09pm (12-hour without seconds)
                    - 15:09:30 (24-hour with seconds)
                    - 15:09 (24-hour without seconds)
                    (AM/PM is case-insensitive)

                EXAMPLES
                    # Send "Hello" in chat at 5:30 PM
                    at 5:30pm chat Hello there!

                    # Run a loop of commands at 3:15 AM
                    at 3:15AM loop 10 click 0 0 PICKUP

                    # Release delayed packets at 22:00 (10 PM)
                    at 22:00 mcfw release
                """;
    }
}
