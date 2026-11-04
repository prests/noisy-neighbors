package com.github.prests.noisyneighbors.client.zone;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Draws enabled zones in the current dimension as depth-tested wireframes. */
public final class ZoneOutlines {
  private ZoneOutlines() {}

  public static void register() {
    WorldRenderEvents.AFTER_ENTITIES.register(ZoneOutlines::render);
  }

  private static void render(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {
    if (!SettingsStore.data().showZoneOutlines || context.matrixStack() == null || context.consumers() == null) return;
    WorldIdentity world = WorldIdentity.current(Minecraft.getInstance());
    SettingsStore.World settings = world == null ? null : SettingsStore.data().worlds.get(world.key());
    if (settings == null) return;

    String dimension = context.world().dimension().location().toString();
    Vec3 camera = context.camera().getPosition();
    PoseStack matrices = context.matrixStack();
    VertexConsumer vertices = context.consumers().getBuffer(RenderType.lines());
    matrices.pushPose();
    matrices.translate(-camera.x, -camera.y, -camera.z);
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
