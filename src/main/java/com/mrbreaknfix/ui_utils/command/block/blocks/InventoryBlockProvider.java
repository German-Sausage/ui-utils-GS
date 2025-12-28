/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file InventoryBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.BlockBlueprint;
import com.mrbreaknfix.ui_utils.command.block.blueprint.CodeGenerator;
import com.mrbreaknfix.ui_utils.command.block.blueprint.Input;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ToolboxCategory;

public class InventoryBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        String[][] modes = {
            {"auto-detect", "AUTO"},
            {"player only", "PLAYER"},
            {"riding only", "RIDING"}
        };

        return List.of(
                BlockBlueprint.create("uiutils_inventory")
                        .message("open inventory %1")
                        .args(Input.dropdown("MODE", modes))
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(240)
                        .tooltip("Opens an inventory screen with optional overrides.")
                        .categories(ToolboxCategory.PLAYER)
                        .withCodeGenerator(CodeGenerator.fromFieldsCommand("inventory", "MODE")));
    }
}
