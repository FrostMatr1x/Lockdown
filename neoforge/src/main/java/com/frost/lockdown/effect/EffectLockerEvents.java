package com.frost.lockdown.effect;

import com.frost.lockdown.Config;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class EffectLockerEvents {

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof Player player) {
            UsedItemContext.clear(player);
            Notifications.clear(player);
        }
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null) {
            return;
        }
        if (!EffectLockerConfig.isBanned(instance.getEffect())) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        String usedItemId = UsedItemContext.getItemIdFor(player);
        if (usedItemId != null && EffectLockerConfig.isBypassed(instance.getEffect(), usedItemId)) {
            return;
        }

        event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);

        String effectId = effectId(instance.getEffect());
        if (usedItemId != null) {
            Notifications.send(player, Config.MSG_ITEM_USE_BLOCKED, "item", usedItemId, "effect", effectId);
        } else {
            Notifications.send(player, Config.MSG_EFFECT_BLOCKED, "effect", effectId);
        }
    }

    private static String effectId(Holder<MobEffect> effect) {
        return effect.unwrapKey().map(key -> key.location().toString()).orElse("unknown");
    }
}
