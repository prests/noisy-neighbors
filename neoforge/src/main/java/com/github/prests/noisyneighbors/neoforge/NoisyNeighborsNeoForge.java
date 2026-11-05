package com.github.prests.noisyneighbors.neoforge;

import com.github.prests.noisyneighbors.NoisyNeighbors;
import com.github.prests.noisyneighbors.client.NoisyNeighborsKeyMappings;
import com.github.prests.noisyneighbors.client.zone.ZoneOutlines;
import com.github.prests.noisyneighbors.client.zone.ZoneSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(value = "noisy_neighbors", dist = Dist.CLIENT)
public final class NoisyNeighborsNeoForge {
  public NoisyNeighborsNeoForge(IEventBus modBus) {
    NoisyNeighbors.initialize(FMLPaths.CONFIGDIR.get());
    modBus.addListener(this::registerKeyMappings);
    NeoForge.EVENT_BUS.addListener(this::clientTick);
    NeoForge.EVENT_BUS.addListener(this::loggingOut);
    NeoForge.EVENT_BUS.addListener(this::rightClickBlock);
    NeoForge.EVENT_BUS.addListener(this::renderLevel);
  }

  private void registerKeyMappings(RegisterKeyMappingsEvent event) {
    NoisyNeighborsKeyMappings.register(event::register);
  }

  private void clientTick(ClientTickEvent.Post event) {
    NoisyNeighborsKeyMappings.tick(Minecraft.getInstance());
  }

  private void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
    ZoneSelection.reset();
    NoisyNeighbors.LOGGER.debug("Noisy Neighbors cleared active selection");
  }

  private void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    InteractionResult result = ZoneSelection.useBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
    if (result != InteractionResult.PASS) {
      event.setCancellationResult(result);
      event.setCanceled(true);
    }
  }

  private void renderLevel(RenderLevelStageEvent event) {
    Minecraft minecraft = Minecraft.getInstance();
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || minecraft.level == null) return;
    var buffers = minecraft.renderBuffers().bufferSource();
    ZoneOutlines.render(minecraft.level, event.getCamera(), event.getPoseStack(), buffers);
    buffers.endBatch(RenderType.lines());
  }
}
