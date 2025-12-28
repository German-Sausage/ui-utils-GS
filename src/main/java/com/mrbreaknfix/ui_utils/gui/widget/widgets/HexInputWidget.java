/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file HexInputWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets;

import java.util.function.Consumer;
import java.util.function.Function;

import com.mrbreaknfix.ui_utils.gui.GuiTheme;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class HexInputWidget extends TextInputWidget {

    private Consumer<Integer> onEnterCallback = (color) -> {};

    public HexInputWidget(String id, int x, int y, int width, int height) {
        super(id, x, y, width, height, null);
        setDefaultInput("AARRGGBB");
    }

    public void setOnEnter(Consumer<Integer> onEnter) {
        this.onEnterCallback = onEnter;
    }

    @Override
    public void onEnter() {
        try {
            String hex = getInput();
            if (hex.isEmpty()) return;

            long parsed = Long.parseUnsignedLong(hex, 16);
            int color;
            if (hex.length() <= 6) {
                color = 0xFF000000 | (int) parsed; // full alpha for RGB
            } else {
                color = (int) parsed;
            }
            onEnterCallback.accept(color);
            setFocused(false);
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        if (!isVisible()) return;

        int borderColor =
                isFocused()
                        ? GuiTheme.WIDGET_BORDER_FOCUSED
                        : (isMouseOver(mouseX, mouseY)
                                ? GuiTheme.WIDGET_BORDER_HOVER
                                : GuiTheme.WIDGET_BORDER);
        context.fill(x, y, x + width, y + height, borderColor);
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, GuiTheme.WIDGET_BACKGROUND);

        String currentInput = getInput();
        String textToRender = currentInput.isEmpty() ? getDefaultInput() : currentInput;
        boolean isPlaceholder = currentInput.isEmpty();
        int textColor = isPlaceholder ? GuiTheme.TEXT_PLACEHOLDER : GuiTheme.TEXT_PRIMARY;

        int prefixWidth = mc.textRenderer.getWidth("#");
        int textX = x + GuiTheme.PADDING;
        int textY = y + (height - mc.textRenderer.fontHeight) / 2 + 1;

        context.enableScissor(x + 1, y + 1, x + width - 1, y + height - 1);

        // selection
        if (isFocused() && hasSelection()) {
            int selStart = Math.min(getSelectionStart(), getSelectionEnd());
            int selEnd = Math.max(getSelectionStart(), getSelectionEnd());
            int selX1 =
                    textX
                            + prefixWidth
                            + screen.getTextRenderer().getWidth(currentInput.substring(0, selStart))
                            - getScrollOffset();
            int selX2 =
                    textX
                            + prefixWidth
                            + screen.getTextRenderer().getWidth(currentInput.substring(0, selEnd))
                            - getScrollOffset();
            context.fill(selX1, textY - 1, selX2, textY + 9, GuiTheme.TEXT_INPUT_SELECTION);
        }

        context.drawText(screen.getTextRenderer(), "#", textX, textY, GuiTheme.TEXT_PRIMARY, false);
        context.drawText(
                screen.getTextRenderer(),
                textToRender,
                textX + prefixWidth - getScrollOffset(),
                textY,
                textColor,
                false);

        // cursor
        if (isFocused() && shouldShowCursor()) {
            int cursorPixelPos =
                    screen.getTextRenderer().getWidth(currentInput.substring(0, getCursorPos()));
            int cursorX = textX + prefixWidth + cursorPixelPos - getScrollOffset();
            context.fill(cursorX, textY - 1, cursorX + 1, textY + 9, GuiTheme.TEXT_PRIMARY);
        }

        context.disableScissor();
    }

    @Override
    public boolean onInput(Function<String, String> factory) {
        if (!isFocused()) return false;

        String toInsert = factory.apply("");
        if (toInsert == null) return false;

        String currentText = getInput();
        int start = Math.min(getSelectionStart(), getSelectionEnd());
        int end = Math.max(getSelectionStart(), getSelectionEnd());
        String newText = new StringBuilder(currentText).replace(start, end, toInsert).toString();

        if (newText.matches("^[0-9a-fA-F]{0,8}$")) {
            return super.onInput(s -> toInsert.toUpperCase());
        }
        return true;
    }

    @Override
    public void setText(String text) {
        if (text == null) {
            super.setText("");
            return;
        }
        String sanitized = text.toUpperCase().replace("#", "");
        if (sanitized.matches("^[0-9a-fA-F]{0,8}$")) {
            super.setText(sanitized);
        }
    }
}
