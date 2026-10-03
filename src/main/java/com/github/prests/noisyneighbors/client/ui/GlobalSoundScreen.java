package com.github.prests.noisyneighbors.client.ui;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import java.util.Comparator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

/** Uses Minecraft's options list so rows remain keyboard and mouse scrollable. */
public final class GlobalSoundScreen extends OptionsSubScreen {
  private EditBox search;

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

  @Override protected void addOptions() {
    refreshOptions();
  }

  private void refreshOptions() {
    list.replaceEntries(java.util.List.of());
    java.util.List<String> mobs = MobSoundCatalog.mobs().stream().sorted(Comparator.naturalOrder())
        .filter(mob -> MobSoundCatalog.matchesSearch(mob, search.getValue())).toList();
    mobs.forEach(mob -> list.addBig(new MobSlider(mob)));
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
    MobSlider(String mob) {
      super(0, 0, 310, 20, Component.empty(), SettingsStore.data().global.getOrDefault(mob, 100) / 100D);
      this.mob = mob;
      updateMessage();
    }
    @Override protected void updateMessage() {
      setMessage(Component.literal(MobSoundCatalog.displayName(mob) + ": " + Math.round(value * 100) + "%"));
    }
    @Override protected void applyValue() {
      SettingsStore.data().global.put(mob, (int) Math.round(value * 100));
    }
    @Override public void setX(int x) { super.setX(x + MobIcons.WIDTH); }
    @Override public void setWidth(int width) { super.setWidth(width - MobIcons.WIDTH); }
    @Override public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);
      MobIcons.draw(graphics, mob, getX() - MobIcons.WIDTH, getY());
    }
  }
}
