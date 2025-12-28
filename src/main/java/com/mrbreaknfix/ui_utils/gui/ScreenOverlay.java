/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ScreenOverlay.java
 */
package com.mrbreaknfix.ui_utils.gui;

import com.mrbreaknfix.ui_utils.gui.widget.Widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import org.lwjgl.glfw.GLFW;

public class ScreenOverlay extends BaseOverlay {

    private Screen parentScreen;

    public ScreenOverlay() {
        super(null);
    }

    public void init(Screen screen) {
        this.parentScreen = screen;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (parentScreen == null) return;
        super.render(context, parentScreen, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.onMouseClick(mouseX, mouseY, GLFW.GLFW_PRESS, button);
    }

    public boolean mouseScrolled(
            double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.onMouseScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.onMouseClick(mouseX, mouseY, GLFW.GLFW_RELEASE, button);
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.onKey(keyCode, scanCode, GLFW.GLFW_PRESS, modifiers);
    }

    public boolean charTyped(char chr, int modifiers) {
        return this.onChar(chr, modifiers);
    }

    @Override
    public void saveWidgetPosition(Widget widget) {}

    @Override
    public void close() {
        super.close();
        this.parentScreen = null;
    }
}
