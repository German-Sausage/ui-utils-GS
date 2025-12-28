/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ClientConnectionMixin.java
 */
package com.mrbreaknfix.ui_utils.mixin;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import com.mrbreaknfix.ui_utils.event.Event;
import com.mrbreaknfix.ui_utils.event.events.PacketEvent;
import com.mrbreaknfix.ui_utils.packet.Mcfw;
import com.mrbreaknfix.ui_utils.packet.McfwFilterType;
import com.mrbreaknfix.ui_utils.utils.UIActions;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mrbreaknfix.ui_utils.UiUtils.eventManager;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    // todo: rewrite
    // called when sending any packet
    @Inject(at = @At("HEAD"), method = "sendImmediately", cancellable = true)
    public void onSendImmediately(
            Packet<?> packet,
            @Nullable ChannelFutureListener channelFutureListener,
            boolean flush,
            CallbackInfo ci) {
        // checks for if packets should be sent and if the packet is a gui related packet
        if (!UIActions.shouldSendPackets()
                && (packet instanceof ClickSlotC2SPacket
                        || packet instanceof ButtonClickC2SPacket)) {
            ci.cancel();
            return;
        }

        // checks for if packets should be delayed and if the packet is a gui related packet and is
        // added to a list
        if (UIActions.shouldDelayPackets()
                && (packet instanceof ClickSlotC2SPacket
                        || packet instanceof ButtonClickC2SPacket)) {
            UIActions.delayedUIPackets.add(packet);
            ci.cancel();
        }

        // cancels sign update packets if sign editing is disabled and re-enables sign editing
        if (!UIActions.shouldEditSign && (packet instanceof UpdateSignC2SPacket)) {
            UIActions.shouldEditSign = true;
            ci.cancel();
        }
    }

    @Inject(
            method =
                    "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/network/ClientConnection;handlePacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;)V",
                            shift = At.Shift.BEFORE),
            cancellable = true)
    private void onHandlePacket(
            ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        PacketEvent.Receive e = new PacketEvent.Receive(packet, (ClientConnection) (Object) this);
        eventManager.trigger(e);
        if (e.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(
            at = @At("HEAD"),
            method =
                    "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;)V",
            cancellable = true)
    private void onSendPacketHead(
            Packet<?> packet,
            @Nullable ChannelFutureListener channelFutureListener,
            CallbackInfo ci) {
        Event e = new PacketEvent.Send(packet, (ClientConnection) (Object) this);
        eventManager.trigger(e);
        if (e.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(
            method =
                    "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;)V",
            at = @At("TAIL"))
    private void onSendPacketTail(
            Packet<?> packet,
            @Nullable ChannelFutureListener channelFutureListener,
            CallbackInfo ci) {
        Event e = new PacketEvent.Sent(packet, (ClientConnection) (Object) this);
        eventManager.trigger(e);
    }

    @Inject(
            method = "send(Lnet/minecraft/network/packet/Packet;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        McfwFilterType result = Mcfw.handlePacket(packet);

        if (result == McfwFilterType.DROP || result == McfwFilterType.DELAY) {
            ci.cancel();
        }
    }
}
