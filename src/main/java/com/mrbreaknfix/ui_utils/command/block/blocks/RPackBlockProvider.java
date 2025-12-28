/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file RPackBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.Arrays;
import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.BlockBlueprint;
import com.mrbreaknfix.ui_utils.command.block.blueprint.CodeGenerator;
import com.mrbreaknfix.ui_utils.command.block.blueprint.Input;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ToolboxCategory;

import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;

public class RPackBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        String[][] statuses =
                Arrays.stream(ResourcePackStatusC2SPacket.Status.values())
                        .map(
                                status -> {
                                    String name = status.name().toLowerCase().replace("_", " ");
                                    String value =
                                            status.name()
                                                    .toLowerCase()
                                                    .replace("successfully_", "");
                                    return new String[] {name, value};
                                })
                        .toArray(String[][]::new);

        return List.of(
                BlockBlueprint.create("uiutils_rpack")
                        .message("send resource pack status %1")
                        .args(Input.dropdown("STATUS", statuses))
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(20)
                        .tooltip("Responds to a server-side resource pack request.")
                        .categories(ToolboxCategory.NETWORK)
                        .withCodeGenerator(CodeGenerator.fromFieldsCommand("rpack", "STATUS")));
    }
}
