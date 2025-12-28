/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ButtonBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.*;

public class ButtonBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        MutatorConfig mutator =
                MutatorConfig.create("button")
                        .colour(160)
                        .addClause("syncId", "with syncId", "Number")
                        .addClause("times", "with times", "Number");

        return List.of(
                BlockBlueprint.create("uiutils_button")
                        .message("Button Click for ID %1")
                        .args(Input.value("BUTTON_ID", "Number"))
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(160)
                        .tooltip(
                                "Simulates a screen button click. Use the gear to add optional flags.")
                        .categories(ToolboxCategory.SCREEN)
                        .withMutator(mutator)
                        .withCodeGenerator(CodeGenerator.mutatorCommand("button")));
    }
}
