/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file LoopBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.BlockBlueprint;
import com.mrbreaknfix.ui_utils.command.block.blueprint.CodeGenerator;
import com.mrbreaknfix.ui_utils.command.block.blueprint.Input;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ToolboxCategory;

public class LoopBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        return List.of(
                BlockBlueprint.create("uiutils_loop")
                        .message("loop %1 times, command %2")
                        .args(Input.value("TIMES", "Number"), Input.value("COMMAND", "String"))
                        .inputsInline(true)
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(120)
                        .tooltip("Executes the given command string a specified number of times.")
                        .categories(ToolboxCategory.UTILITY)
                        .withCodeGenerator(CodeGenerator.standardCommand("loop")));
    }
}
