package com.github.prests.noisyneighbors.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EntityVolumeSchema {
    private Double mainVolume;

    @JsonCreator
    public EntityVolumeSchema(@JsonProperty("mainVolume") Double mainVolume) {
        this.mainVolume = mainVolume;
    }

    public Double getMainVolume() {
        return mainVolume;
    }

    public void setMainVolume(Double mainVolume) {
        this.mainVolume = mainVolume;
    }
}
