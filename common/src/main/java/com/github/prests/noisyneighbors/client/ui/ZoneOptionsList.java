package com.github.prests.noisyneighbors.client.ui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;

final class ZoneOptionsList extends OptionsList {
  private static final int ROW_WIDTH = 310;
  private static final int TOGGLE_WIDTH = 20;
  private static final int GAP = 2;
  private final OptionsSubScreen screen;

  ZoneOptionsList(Minecraft minecraft, int width, OptionsSubScreen screen) {
    super(minecraft, width, screen);
    this.screen = screen;
  }

  void addZone(AbstractWidget select, AbstractWidget toggle) {
    addEntry(new ZoneEntry(select, toggle), 25);
  }

  private final class ZoneEntry extends AbstractEntry {
    private final AbstractWidget select;
    private final AbstractWidget toggle;
    private final List<OptionsList.OptionInstanceWidget> children;

    ZoneEntry(AbstractWidget select, AbstractWidget toggle) {
      this.select = select;
      this.toggle = toggle;
      children = List.of(new OptionsList.OptionInstanceWidget(select), new OptionsList.OptionInstanceWidget(toggle));
    }

    @Override public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
      int x = screen.width / 2 - ROW_WIDTH / 2;
      select.setPosition(x, getContentY());
      select.setWidth(ROW_WIDTH - TOGGLE_WIDTH - GAP);
      toggle.setPosition(select.getRight() + GAP, getContentY());
      toggle.setWidth(TOGGLE_WIDTH);
      select.extractRenderState(graphics, mouseX, mouseY, delta);
      toggle.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override public List<? extends GuiEventListener> children() {
      return children.stream().map(OptionsList.OptionInstanceWidget::widget).toList();
    }

    @Override public List<? extends NarratableEntry> narratables() {
      return children.stream().map(OptionsList.OptionInstanceWidget::widget).toList();
    }
  }
}
