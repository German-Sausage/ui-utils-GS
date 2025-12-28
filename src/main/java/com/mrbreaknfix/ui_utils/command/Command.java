/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file Command.java
 */
package com.mrbreaknfix.ui_utils.command;

import java.util.List;
import java.util.Map;

public interface Command {
    boolean visible();

    CommandResult<?> execute(List<String> args);

    String manual();

    ArgumentNode getArgumentSchema();

    default Map<String, ArgumentNode[]> getFlagPools() {
        return null; // default
    }
}
