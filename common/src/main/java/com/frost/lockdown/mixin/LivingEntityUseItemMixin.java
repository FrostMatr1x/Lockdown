package com.frost.lockdown.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.frost.lockdown.effect.UsedItemContext;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(LivingEntity.class)
public abstract class LivingEntityUseItemMixin {

    @Inject(method = "startUsingItem", at = @At("RETURN"))
    private void lockdown$onStartUsingItem(InteractionHand hand, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide || !self.isUsingItem() || !(self instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = self.getUseItem();
        UsedItemContext.set(player, stack, self.getUseItemRemainingTicks());
    }

    @Inject(method = "completeUsingItem", at = @At("RETURN"))
    private void lockdown$onCompleteUsingItem(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player player) {
            UsedItemContext.clear(player);
        }
    }

    @Inject(method = "stopUsingItem", at = @At("HEAD"))
    private void lockdown$onStopUsingItem(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player player) {
            UsedItemContext.clear(player);
        }
    }
}
