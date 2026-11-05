package com.github.prests.noisyneighbors.client.ui;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

/** Entity rows with a title above an icon, equal-width sliders, and advanced button. */
class EntityOptionsList extends OptionsList {
  private static final int ROW_WIDTH = 310;
  private static final int ROW_HEIGHT = 36;
  private static final int BUTTON_WIDTH = 20;
  private static final int GAP = 2;
  private static final int SLIDER_WIDTH = (ROW_WIDTH - MobIcons.WIDTH - BUTTON_WIDTH - GAP * 2) / 2;
  static final Component ADVANCED_LABEL = Component.literal("⚙").withStyle(ChatFormatting.BOLD);
  private final OptionsSubScreen screen;

  EntityOptionsList(Minecraft minecraft, int width, OptionsSubScreen screen) {
    super(minecraft, width, screen);
    this.screen = screen;
  }

  void addEntity(String mob, Component title, AbstractWidget volume, AbstractWidget chattiness, AbstractWidget advanced) {
    addEntry(new EntityEntry(mob, title, volume, chattiness, advanced), ROW_HEIGHT);
  }

  private final class EntityEntry extends AbstractEntry {
    private final String mob;
    private final Component title;
    private final AbstractWidget volume;
    private final AbstractWidget chattiness;
    private final AbstractWidget advanced;
    // Matches OptionsList.Entry so existing list inspection remains useful in GameTests.
    private final List<OptionsList.OptionInstanceWidget> children;

    EntityEntry(String mob, Component title, AbstractWidget volume, AbstractWidget chattiness, AbstractWidget advanced) {
      this.mob = mob;
      this.title = title;
      this.volume = volume;
      this.chattiness = chattiness;
      this.advanced = advanced;
      children = List.of(new OptionsList.OptionInstanceWidget(volume), new OptionsList.OptionInstanceWidget(chattiness),
          new OptionsList.OptionInstanceWidget(advanced));
    }

    @Override public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
      int x = screen.width / 2 - ROW_WIDTH / 2;
      int controlsY = getContentY() + 12;
      graphics.text(minecraft.font, title, x, getContentY(), 0xFFFFFFFF);
      MobIcons.draw(graphics, mob, x, controlsY);
      volume.setPosition(x + MobIcons.WIDTH, controlsY);
      volume.setWidth(SLIDER_WIDTH);
      chattiness.setPosition(volume.getRight() + GAP, controlsY);
      chattiness.setWidth(SLIDER_WIDTH);
      advanced.setPosition(x + ROW_WIDTH - BUTTON_WIDTH, controlsY);
      advanced.setWidth(BUTTON_WIDTH);
      volume.extractRenderState(graphics, mouseX, mouseY, delta);
      chattiness.extractRenderState(graphics, mouseX, mouseY, delta);
      advanced.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override public List<? extends GuiEventListener> children() {
      return children.stream().map(OptionsList.OptionInstanceWidget::widget).toList();
    }

    @Override public List<? extends NarratableEntry> narratables() {
      return children.stream().map(OptionsList.OptionInstanceWidget::widget).toList();
    }
  }
}
