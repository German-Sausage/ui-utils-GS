/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file CrashReportMixin.java
 */
package com.mrbreaknfix.ui_utils.mixin;

import com.mrbreaknfix.ui_utils.event.events.GameClosedEvent;

import net.minecraft.util.crash.CrashReport;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.mrbreaknfix.ui_utils.UiUtils.eventManager;

@Mixin(value = CrashReport.class)
public class CrashReportMixin {
    @Inject(at = @At("HEAD"), method = "create")
    private static void onCrash(
            Throwable cause, String title, CallbackInfoReturnable<CrashReport> cir) {
        CrashReport report = new CrashReport(title, cause);
        eventManager.trigger(new GameClosedEvent.Crashed(report));
    }
}
