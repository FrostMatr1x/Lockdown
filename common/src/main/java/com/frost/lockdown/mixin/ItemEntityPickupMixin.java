package com.frost.lockdown.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.frost.lockdown.ItemPickupHandler;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(ItemEntity.class)
public class ItemEntityPickupMixin {

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void lockdown$onPlayerTouch(Player player, CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (player.level().isClientSide) {
            return;
        }
        if (ItemPickupHandler.handle(self.getItem(), player)) {
            ci.cancel();
        }
    }
}
