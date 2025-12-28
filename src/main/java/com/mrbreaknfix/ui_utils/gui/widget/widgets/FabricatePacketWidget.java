/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file FabricatePacketWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets;

import java.util.Map;

import com.mrbreaknfix.ui_utils.gui.BaseOverlay;
import com.mrbreaknfix.ui_utils.gui.GuiTheme;
import com.mrbreaknfix.ui_utils.gui.widget.Widget;
import com.mrbreaknfix.ui_utils.gui.widget.widgets.dropdown.DropdownWidget;
import com.mrbreaknfix.ui_utils.utils.GuiUtils;
import com.mrbreaknfix.ui_utils.utils.UIActions;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class FabricatePacketWidget extends Widget {
    Map<Integer, String> actionOptions =
            Map.of(
                    0,
                    "Pickup",
                    1,
                    "Quick Move",
                    2,
                    "Swap",
                    3,
                    "Clone",
                    4,
                    "Throw",
                    5,
                    "Quick Craft",
                    6,
                    "Pickup All");

    private final NumInputWidget syncIdInput;
    private final NumInputWidget revisionInput;
    private final NumInputWidget buttonInput;
    private final NumInputWidget buttonIdInput;
    private final NumInputWidget timesToSendInput;
    private final DropdownWidget actionsDropdown;
    private final SlotManagerWidget slotManagerWidget;

    private boolean collapsed;
    private final int originalHeight;
    private final ButtonWidget collapseButton;

    public FabricatePacketWidget(int x, int y, int width, int height, BaseOverlay overlay) {
        super("FabricatePacket", x, y, width, height, overlay);
        originalHeight = height;

        collapseButton = new ButtonWidget("fp_cb", "Fabricate Packet", x, y, width, 20, overlay);
        collapseButton.setOnClick(this::toggleCollapsed);
        this.addChild(collapseButton);

        syncIdInput =
                new NumInputWidget(
                        "syncId", x + 40 + 16, y + 24 + 16 + 2, width - 4 - 40 - 15, 15, overlay);
        revisionInput =
                new NumInputWidget(
                        "revision",
                        x + 40 + 16,
                        y + 24 + 16 + 2 + 16,
                        width - 4 - 40 - 15,
                        15,
                        overlay);
        actionsDropdown =
                new DropdownWidget(
                        "action", x + 41, y + 26, width - 4 - 40, 15, actionOptions, overlay);
        slotManagerWidget =
                new SlotManagerWidget("slot_mgr", x + 4, y + 74, width - 10, 15, overlay);
        buttonInput =
                new NumInputWidget(
                        "button",
                        x + 40 + 15 + 9,
                        y + 90 + 1,
                        width - 4 - 40 - 15 - 8,
                        15,
                        overlay);
        buttonInput.setDefaultInput("0");
        buttonIdInput =
                new NumInputWidget(
                        "button_id",
                        x + 40 + 15 + 9,
                        y + 90 + 1 + 16,
                        width - 4 - 40 - 15 - 8,
                        15,
                        overlay);
        buttonIdInput.setDefaultInput("0");
        timesToSendInput =
                new NumInputWidget(
                        "times_to_send",
                        x + 40 + 15 + 9,
                        y + 124,
                        width - 4 - 40 - 15 - 8,
                        15,
                        overlay);
        timesToSendInput.setDefaultInput("1");

        ButtonWidget sendSlot =
                new ButtonWidget(
                        "snd_slot", "Slot", x + 3, y + height - 19, width / 2 - 4, 16, overlay);
        sendSlot.setOnClick(
                () -> {
                    if (mc.player == null) return;
                    mc.player.sendMessage(Text.of("Sending slot packet"), false);
                    UIActions.sendClickSlotPacket(
                            syncIdInput.getInt(),
                            revisionInput.getInt(),
                            slotManagerWidget.getSelectedSlotID(),
                            (byte) buttonInput.getInt(),
                            getSlotAction(actionsDropdown.getSelectedOption()),
                            timesToSendInput.getInt());
                });

        ButtonWidget sendButton =
                new ButtonWidget(
                        "snd_btn",
                        "Button",
                        x + width / 2 + 2,
                        y + height - 19,
                        width / 2 - 4,
                        16,
                        overlay);
        sendButton.setOnClick(
                () -> {
                    if (mc.player == null) return;
                    mc.player.sendMessage(Text.of("Sending button packet"), false);
                    UIActions.sendClickButtonPacket(
                            syncIdInput.getInt(),
                            buttonIdInput.getInt(),
                            timesToSendInput.getInt());
                });

        this.addChild(syncIdInput);
        this.addChild(revisionInput);
        this.addChild(actionsDropdown);
        this.addChild(slotManagerWidget);
        this.addChild(buttonInput);
        this.addChild(buttonIdInput);
        this.addChild(timesToSendInput);
        this.addChild(sendSlot);
        this.addChild(sendButton);

        setCollapsed(true);
    }

    private void setCollapsed(boolean isCollapsed) {
        this.collapsed = isCollapsed;
        setHeight(collapsed ? 20 : originalHeight);
        getChildren().stream()
                .filter(w -> w != collapseButton)
                .forEach(w -> w.setVisible(!collapsed));

        if (collapsed) {
            actionsDropdown.close();
        }
    }

    private void toggleCollapsed() {
        setCollapsed(!this.collapsed);
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        if (!isVisible()) return;

        Text collapsedIndicator = Text.of(collapsed ? " +" : " -");
        int textX = x + width - 14;
        int textY = y + (20 - screen.getTextRenderer().fontHeight) / 2 + 1;

        //        context.drawText(screen.getTextRenderer(), collapsedIndicator, textX, textY,
        // GuiTheme.TEXT_PRIMARY, true);

        if (collapsed) return;

        // Update default values
        syncIdInput.setDefaultInput(
                (mc.player != null && mc.player.currentScreenHandler != null)
                        ? String.valueOf(mc.player.currentScreenHandler.syncId)
                        : "null");
        revisionInput.setDefaultInput(
                (mc.player != null && mc.player.currentScreenHandler != null)
                        ? String.valueOf(mc.player.currentScreenHandler.getRevision())
                        : "null");

        // expanded area
        GuiUtils.drawBorder(context, x, y + 20, width, height - 20, GuiTheme.WIDGET_BORDER);
        context.fill(x + 1, y + 21, x + width - 1, y + height - 1, GuiTheme.WIDGET_BACKGROUND);

        int labelY = y + 28;
        context.drawText(
                screen.getTextRenderer(),
                Text.of("Action:"),
                x + GuiTheme.PADDING,
                labelY,
                GuiTheme.TEXT_PRIMARY,
                false);
        labelY += 18;
        context.drawText(
                screen.getTextRenderer(),
                Text.of("Sync ID:"),
                x + GuiTheme.PADDING,
                labelY,
                GuiTheme.TEXT_PRIMARY,
                false);
        labelY += 16;
        context.drawText(
                screen.getTextRenderer(),
                Text.of("Revision:"),
                x + GuiTheme.PADDING,
                labelY,
                GuiTheme.TEXT_PRIMARY,
                false);
        labelY += 28;
        context.drawText(
                screen.getTextRenderer(),
                Text.of("Button:"),
                x + GuiTheme.PADDING,
                labelY,
                GuiTheme.TEXT_PRIMARY,
                false);
        labelY += 16;
        context.drawText(
                screen.getTextRenderer(),
                Text.of("Button ID:"),
                x + GuiTheme.PADDING,
                labelY,
                GuiTheme.TEXT_PRIMARY,
                false);
        labelY += 16;
        context.drawText(
                screen.getTextRenderer(),
                Text.of("Send times:"),
                x + GuiTheme.PADDING,
                labelY,
                GuiTheme.TEXT_PRIMARY,
                false);
    }

    private SlotActionType getSlotAction(int selectedId) {
        return switch (selectedId) {
            case 1 -> SlotActionType.QUICK_MOVE;
            case 2 -> SlotActionType.SWAP;
            case 3 -> SlotActionType.CLONE;
            case 4 -> SlotActionType.THROW;
            case 5 -> SlotActionType.QUICK_CRAFT;
            case 6 -> SlotActionType.PICKUP_ALL;
            default -> SlotActionType.PICKUP;
        };
    }
}
