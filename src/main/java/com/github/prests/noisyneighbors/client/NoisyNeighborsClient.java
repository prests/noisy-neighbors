package com.github.prests.noisyneighbors.client;

import com.github.prests.noisyneighbors.NoisyNeighbors;
import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import com.github.prests.noisyneighbors.client.zone.ZoneOutlines;
import com.github.prests.noisyneighbors.client.zone.ZoneSelection;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

public final class NoisyNeighborsClient implements ClientModInitializer {
  @Override public void onInitializeClient() {
    SettingsStore.load();
    MobSoundCatalog.load();
    ZoneOutlines.register();
    UseBlockCallback.EVENT.register(ZoneSelection::useBlock);
    UseItemCallback.EVENT.register(ZoneSelection::useItem);
    ClientTickEvents.END_CLIENT_TICK.register(ZoneSelection::tick);
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
      ZoneSelection.reset();
      NoisyNeighbors.LOGGER.debug("Noisy Neighbor cleared active selection");
    });
  }
}
