/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file RPackCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;

import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;

import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class RPackCommand extends BaseCommand {

    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        String action = parsedArgs.getFirst().toString().toLowerCase();

        if (mc.getNetworkHandler() == null) {
            return CommandResult.of(false, "You are not connected to a server.");
        }

        ResourcePackStatusC2SPacket.Status status =
                switch (action) {
                    case "accepted" -> ResourcePackStatusC2SPacket.Status.ACCEPTED;
                    case "loaded" -> ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED;
                    case "declined" -> ResourcePackStatusC2SPacket.Status.DECLINED;
                    case "failed" -> ResourcePackStatusC2SPacket.Status.FAILED_DOWNLOAD;
                    default -> null;
                };

        if (status == null) {
            return CommandResult.of(false, "Invalid resource pack status specified.");
        }

        UUID playerUuid = Objects.requireNonNull(mc.getSession().getUuidOrNull());
        mc.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(playerUuid, status));

        return CommandResult.of(true, "Sent resource pack status packet: " + status.name());
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("rpack", "Resource pack bypass and interaction tool.")
                .then(
                        ArgumentNode.literal("accepted", "Report that the pack has been accepted."),
                        ArgumentNode.literal(
                                "loaded", "Report that the pack was successfully loaded."),
                        ArgumentNode.literal("declined", "Report that the pack has been declined."),
                        ArgumentNode.literal(
                                "failed", "Report that the pack download has failed."));
    }

    @Override
    public String manual() {
        return """
                NAME
                    rpack - A utility for responding to server resource pack requests.

                SYNOPSIS
                    rpack <status>

                DESCRIPTION
                    Sends a status packet to the server regarding a server-side resource pack.
                    This can be used to manually accept, decline, or fake the download
                    status of a resource pack sent by the server.

                ARGUMENTS
                    <status>
                        The status to send to the server. Must be one of the following:

                        accepted
                            Tells the server you have accepted the resource pack offer.

                        loaded
                            Tells the server you have successfully downloaded and applied the pack.

                        declined
                            Tells the server you have declined the resource pack offer.

                        failed
                            Tells the server that your client failed to download the resource pack.

                EXAMPLES
                    # Accept the server's resource pack
                    rpack accepted

                    # Tell the server you've successfully loaded it
                    rpack loaded
                """;
    }
}
