package com.github.prests.noisyneighbors.client.ui;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.zone.Zone;
import java.util.UUID;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Small focused editor for a zone's display name. */
public final class ZoneNameScreen extends Screen {
  private final ZoneSoundScreen parent;
  private final String worldKey;
  private final UUID zoneId;
  private EditBox name;

  public ZoneNameScreen(ZoneSoundScreen parent, String worldKey, UUID zoneId, String currentName) {
    super(Component.translatable("noisy-neighbors.zone.rename.title"));
    this.parent = parent;
    this.worldKey = worldKey;
    this.zoneId = zoneId;
    this.currentName = currentName;
  }

  private final String currentName;

  @Override protected void init() {
    name = addRenderableWidget(new EditBox(font, width / 2 - 155, height / 2 - 10, 310, 20,
        Component.translatable("noisy-neighbors.zone.name")));
    name.setMaxLength(64);
    name.setValue(currentName);
    addRenderableWidget(Button.builder(Component.translatable("noisy-neighbors.save"), button -> save())
        .bounds(width / 2 - 155, height / 2 + 24, 150, 20).build());
    addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
        .bounds(width / 2 + 5, height / 2 + 24, 150, 20).build());
    setInitialFocus(name);
  }

  @Override public void onClose() {
    minecraft.gui.setScreen(parent);
  }

  private void save() {
    SettingsStore.updateZone(worldKey, zoneId, zone -> renamed(zone, name.getValue()));
    SettingsStore.save();
    minecraft.gui.setScreen(parent.refreshed());
  }

  private static Zone renamed(Zone zone, String name) {
    return new Zone(zone.id(), name, zone.enabled(), zone.color(), zone.dimension(), zone.minX(), zone.minY(), zone.minZ(),
        zone.maxX(), zone.maxY(), zone.maxZ(), zone.volumes(), zone.events());
  }
}
