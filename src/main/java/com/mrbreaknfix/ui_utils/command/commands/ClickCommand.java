/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ClickCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;
import com.mrbreaknfix.ui_utils.utils.UIActions;

import net.minecraft.screen.slot.SlotActionType;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class ClickCommand extends BaseCommand {

    @Override
    @SuppressWarnings("unchecked")
    protected CommandResult<?> executeParsed(List<Object> rawArgs) {
        if (mc.player == null || mc.player.currentScreenHandler == null)
            return CommandResult.of(
                    false, "You must have an active screen handler to use this command.");
        ParsedInput input = parseNamedArguments((List<String>) (List<?>) rawArgs);
        List<String> positional = input.positional();
        if (positional.size() < 3) return CommandResult.of(false, getUsage());
        try {
            short slot = Short.parseShort(positional.get(0));
            byte button = Byte.parseByte(positional.get(1));
            SlotActionType action = SlotActionType.valueOf(positional.get(2).toUpperCase());
            int syncId =
                    Integer.parseInt(
                            input.named()
                                    .getOrDefault(
                                            "syncId",
                                            String.valueOf(mc.player.currentScreenHandler.syncId)));
            int revision =
                    Integer.parseInt(
                            input.named()
                                    .getOrDefault(
                                            "revision",
                                            String.valueOf(
                                                    mc.player.currentScreenHandler.getRevision())));
            int times = Integer.parseInt(input.named().getOrDefault("times", "1"));
            if (times <= 0 || times > 100)
                return CommandResult.of(false, "Times must be between 1 and 100.");
            UIActions.sendClickSlotPacket(syncId, revision, slot, button, action, times);
            String message =
                    String.format(
                            "Sent ClickSlot packet %d time(s): slot=%d, button=%d, action=%s, syncId=%d, revision=%d",
                            times, slot, button, action, syncId, revision);
            return CommandResult.of(true, message);
        } catch (NumberFormatException e) {
            return CommandResult.of(false, "Invalid number provided for an argument.");
        } catch (IllegalArgumentException e) {
            return CommandResult.of(false, "Invalid SlotActionType. " + getUsage());
        }
    }

    @Override
    public Map<String, ArgumentNode[]> getFlagPools() {
        ArgumentNode[] flags =
                new ArgumentNode[] {
                    ArgumentNode.literal("--syncId", "Override the auto-detected syncId.")
                            .then(ArgumentNode.argument("<id>", "The sync ID.")),
                    ArgumentNode.literal("--revision", "Override the auto-detected revision.")
                            .then(ArgumentNode.argument("<rev>", "The revision number.")),
                    ArgumentNode.literal("--times", "Set the number of times to send.")
                            .then(ArgumentNode.argument("<count>", "The loop count."))
                };
        return Map.of("clickFlags", flags);
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        ArgumentNode[] actionNodes =
                Arrays.stream(SlotActionType.values())
                        .map(
                                action ->
                                        ArgumentNode.literal(
                                                        action.name(),
                                                        "The " + action.name() + " slot action.")
                                                .then(
                                                        ArgumentNode.flagSet(
                                                                "clickFlags",
                                                                "Optional flags for the click command.")))
                        .toArray(ArgumentNode[]::new);

        return ArgumentNode.literal("click", "Simulates a slot click packet.")
                .then(
                        ArgumentNode.argument("<slot>", "The slot ID to click.")
                                .then(
                                        ArgumentNode.argument(
                                                        "<button>",
                                                        "The mouse button (0=left, 1=right).")
                                                .then(actionNodes)));
    }

    private String getUsage() {
        String validActions =
                Arrays.stream(SlotActionType.values())
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));
        return "Usage: click <slot> <button> <action> [--syncId <id>] [--revision <rev>] [--times <count>]\nValid actions: "
                + validActions;
    }

    @Override
    public String manual() {
        return """
                NAME
                    click - Sends a ClickSlotC2SPacket to the server.

                SYNOPSIS
                    click <slot> <button> <action> [options]

                DESCRIPTION
                    Creates an inventory click packet to interact with a slot in the current screen.
                OPTIONS
                    --syncId <id>
                        Manually specify the screen syncId.
                    --revision <rev>
                        Manually specify the screen revision number.
                    --times <count>
                        Specify how many times to send the packet.

                EXAMPLES
                    # Basic click
                    click 0 0 PICKUP

                    # Specify loop count, then get suggestions for other flags
                    click 1 0 THROW --times 3

                    # Specify flags in any order
                    click 2 0 QUICK_MOVE --times 5 --syncId 123
                """;
    }
}
