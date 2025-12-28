/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file RawCommandBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.*;

public class RawCommandBlockProvider implements IBlockProvider {

    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        MutatorConfig mutator =
                MutatorConfig.create("raw_command")
                        .colour(20)
                        .addVariableClause("set_variable", "set variable to output");

        return List.of(
                BlockBlueprint.create("uiutils_raw_command")
                        .message("raw command %1")
                        .args(Input.value("COMMAND", "String"))
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(20)
                        .tooltip(
                                "Executes a raw command. Use the gear to capture the output to a variable.")
                        .categories(ToolboxCategory.UTILITY)
                        .withMutator(mutator)
                        .withCodeGenerator(CodeGenerator.mutatorCommand("")));
    }
}
