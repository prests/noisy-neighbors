package com.github.prests.noisyneighbors.config;

import java.nio.file.Path;

public class GlobalEntityVolumeConfig extends BaseConfig<GlobalEntityVolumeSchema> {
    private static final String CONFIG_FILE_NAME = "global-entity-volume.json";
    private static final GlobalConfig globalConfig = GlobalConfig.getInstance();

    private static volatile GlobalEntityVolumeConfig instance;

    private GlobalEntityVolumeConfig(Path _configDir) {
        super(GlobalEntityVolumeSchema.class, GlobalEntityVolumeSchema::defaultConfig, _configDir, CONFIG_FILE_NAME, globalConfig.getLogger());
    }

    public static synchronized void initialize(Path _configDir) {
        if (instance == null) {
            instance = new GlobalEntityVolumeConfig(_configDir);
        } else {
            throw new IllegalStateException("GlobalEntityVolumeConfig is already initialized");
        }

        instance.initializeConfig();
    }

    public static GlobalEntityVolumeConfig getInstance() {
        if (instance == null) {
            throw new IllegalStateException("GlobalEntityVolumeConfig has not been initialized. Call initialize(Path) first.");
        }
        return instance;
    }
}
