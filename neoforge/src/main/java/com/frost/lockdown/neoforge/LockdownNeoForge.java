package com.frost.lockdown.neoforge;

import com.frost.lockdown.common.CommonInit;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("lockdown")
public final class LockdownNeoForge {
    private static final Logger LOGGER = LoggerFactory.getLogger("lockdown");

    public LockdownNeoForge() {
        LOGGER.info("Lockdown NeoForge bootstrap");
        CommonInit.init();
    }
}
