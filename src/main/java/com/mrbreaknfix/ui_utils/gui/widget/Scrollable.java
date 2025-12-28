/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file Scrollable.java
 */
package com.mrbreaknfix.ui_utils.gui.widget;

public interface Scrollable {
    boolean onMouseScroll(
            double mouseX, double mouseY, double horizontalAmount, double verticalAmount);
}
