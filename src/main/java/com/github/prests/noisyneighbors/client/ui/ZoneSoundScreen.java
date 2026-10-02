package com.github.prests.noisyneighbors.client.ui;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import com.github.prests.noisyneighbors.client.zone.Zone;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

/** Per-mob volume limits for one saved zone. */
public final class ZoneSoundScreen extends OptionsSubScreen {
  private final String worldKey;
  private final UUID zoneId;

  public ZoneSoundScreen(Screen parent, String worldKey, UUID zoneId) {
    super(parent, Minecraft.getInstance().options, Component.translatable("noisy-neighbors.zone.title"));
    this.worldKey = worldKey;
    this.zoneId = zoneId;
  }

  @Override protected void addOptions() {
    Zone zone = zone();
    if (zone == null) return;
    Button details = Button.builder(Component.literal(zone.name() + " — " + bounds(zone)), button -> {}).width(310).build();
    details.active = false;
    list.addBig(details);
    MobSoundCatalog.mobs().stream().sorted(Comparator.naturalOrder())
        .forEach(mob -> list.addBig(new MobSlider(mob, zone.volumes().getOrDefault(mob, 100))));
  }

  @Override public void onClose() {
    SettingsStore.save();
    super.onClose();
  }

  private Zone zone() {
    SettingsStore.World world = SettingsStore.data().worlds.get(worldKey);
    if (world == null) return null;
    return world.zones.stream().filter(zone -> zone.id().equals(zoneId)).findFirst().orElse(null);
  }

  private static String bounds(Zone zone) {
    return zone.minX() + "," + zone.minY() + "," + zone.minZ() + " → "
        + zone.maxX() + "," + zone.maxY() + "," + zone.maxZ();
  }

  private final class MobSlider extends AbstractSliderButton {
    private final String mob;

    MobSlider(String mob, int volume) {
      super(0, 0, 310, 20, Component.empty(), volume / 100D);
      this.mob = mob;
      updateMessage();
    }

    @Override protected void updateMessage() {
      setMessage(Component.literal(mob.replace("minecraft:", "") + ": " + Math.round(value * 100) + "%"));
    }

    @Override protected void applyValue() {
      int volume = (int) Math.round(value * 100);
      SettingsStore.updateZone(worldKey, zoneId, zone -> {
        var volumes = new HashMap<>(zone.volumes());
        volumes.put(mob, volume);
        return new Zone(zone.id(), zone.name(), zone.enabled(), zone.dimension(), zone.minX(), zone.minY(), zone.minZ(),
            zone.maxX(), zone.maxY(), zone.maxZ(), volumes);
      });
    }
  }
}
