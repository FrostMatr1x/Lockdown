package com.frost.lockdown;

import com.frost.lockdown.compat.BackpackScanner;
import com.frost.lockdown.compat.SophisticatedBackpacksCompat;
import com.frost.lockdown.config.ConfigAccess;
import com.frost.lockdown.effect.EffectLockerEvents;
import com.frost.lockdown.neoforge.NeoForgeCommandEvents;
import com.frost.lockdown.neoforge.NeoForgeConfigAccess;
import com.frost.lockdown.neoforge.NeoForgeConfigEvents;
import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Lockdown.MODID)
public class Lockdown {
    public static final String MODID = "lockdown";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Lockdown(IEventBus modEventBus, ModContainer modContainer) {
        ConfigAccess.set(NeoForgeConfigAccess.INSTANCE);

        JsonStorageManager.init(FMLPaths.CONFIGDIR.get());

        if (ModList.get().isLoaded("sophisticatedbackpacks")) {
            BackpackScanner.register(SophisticatedBackpacksCompat.INSTANCE);
        }

        NeoForge.EVENT_BUS.register(EffectLockerEvents.class);
        NeoForge.EVENT_BUS.register(NeoForgeCommandEvents.class);
        modEventBus.register(NeoForgeConfigEvents.class);

        modContainer.registerConfig(ModConfig.Type.SERVER, NeoForgeConfigAccess.SPEC, "lockdown-server.toml");
    }
}
