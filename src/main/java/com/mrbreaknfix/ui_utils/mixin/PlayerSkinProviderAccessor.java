/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file PlayerSkinProviderAccessor.java
 */
package com.mrbreaknfix.ui_utils.mixin;

import net.minecraft.client.texture.PlayerSkinProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerSkinProvider.class)
public interface PlayerSkinProviderAccessor {
    @Accessor("skinCache")
    PlayerSkinProvider.FileCache getSkinCache();
}
