/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file YggdrasilAuthenticationServiceMixin.java
 */
package com.mrbreaknfix.ui_utils.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mrbreaknfix.ui_utils.event.events.UserApiServiceCreatedEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.mrbreaknfix.ui_utils.UiUtils.eventManager;

@Mixin(value = YggdrasilAuthenticationService.class, remap = false)
public abstract class YggdrasilAuthenticationServiceMixin {

    @Inject(method = "createUserApiService", at = @At("RETURN"))
    private void onUserApiServiceCreated(
            String accessToken, CallbackInfoReturnable<UserApiService> cir) {
        eventManager.trigger(new UserApiServiceCreatedEvent(accessToken));
    }
}
