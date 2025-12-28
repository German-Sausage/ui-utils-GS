/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file SlotManagerWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets;

import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.gui.BaseOverlay;
import com.mrbreaknfix.ui_utils.gui.GuiTheme;
import com.mrbreaknfix.ui_utils.gui.widget.Widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import static com.mrbreaknfix.ui_utils.UiUtils.slotManager;

public class SlotManagerWidget extends Widget {
    public NumInputWidget slotIDInputWidget;
    PickSlotWidget pickSlotWidget;
    ShowSlotIDSWidget ShowSlotIDS;

    public SlotManagerWidget(String id, int x, int y, int width, int height, BaseOverlay overlay) {
        super(id, x, y, width, height, overlay);

        pickSlotWidget =
                new PickSlotWidget(
                        "%s_p_ps".formatted(this.getId()), x + 23, y, 16, 16, this, overlay);
        ShowSlotIDS =
                new ShowSlotIDSWidget(
                        "%s_p_ss".formatted(this.getId()), x + 23 + 16, y, 16, 16, overlay);
        slotIDInputWidget =
                new NumInputWidget(
                        "%s_p_si".formatted(this.getId()), x + width - 50 + 10, y, 43, 16, overlay);
        slotIDInputWidget.setDefaultInput("-1");

        this.addChild(pickSlotWidget);
        this.addChild(ShowSlotIDS);
        this.addChild(slotIDInputWidget);
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        if (!isVisible()) return;

        Text slotText = Text.of("Slot:");
        context.drawText(
                screen.getTextRenderer(), slotText, x, y + 5, GuiTheme.TEXT_PRIMARY, false);

        slotIDInputWidget.setDefaultInput(String.valueOf(slotManager.getHighlightedSlotID()));

        pickSlotWidget.setDisabled(
                slotIDInputWidget.getInt() == slotIDInputWidget.getDefaultInt()
                        || slotIDInputWidget.getInput().equals("-"));

        try {
            slotManager.setHighlightedSlotID(slotIDInputWidget.getInt());
        } catch (NumberFormatException e) {
            UiUtils.LOGGER.error(
                    "Could not parse slot ID: {}", slotIDInputWidget.getTextOrDefault());
        }
    }

    public short getSelectedSlotID() {
        return (short) slotManager.getHighlightedSlotID();
    }
}
