package com.github.prests.noisyneighbors.config;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class GlobalEntityVolumeConfig extends BaseConfig<GlobalEntityVolumeSchema> {
    private static final String CONFIG_FILE_NAME = "global-entity-volume.json";
    private static final GlobalConfig globalConfig = GlobalConfig.getInstance();

    private GlobalEntityVolumeConfig(Path _configDir) {
        super(GlobalEntityVolumeSchema.class, GlobalEntityVolumeSchema::defaultConfig, _configDir, CONFIG_FILE_NAME, globalConfig.getLogger());
    }

    private static class Holder {
        private static final GlobalEntityVolumeConfig INSTANCE = new GlobalEntityVolumeConfig(FabricLoader.getInstance().getConfigDir().resolve(globalConfig.getModId()));

        static {
            INSTANCE.initializeConfig();
        }
    }

    public static GlobalEntityVolumeConfig getInstance() {
        return Holder.INSTANCE;
    }
}
