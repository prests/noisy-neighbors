package com.github.prests.noisyneighbors.client.ui;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.github.prests.noisyneighbors.client.zone.Zone;
import com.github.prests.noisyneighbors.client.zone.ZoneSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

/** Lists the saved zones for the currently connected world. */
public final class WorldZonesScreen extends OptionsSubScreen {
  private final WorldIdentity world;
  private Button outlines;

  public WorldZonesScreen(Screen parent, WorldIdentity world) {
    super(parent, Minecraft.getInstance().options, Component.translatable("noisy-neighbors.zones.title", world.displayName()));
    this.world = world;
  }

  @Override protected void addTitle() {
    layout.setHeaderHeight(58);
    outlines = Button.builder(Component.empty(), button -> {
      SettingsStore.data().showZoneOutlines = !SettingsStore.data().showZoneOutlines;
      updateOutlineLabel();
    }).width(150).build();
    updateOutlineLabel();
    Button create = Button.builder(Component.translatable("noisy-neighbors.create-zone"), button -> {
      ZoneSelection.begin();
      Minecraft.getInstance().gui.setScreen(null);
    }).width(150).build();
    LinearLayout row = LinearLayout.horizontal().spacing(10);
    row.addChild(outlines);
    row.addChild(create);
    LinearLayout header = LinearLayout.vertical().spacing(4);
    header.addChild(new StringWidget(title, font), settings -> settings.alignHorizontallyCenter());
    header.addChild(row);
    layout.addToHeader(header);
  }

  @Override protected void addContents() {
    list = (ZoneOptionsList) layout.addToContents(new ZoneOptionsList(Minecraft.getInstance(), width, this));
    addOptions();
  }

  @Override protected void addOptions() {
    SettingsStore.World settings = SettingsStore.data().worlds.get(world.key());
    if (settings == null || settings.zones.isEmpty()) {
      Button empty = Button.builder(Component.translatable("noisy-neighbors.zones.empty"), button -> {}).width(310).build();
      empty.active = false;
      list.addBig(empty);
      return;
    }

    ZoneOptionsList zones = (ZoneOptionsList) list;
    for (Zone zone : settings.zones) zones.addZone(createZoneSelector(zone), createZoneToggle(zone));
  }

  private Button createZoneSelector(Zone zone) {
    return Button.builder(Component.literal(zone.name() + " — " + zone.dimension().replace("minecraft:", "")),
        button -> Minecraft.getInstance().gui.setScreen(new ZoneSoundScreen(new WorldZonesScreen(lastScreen, world), world.key(), zone.id())))
        .build();
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

  @Override public void onClose() {
    SettingsStore.save();
    super.onClose();
  }
}
