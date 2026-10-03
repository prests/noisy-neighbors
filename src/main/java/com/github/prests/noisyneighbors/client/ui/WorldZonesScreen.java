package com.github.prests.noisyneighbors.client.ui;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.github.prests.noisyneighbors.client.zone.Zone;
import com.github.prests.noisyneighbors.client.zone.ZoneSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
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

  @Override protected void addOptions() {
    SettingsStore.World settings = SettingsStore.data().worlds.get(world.key());
    if (settings == null || settings.zones.isEmpty()) {
      Button empty = Button.builder(Component.translatable("noisy-neighbors.zones.empty"), button -> {}).width(310).build();
      empty.active = false;
      list.addBig(empty);
      return;
    }

    for (Zone zone : settings.zones) {
      list.addBig(Button.builder(Component.literal(zone.name() + " — " + zone.dimension().replace("minecraft:", "")),
          button -> Minecraft.getInstance().gui.setScreen(new ZoneSoundScreen(new WorldZonesScreen(lastScreen, world), world.key(), zone.id())))
          .width(310).build());
    }
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
