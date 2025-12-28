/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file InBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.BlockBlueprint;
import com.mrbreaknfix.ui_utils.command.block.blueprint.CodeGenerator;
import com.mrbreaknfix.ui_utils.command.block.blueprint.Input;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ToolboxCategory;

public class InBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        return List.of(
                BlockBlueprint.create("uiutils_in")
                        .message("in %1 run command %2")
                        .args(Input.value("DURATION", "String"), Input.value("COMMAND", "String"))
                        .inputsInline(true)
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(50)
                        .categories(ToolboxCategory.UTILITY)
                        .tooltip(
                                "Schedules a command to run after a specific delay (e.g., '5s', '1m30s').")
                        .withCodeGenerator(CodeGenerator.standardCommand("in")));
    }
}
