package com.github.prests.noisyneighbors.client.zone;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/** Client-only two-corner block selection. */
public final class ZoneSelection {
  private static BlockPos firstCorner;
  private static boolean active;
  private static String editingWorldKey;
  private static UUID editingZoneId;

  private ZoneSelection() {}

  public static void begin() {
    active = true;
    firstCorner = null;
    editingWorldKey = null;
    editingZoneId = null;
  }

  public static void beginEdit(String worldKey, UUID zoneId) {
    active = true;
    firstCorner = null;
    editingWorldKey = worldKey;
    editingZoneId = zoneId;
  }

  public static void reset() {
    active = false;
    firstCorner = null;
    editingWorldKey = null;
    editingZoneId = null;
  }

  public static boolean isActive() {
    return active;
  }

  public static InteractionResult useBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
    if (!active || !level.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

    BlockPos corner = hit.getBlockPos().immutable();
    if (firstCorner == null) {
      firstCorner = corner;
      overlay(Component.translatable("noisy-neighbors.selection.first", corner.getX(), corner.getY(), corner.getZ()));
    } else {
      boolean updated = editingZoneId != null;
      saveZone(level, firstCorner, corner);
      overlay(Component.translatable(updated ? "noisy-neighbors.selection.updated" : "noisy-neighbors.selection.created"));
      reset();
    }
    return InteractionResult.FAIL;
  }

  public static void tick(Minecraft client, Component cancelKey) {
    if (!active || client.player == null) return;
    Component message = firstCorner == null
        ? Component.translatable("noisy-neighbors.selection.first-prompt", cancelKey)
        : Component.translatable("noisy-neighbors.selection.second-prompt", cancelKey);
    overlay(message);
  }

  private static void overlay(Component message) {
    if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.sendOverlayMessage(message);
  }

  private static void saveZone(Level level, BlockPos first, BlockPos second) {
    WorldIdentity identity = WorldIdentity.current(Minecraft.getInstance());
    if (identity == null) return;
    if (editingZoneId != null) {
      if (!identity.key().equals(editingWorldKey)) return;
      SettingsStore.updateZone(editingWorldKey, editingZoneId, zone -> new Zone(zone.id(), zone.name(), zone.enabled(), zone.color(),
          level.dimension().identifier().toString(), first.getX(), first.getY(), first.getZ(), second.getX(), second.getY(), second.getZ(), zone.volumes(), zone.events()));
    } else {
      int number = SettingsStore.data().worlds.getOrDefault(identity.key(), new SettingsStore.World()).zones.size() + 1;
      SettingsStore.addZone(identity, new Zone(UUID.randomUUID(), "Zone " + number, true,
          level.dimension().identifier().toString(), first.getX(), first.getY(), first.getZ(),
          second.getX(), second.getY(), second.getZ(), Map.of()));
    }
    SettingsStore.save();
  }
}
