package com.github.prests.noisyneighbors.client.ui;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

/** Entity rows with a title, icon, two sliders, and advanced button. */
class EntityOptionsList extends ContainerObjectSelectionList<EntityOptionsList.Row> {
  private static final int ROW_WIDTH = 310;
  private static final int ROW_HEIGHT = 36;
  private static final int BUTTON_WIDTH = 20;
  private static final int GAP = 2;
  private static final int SLIDER_WIDTH = (ROW_WIDTH - MobIcons.WIDTH - BUTTON_WIDTH - GAP * 2) / 2;
  static final Component ADVANCED_LABEL = Component.literal("⚙").withStyle(ChatFormatting.BOLD);

  EntityOptionsList(Minecraft minecraft, int width, int height, int top, int bottom) {
    super(minecraft, width, bottom - top, top, ROW_HEIGHT);
  }

  @Override public int getRowWidth() {
    return ROW_WIDTH;
  }

  @Override protected int getRowTop(int index) {
    int top = getY() + 4 - (int) getScrollAmount();
    for (int row = 0; row < index; row++) top += getEntry(row).rowHeight();
    return top;
  }

  @Override protected int getRowBottom(int index) {
    return getRowTop(index) + getEntry(index).rowHeight();
  }

  @Override protected int getMaxPosition() {
    int height = 0;
    for (Row row : children()) height += row.rowHeight();
    return height;
  }

  @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (!isMouseOver(mouseX, mouseY)) return false;
    for (int index = 0; index < getItemCount(); index++) {
      if (mouseY >= getRowTop(index) && mouseY < getRowBottom(index)) {
        Row row = getEntry(index);
        if (row.mouseClicked(mouseX, mouseY, button)) {
          setFocused(row);
          setDragging(true);
          return true;
        }
      }
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  void clear() {
    clearEntries();
  }

  void addEntity(String mob, Component title, AbstractWidget volume, AbstractWidget chattiness, AbstractWidget advanced) {
    addEntry(new EntityEntry(mob, title, volume, chattiness, advanced));
  }

  void addFullWidthWidget(AbstractWidget widget) {
    addEntry(new WidgetEntry(widget, null));
  }

  void addWidgetPair(AbstractWidget left, AbstractWidget right) {
    addEntry(new WidgetEntry(left, right));
  }

  abstract class Row extends ContainerObjectSelectionList.Entry<Row> {
    abstract int rowHeight();
  }

  final class EntityEntry extends Row {
    private final String mob;
    private final Component title;
    private final AbstractWidget volume;
    private final AbstractWidget chattiness;
    private final AbstractWidget advanced;
    private final List<AbstractWidget> children;

    EntityEntry(String mob, Component title, AbstractWidget volume, AbstractWidget chattiness, AbstractWidget advanced) {
      this.mob = mob;
      this.title = title;
      this.volume = volume;
      this.chattiness = chattiness;
      this.advanced = advanced;
      children = List.of(volume, chattiness, advanced);
    }

    @Override int rowHeight() {
      return ROW_HEIGHT;
    }

    @Override public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                                 int mouseX, int mouseY, boolean hovered, float delta) {
      int controlsY = top + 12;
      graphics.drawString(minecraft.font, title, left, top, 0xFFFFFFFF);
      MobIcons.draw(graphics, mob, left, controlsY);
      volume.setPosition(left + MobIcons.WIDTH, controlsY);
      volume.setWidth(SLIDER_WIDTH);
      chattiness.setPosition(volume.getRight() + GAP, controlsY);
      chattiness.setWidth(SLIDER_WIDTH);
      advanced.setPosition(left + ROW_WIDTH - BUTTON_WIDTH, controlsY);
      advanced.setWidth(BUTTON_WIDTH);
      children.forEach(widget -> widget.render(graphics, mouseX, mouseY, delta));
    }

    @Override public List<? extends GuiEventListener> children() {
      return children;
    }

    @Override public List<? extends NarratableEntry> narratables() {
      return children;
    }
  }

  final class WidgetEntry extends Row {
    private static final int ROW_HEIGHT = 24;
    private final AbstractWidget left;
    private final AbstractWidget right;

    WidgetEntry(AbstractWidget left, AbstractWidget right) {
      this.left = left;
      this.right = right;
    }

    @Override int rowHeight() {
      return ROW_HEIGHT;
    }

    @Override public void render(GuiGraphics graphics, int index, int top, int x, int width, int height,
                                 int mouseX, int mouseY, boolean hovered, float delta) {
      left.setPosition(x, top + 2);
      left.setWidth(right == null ? ROW_WIDTH : (ROW_WIDTH - 10) / 2);
      left.render(graphics, mouseX, mouseY, delta);
      if (right != null) {
        right.setPosition(left.getRight() + 10, top + 2);
        right.setWidth((ROW_WIDTH - 10) / 2);
        right.render(graphics, mouseX, mouseY, delta);
      }
    }

    @Override public List<? extends GuiEventListener> children() {
      return right == null ? List.of(left) : List.of(left, right);
    }

    @Override public List<? extends NarratableEntry> narratables() {
      return right == null ? List.of(left) : List.of(left, right);
    }
  }
}
