package com.github.prests.noisyneighbors.fabric;

import com.github.prests.noisyneighbors.NoisyNeighbors;
import com.github.prests.noisyneighbors.client.NoisyNeighborsKeyMappings;
import com.github.prests.noisyneighbors.client.zone.ZoneOutlines;
import com.github.prests.noisyneighbors.client.zone.ZoneSelection;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;

public final class NoisyNeighborsFabricClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    NoisyNeighbors.initialize(FabricLoader.getInstance().getConfigDir());
    NoisyNeighborsKeyMappings.register(KeyBindingHelper::registerKeyBinding);
    UseBlockCallback.EVENT.register(ZoneSelection::useBlock);
    ClientTickEvents.END_CLIENT_TICK.register(NoisyNeighborsKeyMappings::tick);
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
      ZoneSelection.reset();
      NoisyNeighbors.LOGGER.debug("Noisy Neighbors cleared active selection");
    });
    WorldRenderEvents.AFTER_ENTITIES.register(context -> {
      if (context.matrixStack() != null && context.consumers() != null) {
        ZoneOutlines.render(context.world(), context.camera(), context.matrixStack(), context.consumers());
      }
    });
  }
}
