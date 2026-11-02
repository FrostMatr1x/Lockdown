package com.frost.lockdown.effect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.frost.lockdown.Messages;
import com.frost.lockdown.config.Setting;

import net.minecraft.world.entity.player.Player;

public final class Notifications {

    private static final long COOLDOWN_TICKS = 40L;
    private static final Map<String, Long> COOLDOWN = new ConcurrentHashMap<>();

    private Notifications() {
    }

    public static void send(Player player, Setting<String> key, Object... placeholders) {
        String notifyKey = player.getUUID() + "|" + key.id();
        long now = player.level().getGameTime();
        Long last = COOLDOWN.get(notifyKey);
        if (last != null && now - last < COOLDOWN_TICKS) {
            return;
        }
        COOLDOWN.put(notifyKey, now);
        Messages.send(player, key, placeholders);
    }

    public static void clear(Player player) {
        COOLDOWN.keySet().removeIf(key -> key.startsWith(player.getUUID() + "|"));
    }
}
