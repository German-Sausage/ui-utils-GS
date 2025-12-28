/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file DisconnectBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.BlockBlueprint;
import com.mrbreaknfix.ui_utils.command.block.blueprint.CodeGenerator;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ToolboxCategory;

public class DisconnectBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        return List.of(
                BlockBlueprint.create("uiutils_disconnect")
                        .message("Disconnect from Server")
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(0)
                        .tooltip("Disconnects from the current server.")
                        .categories(ToolboxCategory.NETWORK)
                        .withCodeGenerator(CodeGenerator.standardCommand("disconnect")));
    }
}
