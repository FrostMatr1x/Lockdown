package com.frost.lockdown.effect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class UsedItemContext {

    private record Context(String itemId, long expireTick) {
    }

    private static final ThreadLocal<Map<UUID, Context>> CURRENT = ThreadLocal.withInitial(HashMap::new);

    private UsedItemContext() {
    }

    public static void set(Player player, ItemStack stack, int duration) {
        if (stack.isEmpty()) {
            return;
        }
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        long expireTick = player.level().getGameTime() + Math.max(duration, 0) + 10L;
        CURRENT.get().put(player.getUUID(), new Context(itemId, expireTick));
    }

    public static String getItemIdFor(Player player) {
        Map<UUID, Context> contexts = CURRENT.get();
        Context context = contexts.get(player.getUUID());
        if (context == null) {
            return null;
        }
        if (player.level().getGameTime() > context.expireTick()) {
            contexts.remove(player.getUUID());
            return null;
        }
        return context.itemId();
    }

    public static void clear(Player player) {
        CURRENT.get().remove(player.getUUID());
    }

    public static void clear() {
        CURRENT.remove();
    }
}
