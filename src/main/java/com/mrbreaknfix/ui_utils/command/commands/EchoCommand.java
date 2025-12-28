/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file EchoCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.List;

import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;

public class EchoCommand extends BaseCommand {

    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        String message =
                String.join(
                        " ",
                        parsedArgs.stream().map(Object::toString).toArray(CharSequence[]::new));
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("echoed_message", message);
        return CommandResult.of(true, message, jsonBody);
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("echo", "Echoes the provided arguments back.")
                .then(ArgumentNode.argument("<text...>", "The text to be echoed."));
    }

    @Override
    public String manual() {
        return """
                NAME
                    echo - Echo the provided arguments

                SYNOPSIS
                    echo [text...]

                DESCRIPTION
                    Echoes the provided arguments back as a single string.

                EXAMPLES
                    echo Hello World
                """;
    }
}
