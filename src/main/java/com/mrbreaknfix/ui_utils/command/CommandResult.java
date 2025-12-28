/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file CommandResult.java
 */
package com.mrbreaknfix.ui_utils.command;

import com.google.gson.JsonElement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CommandResult<T>(
        boolean success, String message, T data, @Nullable JsonElement jsonBody) {

    public static <T> CommandResult<T> of(
            boolean success, String message, T data, @Nullable JsonElement jsonBody) {
        return new CommandResult<>(success, message, data, jsonBody);
    }

    public static <T> CommandResult<T> of(boolean success, String message, T data) {
        return new CommandResult<>(success, message, data, null);
    }

    public static CommandResult<Void> of(
            boolean success, String message, @Nullable JsonElement jsonBody) {
        return new CommandResult<>(success, message, null, jsonBody);
    }

    public static CommandResult<Void> of(boolean success, String message) {
        return new CommandResult<>(success, message, null, null);
    }

    @Override
    public @NotNull String toString() {
        return (success ? "[SUCCESS] " : "[FAILURE] ")
                + message
                + (data != null ? " | Data: " + data : "");
    }
}
