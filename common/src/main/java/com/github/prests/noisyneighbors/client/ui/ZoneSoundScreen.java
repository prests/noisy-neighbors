package com.github.prests.noisyneighbors.client.ui;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import com.github.prests.noisyneighbors.client.zone.Zone;
import com.github.prests.noisyneighbors.client.zone.ZoneSelection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Per-mob volume limits for one saved zone. */
public final class ZoneSoundScreen extends Screen {
  private static final int CONTENT_WIDTH = 310;
  private final Screen parent;
  private final String worldKey;
  private final UUID zoneId;
  private EditBox search;
  private EntityOptionsList entityList;
  private Button details;
  private Button rename;
  private Button delete;
  private Button color;
  private Button editBounds;

  public ZoneSoundScreen(Screen parent, String worldKey, UUID zoneId) {
    super(Component.translatable("noisy-neighbors.zone.title"));
    this.parent = parent;
    this.worldKey = worldKey;
    this.zoneId = zoneId;
  }

  @Override protected void init() {
    Zone zone = zone();
    if (zone == null) {
      onClose();
      return;
    }
    details = Button.builder(Component.literal(zone.name() + " — " + bounds(zone)), button -> {}).width(CONTENT_WIDTH).build();
    details.active = false;
    rename = Button.builder(Component.translatable("noisy-neighbors.zone.rename"),
        button -> minecraft.setScreen(new ZoneNameScreen(this, worldKey, zoneId, zone.name()))).width(150).build();
    delete = Button.builder(Component.translatable("noisy-neighbors.zone.delete"), button -> confirmDelete(zone)).width(150).build();
    color = Button.builder(Component.translatable("noisy-neighbors.zone.color"),
        button -> minecraft.setScreen(new ZoneColorScreen(this, worldKey, zoneId, zone.color()))).width(150).build();
    editBounds = Button.builder(Component.translatable("noisy-neighbors.zone.edit-bounds"), button -> {
      ZoneSelection.beginEdit(worldKey, zoneId);
      minecraft.setScreen(null);
    }).width(150).build();
    editBounds.active = minecraft.level != null && zone.dimension().equals(minecraft.level.dimension().location().toString());
    search = new EditBox(font, 0, 0, CONTENT_WIDTH, 20, Component.translatable("noisy-neighbors.search.entities"));
    search.setHint(Component.translatable("noisy-neighbors.search.entities").withStyle(ChatFormatting.DARK_GRAY));
    entityList = addRenderableWidget(new EntityOptionsList(minecraft, width, height, 40, height - 40));
    search.setResponder(query -> refreshOptions());
    refreshOptions();
    addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
        .bounds(width / 2 - 100, height - 28, 200, 20).build());
  }

  private void refreshOptions() {
    entityList.clear();
    Zone zone = zone();
    if (zone == null) return;
    entityList.addFullWidthWidget(details);
    entityList.addWidgetPair(rename, delete);
    entityList.addWidgetPair(color, editBounds);
    entityList.addFullWidthWidget(search);
    MobSoundCatalog.mobs().stream().sorted(Comparator.naturalOrder())
        .filter(mob -> MobSoundCatalog.matchesSearch(mob, search.getValue()))
        .forEach(mob -> {
          Button advanced = Button.builder(EntityOptionsList.ADVANCED_LABEL,
              button -> minecraft.setScreen(new FineGrainedSoundScreen(this, worldKey, zoneId, mob))).width(20).build();
          advanced.setTooltip(Tooltip.create(Component.translatable("noisy-neighbors.advanced-controls")));
          entityList.addEntity(mob, Component.literal(MobSoundCatalog.displayName(mob)),
              new MobSlider(mob, zone.volumes().getOrDefault(mob, 100), false),
              new MobSlider(mob, zone.chattiness().getOrDefault(mob, 100), true), advanced);
        });
    entityList.setScrollAmount(0);
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

  private void confirmDelete(Zone zone) {
    minecraft.setScreen(new ConfirmScreen(confirmed -> {
      if (confirmed) {
        SettingsStore.removeZone(worldKey, zoneId);
        SettingsStore.save();
        minecraft.setScreen(parent);
      } else {
        minecraft.setScreen(this);
      }
    }, Component.translatable("noisy-neighbors.zone.delete.title", zone.name()),
        Component.translatable("noisy-neighbors.zone.delete.warning"),
        Component.translatable("noisy-neighbors.zone.delete"), Component.translatable("gui.cancel")));
  }

  ZoneSoundScreen refreshed() {
    return new ZoneSoundScreen(parent, worldKey, zoneId);
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
    private final boolean chattiness;

    MobSlider(String mob, int percentage, boolean chattiness) {
      super(0, 0, 0, 20, Component.empty(), percentage / 100D);
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
