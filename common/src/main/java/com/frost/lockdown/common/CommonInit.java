package com.frost.lockdown.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CommonInit {
    private static final Logger LOGGER = LoggerFactory.getLogger("lockdown-common");

    private CommonInit() {
    }

    public static void init() {
        LOGGER.info("Lockdown common initialized");
    }
}
