/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ClearCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.List;

import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;

public class ClearCommand extends BaseCommand {
    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("action", "clear_terminal");

        return CommandResult.of(true, "Terminal cleared.", jsonBody);
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("clear", "Clear the terminal screen.");
    }

    @Override
    public String manual() {
        return """
                NAME
                    clear - Clear the terminal screen

                DESCRIPTION
                    Clears the terminal screen, removing all previous output.
                """;
    }
}
