package com.github.prests.noisyneighbors.client.zone;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Draws enabled zones in the current dimension as visible, depth-tested wireframes. */
public final class ZoneOutlines {
  private static final Vector4f COLOR_MODULATOR = new Vector4f(1F, 1F, 1F, 1F);
  private static final Vector3f MODEL_OFFSET = new Vector3f();
  private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
  private static final StagedVertexBuffer BUFFER = new StagedVertexBuffer(() -> "Noisy Neighbor zone outlines", RenderType.SMALL_BUFFER_SIZE);
  private static List<Bounds> zones = List.of();

  private ZoneOutlines() {}

  public static void register() {
    LevelExtractionEvents.END_EXTRACTION.register(ZoneOutlines::extract);
    LevelRenderEvents.END_MAIN.register(ZoneOutlines::render);
  }

  public static void close() {
    BUFFER.close();
  }

  private static void extract(LevelExtractionContext context) {
    if (!SettingsStore.data().showZoneOutlines) {
      zones = List.of();
      return;
    }
    WorldIdentity world = WorldIdentity.current(Minecraft.getInstance());
    SettingsStore.World settings = world == null ? null : SettingsStore.data().worlds.get(world.key());
    if (settings == null) {
      zones = List.of();
      return;
    }
    String dimension = context.level().dimension().identifier().toString();
    zones = settings.zones.stream().filter(zone -> zone.enabled() && zone.dimension().equals(dimension))
        .sorted(java.util.Comparator.comparing(zone -> zone.id().toString()))
        .map(zone -> new Bounds(zone.minX(), zone.minY(), zone.minZ(), zone.maxX() + 1, zone.maxY() + 1, zone.maxZ() + 1, zone.color())).toList();
  }

  private static void render(LevelRenderContext context) {
    if (zones.isEmpty()) return;
    RenderPipeline pipeline = RenderPipelines.LINES;
    VertexFormat binding = pipeline.getVertexFormatBinding(0);
    if (binding == null) return;
    StagedVertexBuffer.Draw draw = BUFFER.appendDraw(binding, PrimitiveTopology.LINES, null);
    PoseStack matrices = context.poseStack();
    Vec3 camera = context.levelState().cameraRenderState.pos;
    matrices.pushPose();
    matrices.translate(-camera.x, -camera.y, -camera.z);
    VertexConsumer vertices = BUFFER.getVertexBuilder(draw);
    for (Bounds zone : zones) box(matrices.last().pose(), vertices, zone);
    matrices.popPose();
    BUFFER.upload();
    StagedVertexBuffer.ExecuteInfo info = BUFFER.getExecuteInfo(draw);
    if (info != null) draw(Minecraft.getInstance(), info, pipeline);
    BUFFER.endFrame();
  }

  private static void box(Matrix4fc matrix, VertexConsumer vertices, Bounds box) {
    float x1 = box.minX, y1 = box.minY, z1 = box.minZ, x2 = box.maxX, y2 = box.maxY, z2 = box.maxZ;
    line(matrix, vertices, box.color, x1, y1, z1, x2, y1, z1); line(matrix, vertices, box.color, x2, y1, z1, x2, y1, z2);
    line(matrix, vertices, box.color, x2, y1, z2, x1, y1, z2); line(matrix, vertices, box.color, x1, y1, z2, x1, y1, z1);
    line(matrix, vertices, box.color, x1, y2, z1, x2, y2, z1); line(matrix, vertices, box.color, x2, y2, z1, x2, y2, z2);
    line(matrix, vertices, box.color, x2, y2, z2, x1, y2, z2); line(matrix, vertices, box.color, x1, y2, z2, x1, y2, z1);
    line(matrix, vertices, box.color, x1, y1, z1, x1, y2, z1); line(matrix, vertices, box.color, x2, y1, z1, x2, y2, z1);
    line(matrix, vertices, box.color, x2, y1, z2, x2, y2, z2); line(matrix, vertices, box.color, x1, y1, z2, x1, y2, z2);
  }

  private static void line(Matrix4fc matrix, VertexConsumer vertices, int color, float x1, float y1, float z1, float x2, float y2, float z2) {
    float red = ((color >> 16) & 0xFF) / 255F, green = ((color >> 8) & 0xFF) / 255F, blue = (color & 0xFF) / 255F;
    vertices.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, 0.9F).setNormal(0, 1, 0).setLineWidth(18F);
    vertices.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, 0.9F).setNormal(0, 1, 0).setLineWidth(18F);
  }

  private static void draw(Minecraft client, StagedVertexBuffer.ExecuteInfo info, RenderPipeline pipeline) {
    GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
        .writeTransform(RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
    RenderTarget target = client.gameRenderer.mainRenderTarget();
    GpuTextureView color = target.getColorTextureView();
    if (color == null) return;
    try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
        () -> "Noisy Neighbor zone outlines", color, Optional.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
      pass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
      RenderSystem.bindDefaultUniforms(pass);
      pass.setUniform("DynamicTransforms", transforms);
      pass.setVertexBuffer(0, info.vertexBuffer().slice());
      pass.setIndexBuffer(info.indexBuffer(), info.indexType());
      pass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
    }
  }

  private record Bounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color) {}
}
