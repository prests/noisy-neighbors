package com.github.prests.noisyneighbors.client.ui;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import com.github.prests.noisyneighbors.client.zone.Zone;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

/** Per-event controls for one canonical entity group. */
public final class FineGrainedSoundScreen extends OptionsSubScreen {
  private final String mob;
  private final String worldKey;
  private final UUID zoneId;

  public FineGrainedSoundScreen(Screen parent, String mob) {
    super(parent, Minecraft.getInstance().options, Component.literal(MobSoundCatalog.displayName(mob)));
    this.mob = mob;
    worldKey = null;
    zoneId = null;
  }

  public FineGrainedSoundScreen(Screen parent, String worldKey, UUID zoneId, String mob) {
    super(parent, Minecraft.getInstance().options, Component.literal(MobSoundCatalog.displayName(mob)));
    this.mob = mob;
    this.worldKey = worldKey;
    this.zoneId = zoneId;
  }

  @Override protected void addOptions() {
    MobSoundCatalog.controllableEventsFor(mob).forEach(event -> list.addBig(new EventSlider(event)));
  }

  @Override public void onClose() {
    SettingsStore.save();
    super.onClose();
  }

  private int volume(String event) {
    if (zoneId == null) return SettingsStore.data().events.getOrDefault(mob, Map.of()).getOrDefault(event, 100);
    SettingsStore.World world = SettingsStore.data().worlds.get(worldKey);
    if (world == null) return 100;
    return world.zones.stream().filter(zone -> zone.id().equals(zoneId)).findFirst()
        .map(zone -> zone.events().getOrDefault(mob, Map.of()).getOrDefault(event, 100)).orElse(100);
  }

  private void setVolume(String event, int volume) {
    if (zoneId == null) {
      update(SettingsStore.data().events, event, volume);
      return;
    }
    SettingsStore.updateZone(worldKey, zoneId, zone -> {
      Map<String, Map<String, Integer>> events = new HashMap<>(zone.events());
      update(events, event, volume);
      return new Zone(zone.id(), zone.name(), zone.enabled(), zone.color(), zone.dimension(), zone.minX(), zone.minY(), zone.minZ(),
          zone.maxX(), zone.maxY(), zone.maxZ(), zone.volumes(), events);
    });
  }

  private void update(Map<String, Map<String, Integer>> events, String event, int volume) {
    Map<String, Integer> values = new HashMap<>(events.getOrDefault(mob, Map.of()));
    if (volume == 100) values.remove(event); else values.put(event, volume);
    if (values.isEmpty()) events.remove(mob); else events.put(mob, values);
  }

  private final class EventSlider extends AbstractSliderButton {
    private final String event;

    EventSlider(String event) {
      super(0, 0, 310, 20, Component.empty(), volume(event) / 100D);
      this.event = event;
      updateMessage();
    }

    @Override protected void updateMessage() {
      setMessage(Component.literal(event + ": " + Math.round(value * 100) + "%"));
    }

    @Override protected void applyValue() {
      setVolume(event, (int) Math.round(value * 100));
    }
  }
}
