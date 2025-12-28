/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file TextInputWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets;

import java.util.function.Function;

import com.mrbreaknfix.ui_utils.gui.BaseOverlay;
import com.mrbreaknfix.ui_utils.gui.GuiTheme;
import com.mrbreaknfix.ui_utils.gui.widget.Typeable;
import com.mrbreaknfix.ui_utils.gui.widget.Widget;
import com.mrbreaknfix.ui_utils.utils.GuiUtils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import org.lwjgl.glfw.GLFW;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class TextInputWidget extends Widget implements Typeable {
    private String input = "";
    private String defaultInput = "";
    private boolean editable = true;

    protected int cursorPos = 0;
    protected int selectionStart = 0;
    protected int selectionEnd = 0;
    private boolean draggingSelection = false;
    protected int scrollOffset = 0;

    private long lastBlinkTime = System.currentTimeMillis();
    protected boolean showCursor = true;

    public TextInputWidget(String id, int x, int y, int width, int height, BaseOverlay overlay) {
        super(id, x, y, width, height, overlay);
    }

    @Override
    public void onMouseClick(double mouseX, double mouseY, double button) {
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            setFocused(true);
            draggingSelection = true;
            int textX = x + GuiTheme.PADDING;
            int relX = (int) mouseX - textX + scrollOffset;
            cursorPos = selectionStart = selectionEnd = getCharIndexFromX(relX);
        } else {
            setFocused(false);
        }
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, double button) {
        if (button == 0) {
            draggingSelection = false;
        }
    }

    @Override
    public void update(double mouseX, double mouseY, float delta) {
        //  cursor blink
        if (isFocused()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastBlinkTime >= 500) {
                showCursor = !showCursor;
                lastBlinkTime = currentTime;
            }
        }

        // selection dragging
        if (isFocused() && draggingSelection) {
            int textX = x + GuiTheme.PADDING;
            int relX = (int) mouseX - textX + scrollOffset;
            cursorPos = selectionEnd = getCharIndexFromX(relX);
            adjustScroll();
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
        GuiUtils.drawBorder(context, x, y, width, height, borderColor);
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, GuiTheme.WIDGET_BACKGROUND);

        String textToRender = input.isEmpty() ? defaultInput : input;
        boolean isPlaceholder = input.isEmpty();
        int textColor = isPlaceholder ? GuiTheme.TEXT_PLACEHOLDER : GuiTheme.TEXT_PRIMARY;

        int textX = x + GuiTheme.PADDING;
        int textY = y + (height - mc.textRenderer.fontHeight) / 2 + 1;

        // selection
        if (isFocused() && hasSelection()) {
            int selStart = Math.min(selectionStart, selectionEnd);
            int selEnd = Math.max(selectionStart, selectionEnd);
            int selX1 =
                    textX
                            + screen.getTextRenderer().getWidth(input.substring(0, selStart))
                            - scrollOffset;
            int selX2 =
                    textX
                            + screen.getTextRenderer().getWidth(input.substring(0, selEnd))
                            - scrollOffset;

            context.enableScissor(x + 1, y + 1, x + width - 1, y + height - 1);
            context.fill(selX1, textY - 1, selX2, textY + 9, GuiTheme.TEXT_INPUT_SELECTION);
            context.disableScissor();
        }

        // text
        context.enableScissor(x + 1, y + 1, x + width - 1, y + height - 1);
        context.drawText(
                screen.getTextRenderer(),
                textToRender,
                textX - scrollOffset,
                textY,
                textColor,
                false);
        context.disableScissor();

        // cursor if editable
        if (isFocused() && showCursor && editable) {
            int cursorPixelPos = screen.getTextRenderer().getWidth(input.substring(0, cursorPos));
            int cursorX = textX + cursorPixelPos - scrollOffset;
            context.fill(cursorX, textY - 1, cursorX + 1, textY + 9, GuiTheme.TEXT_PRIMARY);
        }
    }

    private void adjustScroll() {
        int cursorPixelPos = mc.textRenderer.getWidth(input.substring(0, cursorPos));
        int visibleWidth = width - (GuiTheme.PADDING * 2);

        if (cursorPixelPos - scrollOffset > visibleWidth) {
            scrollOffset = cursorPixelPos - visibleWidth;
        } else if (cursorPixelPos - scrollOffset < 0) {
            scrollOffset = cursorPixelPos;
        }

        int maxScroll = Math.max(0, mc.textRenderer.getWidth(input) - visibleWidth);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    @Override
    public boolean onInput(Function<String, String> factory) {
        if (!isFocused() || !editable) return false;

        String newText = factory.apply("");
        if (newText == null) return false;

        int start = Math.min(selectionStart, selectionEnd);
        int end = Math.max(selectionStart, selectionEnd);

        input = input.substring(0, start) + newText + input.substring(end);
        cursorPos = start + newText.length();
        selectionStart = selectionEnd = cursorPos;
        adjustScroll();
        return true;
    }

    @Override
    public boolean onChar(int codePoint, int modifiers) {
        if (!editable) return false;
        return isFocused() && Typeable.super.onChar(codePoint, modifiers);
    }

    @Override
    public boolean onKey(int key, int scan, int modifiers) {
        if (!isFocused()) return false;

        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;

        // navigation and selection keys regardless of editable state
        switch (key) {
            case GLFW.GLFW_KEY_LEFT:
                cursorPos = ctrl ? moveCursorByWords(cursorPos, -1) : Math.max(0, cursorPos - 1);
                updateSelection(shift);
                return true;
            case GLFW.GLFW_KEY_RIGHT:
                cursorPos =
                        ctrl
                                ? moveCursorByWords(cursorPos, 1)
                                : Math.min(input.length(), cursorPos + 1);
                updateSelection(shift);
                return true;
            case GLFW.GLFW_KEY_HOME:
                cursorPos = 0;
                updateSelection(shift);
                return true;
            case GLFW.GLFW_KEY_END:
                cursorPos = input.length();
                updateSelection(shift);
                return true;
            case GLFW.GLFW_KEY_A:
                if (ctrl) {
                    selectionStart = 0;
                    selectionEnd = input.length();
                    cursorPos = input.length();
                    return true;
                }
                break;
            case GLFW.GLFW_KEY_C:
                if (ctrl && hasSelection()) {
                    mc.keyboard.setClipboard(getSelectedText());
                    return true;
                }
                break;
        }

        if (!editable) {
            return false;
        }

        switch (key) {
            case GLFW.GLFW_KEY_X:
                if (ctrl && hasSelection()) {
                    mc.keyboard.setClipboard(getSelectedText());
                    deleteSelection();
                    return true;
                }
                break;
            case GLFW.GLFW_KEY_V:
                if (ctrl) {
                    return onInput(s -> mc.keyboard.getClipboard());
                }
                break;
            case GLFW.GLFW_KEY_BACKSPACE:
                if (hasSelection()) {
                    deleteSelection();
                } else if (ctrl && cursorPos > 0) {
                    int newPos = moveCursorByWords(cursorPos, -1);
                    input = input.substring(0, newPos) + input.substring(cursorPos);
                    cursorPos = newPos;
                } else if (cursorPos > 0) {
                    input = input.substring(0, cursorPos - 1) + input.substring(cursorPos);
                    cursorPos--;
                }
                clearSelection();
                return true;
            case GLFW.GLFW_KEY_DELETE:
                if (hasSelection()) {
                    deleteSelection();
                } else if (ctrl && cursorPos < input.length()) {
                    int newPos = moveCursorByWords(cursorPos, 1);
                    input = input.substring(0, cursorPos) + input.substring(newPos);
                } else if (cursorPos < input.length()) {
                    input = input.substring(0, cursorPos) + input.substring(cursorPos + 1);
                }
                clearSelection();
                return true;
            case GLFW.GLFW_KEY_ENTER:
            case GLFW.GLFW_KEY_KP_ENTER:
                onEnter();
                return true;
        }
        return false;
    }

    public void onEnter() {
        // Default behavior does nothing.
    }

    private int moveCursorByWords(int pos, int direction) {
        if (direction < 0 && pos == 0) return 0;
        if (direction > 0 && pos >= input.length()) return input.length();

        boolean foundWord = false;
        while (true) {
            pos += direction;
            if (pos <= 0 || pos >= input.length()) break;
            if (input.charAt(pos) != ' ' && input.charAt(pos - direction) == ' ') {
                foundWord = true;
            } else if (foundWord && input.charAt(pos) == ' ') {
                pos -= direction;
                break;
            }
        }
        return Math.max(0, Math.min(input.length(), pos));
    }

    public boolean hasSelection() {
        return selectionStart != selectionEnd;
    }

    private String getSelectedText() {
        if (!hasSelection()) return "";
        int start = Math.min(selectionStart, selectionEnd);
        int end = Math.max(selectionStart, selectionEnd);
        return input.substring(start, end);
    }

    private void deleteSelection() {
        if (!hasSelection()) return;
        int start = Math.min(selectionStart, selectionEnd);
        int end = Math.max(selectionStart, selectionEnd);
        input = input.substring(0, start) + input.substring(end);
        cursorPos = start;
        clearSelection();
    }

    private void updateSelection(boolean shift) {
        if (shift) {
            selectionEnd = cursorPos;
        } else {
            selectionStart = selectionEnd = cursorPos;
        }
        adjustScroll();
        showCursor = true;
        lastBlinkTime = System.currentTimeMillis();
    }

    private void clearSelection() {
        selectionStart = selectionEnd = cursorPos;
        adjustScroll();
    }

    public String getTextOrDefault() {
        return input.isEmpty() ? defaultInput : input;
    }

    public void setDefaultInput(String text) {
        this.defaultInput = text;
    }

    public void setText(String text) {
        this.input = text == null ? "" : text;
        this.cursorPos = this.selectionStart = this.selectionEnd = this.input.length();
        adjustScroll();
    }

    public String getInput() {
        return input;
    }

    public TextInputWidget setEditable(boolean editable) {
        this.editable = editable;
        return this;
    }

    public boolean isEditable() {
        return editable;
    }

    public int getCursorPos() {
        return cursorPos;
    }

    public int getSelectionStart() {
        return selectionStart;
    }

    public int getSelectionEnd() {
        return selectionEnd;
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public boolean shouldShowCursor() {
        return showCursor;
    }

    public String getDefaultInput() {
        return defaultInput;
    }

    private int getCharIndexFromX(int relX) {
        if (relX <= 0) return 0;
        int currentWidth = 0;
        for (int i = 0; i < input.length(); i++) {
            int charWidth = mc.textRenderer.getWidth(String.valueOf(input.charAt(i)));
            if (currentWidth + charWidth / 2 >= relX) return i;
            currentWidth += charWidth;
        }
        return input.length();
    }
}
