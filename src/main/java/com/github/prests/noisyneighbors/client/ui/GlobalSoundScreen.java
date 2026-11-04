package com.github.prests.noisyneighbors.client.ui;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import java.util.Comparator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Global per-mob sound controls. */
public final class GlobalSoundScreen extends Screen {
  private static final int CONTENT_WIDTH = 310;
  private final Screen parent;
  private EditBox search;
  private EntityOptionsList entityList;

  public GlobalSoundScreen(Screen parent) {
    super(Component.translatable("noisy-neighbors.title"));
    this.parent = parent;
  }

  @Override protected void init() {
    int left = width / 2 - CONTENT_WIDTH / 2;
    search = addRenderableWidget(new EditBox(font, left, 30, 200, 20, Component.translatable("noisy-neighbors.search.entities")));
    search.setHint(Component.translatable("noisy-neighbors.search.entities").withStyle(ChatFormatting.DARK_GRAY));
    WorldIdentity world = WorldIdentity.current(minecraft);
    Button zones = addRenderableWidget(Button.builder(Component.translatable("noisy-neighbors.edit-world-zones"),
        button -> minecraft.setScreen(new WorldZonesScreen(this, world))).bounds(left + 210, 30, 100, 20).build());
    zones.active = world != null && minecraft.level != null;

    entityList = addRenderableWidget(new EntityOptionsList(minecraft, width, height, 70, height - 40));
    search.setResponder(query -> refreshOptions());
    refreshOptions();
    addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
        .bounds(width / 2 - 100, height - 28, 200, 20).build());
  }

  private void refreshOptions() {
    entityList.clear();
    MobSoundCatalog.mobs().stream().sorted(Comparator.naturalOrder())
        .filter(mob -> MobSoundCatalog.matchesSearch(mob, search.getValue()))
        .forEach(mob -> {
          Button advanced = Button.builder(EntityOptionsList.ADVANCED_LABEL,
              button -> minecraft.setScreen(new FineGrainedSoundScreen(this, mob))).width(20).build();
          advanced.setTooltip(Tooltip.create(Component.translatable("noisy-neighbors.advanced-controls")));
          entityList.addEntity(mob, Component.literal(MobSoundCatalog.displayName(mob)), new MobSlider(mob, false),
              new MobSlider(mob, true), advanced);
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

  private static final class MobSlider extends AbstractSliderButton {
    private final String mob;
    private final boolean chattiness;

    MobSlider(String mob, boolean chattiness) {
      super(0, 0, 0, 20, Component.empty(), (chattiness ? SettingsStore.data().chattiness : SettingsStore.data().global)
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
      (chattiness ? SettingsStore.data().chattiness : SettingsStore.data().global).put(mob, (int) Math.round(value * 100));
    }

    @Override protected net.minecraft.network.chat.MutableComponent createNarrationMessage() {
      return Component.literal(MobSoundCatalog.displayName(mob) + ", ").append(super.createNarrationMessage());
    }
  }
}
