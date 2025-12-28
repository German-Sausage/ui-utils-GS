/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ScreenCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.ScreenSaver;
import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;
import com.mrbreaknfix.ui_utils.utils.ScreenCommandSlotManager;
import com.mrbreaknfix.ui_utils.utils.ScreenHistory;
import com.mrbreaknfix.ui_utils.utils.UIActions;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class ScreenCommand extends BaseCommand {

    private static final Timer timer = new Timer("ScreenCommandTimer", true);
    private static final String DEFAULT_SLOT_NAME = "default";

    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (parsedArgs.isEmpty()) {
            return handleInfo(parsedArgs);
        }
        String subCommand = parsedArgs.getFirst().toString().toLowerCase();
        return switch (subCommand) {
            case "save" -> handleSave(parsedArgs);
            case "desync" -> handleDesync(parsedArgs);
            case "close", "cwop" -> handleCwop(parsedArgs);
            case "load" -> handleLoad(parsedArgs);
            case "list" -> handleList();
            case "info" -> handleInfo(parsedArgs);
            case "remove", "rm" -> handleRemove(parsedArgs);
            case "slot" -> handleSlot(parsedArgs.subList(1, parsedArgs.size()));
            case "reopen" -> handleReopen();
            case "back" -> handleBack();
            //            case "forward" -> handleForward();
            case "history" -> handleHistory(parsedArgs.subList(1, parsedArgs.size()));
            default -> CommandResult.of(false, "Unknown subcommand. See `man screen` for help.");
        };
    }

    private CommandResult<?> handleDesync(List<Object> args) {
        if (mc.player == null) {
            return CommandResult.of(false, "Player is not in a world.");
        }
        if (mc.getNetworkHandler() == null) {
            return CommandResult.of(false, "Player is not connected to a server.");
        }
        if (mc.player.currentScreenHandler == mc.player.playerScreenHandler) {
            return CommandResult.of(false, "No screen is open to desync from.");
        }

        int syncId = mc.player.currentScreenHandler.syncId;
        mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(syncId));

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("syncId", syncId);

        return CommandResult.of(
                true, "Sent CloseHandledScreenC2SPacket for syncId: " + syncId, jsonBody);
    }

    private CommandResult<?> handleCwop(List<Object> args) {
        mc.execute(UIActions::CWoP);
        return CommandResult.of(true, "Closed current screen without sending a packet.");
    }

    private CommandResult<?> handleSave(List<Object> args) {
        String slotName = (args.size() > 1) ? args.get(1).toString() : DEFAULT_SLOT_NAME;
        try {
            ScreenSaver.saveScreen(slotName);
        } catch (Exception e) {
            return CommandResult.of(false, "Error saving screen: " + e.getMessage());
        }
        return CommandResult.of(true, "Current screen state saved to slot: " + slotName);
    }

    private CommandResult<?> handleLoad(List<Object> args) {
        if (args.size() < 2) {
            return loadFromSlot(DEFAULT_SLOT_NAME);
        }
        String target = args.get(1).toString();
        if ("history".equalsIgnoreCase(target)) {
            if (args.size() < 3)
                return CommandResult.of(false, "Usage: screen load history <index>");
            try {
                int index = Integer.parseInt(args.get(2).toString());
                ScreenHistory.loadByDisplayIndex(index);
                return CommandResult.of(true, "Loading screen from history at index " + index);
            } catch (NumberFormatException e) {
                return CommandResult.of(false, "Invalid history index: not a number.");
            } catch (IndexOutOfBoundsException e) {
                return CommandResult.of(false, e.getMessage());
            }
        } else {
            return loadFromSlot(target);
        }
    }

    private CommandResult<?> loadFromSlot(String slotName) {
        try {
            ScreenSaver.loadScreen(slotName);
        } catch (Exception e) {
            return CommandResult.of(false, "Error loading screen: " + e.getMessage());
        }
        return CommandResult.of(true, "Loading screen from slot: " + slotName);
    }

    private CommandResult<?> handleRemove(List<Object> args) {
        if (args.size() < 2) return CommandResult.of(false, "Usage: screen remove <slot_name>");
        String slotName = args.get(1).toString();
        if (ScreenSaver.removeScreen(slotName)) {
            return CommandResult.of(true, "Removed screen slot: " + slotName);
        } else {
            return CommandResult.of(false, "Screen slot not found: " + slotName);
        }
    }

    private CommandResult<?> handleList() {
        List<String> slots = ScreenSaver.savedScreens.keySet().stream().sorted().toList();
        if (slots.isEmpty()) {
            return CommandResult.of(true, "No screens have been saved yet.");
        }
        return CommandResult.of(true, "Saved screens: " + String.join(" ", slots));
    }

    private CommandResult<?> handleInfo(List<Object> args) {
        if (args.size() > 1) {
            String slotName = args.get(1).toString();
            String info = ScreenSaver.getInfo(slotName);
            if (info == null) return CommandResult.of(false, "Screen slot not found: " + slotName);
            return CommandResult.of(true, "Info for saved slot '" + slotName + "': " + info);
        }
        if (mc.currentScreen == null) return CommandResult.of(true, "No screen is currently open.");
        StringBuilder sb = new StringBuilder();
        sb.append("Current Screen Info:\n");
        sb.append("  Class: ").append(mc.currentScreen.getClass().getName()).append("\n");
        sb.append("  Title: ").append(mc.currentScreen.getTitle().getString());
        if (mc.currentScreen instanceof HandledScreen) {
            ScreenHandler handler = ((HandledScreen<?>) mc.currentScreen).getScreenHandler();
            sb.append("\n  Type: Container Screen (HandledScreen)");
            sb.append("\n  Sync ID: ").append(handler.syncId);
            sb.append("\n  Slot Count: ").append(handler.slots.size());
        }
        return CommandResult.of(true, sb.toString());
    }

    private CommandResult<?> handleReopen() {
        if (mc.currentScreen == null) {
            return CommandResult.of(false, "No screen to reopen.");
        }
        Screen current = mc.currentScreen;
        mc.execute(
                () -> {
                    mc.setScreen(null);
                    mc.setScreen(current);
                });
        return CommandResult.of(true, "Reopened current screen.");
    }

    private CommandResult<?> handleBack() {
        if (!ScreenHistory.canGoBack()) {
            return CommandResult.of(false, "Cannot go back further in history.");
        }
        ScreenHistory.back();
        return CommandResult.of(true, "Navigating back...");
    }

    private CommandResult<?> handleForward() {
        if (!ScreenHistory.canGoForward()) {
            return CommandResult.of(false, "Cannot go forward in history.");
        }
        ScreenHistory.forward();
        return CommandResult.of(true, "Navigating forward...");
    }

    private CommandResult<?> handleHistory(List<Object> args) {
        int count = 10;
        final int maxCount = 50;

        if (!args.isEmpty()) {
            try {
                count = Integer.parseInt(args.getFirst().toString());
                if (count > maxCount) {
                    count = maxCount;
                }
            } catch (NumberFormatException e) {
                return CommandResult.of(false, "Invalid count: not a number.");
            }
        }

        List<Screen> historyList = ScreenHistory.getDisplayHistory();
        int displayCurrentIndex = ScreenHistory.getDisplayCurrentIndex();

        if (historyList.isEmpty()) {
            return CommandResult.of(true, "Screen history is empty.");
        }

        List<Screen> subList = historyList.subList(0, Math.min(count, historyList.size()));
        StringBuilder sb =
                new StringBuilder(
                        String.format(
                                "Screen History (showing last %d of %d, newest first):\n",
                                subList.size(), historyList.size()));

        for (int i = 0; i < subList.size(); i++) {
            Screen screen = subList.get(i);
            String prefix = (i == displayCurrentIndex) ? " ->" : "   ";
            String screenName = (screen == null) ? "null" : screen.getClass().getSimpleName();
            sb.append(String.format("%s [%d] %s", prefix, i, screenName));
            if (screen instanceof HandledScreen) {
                ScreenHandler handler = ((HandledScreen<?>) screen).getScreenHandler();
                String title = screen.getTitle().getString();
                if (!title.isEmpty()) sb.append(String.format(": '%s'", title));
                sb.append(String.format(" (Sync ID: %d)", handler.syncId));
            } else if (screen != null) {
                String title = screen.getTitle().getString();
                if (!title.isEmpty()) sb.append(String.format(": '%s'", title));
            }
            sb.append("\n");
        }
        return CommandResult.of(true, sb.toString().trim());
    }

    private CommandResult<?> handleSlot(List<Object> args) {
        // todo: too broad requiring screen for screen click
        if (!(mc.currentScreen instanceof HandledScreen)) {
            return CommandResult.of(
                    false, "'screen slot' can only be used inside a container screen.");
        }
        if (args.isEmpty()) {
            return CommandResult.of(
                    false,
                    "No slot action specified. Use: click, info, list, highlight, show-ids.");
        }
        String slotAction = args.getFirst().toString().toLowerCase();
        ScreenHandler handler = ((HandledScreen<?>) mc.currentScreen).getScreenHandler();
        return switch (slotAction) {
            case "click" -> handleSlotClick(args, handler);
            case "info" -> handleSlotInfo(args, handler);
            case "list" -> handleSlotList(handler);
            case "highlight" -> handleSlotHighlight(args);
            case "show-ids" -> handleSlotShowIds(args);
            default ->
                    CommandResult.of(
                            false,
                            "Unknown slot action. Use: click, info, list, highlight, show-ids.");
        };
    }

    private CommandResult<?> handleSlotClick(List<Object> args, ScreenHandler handler) {
        if (args.size() < 4)
            return CommandResult.of(false, "Usage: screen slot click <slotId> <button> <action>");
        try {
            short slotId = Short.parseShort(args.get(1).toString());
            byte button = Byte.parseByte(args.get(2).toString());
            SlotActionType action = SlotActionType.valueOf(args.get(3).toString().toUpperCase());
            UIActions.sendClickSlotPacket(
                    handler.syncId, handler.getRevision(), slotId, button, action, 1);
            return CommandResult.of(
                    true,
                    String.format(
                            "Executed click on slot %d: button=%d, action=%s",
                            slotId, button, action));
        } catch (Exception e) {
            return CommandResult.of(false, "Invalid arguments for slot click. " + e.getMessage());
        }
    }

    private CommandResult<?> handleSlotInfo(List<Object> args, ScreenHandler handler) {
        if (args.size() < 2) return CommandResult.of(false, "Usage: screen slot info <slotId>");
        try {
            int slotId = Integer.parseInt(args.get(1).toString());
            if (slotId < 0 || slotId >= handler.slots.size())
                return CommandResult.of(false, "Invalid slot ID: " + slotId);
            Slot slot = handler.getSlot(slotId);
            ItemStack stack = slot.getStack();
            if (stack.isEmpty())
                return CommandResult.of(true, String.format("Slot %d is empty.", slotId));
            StringBuilder sb = new StringBuilder();
            sb.append(
                    String.format(
                            "Slot %d contains: %s (Count: %d)",
                            slotId, stack.getName().getString(), stack.getCount()));
            ComponentMap components = stack.getComponents();
            if (!components.isEmpty()) sb.append("\nComponents: ").append(components);
            return CommandResult.of(true, sb.toString());
        } catch (NumberFormatException e) {
            return CommandResult.of(false, "Invalid slot ID.");
        }
    }

    private CommandResult<?> handleSlotList(ScreenHandler handler) {
        String slotList =
                handler.slots.stream()
                        .map(
                                slot -> {
                                    ItemStack stack = slot.getStack();
                                    String content =
                                            stack.isEmpty()
                                                    ? "Empty"
                                                    : String.format(
                                                            "%s x%d",
                                                            stack.getName().getString(),
                                                            stack.getCount());
                                    return String.format("  ID %d: %s", slot.id, content);
                                })
                        .collect(Collectors.joining("\n"));
        return CommandResult.of(true, "Current Screen Slots:\n" + slotList);
    }

    private CommandResult<?> handleSlotHighlight(List<Object> args) {
        if (args.size() < 2)
            return CommandResult.of(false, "Usage: screen slot highlight <slotId> [duration_ms]");
        try {
            int slotId = Integer.parseInt(args.get(1).toString());
            ScreenCommandSlotManager.setHighlightedSlotId(slotId);
            ScreenCommandSlotManager.setShouldRenderHighlight(true);
            long duration = (args.size() > 2) ? Long.parseLong(args.get(2).toString()) : 5000L;
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            if (ScreenCommandSlotManager.getHighlightedSlotId() == slotId) {
                                ScreenCommandSlotManager.setShouldRenderHighlight(false);
                            }
                        }
                    },
                    duration);
            return CommandResult.of(
                    true, String.format("Highlighting slot %d for %d ms.", slotId, duration));
        } catch (NumberFormatException e) {
            return CommandResult.of(false, "Invalid slot ID or duration.");
        }
    }

    private CommandResult<?> handleSlotShowIds(List<Object> args) {
        if (args.size() < 2)
            return CommandResult.of(false, "Usage: screen slot show-ids <on|off|toggle>");
        String mode = args.get(1).toString().toLowerCase();
        switch (mode) {
            case "on" -> ScreenCommandSlotManager.setShouldDrawIds(true);
            case "off" -> ScreenCommandSlotManager.setShouldDrawIds(false);
            case "toggle" ->
                    ScreenCommandSlotManager.setShouldDrawIds(
                            !ScreenCommandSlotManager.shouldDrawIds());
            default -> {
                return CommandResult.of(false, "Invalid mode. Use 'on', 'off', or 'toggle'.");
            }
        }
        return CommandResult.of(
                true,
                "Slot IDs are now " + (ScreenCommandSlotManager.shouldDrawIds() ? "ON." : "OFF."));
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        ArgumentNode[] savedSlotNodes =
                ScreenSaver.savedScreens.keySet().stream()
                        .sorted()
                        .map(slotName -> ArgumentNode.literal(slotName, "A saved screen slot."))
                        .toArray(ArgumentNode[]::new);

        ArgumentNode slotIdNode = ArgumentNode.argument("<slotId>", "The ID of the target slot.");
        ArgumentNode[] actionTypeNodes =
                Arrays.stream(SlotActionType.values())
                        .map(type -> ArgumentNode.literal(type.name(), "The click action type."))
                        .toArray(ArgumentNode[]::new);

        ArgumentNode loadNode =
                ArgumentNode.literal("load", "Loads a screen from a saved slot or history.")
                        .then(
                                ArgumentNode.argument(
                                        "[slot_name]",
                                        "Optional slot name. Defaults to 'default'."))
                        .then(
                                ArgumentNode.literal(
                                                "history", "Load a screen from navigation history.")
                                        .then(
                                                ArgumentNode.argument(
                                                        "<index>",
                                                        "The history index (from `screen history`).")));
        if (savedSlotNodes.length > 0) {
            loadNode.then(savedSlotNodes);
        }

        ArgumentNode slotSubcommand =
                ArgumentNode.literal("slot", "Interact with slots in the current container.")
                        .then(
                                ArgumentNode.literal("click", "Perform a click action on a slot.")
                                        .then(
                                                slotIdNode.then(
                                                        ArgumentNode.argument(
                                                                        "<button>",
                                                                        "Mouse button (0=left, 1=right).")
                                                                .then(actionTypeNodes))),
                                ArgumentNode.literal(
                                                "info", "Get information about an item in a slot.")
                                        .then(slotIdNode),
                                ArgumentNode.literal("list", "List all slots and their contents."),
                                ArgumentNode.literal("highlight", "Visually highlight a slot.")
                                        .then(
                                                slotIdNode.then(
                                                        ArgumentNode.argument(
                                                                "[duration_ms]",
                                                                "How long to highlight in milliseconds."))),
                                ArgumentNode.literal(
                                                "show-ids", "Toggle drawing of slot ID numbers.")
                                        .then(
                                                ArgumentNode.literal("on", "Turn on slot IDs."),
                                                ArgumentNode.literal("off", "Turn off slot IDs."),
                                                ArgumentNode.literal(
                                                        "toggle", "Toggle slot IDs on/off.")));

        return ArgumentNode.literal(
                        "screen", "Manage screen states and interact with screen elements.")
                .then(
                        ArgumentNode.literal("save", "Saves the current screen.")
                                .then(
                                        ArgumentNode.argument(
                                                "[slot_name]",
                                                "Optional name for the slot. Defaults to 'default'.")),
                        loadNode,
                        ArgumentNode.literal("list", "Lists all saved screen slots."),
                        ArgumentNode.literal(
                                        "info", "Gets info on a saved slot or the current screen.")
                                .then(
                                        ArgumentNode.argument(
                                                "[slot_name]", "Optional slot to get info on.")),
                        ArgumentNode.literal("remove", "Deletes a saved screen slot.")
                                .then(savedSlotNodes),
                        ArgumentNode.literal("reopen", "Closes and reopens the current screen."),
                        ArgumentNode.literal("back", "Navigates to the previously opened screen."),
                        /*
                                                                        ArgumentNode.literal("forward", "Navigates forward in the screen history."),
                        */
                        ArgumentNode.literal(
                                        "history", "Shows the list of recently opened screens.")
                                .then(
                                        ArgumentNode.argument(
                                                "[count]",
                                                "Number of entries to show. Max 50, default 10.")),
                        ArgumentNode.literal(
                                "desync",
                                "Keeps screen open client-side but tells the server it closed."),
                        ArgumentNode.literal(
                                "close",
                                "Closes the current screen without sending a packet to the server."),
                        ArgumentNode.literal("cwop", "Alias for the 'close' command."),
                        slotSubcommand);
    }

    @Override
    public String manual() {
        return """
                NAME
                    screen - A toolkit for screen navigation, state, and interaction.

                SYNOPSIS
                    screen [subcommand] [args]

                DESCRIPTION
                    A powerful utility for saving/loading screen states, navigating history,
                    and interacting with container slots. Running `screen` with no subcommand
                    is an alias for `screen info`.

                STATE MANAGEMENT (In-Session Snapshots)
                    save [slot_name]
                        Saves a snapshot of the current screen instance. Defaults to "default".

                    load [slot_name]
                        Restores a saved screen snapshot. Defaults to "default".

                    list
                        Lists all currently saved screen slots for this session.

                    info [slot_name]
                        If a `slot_name` is provided, gets info about that saved slot.
                        If omitted, displays detailed information about the currently open screen.

                    remove <slot_name>
                        Deletes a saved screen snapshot.

                NAVIGATION
                    history [count]
                        Shows a detailed, numbered list of recently opened screens. The `->`
                        arrow indicates your current position in the history. `[count]` is an
                        optional number of entries to show (default: 10, max: 50).

                    back
                        Navigates one step back in the history.

                    forward
                        Navigates one step forward in the history, if you have gone back.

                    load history <index>
                        Navigates to a specific screen from your session history, where 0 is the
                        most recent entry (from `screen history`).

                    reopen
                        Closes and immediately re-opens the current screen. Great for resetting UI.

                INTERACTION & NETWORKING
                    desync
                        Keeps the current container screen open on your client, but sends a
                        packet to the server telling it the screen has been closed.

                    close | cwop
                        Closes the current screen on the client-side only, without sending
                        a packet to the server. `cwop` is a common alias: 'close without packet'

                    slot <action> [args]
                        Subcommand for interacting with container slots.

                SLOT ACTIONS (e.g., `screen slot list`)
                    click <slotId> <button> <action>
                        Performs a raw click action on a slot.
                    info <slotId>
                        Displays detailed info about the item in the specified slot.
                    list
                        Prints a list of all slots in the current container.
                    highlight <slotId> [duration_ms]
                        Visually highlights a slot on-screen. Defaults to 5000ms.
                    show-ids <on|off|toggle>
                        Controls whether slot ID numbers are drawn over every slot.

                EXAMPLES
                    # See the last 15 screens in your navigation history
                    screen history 15

                    # Go back a screen
                    screen back

                    # Jump directly to the 3rd most recent screen
                    screen load history 2

                    # Tell the server you've closed a chest, but keep it open
                    screen desync
                """;
    }
}
