/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file WaitBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.BlockBlueprint;
import com.mrbreaknfix.ui_utils.command.block.blueprint.CodeGenerator;
import com.mrbreaknfix.ui_utils.command.block.blueprint.Input;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ToolboxCategory;

public class WaitBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        return List.of(
                BlockBlueprint.create("util_delay")
                        .message("wait %1 milliseconds")
                        .args(Input.value("DELAY_MS", "Number"))
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(260)
                        .tooltip("Pauses the script for a specified number of milliseconds.")
                        .categories(ToolboxCategory.UTILITY)
                        .withCodeGenerator(
                                CodeGenerator.awaitPromise(
                                        "new Promise(resolve => setTimeout(resolve, ${DELAY_MS}))")));
    }
}
