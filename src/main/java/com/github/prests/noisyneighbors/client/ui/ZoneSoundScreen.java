package com.github.prests.noisyneighbors.client.ui;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import com.github.prests.noisyneighbors.client.zone.Zone;
import com.github.prests.noisyneighbors.client.zone.ZoneSelection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

/** Per-mob volume limits for one saved zone. */
public final class ZoneSoundScreen extends OptionsSubScreen {
  private final String worldKey;
  private final UUID zoneId;
  private FilteredOptionsList filteredList;
  private EditBox search;

  public ZoneSoundScreen(Screen parent, String worldKey, UUID zoneId) {
    super(parent, Minecraft.getInstance().options, Component.translatable("noisy-neighbors.zone.title"));
    this.worldKey = worldKey;
    this.zoneId = zoneId;
  }

  @Override protected void addContents() {
    filteredList = (FilteredOptionsList) layout.addToContents(new FilteredOptionsList(Minecraft.getInstance(), width, this));
    list = filteredList;
    addOptions();
  }

  @Override protected void addOptions() {
    Zone zone = zone();
    if (zone == null) return;
    Button details = Button.builder(Component.literal(zone.name() + " — " + bounds(zone)), button -> {}).width(310).build();
    details.active = false;
    list.addBig(details);
    Button rename = Button.builder(Component.translatable("noisy-neighbors.zone.rename"),
        button -> Minecraft.getInstance().gui.setScreen(new ZoneNameScreen(this, worldKey, zoneId, zone.name())))
        .width(150).build();
    Button delete = Button.builder(Component.translatable("noisy-neighbors.zone.delete"),
        button -> confirmDelete(zone)).width(150).build();
    list.addSmall(rename, delete);
    Button color = Button.builder(Component.translatable("noisy-neighbors.zone.color"),
        button -> Minecraft.getInstance().gui.setScreen(new ZoneColorScreen(this, worldKey, zoneId, zone.color())))
        .width(150).build();
    Button editBounds = Button.builder(Component.translatable("noisy-neighbors.zone.edit-bounds"), button -> {
      ZoneSelection.beginEdit(worldKey, zoneId);
      Minecraft.getInstance().gui.setScreen(null);
    }).width(150).build();
    editBounds.active = Minecraft.getInstance().level != null
        && zone.dimension().equals(Minecraft.getInstance().level.dimension().identifier().toString());
    list.addSmall(color, editBounds);
    search = new EditBox(font, 0, 0, 310, 20, Component.translatable("noisy-neighbors.search.entities"));
    search.setHint(Component.translatable("noisy-neighbors.search.entities").withStyle(EditBox.SEARCH_HINT_STYLE));
    search.setResponder(query -> refreshOptions());
    list.addBig(search);
    filteredList.rememberEntries();
    refreshOptions();
  }

  private void refreshOptions() {
    filteredList.showFixedEntries();
    Zone zone = zone();
    List<String> mobs = zone == null ? List.of() : MobSoundCatalog.mobs().stream().sorted(Comparator.naturalOrder())
        .filter(mob -> MobSoundCatalog.matchesSearch(mob, search.getValue())).toList();
    mobs.forEach(mob -> {
      Button advanced = Button.builder(EntityOptionsList.ADVANCED_LABEL, button -> Minecraft.getInstance().gui.setScreen(
          new FineGrainedSoundScreen(this, worldKey, zoneId, mob))).width(20).build();
      advanced.setTooltip(Tooltip.create(Component.translatable("noisy-neighbors.advanced-controls")));
      filteredList.addEntity(mob, Component.literal(MobSoundCatalog.displayName(mob)),
          new MobSlider(mob, zone.volumes().getOrDefault(mob, 100), false),
          new MobSlider(mob, zone.chattiness().getOrDefault(mob, 100), true), advanced);
    });
    if (mobs.isEmpty()) {
      Button empty = Button.builder(Component.translatable("noisy-neighbors.search.empty"), button -> {}).width(310).build();
      empty.active = false;
      list.addBig(empty);
    }
    list.setScrollAmount(0);
  }

  @Override public void onClose() {
    SettingsStore.save();
    super.onClose();
  }

  private void confirmDelete(Zone zone) {
    Minecraft.getInstance().gui.setScreen(new ConfirmScreen(confirmed -> {
      if (confirmed) {
        SettingsStore.removeZone(worldKey, zoneId);
        SettingsStore.save();
        Minecraft.getInstance().gui.setScreen(lastScreen);
      } else {
        Minecraft.getInstance().gui.setScreen(this);
      }
    }, Component.translatable("noisy-neighbors.zone.delete.title", zone.name()),
        Component.translatable("noisy-neighbors.zone.delete.warning"),
        Component.translatable("noisy-neighbors.zone.delete"), Component.translatable("gui.cancel")));
  }

  ZoneSoundScreen refreshed() {
    return new ZoneSoundScreen(lastScreen, worldKey, zoneId);
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

  private static final class FilteredOptionsList extends EntityOptionsList {
    private List<AbstractEntry> fixedEntries = List.of();

    FilteredOptionsList(Minecraft minecraft, int width, OptionsSubScreen screen) {
      super(minecraft, width, screen);
    }

    void rememberEntries() {
      fixedEntries = List.copyOf(children());
    }

    void showFixedEntries() {
      replaceEntries(fixedEntries);
    }
  }

  private final class MobSlider extends AbstractSliderButton {
    private final String mob;
    private final boolean chattiness;

    MobSlider(String mob, int percentage, boolean chattiness) {
      super(0, 0, 310, 20, Component.empty(), percentage / 100D);
      this.mob = mob;
      this.chattiness = chattiness;
      updateMessage();
    }

    @Override protected void updateMessage() {
      setMessage(Component.translatable(chattiness ? "noisy-neighbors.chattiness" : "noisy-neighbors.volume",
          Math.round(value * 100)));
    }

    @Override protected void applyValue() {
      int percentage = (int) Math.round(value * 100);
      SettingsStore.updateZone(worldKey, zoneId, zone -> {
        var values = new HashMap<>(chattiness ? zone.chattiness() : zone.volumes());
        values.put(mob, percentage);
        return chattiness
            ? new Zone(zone.id(), zone.name(), zone.enabled(), zone.color(), zone.dimension(), zone.minX(), zone.minY(), zone.minZ(),
                zone.maxX(), zone.maxY(), zone.maxZ(), zone.volumes(), zone.events(), values)
            : new Zone(zone.id(), zone.name(), zone.enabled(), zone.color(), zone.dimension(), zone.minX(), zone.minY(), zone.minZ(),
                zone.maxX(), zone.maxY(), zone.maxZ(), values, zone.events(), zone.chattiness());
      });
    }

    @Override protected net.minecraft.network.chat.MutableComponent createNarrationMessage() {
      return Component.literal(MobSoundCatalog.displayName(mob) + ", ").append(super.createNarrationMessage());
    }
  }
}
