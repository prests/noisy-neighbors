package com.github.prests.noisyneighbors.client.ui;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.zone.Zone;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** Native RGB picker: no config-screen dependency required. */
public final class ZoneColorScreen extends Screen {
  private final ZoneSoundScreen parent;
  private final String worldKey;
  private final UUID zoneId;
  private int color;
  private ColorPreview preview;
  private EditBox hex;
  private final RgbSlider[] sliders = new RgbSlider[3];
  private boolean updatingHex;

  public ZoneColorScreen(ZoneSoundScreen parent, String worldKey, UUID zoneId, int color) {
    super(Component.translatable("noisy-neighbors.zone.color.title"));
    this.parent = parent;
    this.worldKey = worldKey;
    this.zoneId = zoneId;
    this.color = color;
  }

  @Override protected void init() {
    preview = addRenderableWidget(new ColorPreview(width / 2 - 155, height / 2 - 52));
    hex = addRenderableWidget(new EditBox(font, width / 2 - 127, height / 2 - 52, 282, 20,
        Component.translatable("noisy-neighbors.zone.color.hex")));
    hex.setMaxLength(7);
    hex.setResponder(this::readHex);
    updatePreview();
    sliders[0] = addRenderableWidget(new RgbSlider(width / 2 - 155, 0, height / 2 - 24, "noisy-neighbors.zone.color.red"));
    sliders[1] = addRenderableWidget(new RgbSlider(width / 2 - 155, 1, height / 2, "noisy-neighbors.zone.color.green"));
    sliders[2] = addRenderableWidget(new RgbSlider(width / 2 - 155, 2, height / 2 + 24, "noisy-neighbors.zone.color.blue"));
    addRenderableWidget(Button.builder(Component.translatable("noisy-neighbors.save"), button -> save())
        .bounds(width / 2 - 155, height / 2 + 58, 150, 20).build());
    addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
        .bounds(width / 2 + 5, height / 2 + 58, 150, 20).build());
  }

  @Override public void onClose() {
    minecraft.gui.setScreen(parent);
  }

  private void updatePreview() {
    preview.setColor(color);
    if (hex != null) {
      updatingHex = true;
      hex.setValue(String.format("#%06X", color));
      updatingHex = false;
    }
  }

  private void readHex(String value) {
    if (updatingHex) return;
    String digits = value.startsWith("#") ? value.substring(1) : value;
    if (digits.matches("[0-9a-fA-F]{6}")) {
      color = Integer.parseInt(digits, 16);
      preview.setColor(color);
      for (RgbSlider slider : sliders) slider.sync();
    }
  }

  private void save() {
    SettingsStore.updateZone(worldKey, zoneId, zone -> recolored(zone, color));
    SettingsStore.save();
    minecraft.gui.setScreen(parent.refreshed());
  }

  private static Zone recolored(Zone zone, int color) {
    return new Zone(zone.id(), zone.name(), zone.enabled(), color, zone.dimension(), zone.minX(), zone.minY(), zone.minZ(),
        zone.maxX(), zone.maxY(), zone.maxZ(), zone.volumes(), zone.events(), zone.chattiness());
  }

  private final class RgbSlider extends AbstractSliderButton {
    private final int shift;
    private final String label;

    RgbSlider(int x, int channel, int y, String label) {
      super(x, y, 310, 20, Component.empty(), ((color >> ((2 - channel) * 8)) & 0xFF) / 255D);
      shift = (2 - channel) * 8;
      this.label = label;
      updateMessage();
    }

    @Override protected void updateMessage() {
      setMessage(Component.translatable(label, Math.round(value * 255)));
    }

    void sync() {
      value = ((color >> shift) & 0xFF) / 255D;
      updateMessage();
    }

    @Override protected void applyValue() {
      color = (color & ~(0xFF << shift)) | ((int) Math.round(value * 255) << shift);
      updatePreview();
    }
  }

  private static final class ColorPreview extends AbstractWidget {
    private int color;

    ColorPreview(int x, int y) {
      super(x, y, 20, 20, Component.translatable("noisy-neighbors.zone.color.preview"));
    }

    void setColor(int color) {
      this.color = color;
    }

    @Override protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFFFFFFFF);
      graphics.fill(getX() + 2, getY() + 2, getX() + width - 2, getY() + height - 2, 0xFF000000 | color);
    }

    @Override protected void updateWidgetNarration(NarrationElementOutput output) {}
  }
}
