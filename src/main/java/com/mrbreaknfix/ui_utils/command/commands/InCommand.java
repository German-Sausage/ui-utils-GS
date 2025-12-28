/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file InCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.time.Duration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

public class InCommand extends BaseCommand {

    private static final Timer scheduler = new Timer("InCommandScheduler", true);
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*([hms])");

    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (parsedArgs.size() < 2) {
            return CommandResult.of(false, "Usage: in <duration> <command...>");
        }
        String durationStr = parsedArgs.getFirst().toString();
        String commandToRun =
                parsedArgs.stream().skip(1).map(Object::toString).collect(Collectors.joining(" "));

        try {
            Duration duration = parseDuration(durationStr);
            if (duration.isZero() || duration.isNegative()) {
                return CommandResult.of(false, "Duration must be positive.");
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
                                                Text.literal("[IN] ")
                                                        .formatted(Formatting.GOLD)
                                                        .append(
                                                                Text.literal("Executing: ")
                                                                        .formatted(Formatting.GRAY))
                                                        .append(
                                                                Text.literal(commandToRun)
                                                                        .formatted(
                                                                                Formatting.WHITE));
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
                                            UiUtils.webSocketCommandServer
                                                    .broadcastToSubscribedClients(
                                                            resultMessage,
                                                            "scheduled_command_result");
                                        }
                                    });
                        }
                    };

            scheduler.schedule(task, delayInMillis);

            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("scheduled_command", commandToRun);
            jsonBody.addProperty("duration_string", durationStr);
            jsonBody.addProperty("delay_ms", delayInMillis);

            return CommandResult.of(
                    true,
                    "Command scheduled to run in " + formatDuration(duration) + ".",
                    null,
                    jsonBody);

        } catch (IllegalArgumentException e) {
            return CommandResult.of(false, e.getMessage());
        }
    }

    private Duration parseDuration(String input) throws IllegalArgumentException {
        long totalSeconds = 0;
        String sanitizedInput = input.toLowerCase().replaceAll("\\s+", "");
        Matcher matcher = DURATION_PATTERN.matcher(sanitizedInput);
        if (!matcher.find()) {
            try {
                return Duration.ofSeconds(Long.parseLong(sanitizedInput));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Invalid duration format. Use numbers with h, m, s units (e.g., '2m30s') or just seconds (e.g., '150').");
            }
        }
        matcher.reset();
        int lastEnd = 0;
        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            switch (unit) {
                case "h" -> totalSeconds += value * 3600;
                case "m" -> totalSeconds += value * 60;
                case "s" -> totalSeconds += value;
            }
            lastEnd = matcher.end();
        }
        if (lastEnd != sanitizedInput.length()) {
            throw new IllegalArgumentException(
                    "Invalid characters in duration string: '" + sanitizedInput + "'");
        }
        return Duration.ofSeconds(totalSeconds);
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        if (hours > 0)
            return String.format("%d hours, %d minutes, and %d seconds", hours, minutes, seconds);
        if (minutes > 0) return String.format("%d minutes and %d seconds", minutes, seconds);
        return String.format("%d seconds", seconds);
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("in", "Schedules a command to run after a specific delay.")
                .then(
                        ArgumentNode.argument(
                                        "<duration>", "The delay (e.g., '1h30m', '5m', '90s').")
                                .then(
                                        ArgumentNode.nestedCommand(
                                                "<command...>",
                                                "The full command and its arguments to execute.")));
    }

    @Override
    public String manual() {
        return """
                NAME
                    in - Schedules a command to run after a specific delay.

                SYNOPSIS
                    in <duration> <command...>

                DESCRIPTION
                    Executes a given command after a specified duration has passed.
                    The duration is a string of numbers followed by units.

                SUPPORTED DURATION UNITS
                    h - hours
                    m - minutes
                    s - seconds
                    If no unit is specified, the number is treated as seconds.
                    Units can be combined in any order.

                EXAMPLES
                    # Send "Boo!" in 5 seconds
                    in 5s chat Boo!

                    # Release delayed packets in 1 minute and 30 seconds
                    in 1m30s mcfw release

                    # Send a command in 10 minutes (no unit defaults to seconds, so use 'm')
                    in 10m chat This is a delayed message.

                    # Send a command in 90 seconds (no unit)
                    in 90 chat 90 seconds have passed.
                """;
    }
}
