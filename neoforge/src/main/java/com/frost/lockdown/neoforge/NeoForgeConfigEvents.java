package com.frost.lockdown.neoforge;

import com.frost.lockdown.Config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;

public final class NeoForgeConfigEvents {

    private NeoForgeConfigEvents() {
    }

    @SubscribeEvent
    public static void onConfigLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != NeoForgeConfigAccess.SPEC) {
            return;
        }
        if (event instanceof ModConfigEvent.Unloading) {
            return;
        }
        Config.rebuildAll();
    }
}
