package com.frost.lockdown.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.frost.lockdown.Config;
import com.frost.lockdown.effect.EffectLockerConfig;
import com.frost.lockdown.effect.Notifications;
import com.frost.lockdown.effect.UsedItemContext;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(LivingEntity.class)
public abstract class LivingEntityEffectMixin {

    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), argsOnly = true, index = 1)
    private MobEffectInstance lockdown$capEffect(MobEffectInstance effectInstance) {
        if (effectInstance == null) {
            return null;
        }

        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide) {
            return effectInstance;
        }

        if (self instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                return effectInstance;
            }
            String usedItemId = UsedItemContext.getItemIdFor(player);
            if (usedItemId != null && EffectLockerConfig.isBypassed(effectInstance.getEffect(), usedItemId)) {
                return effectInstance;
            }
        }

        int cap = EffectLockerConfig.getMaxAmplifier(effectInstance.getEffect());
        int currentAmplifier = effectInstance.getAmplifier();
        if (cap < 0 || currentAmplifier <= cap) {
            return effectInstance;
        }

        MobEffectInstance capped = new MobEffectInstance(effectInstance.getEffect(),
                effectInstance.getDuration(), cap - 1, effectInstance.isAmbient(),
                effectInstance.isVisible(), effectInstance.showIcon());
        capped.copyBlendState(effectInstance);

        if (self instanceof Player player) {
            Notifications.send(player, Config.MSG_EFFECT_CAPPED,
                    "effect", effectId(effectInstance.getEffect()), "level", cap);
        }
        return capped;
    }

    private static String effectId(Holder<MobEffect> effect) {
        return effect.unwrapKey().map(key -> key.location().toString()).orElse("unknown");
    }
}
