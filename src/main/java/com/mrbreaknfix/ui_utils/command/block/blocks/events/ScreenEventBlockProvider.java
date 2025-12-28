/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ScreenEventBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks.events;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.*;

public class ScreenEventBlockProvider implements IBlockProvider {

    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        EventMutatorConfig eventMutator =
                EventMutatorConfig.create("screen")
                        .colour(20)
                        .addClause("VAR_CLASSNAME", "Get Class Name as", "className")
                        .addClause("VAR_TITLE", "Get Title as", "title")
                        .addClause("VAR_SYNCID", "Get SyncId as", "syncId")
                        .addClause("VAR_REVISION", "Get revision as", "revision");

        return List.of(
                BlockBlueprint.create("event_screen")
                        .message("Every Screen Change: %1 %2")
                        .args(Input.dummy(), Input.statement("ACTION"))
                        .colour(20)
                        .tooltip(
                                "Triggers on screen changes. Use the gear to extract data to variables.")
                        .withCodeGenerator(CodeGenerator.eventHandlerWithVars())
                        .categories(ToolboxCategory.EVENTS)
                        .withMutator(eventMutator));
    }
}
