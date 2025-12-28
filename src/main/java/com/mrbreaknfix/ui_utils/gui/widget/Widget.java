/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file Widget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget;

import java.util.ArrayList;
import java.util.List;

import com.mrbreaknfix.ui_utils.gui.BaseOverlay;
import com.mrbreaknfix.ui_utils.utils.MathUtils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import static com.mrbreaknfix.ui_utils.UiUtils.eventManager;
import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class Widget {
    public int x, y;
    public int width, height;
    private int zIndex;
    private Widget parent;
    private final List<Widget> children;
    private final String id;
    private final BaseOverlay overlay;
    private boolean focused = false;
    protected boolean moveable = true;
    protected boolean visible = true;

    public Widget(String id, int x, int y, int width, int height, BaseOverlay overlay) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.zIndex = 0;
        this.children = new ArrayList<>();
        this.parent = null;
        this.overlay = overlay;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        // Propagate visibility to children
        for (Widget child : children) {
            child.setVisible(visible);
        }
    }

    public boolean shouldChangeCursor() {
        return true;
    }

    public boolean isMovable() {
        return moveable;
    }

    public void setMovable(boolean moveable) {
        this.moveable = moveable;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getZIndex() {
        return zIndex;
    }

    public String getId() {
        return id;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public Widget getParent() {
        return parent;
    }

    public void setParent(Widget parent) {
        this.parent = parent;
    }

    public List<Widget> getChildren() {
        return children;
    }

    public void addChild(Widget child) {
        children.add(child);
        child.setParent(this);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!visible) return false;
        return mouseX >= x && mouseX <= (x + width) && mouseY >= y && mouseY <= (y + height);
    }

    public void onMouseClick(double mouseX, double mouseY, double button) {}

    public void update(double mouseX, double mouseY, float deltaTicks) {}

    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        if (!visible) return;

        context.fill(x, y, x + width, y + height, 0xb2ff0000);

        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int textY = y + height / 6;
        Text defaultWidgetText = Text.of("[" + id + "]: render method not overridden");
        int textWidth = mc.textRenderer.getWidth(defaultWidgetText);
        context.drawText(
                mc.textRenderer,
                defaultWidgetText,
                centerX - textWidth / 2,
                textY + 2,
                0xffffffff,
                true);
    }

    public void moveTo(int x, int y) {
        int delX = x - this.x;
        int delY = y - this.y;

        this.x = x;
        this.y = y;

        this.children.forEach(child -> child.move(delX, delY));
    }

    public Widget getDeepestParent() {
        Widget deepestParent = this;
        while (deepestParent.parent != null) {
            deepestParent = deepestParent.parent;
        }
        return deepestParent;
    }

    public void move(double deltaX, double deltaY) {
        this.x += deltaX;
        this.y += deltaY;

        this.children.forEach(child -> child.move(deltaX, deltaY));
    }

    public void bound(int frameWidth, int frameHeight) {
        int newX = MathUtils.clamp(this.x, 0, frameWidth - width);
        int newY = MathUtils.clamp(this.y, 0, frameHeight - height);
        moveTo(newX, newY);
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        if (this.focused != focused) {
            this.focused = focused;
        }
    }

    public BaseOverlay getOverlay() {
        return overlay;
    }

    public void onUnload() {
        eventManager.removeListener(this);
        for (Widget child : children) {
            child.onUnload();
        }
    }

    public void onMouseRelease(double mouseX, double mouseY, double button) {}
}
