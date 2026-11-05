package com.github.prests.noisyneighbors;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NoisyNeighbors {
  public static final String MOD_ID = "noisy-neighbors";
  public static final String NEOFORGE_MOD_ID = "noisy_neighbors";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  private NoisyNeighbors() {}

  public static void initialize(Path configDirectory) {
    MobSoundCatalog.load();
    SettingsStore.initialize(configDirectory);
    SettingsStore.load();
    LOGGER.info("Noisy Neighbors initialized");
  }
}
