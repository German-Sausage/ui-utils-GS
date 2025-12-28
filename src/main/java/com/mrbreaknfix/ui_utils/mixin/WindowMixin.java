/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file WindowMixin.java
 */
package com.mrbreaknfix.ui_utils.mixin;

import com.mrbreaknfix.ui_utils.event.events.ScreenResizeEvent;
import com.mrbreaknfix.ui_utils.event.events.WindowPosChangedEvent;
import com.mrbreaknfix.ui_utils.event.events.WindowSizeChangedEvent;

import net.minecraft.client.util.Window;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mrbreaknfix.ui_utils.UiUtils.eventManager;
import static com.mrbreaknfix.ui_utils.UiUtils.mc;

@Mixin(Window.class)
public class WindowMixin {
    @Inject(at = @At("HEAD"), method = "onWindowSizeChanged")
    private void onWindowSizeChanged(long window, int width, int height, CallbackInfo ci) {
        eventManager.trigger(new WindowSizeChangedEvent(width, height));
        eventManager.trigger(
                new ScreenResizeEvent(
                        mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight()));
    }

    @Inject(at = @At("HEAD"), method = "onWindowPosChanged")
    private void onWindowPosChanged(long window, int width, int height, CallbackInfo ci) {
        eventManager.trigger(new WindowPosChangedEvent(width, height));
    }
}
