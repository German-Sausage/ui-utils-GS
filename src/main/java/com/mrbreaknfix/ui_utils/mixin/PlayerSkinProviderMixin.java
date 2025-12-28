/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file PlayerSkinProviderMixin.java
 */
package com.mrbreaknfix.ui_utils.mixin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.utils.ApiUtils;

import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.entity.player.SkinTextures;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerSkinProvider.class)
public abstract class PlayerSkinProviderMixin {
    @Unique
    // 64x32
    private static final String BASIC_CAPE_URL =
            "https://raw.githubusercontent.com/ui-utils/capes/refs/heads/main/basicsponsor.png";

    @Unique private static HashMap<String, String> capes;
    @Unique private static HashSet<String> basicCapes;
    // can go up to 1024x512
    @Unique private MinecraftProfileTexture cape;

    // todo: maybe not use github?
    @Inject(
            method =
                    "fetchSkinTextures(Ljava/util/UUID;Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;)Ljava/util/concurrent/CompletableFuture;",
            at = @At("HEAD"))
    private void fetchSkinTextures(
            UUID uuid,
            MinecraftProfileTextures textures,
            CallbackInfoReturnable<CompletableFuture<SkinTextures>> cir) {
        String uuidString = uuid.toString();

        try {
            if (capes == null || basicCapes == null) getCapes();

            /*
            if (mc.player != null) {
                mc.player.sendMessage(Text.of("Replacing texture for %s".formatted(uuidString)), false);
            }
            */

            if (capes.containsKey(uuidString)) {
                cape = new MinecraftProfileTexture(capes.get(uuidString), null);
            } else if (basicCapes.contains(uuidString)) {
                cape = new MinecraftProfileTexture(BASIC_CAPE_URL, null);
            } else {
                cape = null;
            }
        } catch (Exception e) {
            UiUtils.LOGGER.error("Failed to load cape for {}", uuidString, e);
        }
    }

    @ModifyVariable(
            at = @At("STORE"),
            method =
                    "fetchSkinTextures(Ljava/util/UUID;Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;)Ljava/util/concurrent/CompletableFuture;",
            ordinal = 1,
            name = "minecraftProfileTexture2")
    private MinecraftProfileTexture replaceTexture(MinecraftProfileTexture original) {
        if (cape == null) return original;

        MinecraftProfileTexture result = cape;
        cape = null;
        return result;
    }

    @Unique
    private void getCapes() {
        try {
            capes = new HashMap<>();
            basicCapes = new HashSet<>();
            Gson gson = new Gson();

            String customCapesJson =
                    ApiUtils.getRequest(
                            "https://raw.githubusercontent.com/ui-utils/capes/refs/heads/main/capes.json");
            if (!customCapesJson.isEmpty()) {
                capes =
                        gson.fromJson(
                                customCapesJson,
                                new TypeToken<HashMap<String, String>>() {}.getType());
            }

            String basicCapesJson =
                    ApiUtils.getRequest(
                            "https://raw.githubusercontent.com/ui-utils/capes/refs/heads/main/sponsors.json");
            if (!basicCapesJson.isEmpty()) {
                basicCapes =
                        gson.fromJson(
                                basicCapesJson, new TypeToken<HashSet<String>>() {}.getType());
            }

        } catch (Exception ignored) {
        }
    }
}
