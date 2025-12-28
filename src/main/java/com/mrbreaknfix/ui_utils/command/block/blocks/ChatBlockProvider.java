/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ChatBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.BlockBlueprint;
import com.mrbreaknfix.ui_utils.command.block.blueprint.CodeGenerator;
import com.mrbreaknfix.ui_utils.command.block.blueprint.Input;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ToolboxCategory;

public class ChatBlockProvider implements IBlockProvider {

    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        return List.of(
                BlockBlueprint.create("uiutils_chat")
                        .message("Chat: %1")
                        .args(Input.value("MESSAGE", "String"))
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(160)
                        .tooltip("Sends a chat message or command to the server.")
                        .categories(ToolboxCategory.PLAYER)
                        .withCodeGenerator(CodeGenerator.standardCommand("chat")));
    }
}
