package com.github.prests.noisyneighbors.client.ui;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import java.util.Comparator;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

/** Uses Minecraft's options list so rows remain keyboard and mouse scrollable. */
public final class GlobalSoundScreen extends OptionsSubScreen {
  private EditBox search;
  private EntityOptionsList entityList;

  public GlobalSoundScreen(Screen parent) {
    super(parent, net.minecraft.client.Minecraft.getInstance().options, Component.translatable("noisy-neighbors.title"));
  }

  @Override protected void addTitle() {
    layout.setHeaderHeight(58);
    net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
    search = new EditBox(font, 200, 20, Component.translatable("noisy-neighbors.search.entities"));
    search.setHint(Component.translatable("noisy-neighbors.search.entities").withStyle(EditBox.SEARCH_HINT_STYLE));
    search.setResponder(query -> refreshOptions());
    WorldIdentity world = WorldIdentity.current(client);
    Button zones = Button.builder(Component.translatable("noisy-neighbors.edit-world-zones"),
        button -> client.gui.setScreen(new WorldZonesScreen(this, world))).width(100).build();
    zones.active = world != null && client.level != null;
    LinearLayout row = LinearLayout.horizontal().spacing(10);
    row.addChild(search);
    row.addChild(zones);
    LinearLayout header = LinearLayout.vertical().spacing(4);
    header.addChild(new StringWidget(title, font), settings -> settings.alignHorizontallyCenter());
    header.addChild(row);
    layout.addToHeader(header);
  }

  @Override protected void addContents() {
    entityList = (EntityOptionsList) layout.addToContents(new EntityOptionsList(
        net.minecraft.client.Minecraft.getInstance(), width, this));
    list = entityList;
    addOptions();
  }

  @Override protected void addOptions() {
    refreshOptions();
  }

  private void refreshOptions() {
    list.replaceEntries(java.util.List.of());
    java.util.List<String> mobs = MobSoundCatalog.mobs().stream().sorted(Comparator.naturalOrder())
        .filter(mob -> MobSoundCatalog.matchesSearch(mob, search.getValue())).toList();
    mobs.forEach(mob -> {
      Button advanced = Button.builder(EntityOptionsList.ADVANCED_LABEL, button ->
          net.minecraft.client.Minecraft.getInstance().gui.setScreen(new FineGrainedSoundScreen(this, mob))).width(20).build();
      advanced.setTooltip(Tooltip.create(Component.translatable("noisy-neighbors.advanced-controls")));
      entityList.addEntity(mob, Component.literal(MobSoundCatalog.displayName(mob)), new MobSlider(mob, false),
          new MobSlider(mob, true), advanced);
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

  private static final class MobSlider extends AbstractSliderButton {
    private final String mob;
    private final boolean chattiness;

    MobSlider(String mob, boolean chattiness) {
      super(0, 0, 310, 20, Component.empty(), (chattiness ? SettingsStore.data().chattiness : SettingsStore.data().global)
          .getOrDefault(mob, 100) / 100D);
      this.mob = mob;
      this.chattiness = chattiness;
      updateMessage();
    }

    @Override protected void updateMessage() {
      setMessage(Component.translatable(chattiness ? "noisy-neighbors.chattiness" : "noisy-neighbors.volume",
          Math.round(value * 100)));
    }

    @Override protected void applyValue() {
      (chattiness ? SettingsStore.data().chattiness : SettingsStore.data().global)
          .put(mob, (int) Math.round(value * 100));
    }

    @Override protected net.minecraft.network.chat.MutableComponent createNarrationMessage() {
      return Component.literal(MobSoundCatalog.displayName(mob) + ", ").append(super.createNarrationMessage());
    }
  }
}
