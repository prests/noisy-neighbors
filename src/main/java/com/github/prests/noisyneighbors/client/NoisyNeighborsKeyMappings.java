package com.github.prests.noisyneighbors.client;

import com.github.prests.noisyneighbors.NoisyNeighbors;
import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.github.prests.noisyneighbors.client.ui.WorldZonesScreen;
import com.github.prests.noisyneighbors.client.zone.ZoneSelection;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Customizable controls shown in Minecraft's Controls menu. */
public final class NoisyNeighborsKeyMappings {
  private static final String CATEGORY = "key.categories.noisy-neighbors.controls";
  private static final KeyMapping TOGGLE_OUTLINES = KeyBindingHelper.registerKeyBinding(
      new KeyMapping("key.noisy-neighbors.toggle_outlines", InputConstants.Type.KEYSYM, InputConstants.KEY_K, CATEGORY));
  private static final KeyMapping CREATE_ZONE = KeyBindingHelper.registerKeyBinding(
      new KeyMapping("key.noisy-neighbors.create_zone", InputConstants.Type.KEYSYM, InputConstants.KEY_Z, CATEGORY));
  private static final KeyMapping OPEN_WORLD_ZONES = KeyBindingHelper.registerKeyBinding(
      new KeyMapping("key.noisy-neighbors.open_world_zones", InputConstants.Type.KEYSYM, InputConstants.KEY_U, CATEGORY));

  private NoisyNeighborsKeyMappings() {}
  public static void register() {}

  public static void tick(Minecraft client) {
    while (TOGGLE_OUTLINES.consumeClick()) {
      SettingsStore.data().showZoneOutlines = !SettingsStore.data().showZoneOutlines;
      SettingsStore.save();
      if (client.player != null) client.gui.setOverlayMessage(Component.translatable(
          SettingsStore.data().showZoneOutlines ? "noisy-neighbors.zones.outlines.on" : "noisy-neighbors.zones.outlines.off"), false);
    }
    while (CREATE_ZONE.consumeClick()) {
      if (ZoneSelection.isActive()) {
        ZoneSelection.reset();
        if (client.player != null) client.gui.setOverlayMessage(Component.translatable("noisy-neighbors.selection.cancelled"), false);
      } else if (client.level != null && client.player != null) {
        ZoneSelection.begin();
      }
    }
    while (OPEN_WORLD_ZONES.consumeClick()) {
      WorldIdentity world = WorldIdentity.current(client);
      if (world != null) client.setScreen(new WorldZonesScreen(null, world));
    }
    ZoneSelection.tick(client, CREATE_ZONE.getTranslatedKeyMessage());
  }
}
