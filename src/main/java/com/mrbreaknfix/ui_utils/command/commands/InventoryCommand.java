/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file InventoryCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class InventoryCommand extends BaseCommand {

    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (mc.player == null || mc.interactionManager == null) {
            return CommandResult.of(false, "You must be in a world to use this command.");
        }

        if (parsedArgs.isEmpty()) {
            return openAutoInventory();
        }

        String mode = parsedArgs.getFirst().toString().toLowerCase();
        return switch (mode) {
            case "player" -> openPlayerInventory();
            case "riding" -> openRidingInventory();
            default ->
                    CommandResult.of(
                            false,
                            "Unknown subcommand. Use 'player' or 'riding'. See `man inventory`.");
        };
    }

    private CommandResult<?> openAutoInventory() {
        mc.execute(
                () -> {
                    if (mc.player.getVehicle() != null) {
                        mc.player.openRidingInventory();
                    } else {
                        mc.getTutorialManager().onInventoryOpened();
                        mc.setScreen(new InventoryScreen(mc.player));
                    }
                });
        return CommandResult.of(true, "Opening inventory (auto-detect)...");
    }

    private CommandResult<?> openPlayerInventory() {
        mc.execute(
                () -> {
                    mc.getTutorialManager().onInventoryOpened();
                    mc.setScreen(new InventoryScreen(mc.player));
                });
        return CommandResult.of(true, "Forcing player inventory open.");
    }

    private CommandResult<?> openRidingInventory() {
        // todo: see if this vehicle check can be removed
        if (mc.player.getVehicle() == null) {
            return CommandResult.of(false, "You are not riding a vehicle with an inventory.");
        }
        mc.execute(() -> mc.player.openRidingInventory());
        return CommandResult.of(true, "Attempting to open riding inventory.");
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal(
                        "inventory", "Opens the inventory screen with optional overrides.")
                .then(
                        ArgumentNode.literal(
                                "player", "Forces the standard player inventory to open."))
                .then(
                        ArgumentNode.literal(
                                "riding",
                                "Forces an attempt to open a riding inventory (e.g., horse)."));
    }

    @Override
    public String manual() {
        return """
                NAME
                    inventory - Opens the player or riding inventory screen.

                SYNOPSIS
                    inventory [subcommand]
                    inv [subcommand]

                DESCRIPTION
                    Programmatically opens an inventory screen, replicating the inventory hotkey.
                    By default, it auto-detects whether to open the player inventory or a
                    vehicle's inventory (like a horse or chest boat). Subcommands can override this.

                SUBCOMMANDS
                    (no subcommand)
                        Runs the default auto-detection logic. This is the default action for 'inv'.

                    player
                        Forces the standard player inventory screen to open, even if on a horse.

                    riding
                        Forces an attempt to open a riding inventory. Fails if not applicable.

                EXAMPLES
                    # Open inventory using default logic (player or vehicle)
                    inv

                    # Ensure your player inventory opens, even if on a horse
                    inventory player
                """;
    }
}
