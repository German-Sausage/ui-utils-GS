/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file AtBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.BlockBlueprint;
import com.mrbreaknfix.ui_utils.command.block.blueprint.CodeGenerator;
import com.mrbreaknfix.ui_utils.command.block.blueprint.Input;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ToolboxCategory;

public class AtBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        return List.of(
                BlockBlueprint.create("uiutils_at")
                        .message("At this time: %1 run this command: %2")
                        .args(Input.value("TIME", "String"), Input.value("COMMAND", "String"))
                        .inputsInline(true)
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(50)
                        .tooltip("Schedules a command to run at a specific time of day.")
                        .categories(ToolboxCategory.UTILITY)
                        .withCodeGenerator(CodeGenerator.standardCommand("at")));
    }
}
