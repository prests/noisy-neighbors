package com.github.prests.noisyneighbors.client;

import com.github.prests.noisyneighbors.NoisyNeighbors;
import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.zone.ZoneSelection;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Customizable controls shown in Minecraft's Controls menu. */
public final class NoisyNeighborsKeyMappings {
  private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(NoisyNeighbors.MOD_ID, "controls"));
  private static final KeyMapping TOGGLE_OUTLINES = KeyMappingHelper.registerKeyMapping(
      new KeyMapping("key.noisy-neighbors.toggle_outlines", InputConstants.Type.KEYBOARD, 14, CATEGORY)); // K
  private static final KeyMapping CREATE_ZONE = KeyMappingHelper.registerKeyMapping(
      new KeyMapping("key.noisy-neighbors.create_zone", InputConstants.Type.KEYBOARD, 29, CATEGORY)); // Z

  private NoisyNeighborsKeyMappings() {}
  public static void register() {}

  public static void tick(Minecraft client) {
    while (TOGGLE_OUTLINES.consumeClick()) {
      SettingsStore.data().showZoneOutlines = !SettingsStore.data().showZoneOutlines;
      SettingsStore.save();
      if (client.player != null) client.player.sendOverlayMessage(Component.translatable(
          SettingsStore.data().showZoneOutlines ? "noisy-neighbors.zones.outlines.on" : "noisy-neighbors.zones.outlines.off"));
    }
    while (CREATE_ZONE.consumeClick()) {
      if (ZoneSelection.isActive()) {
        ZoneSelection.reset();
        if (client.player != null) client.player.sendOverlayMessage(Component.translatable("noisy-neighbors.selection.cancelled"));
      } else if (client.level != null && client.player != null) {
        ZoneSelection.begin();
      }
    }
    ZoneSelection.tick(client, CREATE_ZONE.getTranslatedKeyMessage());
  }
}
