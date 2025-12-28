/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file CloseCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;
import com.mrbreaknfix.ui_utils.utils.UIActions;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class CloseCommand extends BaseCommand {
    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        mc.execute(UIActions::CWoP);
        return CommandResult.of(true, "Closed current screen without sending a packet.");
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("close", "Closes the current screen without sending a packet.");
    }

    @Override
    public String manual() {
        return """
                NAME
                    close - Soft close the current screen

                DESCRIPTION
                    Closes the current screen on the client side without sending a
                    CloseHandledScreenC2SPacket to the server. This is the opposite of 'desync'.
                """;
    }
}
