package com.frost.lockdown.neoforge;

import com.frost.lockdown.LockdownCommands;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class NeoForgeCommandEvents {

    private NeoForgeCommandEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LockdownCommands.register(event.getDispatcher(), event.getBuildContext());
    }
}
