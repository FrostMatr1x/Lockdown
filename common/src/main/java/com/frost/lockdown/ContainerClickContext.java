package com.frost.lockdown;

import net.minecraft.world.entity.player.Player;

public class ContainerClickContext {
    private static final ThreadLocal<Player> CURRENT_PLAYER = new ThreadLocal<>();

    public static void set(Player player) {
        CURRENT_PLAYER.set(player);
    }

    public static Player get() {
        return CURRENT_PLAYER.get();
    }

    public static void clear() {
        CURRENT_PLAYER.remove();
    }
}
