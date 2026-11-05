package com.github.prests.noisyneighbors.client.ui;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.github.prests.noisyneighbors.client.zone.Zone;
import com.github.prests.noisyneighbors.client.zone.ZoneSelection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Lists the saved zones for the currently connected world. */
public final class WorldZonesScreen extends Screen {
  private static final int CONTENT_WIDTH = 310;
  private final Screen parent;
  private final WorldIdentity world;
  private Button outlines;

  public WorldZonesScreen(Screen parent, WorldIdentity world) {
    super(Component.translatable("noisy-neighbors.zones.title", world.displayName()));
    this.parent = parent;
    this.world = world;
  }

  @Override protected void init() {
    int left = width / 2 - CONTENT_WIDTH / 2;
    outlines = addRenderableWidget(Button.builder(Component.empty(), button -> {
      SettingsStore.data().showZoneOutlines = !SettingsStore.data().showZoneOutlines;
      updateOutlineLabel();
    }).bounds(left, 30, 150, 20).build());
    updateOutlineLabel();
    addRenderableWidget(Button.builder(Component.translatable("noisy-neighbors.create-zone"), button -> {
      ZoneSelection.begin();
      minecraft.setScreen(null);
    }).bounds(left + 160, 30, 150, 20).build());

    ZoneOptionsList zones = addRenderableWidget(new ZoneOptionsList(minecraft, width, height, 70, height - 40));
    SettingsStore.World settings = SettingsStore.data().worlds.get(world.key());
    if (settings == null || settings.zones.isEmpty()) {
      Button empty = Button.builder(Component.translatable("noisy-neighbors.zones.empty"), button -> {}).width(CONTENT_WIDTH).build();
      empty.active = false;
      zones.addWidget(empty);
    } else {
      for (Zone zone : settings.zones) zones.addZone(createZoneSelector(zone), createZoneToggle(zone));
    }
    addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
        .bounds(width / 2 - 100, height - 28, 200, 20).build());
  }

  private Button createZoneSelector(Zone zone) {
    return Button.builder(Component.literal(zone.name() + " — " + zone.dimension().replace("minecraft:", "")),
        button -> minecraft.setScreen(new ZoneSoundScreen(new WorldZonesScreen(parent, world), world.key(), zone.id()))).build();
  }

  private Button createZoneToggle(Zone zone) {
    Button toggle = Button.builder(zoneLabel(zone.enabled()), button -> {
      boolean enabled = !zoneEnabled(zone.id());
      SettingsStore.updateZone(world.key(), zone.id(), current -> new Zone(current.id(), current.name(), enabled, current.color(),
          current.dimension(), current.minX(), current.minY(), current.minZ(), current.maxX(), current.maxY(), current.maxZ(),
          current.volumes(), current.events(), current.chattiness()));
      button.setMessage(zoneLabel(enabled));
      button.setTooltip(zoneTooltip(enabled));
    }).build();
    toggle.setTooltip(zoneTooltip(zone.enabled()));
    return toggle;
  }

  private boolean zoneEnabled(java.util.UUID zoneId) {
    SettingsStore.World settings = SettingsStore.data().worlds.get(world.key());
    return settings != null && settings.zones.stream().filter(zone -> zone.id().equals(zoneId)).findFirst()
        .map(Zone::enabled).orElse(false);
  }

  private static Tooltip zoneTooltip(boolean enabled) {
    return Tooltip.create(Component.translatable(enabled ? "noisy-neighbors.zone.disable" : "noisy-neighbors.zone.enable"));
  }

  private static Component zoneLabel(boolean enabled) {
    return Component.literal(enabled ? "🔊" : "🔇");
  }

  private void updateOutlineLabel() {
    outlines.setMessage(Component.translatable(SettingsStore.data().showZoneOutlines
        ? "noisy-neighbors.zones.outlines.on" : "noisy-neighbors.zones.outlines.off"));
  }

  @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    renderBackground(graphics, mouseX, mouseY, delta);
    super.render(graphics, mouseX, mouseY, delta);
    graphics.drawCenteredString(font, title, width / 2, 10, 0xFFFFFFFF);
  }

  @Override public void onClose() {
    SettingsStore.save();
    minecraft.setScreen(parent);
  }
}
