package com.github.prests.noisyneighbors.client.ui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Full-width entity row with an icon, slider, and separately focusable advanced button. */
class EntityOptionsList extends OptionsList {
  private static final int ROW_WIDTH = 310;
  private static final int BUTTON_WIDTH = 20;
  private static final int GAP = MobIcons.WIDTH - 20;
  static final Component ADVANCED_LABEL = Component.literal("⚙").withStyle(ChatFormatting.BOLD);
  private final OptionsSubScreen screen;

  EntityOptionsList(Minecraft minecraft, int width, OptionsSubScreen screen) {
    super(minecraft, width, screen);
    this.screen = screen;
  }

  void addEntity(AbstractWidget slider, AbstractWidget advanced) {
    addEntry(new EntityEntry(slider, advanced));
  }

  private final class EntityEntry extends AbstractEntry {
    private final AbstractWidget slider;
    private final AbstractWidget advanced;
    // Matches OptionsList.Entry so existing list inspection remains useful in GameTests.
    private final List<OptionsList.OptionInstanceWidget> children;

    EntityEntry(AbstractWidget slider, AbstractWidget advanced) {
      this.slider = slider;
      this.advanced = advanced;
      children = List.of(new OptionsList.OptionInstanceWidget(slider), new OptionsList.OptionInstanceWidget(advanced));
    }

    @Override public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
      int x = screen.width / 2 - ROW_WIDTH / 2;
      slider.setPosition(x, getContentY());
      slider.setWidth(ROW_WIDTH - BUTTON_WIDTH - GAP);
      advanced.setPosition(x + ROW_WIDTH - BUTTON_WIDTH, getContentY());
      advanced.setWidth(BUTTON_WIDTH);
      slider.extractRenderState(graphics, mouseX, mouseY, delta);
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
