/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file DesyncBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.BlockBlueprint;
import com.mrbreaknfix.ui_utils.command.block.blueprint.CodeGenerator;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ToolboxCategory;

public class DesyncBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        return List.of(
                BlockBlueprint.create("uiutils_desync")
                        .message("Desync Screen")
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(180)
                        .tooltip(
                                "Keeps the current screen open client-side, but tells the server it was closed.")
                        .categories(ToolboxCategory.SCREEN)
                        .withCodeGenerator(CodeGenerator.standardCommand("desync")));
    }
}
