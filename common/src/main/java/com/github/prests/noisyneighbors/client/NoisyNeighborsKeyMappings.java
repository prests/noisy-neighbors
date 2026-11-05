package com.github.prests.noisyneighbors.client;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.github.prests.noisyneighbors.client.ui.WorldZonesScreen;
import com.github.prests.noisyneighbors.client.zone.ZoneSelection;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Customizable controls shown in Minecraft's Controls menu. */
public final class NoisyNeighborsKeyMappings {
  private static final String CATEGORY = "key.categories.noisy-neighbors.controls";
  private static KeyMapping toggleOutlines;
  private static KeyMapping createZone;
  private static KeyMapping openWorldZones;

  private NoisyNeighborsKeyMappings() {}

  public static void register(Consumer<KeyMapping> registrar) {
    toggleOutlines = register(registrar, "key.noisy-neighbors.toggle_outlines", InputConstants.KEY_K);
    createZone = register(registrar, "key.noisy-neighbors.create_zone", InputConstants.KEY_Z);
    openWorldZones = register(registrar, "key.noisy-neighbors.open_world_zones", InputConstants.KEY_U);
  }

  private static KeyMapping register(Consumer<KeyMapping> registrar, String translationKey, int defaultKey) {
    KeyMapping mapping = new KeyMapping(translationKey, InputConstants.Type.KEYSYM, defaultKey, CATEGORY);
    registrar.accept(mapping);
    return mapping;
  }

  public static void tick(Minecraft client) {
    if (toggleOutlines == null || createZone == null || openWorldZones == null) {
      throw new IllegalStateException("Noisy Neighbors key mappings were used before registration");
    }
    while (toggleOutlines.consumeClick()) {
      SettingsStore.data().showZoneOutlines = !SettingsStore.data().showZoneOutlines;
      SettingsStore.save();
      if (client.player != null) client.gui.setOverlayMessage(Component.translatable(
          SettingsStore.data().showZoneOutlines ? "noisy-neighbors.zones.outlines.on" : "noisy-neighbors.zones.outlines.off"), false);
    }
    while (createZone.consumeClick()) {
      if (ZoneSelection.isActive()) {
        ZoneSelection.reset();
        if (client.player != null) client.gui.setOverlayMessage(Component.translatable("noisy-neighbors.selection.cancelled"), false);
      } else if (client.level != null && client.player != null) {
        ZoneSelection.begin();
      }
    }
    while (openWorldZones.consumeClick()) {
      WorldIdentity world = WorldIdentity.current(client);
      if (world != null) client.setScreen(new WorldZonesScreen(null, world));
    }
    ZoneSelection.tick(client, createZone.getTranslatedKeyMessage());
  }
}
