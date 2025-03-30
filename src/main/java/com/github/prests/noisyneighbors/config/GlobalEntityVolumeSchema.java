package com.github.prests.noisyneighbors.config;

import com.github.prests.noisyneighbors.constant.EntityConstants;

import java.util.Map;

public class GlobalEntityVolumeSchema {
    private Map<String, EntityVolumeSchema> entityVolumes;

    public Map<String, EntityVolumeSchema> getEntityVolumes() {
        return entityVolumes;
    }

    public void setEntityVolumes(Map<String, EntityVolumeSchema> entityVolumes) {
        this.entityVolumes = entityVolumes;
    }

    public static GlobalEntityVolumeSchema defaultConfig() {
        GlobalEntityVolumeSchema config = new GlobalEntityVolumeSchema();

        config.entityVolumes = Map.of(
                EntityConstants.PIG_FRIENDLY_NAME, new EntityVolumeSchema(1.0)
        );

        return config;
    }
}
