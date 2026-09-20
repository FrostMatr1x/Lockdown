package com.frost.lockdown;

import org.slf4j.Logger;

import com.frost.lockdown.effect.EffectLockerEvents;
import com.frost.lockdown.effect.LockedItemUseHandler;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Lockdown.MODID)
public class Lockdown {
    public static final String MODID = "lockdown";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Lockdown(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(GameEvents.class);
        NeoForge.EVENT_BUS.register(EffectLockerEvents.class);
        NeoForge.EVENT_BUS.register(LockedItemUseHandler.class);
        modEventBus.register(Config.class);

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC, "lockdown-server.toml");
    }
}
