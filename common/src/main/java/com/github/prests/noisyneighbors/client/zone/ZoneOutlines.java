package com.github.prests.noisyneighbors.client.zone;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Draws enabled zones in the current dimension as depth-tested wireframes. */
public final class ZoneOutlines {
  private ZoneOutlines() {}

  public static void render(ClientLevel level, Camera camera, PoseStack matrices, MultiBufferSource consumers) {
    if (!SettingsStore.data().showZoneOutlines) return;
    WorldIdentity world = WorldIdentity.current(Minecraft.getInstance());
    SettingsStore.World settings = world == null ? null : SettingsStore.data().worlds.get(world.key());
    if (settings == null) return;

    String dimension = level.dimension().location().toString();
    Vec3 cameraPosition = camera.getPosition();
    var vertices = consumers.getBuffer(RenderType.lines());
    matrices.pushPose();
    matrices.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
    for (Zone zone : settings.zones) {
      if (!zone.enabled() || !zone.dimension().equals(dimension)) continue;
      int color = zone.color();
      LevelRenderer.renderLineBox(matrices, vertices,
          new AABB(zone.minX(), zone.minY(), zone.minZ(), zone.maxX() + 1D, zone.maxY() + 1D, zone.maxZ() + 1D),
          ((color >> 16) & 0xFF) / 255F, ((color >> 8) & 0xFF) / 255F, (color & 0xFF) / 255F, .9F);
    }
    matrices.popPose();
  }
}
