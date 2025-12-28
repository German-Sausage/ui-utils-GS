/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file McfwBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.*;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ShapeShifterConfig.ShapeRule;
import com.mrbreaknfix.ui_utils.packet.McfwFilterType;
import com.mrbreaknfix.ui_utils.packet.PacketNameUtil;

public class McfwBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        String[][] subcommands = {
            {"enable", "ENABLE"}, {"disable", "DISABLE"}, {"add rule", "ADD"},
            {"remove rule", "REMOVE"}, {"list rules", "LIST"}, {"release queue", "RELEASE"},
            {"clear queue", "CLEAR"}, {"reset all rules", "RESET"}
        };

        List<String[]> packetList = new ArrayList<>();
        packetList.add(new String[] {"all", "all"});
        PacketNameUtil.getAllPacketNames()
                .forEach(name -> packetList.add(new String[] {name, name}));
        String[][] packetOptions = packetList.toArray(new String[0][]);

        String[][] typeOptions =
                Arrays.stream(McfwFilterType.values())
                        .map(t -> new String[] {t.name().toLowerCase(), t.name().toUpperCase()})
                        .toArray(String[][]::new);
        //                .toArray(String[][]::new);

        ShapeShifterConfig shifter =
                ShapeShifterConfig.create("SUBCOMMAND")
                        .addOptionsSource("packetOptions", packetOptions)
                        .addOptionsSource("typeOptions", typeOptions)
                        .addRule(
                                "ADD",
                                ShapeRule.fieldDropdown(
                                        "PACKET_INPUT", "packet", "PACKET", "packetOptions"),
                                ShapeRule.fieldDropdown(
                                        "TYPE_INPUT", "type", "TYPE", "typeOptions"))
                        .addRule(
                                "REMOVE",
                                ShapeRule.fieldDropdown(
                                        "PACKET_INPUT", "packet", "PACKET", "packetOptions"))
                        .addRule("ENABLE")
                        .addRule("DISABLE")
                        .addRule("LIST")
                        .addRule("RELEASE")
                        .addRule("CLEAR")
                        .addRule("RESET");

        return List.of(
                BlockBlueprint.create("uiutils_mcfw")
                        .message("mcfw %1")
                        .args(Input.dropdown("SUBCOMMAND", subcommands))
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(290)
                        .tooltip(
                                "Controls the Minecraft Firewall. Shape changes based on the command.")
                        .categories(ToolboxCategory.NETWORK)
                        .withShapeShifter(shifter)
                        .withCodeGenerator(CodeGenerator.shapeShifterCommand("mcfw")));
    }
}
