package com.github.prests.noisyneighbors.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalConfig {
    private static final String MOD_ID = "noisy-neighbors";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private GlobalConfig() {}

    private static class Holder {
        private static final GlobalConfig INSTANCE = new GlobalConfig();
    }

    public static GlobalConfig getInstance() {
        return Holder.INSTANCE;
    }

    public Logger getLogger() {
        return LOGGER;
    }

    public String getModId() {
        return MOD_ID;
    }
}
