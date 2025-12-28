/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ButtonCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.List;
import java.util.Map;

import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;
import com.mrbreaknfix.ui_utils.utils.UIActions;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class ButtonCommand extends BaseCommand {

    @Override
    @SuppressWarnings("unchecked")
    protected CommandResult<?> executeParsed(List<Object> rawArgs) {
        if (mc.player == null || mc.player.currentScreenHandler == null)
            return CommandResult.of(false, "You must be in a screen to use this command.");
        ParsedInput input = parseNamedArguments((List<String>) (List<?>) rawArgs);
        List<String> positional = input.positional();
        if (positional.isEmpty())
            return CommandResult.of(
                    false, "Usage: button <buttonId> [--syncId <id>] [--times <count>]");
        try {
            int buttonId = Integer.parseInt(positional.getFirst());
            int syncId =
                    Integer.parseInt(
                            input.named()
                                    .getOrDefault(
                                            "syncId",
                                            String.valueOf(mc.player.currentScreenHandler.syncId)));
            int times = Integer.parseInt(input.named().getOrDefault("times", "1"));
            if (times <= 0 || times > 100)
                return CommandResult.of(false, "Times must be between 1 and 100.");
            UIActions.sendClickButtonPacket(syncId, buttonId, times);
            String message =
                    String.format(
                            "Sent ClickButton packet %d time(s): buttonId=%d, syncId=%d",
                            times, buttonId, syncId);
            return CommandResult.of(true, message);
        } catch (NumberFormatException e) {
            return CommandResult.of(false, "Invalid number provided for an argument.");
        }
    }

    @Override
    public Map<String, ArgumentNode[]> getFlagPools() {
        ArgumentNode[] flags =
                new ArgumentNode[] {
                    ArgumentNode.literal("--syncId", "Override the auto-detected syncId.")
                            .then(ArgumentNode.argument("<id>", "The sync ID.")),
                    ArgumentNode.literal("--times", "Set the number of times to send.")
                            .then(ArgumentNode.argument("<count>", "The loop count."))
                };
        return Map.of("buttonFlags", flags);
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("button", "Simulates a screen button click packet.")
                .then(
                        ArgumentNode.argument("<buttonId>", "The ID of the button to press.")
                                .then(
                                        ArgumentNode.flagSet(
                                                "buttonFlags",
                                                "Optional flags for the button command.")));
    }

    @Override
    public String manual() {
        return """
                NAME
                    button - Sends a ButtonClickC2SPacket to the server.

                SYNOPSIS
                    button <buttonId> [options]

                DESCRIPTION
                    Creates a button click, e.g. pressing a button in a handled GUI.

                OPTIONS
                    --syncId <id>
                        Manually specify the screen syncId.
                    --times <count>
                        Specify how many times to send the packet.

                EXAMPLES
                    # In an enchanting table, enchants an item with level 3.
                    button 2
                """;
    }
}
