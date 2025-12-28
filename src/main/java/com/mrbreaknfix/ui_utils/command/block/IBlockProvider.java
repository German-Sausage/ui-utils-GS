/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file IBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.blueprint.BlockBlueprint;

public interface IBlockProvider {
    List<BlockBlueprint> getBlockBlueprints();
}
