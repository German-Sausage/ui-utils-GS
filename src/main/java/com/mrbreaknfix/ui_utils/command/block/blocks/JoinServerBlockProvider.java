/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file JoinServerBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.BlockBlueprint;
import com.mrbreaknfix.ui_utils.command.block.blueprint.CodeGenerator;
import com.mrbreaknfix.ui_utils.command.block.blueprint.Input;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ToolboxCategory;

public class JoinServerBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        return List.of(
                BlockBlueprint.create("uiutils_joinserver")
                        .message("join server %1")
                        .args(Input.value("IP", "String"))
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(20)
                        .tooltip("Connects to the specified Minecraft server.")
                        .categories(ToolboxCategory.NETWORK)
                        .withCodeGenerator(CodeGenerator.standardCommand("joinserver")));
    }
}
