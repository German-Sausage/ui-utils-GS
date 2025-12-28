/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file PickSlotWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets;

import com.mrbreaknfix.ui_utils.gui.BaseOverlay;
import com.mrbreaknfix.ui_utils.gui.widget.HandCursor;
import com.mrbreaknfix.ui_utils.gui.widget.Noisy;
import com.mrbreaknfix.ui_utils.gui.widget.Widget;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;

import static com.mrbreaknfix.ui_utils.UiUtils.slotManager;

public class PickSlotWidget extends Widget implements HandCursor, Noisy {
    private final SlotManagerWidget parent;
    private boolean picking;
    private boolean disabled;

    public PickSlotWidget(
            String id,
            int x,
            int y,
            int width,
            int height,
            SlotManagerWidget parent,
            BaseOverlay overlay) {
        super(id, x, y, width, height, overlay);
        this.parent = parent;
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        if (!isVisible()) return;

        if (slotManager.shouldStopPicking()) {
            slotManager.setShouldStopPicking(false);
            picking = false;
            return;
        }
        slotManager.setPicking(picking);

        Identifier activeId = Identifier.of("ui_utils", "pickslot_active.png");
        Identifier inactiveId = Identifier.of("ui_utils", "pickslot_inactive.png");
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                picking ? activeId : inactiveId,
                x,
                y,
                0,
                0,
                width,
                height,
                width,
                height,
                width,
                height);
    }

    @Override
    public void onMouseClick(double mouseX, double mouseY, double button) {
        if (!isVisible() || button != 0) return;

        if (disabled) {
            parent.slotIDInputWidget.clearInput();
            this.setDisabled(false);
            picking = true;
        } else if (!picking) {
            picking = true;
        }
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
