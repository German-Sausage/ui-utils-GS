/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file OnTickBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks.events;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.BlockBlueprint;
import com.mrbreaknfix.ui_utils.command.block.blueprint.CodeGenerator;
import com.mrbreaknfix.ui_utils.command.block.blueprint.Input;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ToolboxCategory;

// for testing only
@SuppressWarnings("unused")
public class OnTickBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        return List.of(
                BlockBlueprint.create("event_tick")
                        .message("Every Tick: %1")
                        .args(Input.statement("ACTION"))
                        .colour(20)
                        .tooltip("Runs on every client tick. Use with caution.")
                        .categories(ToolboxCategory.EVENTS)
                        .withCodeGenerator(CodeGenerator.eventHandler()));
    }
}
