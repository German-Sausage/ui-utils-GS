/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file McfwCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;
import com.mrbreaknfix.ui_utils.packet.Mcfw;
import com.mrbreaknfix.ui_utils.packet.McfwFilterType;
import com.mrbreaknfix.ui_utils.packet.PacketNameUtil;

public class McfwCommand extends BaseCommand {
    // todo: listen to S2C packets and dump as json

    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (parsedArgs.isEmpty()) {
            return CommandResult.of(false, "No subcommand specified. See `help mcfw` for usage.");
        }

        String subCommand = parsedArgs.get(0).toString().toLowerCase();
        JsonObject jsonBody = new JsonObject();

        switch (subCommand) {
            case "on", "enable" -> {
                Mcfw.enabled = true;
                jsonBody.addProperty("status", "enabled");
                return CommandResult.of(true, "MCFW enabled.", null, jsonBody);
            }
            case "off", "disable" -> {
                Mcfw.enabled = false;
                jsonBody.addProperty("status", "disabled");
                return CommandResult.of(true, "MCFW disabled.", null, jsonBody);
            }
            case "add" -> {
                if (parsedArgs.size() < 3)
                    return CommandResult.of(false, "Usage: mcfw add <packet_name> <type>");
                String packetToAdd = parsedArgs.get(1).toString().toLowerCase();
                try {
                    McfwFilterType type =
                            McfwFilterType.valueOf(parsedArgs.get(2).toString().toUpperCase());
                    Mcfw.rules.put(packetToAdd, type);
                    jsonBody.addProperty("action", "add");
                    jsonBody.addProperty("packet", packetToAdd);
                    jsonBody.addProperty("type", type.name());
                    return CommandResult.of(
                            true, "Rule added: " + packetToAdd + " -> " + type, null, jsonBody);
                } catch (IllegalArgumentException e) {
                    return CommandResult.of(
                            false, "Invalid filter type. Use: allow, drop, delay, log.");
                }
            }
            case "remove", "rm" -> {
                if (parsedArgs.size() < 2)
                    return CommandResult.of(false, "Usage: mcfw remove <packet_name>");
                String packetToRemove = parsedArgs.get(1).toString().toLowerCase();
                jsonBody.addProperty("action", "remove");
                jsonBody.addProperty("packet", packetToRemove);
                if (Mcfw.rules.remove(packetToRemove) != null) {
                    jsonBody.addProperty("found", true);
                    return CommandResult.of(
                            true, "Rule removed for: " + packetToRemove, null, jsonBody);
                } else {
                    jsonBody.addProperty("found", false);
                    return CommandResult.of(
                            false, "No rule found for: " + packetToRemove, null, jsonBody);
                }
            }
            case "list", "ls" -> {
                if (Mcfw.rules.isEmpty()) return CommandResult.of(true, "No active MCFW rules.");
                String rules =
                        Mcfw.rules.entrySet().stream()
                                .map(entry -> entry.getKey() + " -> " + entry.getValue())
                                .collect(Collectors.joining("\n"));
                JsonArray rulesJson = new JsonArray();
                Mcfw.rules.forEach(
                        (key, value) -> {
                            JsonObject ruleObj = new JsonObject();
                            ruleObj.addProperty("packet", key);
                            ruleObj.addProperty("action", value.name());
                            rulesJson.add(ruleObj);
                        });
                jsonBody.add("rules", rulesJson);
                return CommandResult.of(true, "Active MCFW Rules:\n" + rules, null, jsonBody);
            }
            case "release" -> {
                int released = Mcfw.release();
                jsonBody.addProperty("action", "release");
                jsonBody.addProperty("count", released);
                return CommandResult.of(
                        true, "Released " + released + " delayed packets.", null, jsonBody);
            }
            case "clear", "flush" -> {
                int cleared = Mcfw.clear();
                jsonBody.addProperty("action", "clear");
                jsonBody.addProperty("count", cleared);
                return CommandResult.of(
                        true,
                        "Cleared " + cleared + " delayed packets from the queue.",
                        null,
                        jsonBody);
            }
            case "reset" -> {
                int initialRuleCount = Mcfw.rules.size();
                Mcfw.rules.clear();
                jsonBody.addProperty("action", "reset");
                jsonBody.addProperty("cleared_rules_count", initialRuleCount);
                return CommandResult.of(
                        true,
                        "All " + initialRuleCount + " MCFW rules have been cleared.",
                        null,
                        jsonBody);
            }
            default -> {
                return CommandResult.of(false, "Unknown subcommand: " + subCommand);
            }
        }
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        ArgumentNode[] packetLiterals =
                PacketNameUtil.getAllPacketNames().stream()
                        .map(name -> ArgumentNode.literal(name, "A C2S packet."))
                        .toArray(ArgumentNode[]::new);
        ArgumentNode allPacketsNode = ArgumentNode.literal("all", "A wildcard for all packets.");
        ArgumentNode[] allPacketOptions = new ArgumentNode[packetLiterals.length + 1];
        allPacketOptions[0] = allPacketsNode;
        System.arraycopy(packetLiterals, 0, allPacketOptions, 1, packetLiterals.length);
        ArgumentNode[] filterTypeNodes =
                Arrays.stream(McfwFilterType.values())
                        .map(
                                type ->
                                        ArgumentNode.literal(
                                                type.name().toLowerCase(),
                                                "Filter action: " + type.name()))
                        .toArray(ArgumentNode[]::new);
        ArgumentNode addNode =
                ArgumentNode.literal("add", "Adds a new packet filtering rule.")
                        .then(
                                Arrays.stream(allPacketOptions)
                                        .map(packetNode -> packetNode.then(filterTypeNodes))
                                        .toArray(ArgumentNode[]::new));
        ArgumentNode removeNode =
                ArgumentNode.literal("remove", "Removes an existing packet filtering rule.")
                        .then(allPacketOptions);
        return ArgumentNode.literal("mcfw", "A client-side packet firewall.")
                .then(
                        ArgumentNode.literal("on", "Enables the MCFW packet firewall."),
                        ArgumentNode.literal("off", "Disables the MCFW packet firewall."),
                        addNode,
                        removeNode,
                        ArgumentNode.literal("list", "Lists all currently active firewall rules."),
                        ArgumentNode.literal(
                                "release", "Sends all packets currently held in the delay queue."),
                        ArgumentNode.literal(
                                "clear",
                                "Discards all packets from the delay queue without sending."),
                        ArgumentNode.literal(
                                "reset", "Removes all active packet filtering rules."));
    }

    @Override
    public String manual() {
        return """
                NAME
                    mcfw - A client-side Minecraft Firewall to filter outgoing packets.

                SYNOPSIS
                    mcfw <subcommand> [args]

                DESCRIPTION
                    Provides fine-grained control over outgoing client-to-server packets.
                    You can drop, delay, log, or explicitly allow packets.

                RULE PRECEDENCE
                    Specific packet rules always take precedence over the wildcard `all` rule.
                    This allows you to create exceptions. For example, to block everything
                    except chat messages, you would use two rules:
                    1. `mcfw add all drop`
                    2. `mcfw add ChatMessage allow`

                SUBCOMMANDS
                    on (alias: enable)
                        Activates the firewall.
                    off (alias: disable)
                        Deactivates the firewall.

                    add <packet_name|all> <type>
                        Adds a rule. Type can be: allow, drop, delay, log.

                    remove (alias: rm) <packet_name|all>
                        Removes an *explicit* rule. Note: this cannot remove the effect of a
                        wildcard rule on a specific packet. To do that, add a new `allow` rule.

                    list (alias: ls)
                        Shows all currently active rules.

                    release
                        Sends all packets currently held in the delay queue.

                    clear (alias: flush)
                        Drops all packets in the delay queue without sending them.

                    reset
                        Removes all active packet filtering rules.

                EXAMPLES
                    # Drop all movement packets
                    mcfw add PlayerMove.Full drop

                    # Log all interactions with blocks but allow them
                    mcfw add PlayerInteractBlock log

                    # Create an exception to a wildcard rule
                    mcfw add all drop
                    mcfw add ChatMessage allow

                    # Clear all rules
                    mcfw reset
                """;
    }
}
