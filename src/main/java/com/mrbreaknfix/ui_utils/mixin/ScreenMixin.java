/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ScreenMixin.java
 */
package com.mrbreaknfix.ui_utils.mixin;

import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.gui.screen.UiUtilsConfigScreen;
import com.mrbreaknfix.ui_utils.gui.screen.UpdateAvailableScreen;
import com.mrbreaknfix.ui_utils.gui.screen.UpdatedScreen;
import com.mrbreaknfix.ui_utils.persistance.Settings;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;
import static com.mrbreaknfix.ui_utils.UiUtils.overlay;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Shadow
    public abstract void render(DrawContext context, int mouseX, int mouseY, float deltaTicks);

    @Inject(at = @At("RETURN"), method = "renderWithTooltip")
    private void onRender(
            DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;

        boolean shouldRender = true;
        if (screen != null) {
            // todo: configurable list of screens to disable overlay on
            boolean isBlacklistedScreen =
                    screen instanceof UiUtilsConfigScreen
                            || screen instanceof UpdatedScreen
                            || screen instanceof UpdateAvailableScreen
                            || screen.getClass()
                                    .getPackageName()
                                    .contains("meteordevelopment.meteorclient");

            if (isBlacklistedScreen) {
                shouldRender = false;
            } else if (Settings.onlyShowOverlayIngame) {
                if (mc.world == null || mc.player == null) {
                    shouldRender = false;
                }
            }
        }
        overlay.setEnabled(shouldRender);

        context.createNewRootLayer();

        context.getMatrices().pushMatrix();

        int mx = mouseX;
        int my = mouseY;

        overlay.render(context, screen, mx, my, deltaTicks);

        context.getMatrices().popMatrix();
    }

    @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    public void onKeyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (input.key() == GLFW.GLFW_KEY_R && input.modifiers() == GLFW.GLFW_MOD_CONTROL) {
            if (mc.player != null)
                mc.player.sendMessage(
                        Text.literal("Reloading UI-Utils Overlay...")
                                .formatted(Formatting.LIGHT_PURPLE),
                        true);
            UiUtils.LOGGER.info("Reloading UI-Utils Overlay...");
            overlay.reload();
        }
        if (overlay.onKey(input.key(), input.scancode(), 1, input.modifiers()))
            cir.setReturnValue(false);
    }
}
