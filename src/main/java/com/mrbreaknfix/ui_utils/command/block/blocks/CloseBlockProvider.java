/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file CloseBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.BlockBlueprint;
import com.mrbreaknfix.ui_utils.command.block.blueprint.CodeGenerator;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ToolboxCategory;

public class CloseBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        return List.of(
                BlockBlueprint.create("uiutils_close")
                        .message("Close Screen Without Packet")
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .categories(ToolboxCategory.SCREEN)
                        .colour(180)
                        .tooltip(
                                "Closes the current screen without sending a packet to the server.")
                        .withCodeGenerator(CodeGenerator.standardCommand("close")));
    }
}
