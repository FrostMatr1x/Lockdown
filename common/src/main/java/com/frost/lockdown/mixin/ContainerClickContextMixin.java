package com.frost.lockdown.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.frost.lockdown.ContainerClickContext;

import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Mixin(ServerGamePacketListenerImpl.class)
public class ContainerClickContextMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleContainerClick", at = @At("HEAD"))
    private void itemlocker$setContext(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        ContainerClickContext.set(this.player);
    }

    @Inject(method = "handleContainerClick", at = @At("RETURN"))
    private void itemlocker$clearContext(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        ContainerClickContext.clear();
    }
}