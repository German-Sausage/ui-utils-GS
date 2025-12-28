/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file OnStartBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks.events;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.BlockBlueprint;
import com.mrbreaknfix.ui_utils.command.block.blueprint.CodeGenerator;
import com.mrbreaknfix.ui_utils.command.block.blueprint.Input;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ToolboxCategory;

public class OnStartBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        return List.of(
                BlockBlueprint.create("event_on_start")
                        .message("On Start: %1")
                        .args(Input.statement("ACTION"))
                        .colour(120)
                        .tooltip("Runs once when the script starts.")
                        .categories(ToolboxCategory.EVENTS)
                        .withCodeGenerator(CodeGenerator.eventHandler()));
    }
}
