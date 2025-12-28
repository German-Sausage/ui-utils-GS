/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ClickBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.Arrays;
import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.*;

import net.minecraft.screen.slot.SlotActionType;

public class ClickBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        MutatorConfig mutator =
                MutatorConfig.create("click")
                        .colour(220)
                        .addClause("times", "with times", "Number")
                        .addClause("syncId", "with syncId", "Number")
                        .addClause("revision", "with revision", "Number");

        String[][] actions =
                Arrays.stream(SlotActionType.values())
                        .map(action -> new String[] {action.name(), action.name()})
                        .toArray(String[][]::new);

        return List.of(
                BlockBlueprint.create("uiutils_click")
                        .message("Click on slot: %1 button %2 with action: %3")
                        .args(
                                Input.value("SLOT", "Number"),
                                Input.value("BUTTON", "Number"),
                                Input.dropdown("ACTION", actions))
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(220)
                        .tooltip(
                                "Creates an inventory click packet. Use the gear to add optional flags.")
                        .categories(ToolboxCategory.SCREEN)
                        .withMutator(mutator)
                        .withCodeGenerator(CodeGenerator.mutatorCommand("click")));
    }
}
