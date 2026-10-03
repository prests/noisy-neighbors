package com.github.prests.noisyneighbors.client.ui;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import java.util.Comparator;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

/** Uses Minecraft's options list so rows remain keyboard and mouse scrollable. */
public final class GlobalSoundScreen extends OptionsSubScreen {
  public GlobalSoundScreen(Screen parent) {
    super(parent, net.minecraft.client.Minecraft.getInstance().options, Component.translatable("noisy-neighbors.title"));
  }

  @Override protected void addOptions() {
    net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
    WorldIdentity world = WorldIdentity.current(client);
    net.minecraft.client.gui.components.Button zones = net.minecraft.client.gui.components.Button.builder(
        Component.translatable("noisy-neighbors.edit-world-zones"), button -> client.gui.setScreen(new WorldZonesScreen(this, world)))
        .width(310).build();
    zones.active = world != null && client.level != null;
    list.addBig(zones);
    MobSoundCatalog.mobs().stream().sorted(Comparator.naturalOrder())
        .forEach(mob -> list.addBig(new MobSlider(mob)));
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
      String name = mob.equals("minecraft:cow") ? "cow/mooshroom" : mob.replace("minecraft:", "");
      setMessage(Component.literal(name + ": " + Math.round(value * 100) + "%"));
    }
    @Override protected void applyValue() {
      SettingsStore.data().global.put(mob, (int) Math.round(value * 100));
    }
  }
}
