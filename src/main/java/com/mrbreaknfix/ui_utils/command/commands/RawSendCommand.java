/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file RawSendCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;
import com.mrbreaknfix.ui_utils.packet.GeneratedPacketRegistry;
import com.mrbreaknfix.ui_utils.packet.PacketMetadata;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.c2s.login.LoginQueryResponsePayload;
import net.minecraft.recipe.NetworkRecipeId;
import net.minecraft.util.Identifier;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

// Important: GeneratedPacketRegistry is generated at build time! Please build the project to
// resolve it.
public class RawSendCommand extends BaseCommand {

    private static final Map<String, PacketMetadata.PacketInfo> PACKET_INFO_MAP = new TreeMap<>();

    static {

        // Important: GeneratedPacketRegistry is generated at build time! Please build the project
        // to resolve it.
        GeneratedPacketRegistry.registerAll(info -> PACKET_INFO_MAP.put(info.key(), info));

        int fullySupportedCount = 0;
        int partiallySupportedCount = 0;
        int unsupportedCount = 0;

        for (PacketMetadata.PacketInfo info : PACKET_INFO_MAP.values()) {
            Parameter[] params = info.constructor().getParameters();
            boolean hasUnsupported = Arrays.stream(params).anyMatch(p -> !isSupported(p.getType()));
            boolean isPartiallySupported =
                    Arrays.stream(params).anyMatch(p -> !isUserProvided(p.getType()));

            if (hasUnsupported) {
                unsupportedCount++;
            } else if (isPartiallySupported) {
                partiallySupportedCount++;
            } else {
                fullySupportedCount++;
            }
        }

        UiUtils.LOGGER.info(
                "RawSendCommand initialized. Loaded {} packets ({} fully supported, {} partially supported (~), {} unsupported (!)).",
                PACKET_INFO_MAP.size(),
                fullySupportedCount,
                partiallySupportedCount,
                unsupportedCount);
    }

    private static boolean isUserProvided(Class<?> type) {
        // true if user must provide
        return !(type == ItemStack.class
                || type == Int2ObjectMap.class
                || type == BlockHitResult.class
                || type == SyncedClientOptions.class
                || type == PlayerAbilities.class);
    }

    private static boolean isSupported(Class<?> type) {
        // true if user providable OR auto-fillable; unsupported is neither
        if (!isUserProvided(type)) return true;

        return type == String.class
                || type == int.class
                || type == Integer.class
                || type == long.class
                || type == Long.class
                || type == float.class
                || type == Float.class
                || type == double.class
                || type == Double.class
                || type == boolean.class
                || type == Boolean.class
                || type.isEnum()
                || type == UUID.class
                || type == Identifier.class
                || type == BlockPos.class
                || type == Vec3d.class
                || type == Optional.class
                || type == byte[].class
                || type == Instant.class
                || type == List.class
                || type == NetworkRecipeId.class
                || type == PlayerInput.class
                || type == LoginQueryResponsePayload.class;
    }

    private static String getSimpleTypeName(Class<?> type) {
        if (!isUserProvided(type)) return type.getSimpleName() + "(auto)";
        if (type == BlockPos.class) return "BlockPos(x y z)";
        if (type == Vec3d.class) return "Vec3d(x y z)";
        if (type == byte[].class) return "byte[](hex)";
        if (type == List.class) return "List(csv)";
        if (type == PlayerInput.class) return "PlayerInput(fwd back left right jump sneak sprint)";
        return type.getSimpleName();
    }

    private static byte[] hexStringToByteArray(String s) {
        if (s.length() % 2 != 0)
            throw new IllegalArgumentException(
                    "Hex string must have an even number of characters.");
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] =
                    (byte)
                            ((Character.digit(s.charAt(i), 16) << 4)
                                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (parsedArgs.size() < 2) {
            return CommandResult.of(false, "Usage: rawsend <times> <packet_name> [args...]");
        }

        int times;
        try {
            times = Integer.parseInt(parsedArgs.getFirst().toString());
            if (times <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            return CommandResult.of(false, "Invalid number for <times>: " + parsedArgs.getFirst());
        }

        String packetKey = parsedArgs.get(1).toString().toLowerCase();
        PacketMetadata.PacketInfo info = PACKET_INFO_MAP.get(packetKey);
        if (info == null) {
            return CommandResult.of(false, "Unknown packet: " + packetKey);
        }

        if (mc.getNetworkHandler() == null) {
            return CommandResult.of(false, "Not connected to a server.");
        }

        List<String> stringArgs =
                parsedArgs.stream().skip(2).map(Object::toString).collect(Collectors.toList());

        try {
            Packet<?> packet = createPacket(info, stringArgs);
            for (int i = 0; i < times; i++) {
                mc.getNetworkHandler().sendPacket(packet);
            }

            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("packet_key", packetKey);
            jsonBody.addProperty("count", times);
            jsonBody.addProperty("packet_class", packet.getClass().getName());
            return CommandResult.of(
                    true, "Sent packet '" + packetKey + "' " + times + " times.", null, jsonBody);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            if (e instanceof InvocationTargetException && e.getCause() != null) {
                message = "Constructor error: " + e.getCause().getMessage();
            }
            return CommandResult.of(
                    false,
                    "Failed to create packet: "
                            + message
                            + "\nUsage: rawsend "
                            + times
                            + " "
                            + info.usageString());
        }
    }

    private Packet<?> createPacket(PacketMetadata.PacketInfo info, List<String> stringArgs)
            throws Exception {
        Parameter[] params = info.constructor().getParameters();
        List<Object> constructorArgs = new ArrayList<>();
        ParsingContext context = new ParsingContext(stringArgs);

        for (Parameter param : params) {
            constructorArgs.add(provideParameter(context, param.getType(), param.getName()));
        }

        return (Packet<?>) info.constructor().newInstance(constructorArgs.toArray());
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        ArgumentNode root = ArgumentNode.literal("rawsend", "Construct and send raw C2S packets.");
        ArgumentNode timesNode = ArgumentNode.argument("<times>", "Number of times to send.");
        root.then(timesNode);

        for (PacketMetadata.PacketInfo info : PACKET_INFO_MAP.values()) {
            Parameter[] params = info.constructor().getParameters();
            boolean hasUnsupported = Arrays.stream(params).anyMatch(p -> !isSupported(p.getType()));
            boolean isPartiallySupported =
                    Arrays.stream(params).anyMatch(p -> !isUserProvided(p.getType()));

            String indicator;
            if (hasUnsupported) {
                indicator = " (!)"; // Unsupported takes precedence
            } else if (isPartiallySupported) {
                indicator = " (~)";
            } else {
                indicator = "";
            }

            String nodeName = info.key();
            String description = info.packetClass().getSimpleName() + indicator;
            ArgumentNode packetNode = ArgumentNode.literal(nodeName, description);

            buildSchemaForParameters(packetNode, params);
            timesNode.then(packetNode);
        }
        return root;
    }

    @Override
    public String manual() {
        StringBuilder sb = new StringBuilder();
        sb.append(
                """
                NAME
                    rawsend - Construct and send raw C2S packets from a generated registry.

                SYNOPSIS
                    rawsend <times> <packet_name> [args...]

                DESCRIPTION
                    A powerful utility for sending C2S packets. The autocomplete list will show
                    a tilde `(~)` next to packets that have one or more arguments automatically
                    filled by the client. It will show an exclamation mark `(!)` for packets
                    that have one or more arguments of a type that this command cannot provide,
                    which will likely cause creation to fail.

                    WARNING: Sending packets outside their intended state (e.g., a 'handshake'
                    packet while in-game) will likely result in a disconnect.
                """);

        List<String> fullySupported = new ArrayList<>();
        List<String> partiallySupported = new ArrayList<>();
        List<String> unsupported = new ArrayList<>();

        for (PacketMetadata.PacketInfo info : PACKET_INFO_MAP.values()) {
            if (info.usageString() == null || info.usageString().isBlank()) {
                continue;
            }

            Parameter[] params = info.constructor().getParameters();
            boolean hasUnsupported = Arrays.stream(params).anyMatch(p -> !isSupported(p.getType()));
            boolean isPartiallySupported =
                    Arrays.stream(params).anyMatch(p -> !isUserProvided(p.getType()));

            String usage = "    " + info.usageString();
            if (hasUnsupported) {
                unsupported.add(usage);
            } else if (isPartiallySupported) {
                partiallySupported.add(usage);
            } else {
                fullySupported.add(usage);
            }
        }

        sb.append("\n--- FULLY SUPPORTED ---\n");
        sb.append("All arguments must be provided by the user.\n");
        if (fullySupported.isEmpty()) {
            sb.append("    None\n");
        } else {
            fullySupported.forEach(s -> sb.append(s).append("\n"));
        }

        sb.append("\n--- PARTIALLY SUPPORTED (~) ---\n");
        sb.append("Packets with some arguments auto-filled by the client.\n");
        if (partiallySupported.isEmpty()) {
            sb.append("    None\n");
        } else {
            partiallySupported.forEach(s -> sb.append(s).append("\n"));
        }

        sb.append("\n--- UNSUPPORTED (!) ---\n");
        sb.append("Packets with arguments this command cannot provide. May fail to send.\n");
        if (unsupported.isEmpty()) {
            sb.append("    None\n");
        } else {
            unsupported.forEach(s -> sb.append(s).append("\n"));
        }

        sb.append(
                """

                EXAMPLES
                    # Swing main hand 5 times
                    rawsend 5 play.hand_swing main_hand

                    # Update a sign at position 100, 64, -200.
                    rawsend 1 play.update_sign 100 64 -200 true "Line 1" "Line 2" "Line 3" "Line 4"

                    # Send a custom payload packet
                    rawsend 1 common.custom_payload my_mod:my_channel 48656c6c6f
                """);
        return sb.toString();
    }

    private void buildSchemaForParameters(ArgumentNode parent, Parameter[] parameters) {
        List<ArgumentNode> currentLeaves = new ArrayList<>();
        currentLeaves.add(parent);

        for (Parameter param : parameters) {
            if (!isUserProvided(param.getType())) continue;

            List<ArgumentNode> newArgumentNodes =
                    buildSchemaForType(param.getType(), param.getName());
            List<ArgumentNode> nextLeaves = new ArrayList<>();

            // For each current leaf node, add all the new argument nodes as children.
            // This handles enums/booleans, where one argument can lead to multiple branches.
            for (ArgumentNode leaf : currentLeaves) {
                for (ArgumentNode newNode : newArgumentNodes) {
                    leaf.then(newNode);
                }
            }

            // The new leaves are the terminal nodes of the subtrees we just added.
            for (ArgumentNode newNode : newArgumentNodes) {
                nextLeaves.addAll(findAllLeaves(newNode));
            }

            currentLeaves = nextLeaves;
        }
    }

    private List<ArgumentNode> findAllLeaves(ArgumentNode node) {
        List<ArgumentNode> leaves = new ArrayList<>();
        if (node.getChildren().isEmpty()) {
            leaves.add(node);
            return leaves;
        }
        for (ArgumentNode child : node.getChildren()) {
            leaves.addAll(findAllLeaves(child));
        }
        return leaves;
    }

    private List<ArgumentNode> buildSchemaForType(Class<?> type, String name) {
        String argName = "<" + name + ":" + getSimpleTypeName(type) + ">";

        if (type == boolean.class || type == Boolean.class) {
            return List.of(
                    ArgumentNode.literal("true", argName), ArgumentNode.literal("false", argName));
        }
        if (type.isEnum()) {
            return Arrays.stream(type.getEnumConstants())
                    .map(e -> ArgumentNode.literal(e.toString().toLowerCase(), argName))
                    .collect(Collectors.toList());
        }

        ArgumentNode node = ArgumentNode.argument(argName, "Type: " + type.getSimpleName());
        if (type == BlockPos.class)
            node.then(
                    ArgumentNode.argument("<y:int>", "Y")
                            .then(ArgumentNode.argument("<z:int>", "Z")));
        if (type == Vec3d.class)
            node.then(
                    ArgumentNode.argument("<y:double>", "Y")
                            .then(ArgumentNode.argument("<z:double>", "Z")));
        if (type == PlayerInput.class) {
            node.then(
                    ArgumentNode.argument("<forward:boolean>", "Forward")
                            .then(
                                    ArgumentNode.argument("<backward:boolean>", "Backward")
                                            .then(
                                                    ArgumentNode.argument("<left:boolean>", "Left")
                                                            .then(
                                                                    ArgumentNode.argument(
                                                                                    "<right:boolean>",
                                                                                    "Right")
                                                                            .then(
                                                                                    ArgumentNode
                                                                                            .argument(
                                                                                                    "<jump:boolean>",
                                                                                                    "Jump")
                                                                                            .then(
                                                                                                    ArgumentNode
                                                                                                            .argument(
                                                                                                                    "<sneak:boolean>",
                                                                                                                    "Sneak")
                                                                                                            .then(
                                                                                                                    ArgumentNode
                                                                                                                            .argument(
                                                                                                                                    "<sprint:boolean>",
                                                                                                                                    "Sprint"))))))));
        }

        return List.of(node);
    }

    private Object provideParameter(ParsingContext context, Class<?> type, String paramName) {
        if (isUserProvided(type)) {
            return parseUserProvidedParameter(context, type, paramName);
        } else {
            return createAutoFilledParameter(type);
        }
    }

    @SuppressWarnings("unchecked")
    private Object parseUserProvidedParameter(
            ParsingContext context, Class<?> type, String paramName) {
        String val = context.next();
        try {
            if (type == String.class) return val;
            if (type == int.class || type == Integer.class) return Integer.parseInt(val);
            if (type == long.class || type == Long.class) return Long.parseLong(val);
            if (type == float.class || type == Float.class) return Float.parseFloat(val);
            if (type == double.class || type == Double.class) return Double.parseDouble(val);
            if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(val);
            if (type.isEnum()) return Enum.valueOf((Class<Enum>) type, val.toUpperCase());
            if (type == UUID.class) return UUID.fromString(val);
            if (type == Identifier.class) return Identifier.of(val);
            if (type == NetworkRecipeId.class) return new NetworkRecipeId(Integer.parseInt(val));
            if (type == byte[].class) return hexStringToByteArray(val);
            if (type == Instant.class)
                return val.equalsIgnoreCase("now")
                        ? Instant.now()
                        : Instant.ofEpochMilli(Long.parseLong(val));
            if (type == BlockPos.class)
                return new BlockPos(
                        Integer.parseInt(val),
                        Integer.parseInt(context.next()),
                        Integer.parseInt(context.next()));
            if (type == Vec3d.class)
                return new Vec3d(
                        Double.parseDouble(val),
                        Double.parseDouble(context.next()),
                        Double.parseDouble(context.next()));
            if (type == Optional.class) {
                if (val.equalsIgnoreCase("null") || val.equalsIgnoreCase("empty"))
                    return Optional.empty();
                if (paramName.equalsIgnoreCase("title")) return Optional.of(val);
                return Optional.of(Identifier.of(val));
            }
            if (type == List.class) {
                if (paramName.equalsIgnoreCase("pages"))
                    return Arrays.asList(val.split("\\|\\|\\|"));
                return Arrays.stream(val.split(","))
                        .map(Identifier::of)
                        .collect(Collectors.toList());
            }
            if (type == PlayerInput.class) {
                boolean forward = Boolean.parseBoolean(val);
                boolean backward = Boolean.parseBoolean(context.next());
                boolean left = Boolean.parseBoolean(context.next());
                boolean right = Boolean.parseBoolean(context.next());
                boolean jump = Boolean.parseBoolean(context.next());
                boolean sneak = Boolean.parseBoolean(context.next());
                boolean sprint = Boolean.parseBoolean(context.next());
                return new PlayerInput(forward, backward, left, right, jump, sneak, sprint);
            }
            if (type == LoginQueryResponsePayload.class) {
                byte[] data = hexStringToByteArray(val);
                return (LoginQueryResponsePayload) buf -> buf.writeBytes(data);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to parse '"
                            + paramName
                            + "' ("
                            + type.getSimpleName()
                            + ") from '"
                            + val
                            + "'",
                    e);
        }
        throw new IllegalArgumentException(
                "Unsupported user-provided parameter type: " + type.getName());
    }

    private Object createAutoFilledParameter(Class<?> type) {
        if (type == ItemStack.class) return ItemStack.EMPTY;
        if (type == Int2ObjectMap.class) return new Int2ObjectArrayMap<>();
        if (type == SyncedClientOptions.class) return SyncedClientOptions.createDefault();
        if (type == PlayerAbilities.class)
            return mc.player != null ? mc.player.getAbilities() : new PlayerAbilities();
        if (type == BlockHitResult.class) {
            Entity camera = mc.getCameraEntity();
            if (camera == null)
                throw new IllegalStateException(
                        "Cannot generate BlockHitResult: no camera entity.");
            HitResult result = camera.raycast(5.0, 0.0f, false);
            if (result.getType() == HitResult.Type.BLOCK) return result;
            return BlockHitResult.createMissed(
                    camera.getEyePos(),
                    Direction.getFacing(camera.getRotationVector()),
                    BlockPos.ofFloored(camera.getEyePos()));
        }
        throw new IllegalArgumentException(
                "Unsupported auto-filled parameter type: " + type.getName());
    }

    private static class ParsingContext {
        private final List<String> args;
        private int cursor = 0;

        ParsingContext(List<String> args) {
            this.args = args;
        }

        String next() {
            if (cursor >= args.size())
                throw new IllegalArgumentException("Not enough arguments provided.");
            return args.get(cursor++);
        }
    }
}
