package com.github.prests.noisyneighbors.client;

import com.github.prests.noisyneighbors.NoisyNeighbors;
import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class NoisyNeighborsClient implements ClientModInitializer {
  @Override public void onInitializeClient() {
    SettingsStore.load();
    MobSoundCatalog.load();
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> NoisyNeighbors.LOGGER.debug("Noisy Neighbor cleared active selection"));
  }
}
