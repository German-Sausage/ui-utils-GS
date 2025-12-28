/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file WindowCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.nio.IntBuffer;
import java.util.List;

import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;

import net.minecraft.client.util.Window;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class WindowCommand extends BaseCommand {
    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (parsedArgs.isEmpty()) {
            return CommandResult.of(false, "Usage: window <action> [args]");
        }

        String action = parsedArgs.getFirst().toString().toLowerCase();
        Window window = mc.getWindow();
        long handle = window.getHandle();

        return switch (action) {
            case "alwaysontop" -> {
                if (parsedArgs.size() < 2)
                    yield CommandResult.of(false, "Usage: window alwaysontop <true|false>");
                boolean enable = Boolean.parseBoolean(parsedArgs.get(1).toString());
                mc.execute(
                        () ->
                                GLFW.glfwSetWindowAttrib(
                                        handle,
                                        GLFW.GLFW_FLOATING,
                                        enable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE));

                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("alwaysOnTop", enable);
                yield CommandResult.of(true, "Set always-on-top to: " + enable, jsonBody);
            }

            case "minimize" -> {
                mc.execute(() -> GLFW.glfwIconifyWindow(handle));
                yield CommandResult.of(true, "Window minimized.");
            }

            case "maximize" -> {
                mc.execute(() -> GLFW.glfwMaximizeWindow(handle));
                yield CommandResult.of(true, "Window maximized.");
            }

            case "restore" -> {
                mc.execute(() -> GLFW.glfwRestoreWindow(handle));
                yield CommandResult.of(true, "Window restored.");
            }

            case "focus" -> {
                mc.execute(() -> GLFW.glfwFocusWindow(handle));
                yield CommandResult.of(true, "Window focused.");
            }

            case "settitle" -> {
                if (parsedArgs.size() < 2)
                    yield CommandResult.of(false, "Usage: window settitle <title>");
                String title =
                        parsedArgs.subList(1, parsedArgs.size()).stream()
                                .map(Object::toString)
                                .reduce((a, b) -> a + " " + b)
                                .orElse("");
                mc.execute(() -> GLFW.glfwSetWindowTitle(handle, title));

                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("title", title);
                yield CommandResult.of(true, "Window title set to: \"" + title + "\"", jsonBody);
            }

            case "setsize" -> {
                if (parsedArgs.size() < 3)
                    yield CommandResult.of(false, "Usage: window setsize <width> <height>");
                try {
                    int width = Integer.parseInt(parsedArgs.get(1).toString());
                    int height = Integer.parseInt(parsedArgs.get(2).toString());
                    mc.execute(() -> GLFW.glfwSetWindowSize(handle, width, height));

                    JsonObject jsonBody = new JsonObject();
                    jsonBody.addProperty("width", width);
                    jsonBody.addProperty("height", height);
                    yield CommandResult.of(
                            true, "Window size set to: " + width + "x" + height, jsonBody);
                } catch (NumberFormatException e) {
                    yield CommandResult.of(false, "Width and height must be integers.");
                }
            }

            case "setposition" -> {
                if (parsedArgs.size() < 3)
                    yield CommandResult.of(false, "Usage: window setposition <x> <y>");
                try {
                    int x = Integer.parseInt(parsedArgs.get(1).toString());
                    int y = Integer.parseInt(parsedArgs.get(2).toString());
                    mc.execute(() -> GLFW.glfwSetWindowPos(handle, x, y));

                    JsonObject jsonBody = new JsonObject();
                    jsonBody.addProperty("x", x);
                    jsonBody.addProperty("y", y);
                    yield CommandResult.of(
                            true, "Window position set to: " + x + ", " + y, jsonBody);
                } catch (NumberFormatException e) {
                    yield CommandResult.of(false, "X and Y must be integers.");
                }
            }

            case "getsize" -> {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    IntBuffer widthBuffer = stack.mallocInt(1);
                    IntBuffer heightBuffer = stack.mallocInt(1);
                    GLFW.glfwGetWindowSize(handle, widthBuffer, heightBuffer);
                    int width = widthBuffer.get(0);
                    int height = heightBuffer.get(0);

                    JsonObject jsonBody = new JsonObject();
                    jsonBody.addProperty("width", width);
                    jsonBody.addProperty("height", height);
                    yield CommandResult.of(true, "Window size: " + width + "x" + height, jsonBody);
                }
            }

            case "getposition" -> {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    IntBuffer xBuffer = stack.mallocInt(1);
                    IntBuffer yBuffer = stack.mallocInt(1);
                    GLFW.glfwGetWindowPos(handle, xBuffer, yBuffer);
                    int x = xBuffer.get(0);
                    int y = yBuffer.get(0);

                    JsonObject jsonBody = new JsonObject();
                    jsonBody.addProperty("x", x);
                    jsonBody.addProperty("y", y);
                    yield CommandResult.of(true, "Window position: " + x + ", " + y, jsonBody);
                }
            }

            default -> CommandResult.of(false, "Unknown window action: " + action);
        };
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("window", "Control game window behavior.")
                .then(
                        ArgumentNode.literal(
                                        "alwaysontop",
                                        "Sets whether the window should float above others.")
                                .then(
                                        ArgumentNode.literal("true", "Enable always on top."),
                                        ArgumentNode.literal("false", "Disable always on top.")),
                        ArgumentNode.literal("minimize", "Minimizes the window."),
                        ArgumentNode.literal("maximize", "Maximizes the window."),
                        ArgumentNode.literal(
                                "restore", "Restores the window from minimized/maximized state."),
                        ArgumentNode.literal("focus", "Grabs the focus of the window."),
                        ArgumentNode.literal("settitle", "Changes the window title.")
                                .then(
                                        ArgumentNode.argument(
                                                "<title...>", "The new title for the window.")),
                        ArgumentNode.literal("setsize", "Sets the window size in pixels.")
                                .then(ArgumentNode.argument("<width>", "The new width."))
                                .then(ArgumentNode.argument("<height>", "The new height.")),
                        ArgumentNode.literal(
                                        "setposition", "Sets the window position on the screen.")
                                .then(ArgumentNode.argument("<x>", "The new X coordinate."))
                                .then(ArgumentNode.argument("<y>", "The new Y coordinate.")),
                        ArgumentNode.literal(
                                "getsize", "Gets the current window size (width x height)."),
                        ArgumentNode.literal(
                                "getposition", "Gets the current window position (x, y)."));
    }

    @Override
    public String manual() {
        return """
                NAME
                    window - Control game window behavior

                SYNOPSIS
                    window <action> [arguments]

                ACTIONS
                    alwaysontop <true|false>     Sets whether the window should float above others.
                    minimize                     Minimizes the window.
                    maximize                     Maximizes the window.
                    restore                      Restores the window from minimized/maximized.
                    focus                        Gives focus to the window.
                    settitle <title>             Changes the window title.
                    setsize <width> <height>     Sets the window size (in pixels).
                    setposition <x> <y>          Sets the window position (in pixels).
                    getsize                      Gets the current window size (width x height).
                    getposition                  Gets the current window position (x, y).

                EXAMPLES
                    window alwaysontop true
                    window settitle My Cool Mod
                    window setsize 1280 720
                """;
    }
}
