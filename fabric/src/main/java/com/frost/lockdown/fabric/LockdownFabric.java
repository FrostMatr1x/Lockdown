package com.frost.lockdown.fabric;

import java.nio.file.Path;

import com.frost.lockdown.Config;
import com.frost.lockdown.JsonStorageManager;
import com.frost.lockdown.LockdownCommands;
import com.frost.lockdown.common.CommonInit;
import com.frost.lockdown.config.ConfigAccess;
import com.frost.lockdown.effect.Notifications;
import com.frost.lockdown.effect.UsedItemContext;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

public final class LockdownFabric implements ModInitializer {

    private FabricConfigAccess configAccess;

    @Override
    public void onInitialize() {
        Path configDir = FabricLoader.getInstance().getConfigDir();

        configAccess = new FabricConfigAccess(configDir);
        ConfigAccess.set(configAccess);
        JsonStorageManager.init(configDir);

        CommonInit.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                LockdownCommands.register(dispatcher, registryAccess));

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            configAccess.load();
            Config.rebuildAll();
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UsedItemContext.clear(handler.player);
            Notifications.clear(handler.player);
        });
    }
}
