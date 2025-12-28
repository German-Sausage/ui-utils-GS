/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ColorPickerWidget.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mrbreaknfix.ui_utils.gui.GuiTheme;
import com.mrbreaknfix.ui_utils.gui.widget.Typeable;
import com.mrbreaknfix.ui_utils.gui.widget.Widget;
import com.mrbreaknfix.ui_utils.utils.GuiUtils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.ColorHelper;

import org.lwjgl.glfw.GLFW;

public class ColorPickerWidget extends Widget implements Typeable {
    private final Consumer<Integer> onColorChange;
    private float hue, saturation, brightness, alpha;
    private boolean draggingSaturationValue = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;

    private final int colorSquareX;
    private final int colorSquareY;
    private final int colorSquareSize;
    private final int hueSliderX;
    private final int hueSliderY;
    private final int hueSliderWidth;
    private final int hueSliderHeight;
    private final int alphaSliderX;
    private final int alphaSliderY;
    private final int alphaSliderWidth;
    private final int alphaSliderHeight;
    private final int previewBoxX;
    private final int previewBoxY;
    private final int previewBoxSize;

    private final HexInputWidget hexInput;

    public ColorPickerWidget(
            String id,
            int x,
            int y,
            int width,
            int height,
            int initialColor,
            Consumer<Integer> onColorChange) {
        super(id, x, y, width, height, null);
        this.onColorChange = onColorChange;

        final int PADDING = 5;
        this.colorSquareSize = 85;
        this.colorSquareX = x + PADDING;
        this.colorSquareY = y + PADDING;
        this.hueSliderWidth = 15;
        this.hueSliderHeight = this.colorSquareSize;
        this.hueSliderX = this.colorSquareX + this.colorSquareSize + PADDING;
        this.hueSliderY = this.colorSquareY;
        this.alphaSliderWidth = 15;
        this.alphaSliderHeight = this.colorSquareSize;
        this.alphaSliderX = this.hueSliderX + this.hueSliderWidth + PADDING;
        this.alphaSliderY = this.colorSquareY;
        this.previewBoxSize = 25;
        this.previewBoxX = x + PADDING;
        this.previewBoxY = this.colorSquareY + this.colorSquareSize + PADDING * 2;
        int hexInputHeight = 20;
        int hexInputX = this.previewBoxX + this.previewBoxSize + PADDING;
        int hexInputY = this.previewBoxY + (this.previewBoxSize - hexInputHeight) / 2;
        int hexInputWidth = (x + width) - hexInputX - PADDING;

        this.hexInput =
                new HexInputWidget(
                        "hex_input", hexInputX, hexInputY, hexInputWidth, hexInputHeight);
        this.addChild(this.hexInput);
        this.hexInput.setOnEnter(this::setColor);

        setColor(initialColor);
    }

    private void setColor(int argb) {
        this.alpha = ColorHelper.getAlpha(argb) / 255.0f;
        float[] hsb =
                Color.RGBtoHSB(
                        ColorHelper.getRed(argb),
                        ColorHelper.getGreen(argb),
                        ColorHelper.getBlue(argb),
                        null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.hexInput.setText(String.format("%08X", argb));
        onColorChange.accept(argb);
    }

    private int getCurrentColor() {
        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        int alphaInt = (int) (this.alpha * 255);
        return (alphaInt << 24) | (rgb & 0x00FFFFFF);
    }

    @Override
    public void render(
            DrawContext context, Screen screen, double mouseX, double mouseY, float deltaTicks) {
        if (!isVisible()) return;

        context.fill(x, y, x + width, y + height, GuiTheme.WIDGET_BACKGROUND);

        boolean isChildFocused = this.hexInput.isFocused();
        int borderColor =
                isChildFocused
                        ? GuiTheme.WIDGET_BORDER_FOCUSED
                        : (isMouseOver(mouseX, mouseY)
                                ? GuiTheme.WIDGET_BORDER_HOVER
                                : GuiTheme.WIDGET_BORDER);
        GuiUtils.drawBorder(context, x, y, width, height, borderColor);

        int hueColor = Color.HSBtoRGB(hue, 1.0f, 1.0f);
        drawColorSquare(
                context, colorSquareX, colorSquareY, colorSquareSize, colorSquareSize, hueColor);
        drawHueSlider(context, hueSliderX, hueSliderY, hueSliderWidth, hueSliderHeight);
        drawAlphaSlider(
                context,
                alphaSliderX,
                alphaSliderY,
                alphaSliderWidth,
                alphaSliderHeight,
                getCurrentColor());
        drawCheckerboard(context, previewBoxX, previewBoxY, previewBoxSize, previewBoxSize);
        context.fill(
                previewBoxX,
                previewBoxY,
                previewBoxX + previewBoxSize,
                previewBoxY + previewBoxSize,
                getCurrentColor());
        GuiUtils.drawBorder(
                context,
                previewBoxX,
                previewBoxY,
                previewBoxSize,
                previewBoxSize,
                GuiTheme.WIDGET_BORDER);
        hexInput.render(context, screen, mouseX, mouseY, deltaTicks);
        float markerX = colorSquareX + (saturation * colorSquareSize);
        float markerY = colorSquareY + ((1 - brightness) * colorSquareSize);
        GuiUtils.drawBorder(context, (int) markerX - 2, (int) markerY - 2, 4, 4, 0xFF000000);
        GuiUtils.drawBorder(context, (int) markerX - 3, (int) markerY - 3, 6, 6, 0xFFFFFFFF);
        float hueMarkerY = hueSliderY + (hue * hueSliderHeight);
        GuiUtils.drawBorder(
                context, hueSliderX - 1, (int) hueMarkerY - 2, hueSliderWidth + 2, 3, 0xFFFFFFFF);
        float alphaMarkerY = alphaSliderY + ((1 - alpha) * alphaSliderHeight);
        GuiUtils.drawBorder(
                context,
                alphaSliderX - 1,
                (int) alphaMarkerY - 2,
                alphaSliderWidth + 2,
                3,
                0xFFFFFFFF);
    }

    private void drawColorSquare(
            DrawContext context, int x, int y, int width, int height, int hueColor) {
        for (int i = 0; i < width; i++) {
            float ratio = (float) i / (float) (width - 1);
            int interpolatedColor = ColorHelper.lerp(ratio, 0xFFFFFFFF, hueColor);
            context.fill(x + i, y, x + i + 1, y + height, interpolatedColor);
        }
        context.fillGradient(x, y, x + width, y + height, 0x00000000, 0xFF000000);
    }

    private void drawHueSlider(DrawContext context, int x, int y, int width, int height) {
        for (int i = 0; i < height; i++) {
            context.fill(
                    x, y + i, x + width, y + i + 1, Color.HSBtoRGB((float) i / height, 1.0f, 1.0f));
        }
    }

    private void drawAlphaSlider(
            DrawContext context, int x, int y, int width, int height, int color) {
        int colorNoAlpha = color & 0x00FFFFFF;
        drawCheckerboard(context, x, y, width, height);
        context.fillGradient(x, y, x + width, y + height, 0xFF000000 | colorNoAlpha, colorNoAlpha);
    }

    private void drawCheckerboard(DrawContext context, int x, int y, int width, int height) {
        int squareSize = 4;
        for (int row = 0; row * squareSize < height; row++) {
            for (int col = 0; col * squareSize < width; col++) {
                int currentX = x + col * squareSize;
                int currentY = y + row * squareSize;
                int endX = Math.min(currentX + squareSize, x + width);
                int endY = Math.min(currentY + squareSize, y + height);
                boolean isLight = (row + col) % 2 == 0;
                context.fill(currentX, currentY, endX, endY, isLight ? 0xFFFFFFFF : 0xFFC0C0C0);
            }
        }
    }

    @Override
    public void onMouseClick(double mouseX, double mouseY, double button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;

        if (mouseX >= colorSquareX
                && mouseX <= colorSquareX + colorSquareSize
                && mouseY >= colorSquareY
                && mouseY <= colorSquareY + colorSquareSize) {
            draggingSaturationValue = true;
            updateSaturationValue(mouseX, mouseY);
        } else if (mouseX >= hueSliderX
                && mouseX <= hueSliderX + hueSliderWidth
                && mouseY >= hueSliderY
                && mouseY <= hueSliderY + hueSliderHeight) {
            draggingHue = true;
            updateHue(mouseY);
        } else if (mouseX >= alphaSliderX
                && mouseX <= alphaSliderX + alphaSliderWidth
                && mouseY >= alphaSliderY
                && mouseY <= alphaSliderY + alphaSliderHeight) {
            draggingAlpha = true;
            updateAlpha(mouseY);
        }
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, double button) {
        draggingSaturationValue = false;
        draggingHue = false;
        draggingAlpha = false;
        hexInput.onMouseRelease(mouseX, mouseY, button);
    }

    @Override
    public void update(double mouseX, double mouseY, float deltaTicks) {
        hexInput.update(mouseX, mouseY, deltaTicks);

        if (draggingSaturationValue) updateSaturationValue(mouseX, mouseY);
        if (draggingHue) updateHue(mouseY);
        if (draggingAlpha) updateAlpha(mouseY);
    }

    @Override
    public boolean onKey(int key, int scancode, int modifiers) {
        if (hexInput.isFocused()) {
            return hexInput.onKey(key, scancode, modifiers);
        }
        return false;
    }

    @Override
    public boolean onChar(int codePoint, int modifiers) {
        if (hexInput.isFocused()) {
            return hexInput.onChar(codePoint, modifiers);
        }
        return false;
    }

    @Override
    public boolean onInput(Function<String, String> factory) {
        if (hexInput.isFocused()) {
            return hexInput.onInput(factory);
        }
        return false;
    }

    @Override
    public boolean shouldChangeCursor() {
        return false;
    }

    private void triggerColorChange() {
        int currentColor = getCurrentColor();
        hexInput.setText(String.format("%08X", currentColor));
        onColorChange.accept(currentColor);
    }

    private void updateSaturationValue(double mouseX, double mouseY) {
        this.saturation =
                (float) Math.max(0, Math.min(1, (mouseX - colorSquareX) / colorSquareSize));
        this.brightness =
                (float) (1.0 - Math.max(0, Math.min(1, (mouseY - colorSquareY) / colorSquareSize)));
        triggerColorChange();
    }

    private void updateHue(double mouseY) {
        this.hue = (float) Math.max(0, Math.min(1, (mouseY - hueSliderY) / hueSliderHeight));
        triggerColorChange();
    }

    private void updateAlpha(double mouseY) {
        this.alpha =
                (float)
                        (1.0
                                - Math.max(
                                        0,
                                        Math.min(1, (mouseY - alphaSliderY) / alphaSliderHeight)));
        triggerColorChange();
    }
}
