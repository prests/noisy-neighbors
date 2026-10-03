package com.github.prests.noisyneighbors.gametest;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import com.github.prests.noisyneighbors.client.ui.GlobalSoundScreen;
import com.github.prests.noisyneighbors.client.ui.ZoneSoundScreen;
import com.github.prests.noisyneighbors.client.zone.Zone;
import com.github.prests.noisyneighbors.mixin.OptionsSubScreenAccessor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/** Verifies the real client, mod entrypoint, and sound mixin load in a disposable world. */
@SuppressWarnings("UnstableApiUsage")
public final class NoisyNeighborsClientGameTest implements FabricClientGameTest {
  @Override public void runTest(ClientGameTestContext context) {
    try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
      singleplayer.getConnection().waitForChunksRender();
      if (!MobSoundCatalog.isKnown("minecraft:entity.cow.ambient")) {
        throw new AssertionError("client initializer did not load the sound catalog");
      }
      context.runOnClient(NoisyNeighborsClientGameTest::verifySoundMixinCapturesConfiguredVolume);
      context.runOnClient(NoisyNeighborsClientGameTest::verifyEntitySearchFiltersBothMenus);
    }
  }

  private static void verifySoundMixinCapturesConfiguredVolume(Minecraft client) {
    Integer previous = SettingsStore.data().global.put("minecraft:cow", 0);
    try {
      SimpleSoundInstance sound = new SimpleSoundInstance(SoundEvent.createVariableRangeEvent(Identifier.parse("minecraft:entity.cow.ambient")),
          SoundSource.NEUTRAL, 1F, 1F,
          SoundInstance.createUnseededRandom(), client.player.getX(), client.player.getY(), client.player.getZ());
      SoundManager manager = client.getSoundManager();
      manager.play(sound);
      Float multiplier = multipliers(manager).get(sound);
      if (multiplier == null || multiplier != 0F) {
        throw new AssertionError("sound mixin did not capture the configured cow volume: " + multiplier);
      }
    } finally {
      if (previous == null) SettingsStore.data().global.remove("minecraft:cow");
      else SettingsStore.data().global.put("minecraft:cow", previous);
    }
  }

  private static void verifyEntitySearchFiltersBothMenus(Minecraft client) {
    WorldIdentity world = WorldIdentity.current(client);
    if (world == null) throw new AssertionError("singleplayer world identity is unavailable");
    UUID zoneId = UUID.randomUUID();
    SettingsStore.addZone(world, new Zone(zoneId, "Search test", true, 0xFFFFFF,
        client.level.dimension().identifier().toString(), 0, 0, 0, 1, 1, 1, Map.of()));
    try {
      GlobalSoundScreen global = new GlobalSoundScreen(null);
      client.gui.setScreen(global);
      assertSearchResults(global, "moo", "cow/mooshroom: 100%");

      ZoneSoundScreen zone = new ZoneSoundScreen(null, world.key(), zoneId);
      client.gui.setScreen(zone);
      assertSearchResults(zone, "cave", "spider/cave_spider: 100%");
    } finally {
      SettingsStore.removeZone(world.key(), zoneId);
      client.gui.setScreen(null);
    }
  }

  private static void assertSearchResults(Screen screen, String query, String... expected) {
    search(screen).setValue(query);
    OptionsList list = ((OptionsSubScreenAccessor) screen).noisyNeighbors$list();
    List<String> labels = sliderLabels(list);
    if (!labels.equals(List.of(expected))) {
      throw new AssertionError("search '" + query + "' showed " + labels + " instead of " + List.of(expected));
    }
  }

  private static List<String> sliderLabels(OptionsList list) {
    try {
      List<String> labels = new ArrayList<>();
      for (Object entry : (List<?>) list.children()) {
        Field children = entry.getClass().getDeclaredField("children");
        children.setAccessible(true);
        for (Object child : (List<?>) children.get(entry)) {
          AbstractWidget widget = (AbstractWidget) child.getClass().getMethod("widget").invoke(child);
          if (widget instanceof AbstractSliderButton slider) labels.add(slider.getMessage().getString());
        }
      }
      return labels;
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError("options list widgets are unavailable", exception);
    }
  }

  private static EditBox search(Screen screen) {
    try {
      Field search = screen.getClass().getDeclaredField("search");
      search.setAccessible(true);
      return (EditBox) search.get(screen);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError("search field is unavailable", exception);
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<SoundInstance, Float> multipliers(SoundManager manager) {
    try {
      Field engine = SoundManager.class.getDeclaredField("soundEngine");
      engine.setAccessible(true);
      Field multipliers = SoundEngine.class.getDeclaredField("noisyNeighbors$multipliers");
      multipliers.setAccessible(true);
      return (Map<SoundInstance, Float>) multipliers.get(engine.get(manager));
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError("sound mixin test hook is unavailable", exception);
    }
  }
}
