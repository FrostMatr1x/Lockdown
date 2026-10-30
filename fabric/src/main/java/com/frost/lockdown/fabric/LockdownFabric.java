package com.frost.lockdown.fabric;

import com.frost.lockdown.common.CommonInit;
import net.fabricmc.api.ModInitializer;

public final class LockdownFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CommonInit.init();
    }
}
