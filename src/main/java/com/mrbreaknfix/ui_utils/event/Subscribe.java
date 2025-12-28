/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file Subscribe.java
 */
package com.mrbreaknfix.ui_utils.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {}
