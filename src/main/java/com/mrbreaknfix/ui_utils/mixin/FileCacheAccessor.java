/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file FileCacheAccessor.java
 */
package com.mrbreaknfix.ui_utils.mixin;

import java.nio.file.Path;

import net.minecraft.client.texture.PlayerSkinProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerSkinProvider.FileCache.class)
public interface FileCacheAccessor {
    @Accessor
    Path getDirectory();
}
