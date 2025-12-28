/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file MouseMixin.java
 */
package com.mrbreaknfix.ui_utils.mixin;

import com.mrbreaknfix.ui_utils.event.events.ScrollEvent;
import com.mrbreaknfix.ui_utils.event.events.mouse.ClickEvent;

import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.util.Window;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mrbreaknfix.ui_utils.UiUtils.eventManager;
import static com.mrbreaknfix.ui_utils.UiUtils.mc;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
        // Backported for 1.21.4: Manually calculate scaled coordinates
        Window w = mc.getWindow();
        double d = mc.mouse.getX() * (double) w.getScaledWidth() / (double) w.getWidth();
        double e = mc.mouse.getY() * (double) w.getScaledHeight() / (double) w.getHeight();

        ClickEvent clickEvent = new ClickEvent(action, input.modifiers(), input.button(), d, e);
        eventManager.trigger(clickEvent);
        if (clickEvent.isCancelled()) ci.cancel();
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        // Backported for 1.21.4: Manually calculate scaled coordinates
        Window w = mc.getWindow();
        double d = mc.mouse.getX() * (double) w.getScaledWidth() / (double) w.getWidth();
        double e = mc.mouse.getY() * (double) w.getScaledHeight() / (double) w.getHeight();

        ScrollEvent clickEvent = new ScrollEvent(horizontal, vertical, d, e);
        eventManager.trigger(clickEvent);
        if (clickEvent.isCancelled()) ci.cancel();
    }
}
