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

  private ZoneSelection() {}

  public static void begin() {
    active = true;
    firstCorner = null;
  }

  public static void reset() {
    active = false;
    firstCorner = null;
  }

  public static InteractionResult useBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
    if (!active || !level.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

    BlockPos corner = hit.getBlockPos().immutable();
    if (firstCorner == null) {
      firstCorner = corner;
      overlay(Component.translatable("noisy-neighbors.selection.first", corner.getX(), corner.getY(), corner.getZ()));
    } else {
      saveZone(level, firstCorner, corner);
      overlay(Component.translatable("noisy-neighbors.selection.created"));
      reset();
    }
    return InteractionResult.FAIL;
  }

  public static InteractionResult useItem(Player player, Level level, InteractionHand hand) {
    if (!active || !level.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
    reset();
    overlay(Component.translatable("noisy-neighbors.selection.cancelled"));
    return InteractionResult.FAIL;
  }

  public static void tick(Minecraft client) {
    if (!active || client.player == null) return;
    Component message = firstCorner == null
        ? Component.translatable("noisy-neighbors.selection.first-prompt")
        : Component.translatable("noisy-neighbors.selection.second-prompt", firstCorner.getX(), firstCorner.getY(), firstCorner.getZ());
    overlay(message);
  }

  private static void overlay(Component message) {
    if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.sendOverlayMessage(message);
  }

  private static void saveZone(Level level, BlockPos first, BlockPos second) {
    WorldIdentity identity = WorldIdentity.current(Minecraft.getInstance());
    if (identity == null) return;
    SettingsStore.World world = SettingsStore.data().worlds.computeIfAbsent(identity.key(), ignored -> {
      SettingsStore.World created = new SettingsStore.World();
      created.displayName = identity.displayName();
      return created;
    });
    world.zones.add(new Zone(UUID.randomUUID(), "Zone " + (world.zones.size() + 1), true,
        level.dimension().identifier().toString(), first.getX(), first.getY(), first.getZ(),
        second.getX(), second.getY(), second.getZ(), Map.of()));
    SettingsStore.save();
  }
}
