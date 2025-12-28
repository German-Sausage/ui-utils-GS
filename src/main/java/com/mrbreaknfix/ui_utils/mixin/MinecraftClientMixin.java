/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file MinecraftClientMixin.java
 */
package com.mrbreaknfix.ui_utils.mixin;

import com.mrbreaknfix.ui_utils.event.events.*;
import com.mrbreaknfix.ui_utils.utils.ApiUtils;
import com.mrbreaknfix.ui_utils.utils.Bulletin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.mrbreaknfix.ui_utils.UiUtils.eventManager;
import static com.mrbreaknfix.ui_utils.UiUtils.mc;
import static com.mrbreaknfix.ui_utils.utils.ApiUtils.startPinger;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(at = @At("HEAD"), method = "tick")
    private void onPreTick(CallbackInfo info) {
        eventManager.trigger(TickEvent.Pre.get());
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void onTick(CallbackInfo info) {
        eventManager.trigger(TickEvent.Post.get());
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        Runtime.getRuntime().addShutdownHook(new Thread(ApiUtils::shutdown, "ShutdownHook"));
        startPinger();
        eventManager.addListener(Bulletin.class);
        eventManager.trigger(new InitEvent());
    }

    @Inject(method = "setScreen", at = @At("RETURN"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        OpenScreenEvent event = new OpenScreenEvent(screen);
        eventManager.trigger(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void stop(CallbackInfo ci) {
        eventManager.trigger(new GameClosedEvent.Soft());
    }

    @Inject(method = "onInitFinished", at = @At("HEAD"))
    public void onInitFinished(
            MinecraftClient.LoadingContext loadingContext, CallbackInfoReturnable<Runnable> cir) {
        eventManager.trigger(new InitFinishedEvent());
        eventManager.trigger(
                new ScreenResizeEvent(
                        mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight()));
    }

    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    public void onResChanged(CallbackInfo ci) {
        eventManager.trigger(
                new ScreenResizeEvent(
                        mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight()));
    }
}
