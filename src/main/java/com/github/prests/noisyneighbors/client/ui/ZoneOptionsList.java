package com.github.prests.noisyneighbors.client.ui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

/** Full-width zone rows with a compact enable toggle. */
final class ZoneOptionsList extends ContainerObjectSelectionList<ZoneOptionsList.ZoneEntry> {
  private static final int ROW_WIDTH = 310;
  private static final int ROW_HEIGHT = 25;
  private static final int TOGGLE_WIDTH = 20;
  private static final int GAP = 2;

  ZoneOptionsList(Minecraft minecraft, int width, int height, int top, int bottom) {
    super(minecraft, width, bottom - top, top, ROW_HEIGHT);
  }

  @Override public int getRowWidth() {
    return ROW_WIDTH;
  }

  void addZone(AbstractWidget select, AbstractWidget toggle) {
    addEntry(new ZoneEntry(select, toggle));
  }

  void addWidget(AbstractWidget widget) {
    addEntry(new ZoneEntry(widget, null));
  }

  final class ZoneEntry extends ContainerObjectSelectionList.Entry<ZoneEntry> {
    private final AbstractWidget select;
    private final AbstractWidget toggle;

    ZoneEntry(AbstractWidget select, AbstractWidget toggle) {
      this.select = select;
      this.toggle = toggle;
    }

    @Override public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                                 int mouseX, int mouseY, boolean hovered, float delta) {
      select.setPosition(left, top);
      select.setWidth(toggle == null ? ROW_WIDTH : ROW_WIDTH - TOGGLE_WIDTH - GAP);
      select.render(graphics, mouseX, mouseY, delta);
      if (toggle != null) {
        toggle.setPosition(select.getRight() + GAP, top);
        toggle.setWidth(TOGGLE_WIDTH);
        toggle.render(graphics, mouseX, mouseY, delta);
      }
    }

    @Override public List<? extends GuiEventListener> children() {
      return toggle == null ? List.of(select) : List.of(select, toggle);
    }

    @Override public List<? extends NarratableEntry> narratables() {
      return toggle == null ? List.of(select) : List.of(select, toggle);
    }
  }
}
