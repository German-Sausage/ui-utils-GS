/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ColorPickerOpener.java
 */
package com.mrbreaknfix.ui_utils.gui.widget.widgets.setting;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mrbreaknfix.ui_utils.gui.widget.widgets.ColorButtonWidget;

@FunctionalInterface
public interface ColorPickerOpener {
    void open(
            ColorButtonWidget sourceButton,
            Supplier<Integer> colorGetter,
            Consumer<Integer> colorSetter);
}
