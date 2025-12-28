/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file KeyboardMixin.java
 */
package com.mrbreaknfix.ui_utils.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.input.CharInput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mrbreaknfix.ui_utils.UiUtils.overlay;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

    @Inject(at = @At("HEAD"), method = "onChar", cancellable = true)
    private void onChar(long window, CharInput input, CallbackInfo ci) {
        if (overlay.onChar((char) input.codepoint(), input.modifiers())) ci.cancel();
    }
}
