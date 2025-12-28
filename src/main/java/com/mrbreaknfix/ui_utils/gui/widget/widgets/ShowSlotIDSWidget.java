/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ShowSlotIDSWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets;

import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.gui.BaseOverlay;
import com.mrbreaknfix.ui_utils.gui.widget.HandCursor;
import com.mrbreaknfix.ui_utils.gui.widget.Noisy;
import com.mrbreaknfix.ui_utils.gui.widget.Widget;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;

import static com.mrbreaknfix.ui_utils.UiUtils.slotManager;

public class ShowSlotIDSWidget extends Widget implements HandCursor, Noisy {
    private boolean active;

    public ShowSlotIDSWidget(String id, int x, int y, int width, int height, BaseOverlay overlay) {
        super(id, x, y, width, height, overlay);
        this.active = false;
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        Identifier activeId = Identifier.of(UiUtils.MOD_ID, "hide_ids.png");
        Identifier inactiveId = Identifier.of(UiUtils.MOD_ID, "show_ids.png");
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                active ? activeId : inactiveId,
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
        active = !active;
        slotManager.setShouldDrawSlotIDs(active);
        slotManager.setShouldRenderHighlightedSlot(active);
    }
}
