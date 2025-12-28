/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file RawSendBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.lang.reflect.Parameter;
import java.util.*;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.*;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ShapeShifterConfig.ShapeRule;
import com.mrbreaknfix.ui_utils.packet.GeneratedPacketRegistry;
import com.mrbreaknfix.ui_utils.packet.PacketMetadata;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RawSendBlockProvider implements IBlockProvider {

    private static final Map<String, PacketMetadata.PacketInfo> PACKET_INFO_MAP = new TreeMap<>();

    static {
        GeneratedPacketRegistry.registerAll(
                info -> {
                    boolean isBlocklySupported =
                            Arrays.stream(info.constructor().getParameters())
                                    .filter(p -> isUserProvided(p.getType()))
                                    .allMatch(p -> canGenerateRuleForType(p.getType()));
                    if (isBlocklySupported) {
                        PACKET_INFO_MAP.put(info.key(), info);
                    }
                });
    }

    private static boolean isUserProvided(Class<?> type) {
        String name = type.getName();
        return !(name.equals("net.minecraft.item.ItemStack")
                || name.startsWith("it.unimi.dsi.fastutil")
                || name.equals("net.minecraft.util.hit.BlockHitResult")
                || name.equals("net.minecraft.network.packet.c2s.common.SyncedClientOptions")
                || name.equals("net.minecraft.entity.player.PlayerAbilities"));
    }

    private static boolean canGenerateRuleForType(Class<?> type) {
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
                || type.getName().equals("net.minecraft.util.Identifier")
                || type == BlockPos.class
                || type == Vec3d.class
                || type == UUID.class
                || type == byte[].class
                || type.getName().equals("net.minecraft.recipe.NetworkRecipeId")
                || type.getName()
                        .equals("net.minecraft.network.packet.c2s.login.LoginQueryResponsePayload")
                || type.getName().equals("java.util.Optional")
                || type.getName().equals("java.util.List")
                || type.getName().equals("java.time.Instant")
                || type.getName().equals("net.minecraft.util.PlayerInput");
    }

    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        String[][] packetOptions =
                PACKET_INFO_MAP.keySet().stream()
                        .map(key -> new String[] {key, key})
                        .toArray(String[][]::new);

        ShapeShifterConfig shifter = ShapeShifterConfig.create("PACKET_NAME");

        for (PacketMetadata.PacketInfo info : PACKET_INFO_MAP.values()) {
            List<ShapeRule> rules = new ArrayList<>();
            Parameter[] params = info.constructor().getParameters();
            for (int i = 0; i < params.length; i++) {
                Parameter param = params[i];
                // only create inputs for parameters the user must provide.
                if (!isUserProvided(param.getType())) {
                    continue;
                }
                rules.addAll(createRulesForParam(param, i));
            }
            shifter.addRule(info.key(), rules.toArray(new ShapeRule[0]));
        }

        return List.of(
                BlockBlueprint.create("uiutils_rawsend")
                        .message("send packet %1 %2 time(s)")
                        .args(
                                Input.dropdown("PACKET_NAME", packetOptions),
                                Input.value("TIMES", "Number"))
                        .inputsInline(true)
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(290)
                        .tooltip(
                                "Constructs and sends a raw C2S packet. Shape changes based on selected packet.")
                        .categories(ToolboxCategory.NETWORK)
                        .withShapeShifter(shifter)
                        .withCodeGenerator(CodeGenerator.rawSendCommand()));
    }

    private List<ShapeRule> createRulesForParam(Parameter param, int index) {
        Class<?> type = param.getType();
        String paramName = param.getName();
        String baseName = "ARG_" + index + "_" + paramName;

        if (type == BlockPos.class) {
            return List.of(
                    ShapeRule.valueInput(baseName + "_X", paramName + ".x", "Number"),
                    ShapeRule.valueInput(baseName + "_Y", paramName + ".y", "Number"),
                    ShapeRule.valueInput(baseName + "_Z", paramName + ".z", "Number"));
        }
        if (type == Vec3d.class) {
            return List.of(
                    ShapeRule.valueInput(baseName + "_X", paramName + ".x", "Number"),
                    ShapeRule.valueInput(baseName + "_Y", paramName + ".y", "Number"),
                    ShapeRule.valueInput(baseName + "_Z", paramName + ".z", "Number"));
        }
        if (type.isEnum()) {
            String[][] enumOptions =
                    Arrays.stream(type.getEnumConstants())
                            .map(e -> new String[] {e.toString().toLowerCase(), e.toString()})
                            .toArray(String[][]::new);
            return List.of(
                    ShapeRule.fieldDropdown(
                            baseName + "_DUMMY", paramName, baseName + "_FIELD", enumOptions));
        }

        return List.of(ShapeRule.valueInput(baseName, paramName, javaTypeToBlocklyType(type)));
    }

    private String javaTypeToBlocklyType(Class<?> type) {
        if (type == int.class
                || type == long.class
                || type == float.class
                || type == double.class
                || type == Integer.class
                || type == Long.class
                || type == Float.class
                || type == Double.class) {
            return "Number";
        }
        if (type == boolean.class || type == Boolean.class) {
            return "Boolean";
        }
        return "String";
    }
}
