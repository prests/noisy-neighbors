package com.github.prests.noisyneighbors.fabric;

import com.github.prests.noisyneighbors.NoisyNeighbors;
import com.github.prests.noisyneighbors.client.NoisyNeighborsKeyMappings;
import com.github.prests.noisyneighbors.client.zone.ZoneOutlines;
import com.github.prests.noisyneighbors.client.zone.ZoneSelection;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;

public final class NoisyNeighborsFabricClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    NoisyNeighbors.initialize(FabricLoader.getInstance().getConfigDir());
    NoisyNeighborsKeyMappings.register(category -> {}, KeyMappingHelper::registerKeyMapping);
    UseBlockCallback.EVENT.register(ZoneSelection::useBlock);
    ClientTickEvents.END_CLIENT_TICK.register(NoisyNeighborsKeyMappings::tick);
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
      ZoneSelection.reset();
      NoisyNeighbors.LOGGER.debug("Noisy Neighbors cleared active selection");
    });
    LevelExtractionEvents.END_EXTRACTION.register(context -> ZoneOutlines.extract(context.level()));
    LevelRenderEvents.END_MAIN.register(context -> ZoneOutlines.render(context.poseStack(), context.levelState()));
  }
}
