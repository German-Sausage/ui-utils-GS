/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file DisconnectCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;

import net.minecraft.text.Text;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class DisconnectCommand extends BaseCommand {

    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (mc.getNetworkHandler() == null) {
            return CommandResult.of(false, "Not connected to a server.");
        }

        mc.getNetworkHandler().getConnection().disconnect(Text.of("UI-Utils: Disconnected"));
        return CommandResult.of(true, "Disconnecting.");
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("disconnect", "Disconnects from the server.");
    }

    @Override
    public String manual() {
        return """
                NAME
                    disconnect - Disconnects from the current server.

                DESCRIPTION
                    Disconnects from the server.
                """;
    }
}
