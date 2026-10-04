package com.github.prests.noisyneighbors.gametest;

import com.github.prests.noisyneighbors.client.NoisyNeighborsKeyMappings;
import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import com.github.prests.noisyneighbors.client.ui.FineGrainedSoundScreen;
import com.github.prests.noisyneighbors.client.ui.GlobalSoundScreen;
import com.github.prests.noisyneighbors.client.ui.ZoneSoundScreen;
import com.github.prests.noisyneighbors.client.ui.WorldZonesScreen;
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
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundEventListener;
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
      context.runOnClient(NoisyNeighborsClientGameTest::verifySoundMixinRejectsMutedSounds);
      context.runOnClient(NoisyNeighborsClientGameTest::verifyEntitySearchFiltersBothMenus);
      context.runOnClient(NoisyNeighborsClientGameTest::verifyFineGrainedScreens);
      context.runOnClient(NoisyNeighborsClientGameTest::verifyWorldZonesHotkey);
    }
  }

  private static void verifySoundMixinRejectsMutedSounds(Minecraft client) {
    Integer previousVolume = SettingsStore.data().global.put("minecraft:cow", 0);
    Integer previousChattiness = SettingsStore.data().chattiness.remove("minecraft:cow");
    try {
      assertRejected(client, "zero volume");
      SettingsStore.data().global.put("minecraft:cow", 100);
      SettingsStore.data().chattiness.put("minecraft:cow", 0);
      assertRejected(client, "zero chattiness");
    } finally {
      if (previousVolume == null) SettingsStore.data().global.remove("minecraft:cow");
      else SettingsStore.data().global.put("minecraft:cow", previousVolume);
      if (previousChattiness == null) SettingsStore.data().chattiness.remove("minecraft:cow");
      else SettingsStore.data().chattiness.put("minecraft:cow", previousChattiness);
    }
  }

  private static void assertRejected(Minecraft client, String setting) {
    SimpleSoundInstance sound = new SimpleSoundInstance(SoundEvent.createVariableRangeEvent(Identifier.parse("minecraft:entity.cow.ambient")),
        SoundSource.NEUTRAL, 1F, 1F,
        SoundInstance.createUnseededRandom(), client.player.getX(), client.player.getY(), client.player.getZ());
    SoundManager manager = client.getSoundManager();
    int[] notifications = {0};
    SoundEventListener listener = (played, events, range) -> notifications[0]++;
    manager.addListener(listener);
    try {
      SoundEngine.PlayResult result = manager.play(sound);
      if (result != SoundEngine.PlayResult.NOT_STARTED || multipliers(manager).containsKey(sound)
          || channels(manager).containsKey(sound) || manager.isActive(sound) || notifications[0] != 0) {
        throw new AssertionError(setting + " sound was not rejected before listeners or channel allocation: " + result);
      }
    } finally {
      manager.removeListener(listener);
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
      assertSearchResults(global, "moo", "cow/mooshroom");

      ZoneSoundScreen zone = new ZoneSoundScreen(null, world.key(), zoneId);
      client.gui.setScreen(zone);
      assertSearchResults(zone, "cave", "spider/cave_spider");
    } finally {
      SettingsStore.removeZone(world.key(), zoneId);
      client.gui.setScreen(null);
    }
  }

  private static void verifyFineGrainedScreens(Minecraft client) {
    FineGrainedSoundScreen global = new FineGrainedSoundScreen(null, "minecraft:cow");
    client.gui.setScreen(global);
    if (!sliderLabels(((OptionsSubScreenAccessor) (Object) global).noisyNeighbors$list()).contains("minecraft:entity.cow.hurt: 100%")) {
      throw new AssertionError("global advanced controls did not render exact event IDs");
    }
  }

  private static void verifyWorldZonesHotkey(Minecraft client) {
    client.gui.setScreen(null);
    KeyMapping hotkey = KeyMapping.get("key.noisy-neighbors.open_world_zones");
    if (!hotkey.getDefaultKey().getName().equals("key.keyboard.u")) {
      throw new AssertionError("world zones hotkey default is " + hotkey.getDefaultKey().getName() + " instead of U");
    }
    hotkey.setKey(hotkey.getDefaultKey());
    KeyMapping.resetMapping();
    KeyMapping.click(hotkey.getDefaultKey());
    NoisyNeighborsKeyMappings.tick(client);
    if (!(client.gui.screen() instanceof WorldZonesScreen)) {
      throw new AssertionError("world zones hotkey did not open the active world's zone menu");
    }
    client.gui.setScreen(null);
  }

  private static void assertSearchResults(Screen screen, String query, String expectedTitle) {
    search(screen).setValue(query);
    OptionsList list = ((OptionsSubScreenAccessor) screen).noisyNeighbors$list();
    List<String> titles = entityTitles(list);
    List<String> labels = sliderLabels(list);
    if (!titles.equals(List.of(expectedTitle))
        || !labels.equals(List.of("Volume: 100%", "Chattiness: 100%"))) {
      throw new AssertionError("search '" + query + "' showed titles " + titles + " and controls " + labels);
    }
  }

  private static List<String> entityTitles(OptionsList list) {
    List<String> titles = new ArrayList<>();
    for (Object entry : (List<?>) list.children()) {
      try {
        Field title = entry.getClass().getDeclaredField("title");
        title.setAccessible(true);
        titles.add(((net.minecraft.network.chat.Component) title.get(entry)).getString());
      } catch (NoSuchFieldException ignored) {
      } catch (ReflectiveOperationException exception) {
        throw new AssertionError("entity row titles are unavailable", exception);
      }
    }
    return titles;
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
      Field multipliers = SoundEngine.class.getDeclaredField("noisyNeighbors$multipliers");
      multipliers.setAccessible(true);
      return (Map<SoundInstance, Float>) multipliers.get(engine(manager));
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError("sound mixin test hook is unavailable", exception);
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<SoundInstance, ?> channels(SoundManager manager) {
    try {
      Field channels = SoundEngine.class.getDeclaredField("instanceToChannel");
      channels.setAccessible(true);
      return (Map<SoundInstance, ?>) channels.get(engine(manager));
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError("sound engine channels are unavailable", exception);
    }
  }

  private static SoundEngine engine(SoundManager manager) throws ReflectiveOperationException {
    Field engine = SoundManager.class.getDeclaredField("soundEngine");
    engine.setAccessible(true);
    return (SoundEngine) engine.get(manager);
  }
}
